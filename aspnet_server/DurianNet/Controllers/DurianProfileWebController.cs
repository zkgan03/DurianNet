﻿using DurianNet.Data;
using DurianNet.Dtos.Request.DurianProfile;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using DurianNet.Mappers;
using DurianNet.Dtos.Request.DurianProfile;
using DurianNet.Models.DataModels;
using Microsoft.AspNetCore.Authorization;

[Authorize(Policy = "AdminPolicy")]
[ApiController]
[Route("durianprofile")]
public class DurianProfileWebController : Controller
{
    private readonly ApplicationDBContext _context;

    public DurianProfileWebController(ApplicationDBContext context)
    {
        _context = context;
    }

    [HttpGet("DurianProfilePage")]
    public IActionResult DurianProfilePage()
    {
        // This loads the main Durian Profile page.
        return View("~/Views/DurianProfile/DurianProfile.cshtml");
    }

    [HttpGet("AddDurianProfilePage")]
    public IActionResult AddDurianProfilePage()
    {
        return View("~/Views/DurianProfile/AddDurianProfileDetails.cshtml");
    }


    [HttpGet("UpdateDurianProfileDetailsPage")]
    public IActionResult UpdateDurianProfileDetailsPage(int id)
    {
        // TODO: Fetch durian details by ID and pass it to the view for editing.
        // For now, just load the page.
        return View("~/Views/DurianProfile/UpdateDurianProfileDetails.cshtml");
    }

    [HttpGet("Delete")]
    public IActionResult Delete(int id)
    {
        // TODO: Implement delete logic for the durian by ID.
        // Redirect to the Durian Profile page after deletion.
        return RedirectToAction("DurianProfilePage");
    }

    [HttpPost("SaveDurianProfile")]
    public IActionResult SaveDurianProfile(string durianName, string characteristic, string tasteProfile, string durianDescription, string videoDescription)
    {
        // TODO: Save the data to the database or perform other necessary logic.

        // Redirect to the Durian Profile page after saving.
        return RedirectToAction("DurianProfilePage");
    }

    [HttpPost("UpdateDurianProfile")]
    public IActionResult UpdateDurianProfile(int id, string durianName, string characteristic, string tasteProfile, string durianDescription, string videoDescription)
    {
        // TODO: Implement the logic to update the durian profile in the database using the provided data.

        // Redirect back to the Durian Profile page after updating.
        return RedirectToAction("DurianProfilePage");
    }

    [HttpPost("AddDurianProfile")]
    public async Task<IActionResult> AddDurianProfile([FromBody] AddDurianProfileRequestDto dto)
    {
        // Check for duplicate Durian Name
        if (await _context.DurianProfiles.AnyAsync(dp => dp.DurianName == dto.DurianName))
        {
            return BadRequest(new { message = "Durian name already exists." });
        }

        // Check for duplicate Durian Code
        if (await _context.DurianProfiles.AnyAsync(dp => dp.DurianCode == dto.DurianCode))
        {
            return BadRequest(new { message = "Durian code already exists." });
        }

        string imageUrl = null;
        string videoUrl = null;

        // Handle Durian Image upload
        if (!string.IsNullOrEmpty(dto.DurianImage))
        {
            string imageDirectory = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/images");
            if (!Directory.Exists(imageDirectory))
            {
                Directory.CreateDirectory(imageDirectory);
            }

            string imageName = $"durian_{Guid.NewGuid()}_{DateTime.Now.Ticks}.png"; // Unique file name
            string imagePath = Path.Combine(imageDirectory, imageName);
            var imageBytes = Convert.FromBase64String(dto.DurianImage.Split(',')[1]); // Remove Base64 metadata prefix
            await System.IO.File.WriteAllBytesAsync(imagePath, imageBytes);
            imageUrl = $"/images/{imageName}";
        }

        // Handle Durian Video upload
        if (!string.IsNullOrEmpty(dto.DurianVideo))
        {
            string videoDirectory = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/videos");
            if (!Directory.Exists(videoDirectory))
            {
                Directory.CreateDirectory(videoDirectory);
            }

            string videoName = $"durian_{Guid.NewGuid()}_{DateTime.Now.Ticks}.mp4"; // Unique file name
            string videoPath = Path.Combine(videoDirectory, videoName);
            var videoBytes = Convert.FromBase64String(dto.DurianVideo.Split(',')[1]); // Remove Base64 metadata prefix
            await System.IO.File.WriteAllBytesAsync(videoPath, videoBytes);
            videoUrl = $"/videos/{videoName}";
        }

        // Save Durian Video record
        var video = dto.ToDurianVideoFromAddRequest(videoUrl); // Pass video URL explicitly
        _context.DurianVideos.Add(video);
        await _context.SaveChangesAsync();

        // Save Durian Profile record
        var profile = dto.ToDurianProfileFromAddRequest(imageUrl, video.VideoId); // Pass image URL explicitly
        _context.DurianProfiles.Add(profile);
        await _context.SaveChangesAsync();

        return Ok(profile.ToDurianProfileDtoWithNullCheck()); // Use the new mapper
    }

    [HttpGet("GetAllDurianProfiles")]
    public async Task<IActionResult> GetAllDurianProfiles([FromQuery] DurianQueryRequestDto query)
    {
        if (query == null)
        {
            return BadRequest("Query parameters are missing.");
        }

        // Call the repository to get filtered DurianProfiles based on DurianName
        var durianProfiles = await _context.DurianProfiles
                                            .Where(dp => dp.DurianName.Contains(query.DurianName ?? ""))
                                            .Include(dp => dp.DurianVideo)
                                            .ToListAsync();

        if (durianProfiles == null || !durianProfiles.Any())
        {
            return NotFound("No durian profiles found.");
        }

        var profileDtos = durianProfiles.Select(dp => dp.ToDurianProfileDto()).ToList();
        return Ok(profileDtos);
    }

    [HttpDelete("DeleteDurianProfile/{id}")]
    public async Task<IActionResult> DeleteDurianProfile(int id)
    {
        var profile = await _context.DurianProfiles.FindAsync(id);
        if (profile == null)
        {
            return NotFound("Durian profile not found");
        }

        _context.DurianProfiles.Remove(profile);
        await _context.SaveChangesAsync();
        return NoContent();
    }

    [HttpGet("GetDurianProfile/{id}")]
    public async Task<IActionResult> GetDurianProfileById(int id)
    {
        var durianProfile = await _context.DurianProfiles.Include(dp => dp.DurianVideo).FirstOrDefaultAsync(dp => dp.DurianId == id);
        if (durianProfile == null)
        {
            return NotFound("Durian profile not found");
        }

        // Store the Durian ID in session
        HttpContext.Session.SetInt32("DurianId", id);

        return Ok(durianProfile.ToDurianProfileDto());
    }

    [HttpPut("UpdateDurianProfile")]
    public async Task<IActionResult> UpdateDurianProfile([FromBody] UpdateDurianProfileRequestDto dto)
    {
        var durianId = HttpContext.Session.GetInt32("DurianId");
        if (!durianId.HasValue)
        {
            return BadRequest(new { message = "Durian ID is missing from the session." });
        }

        var profile = await _context.DurianProfiles.Include(dp => dp.DurianVideo).FirstOrDefaultAsync(dp => dp.DurianId == durianId.Value);
        if (profile == null)
        {
            return NotFound(new { message = "Durian profile not found." });
        }

        // Check for duplicate Durian Name (excluding the current profile)
        if (await _context.DurianProfiles.AnyAsync(dp => dp.DurianName == dto.DurianName && dp.DurianId != durianId.Value))
        {
            return BadRequest(new { message = "Durian name already exists." });
        }

        // Check for duplicate Durian Code (excluding the current profile)
        if (await _context.DurianProfiles.AnyAsync(dp => dp.DurianCode == dto.DurianCode && dp.DurianId != durianId.Value))
        {
            return BadRequest(new { message = "Durian code already exists." });
        }

        // Update Durian Image if provided
        if (!string.IsNullOrEmpty(dto.DurianImage))
        {
            string imageDirectory = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/images");
            if (!Directory.Exists(imageDirectory))
            {
                Directory.CreateDirectory(imageDirectory);
            }

            string imageName = $"durian_{durianId.Value}_{DateTime.Now.Ticks}.png"; // Unique file name
            string imagePath = Path.Combine(imageDirectory, imageName);
            var imageBytes = Convert.FromBase64String(dto.DurianImage.Split(',')[1]); // Remove metadata prefix
            await System.IO.File.WriteAllBytesAsync(imagePath, imageBytes);
            profile.DurianImage = $"/images/{imageName}";
        }

        // Update Durian Video if provided
        if (!string.IsNullOrEmpty(dto.DurianVideo))
        {
            string videoDirectory = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/videos");
            if (!Directory.Exists(videoDirectory))
            {
                Directory.CreateDirectory(videoDirectory);
            }

            string videoName = $"durian_{durianId.Value}_{DateTime.Now.Ticks}.mp4"; // Unique file name
            string videoPath = Path.Combine(videoDirectory, videoName);
            var videoBytes = Convert.FromBase64String(dto.DurianVideo.Split(',')[1]); // Remove metadata prefix
            await System.IO.File.WriteAllBytesAsync(videoPath, videoBytes);

            // Ensure DurianVideo is initialized
            if (profile.DurianVideo == null)
            {
                profile.DurianVideo = new DurianVideo();
            }

            profile.DurianVideo.VideoUrl = $"/videos/{videoName}";
        }

        // Update other fields
        profile.DurianCode = dto.DurianCode;
        profile.DurianName = dto.DurianName;
        profile.Characteristics = dto.Characteristics;
        profile.TasteProfile = dto.TasteProfile;
        profile.DurianDescription = dto.DurianDescription;

        // Update video description if provided
        if (!string.IsNullOrEmpty(dto.VideoDescription) && profile.DurianVideo != null)
        {
            profile.DurianVideo.Description = dto.VideoDescription;
        }

        await _context.SaveChangesAsync();
        return Ok(profile.ToDurianProfileDtoWithNullCheck()); // Use the new mapper
    }
}

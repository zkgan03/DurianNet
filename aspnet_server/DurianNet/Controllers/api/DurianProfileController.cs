using DurianNet.Data;
using DurianNet.Dtos.Request.DurianProfile;
using DurianNet.Helpers;
using DurianNet.Mappers;
using DurianNet.Models.DataModels;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Newtonsoft.Json;

namespace DurianNet.Controllers.api
{
    [Route("api/[controller]")]
    [ApiController]
    public class DurianProfileController : ControllerBase
    {
        private readonly ApplicationDBContext _context;

        public DurianProfileController(ApplicationDBContext context)
        {
            _context = context;
        }

        [HttpGet("GetAllDurianProfiles")]
        public async Task<IActionResult> GetAllDurianProfiles([FromQuery] DurianQueryObject query)
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

        [HttpPost("AddDurianProfile")]
        public async Task<IActionResult> AddDurianProfile([FromBody] AddDurianProfileRequestDto dto)
        {
            // Check for duplicate Durian Name
            if (await _context.DurianProfiles.AnyAsync(dp => dp.DurianName == dto.DurianName))
            {
                return BadRequest("Durian name already exists.");
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

            return Ok(profile.ToDurianProfileDto());
        }



        [HttpPut("UpdateDurianProfile")]
        public async Task<IActionResult> UpdateDurianProfile([FromBody] UpdateDurianProfileRequestDto dto)
        {
            // Retrieve DurianId from session
            var durianId = HttpContext.Session.GetInt32("DurianId");
            if (!durianId.HasValue)
            {
                return BadRequest("Durian ID is missing from the session.");
            }

            var profile = await _context.DurianProfiles.Include(p => p.DurianVideo).FirstOrDefaultAsync(p => p.DurianId == durianId.Value);

            if (profile == null)
            {
                return NotFound("Durian profile not found");
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
            return Ok(profile.ToDurianProfileDto());
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

        [HttpGet("GetAllDurianProfilesForUser")]
        public async Task<IActionResult> GetAllDurianProfilesForUser()
        {
            var durianProfiles = await _context.DurianProfiles.ToListAsync();
            var profileDtos = durianProfiles.Select(dp => dp.ToDurianProfileForUserDto()).ToList();
            return Ok(profileDtos);
        }

        [HttpGet("GetAllDurianProfilesForAdmin")]
        public async Task<IActionResult> GetAllDurianProfilesForAdmin()
        {
            var durianProfiles = await _context.DurianProfiles.ToListAsync();
            var profileDtos = durianProfiles.Select(dp => dp.ToDurianProfileForAdminDto()).ToList();
            return Ok(profileDtos);
        }

    }
}

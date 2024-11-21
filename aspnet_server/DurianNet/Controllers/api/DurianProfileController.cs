using DurianNet.Data;
using DurianNet.Dtos.Request.DurianProfile;
using DurianNet.Helpers;
using DurianNet.Mappers;
using DurianNet.Models.DataModels;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
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
            return Ok(durianProfile.ToDurianProfileDto());
        }

        [HttpPost("AddDurianProfile")]
        public async Task<IActionResult> AddDurianProfile([FromForm] AddDurianProfileRequestDto dto)
        {
            // Check for duplicate Durian Name
            if (await _context.DurianProfiles.AnyAsync(dp => dp.DurianName == dto.DurianName))
            {
                return BadRequest("Durian name already exists.");
            }

            // Handle Durian Image upload
            string imageDirectory = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/images");
            if (!Directory.Exists(imageDirectory))
            {
                Directory.CreateDirectory(imageDirectory);
            }
            string imagePath = Path.Combine(imageDirectory, dto.DurianImage.FileName);
            using (var stream = new FileStream(imagePath, FileMode.Create))
            {
                await dto.DurianImage.CopyToAsync(stream);
            }
            string imageUrl = $"/images/{dto.DurianImage.FileName}"; // Relative URL for database storage

            // Handle Durian Video upload
            string videoDirectory = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/videos");
            if (!Directory.Exists(videoDirectory))
            {
                Directory.CreateDirectory(videoDirectory);
            }
            string videoPath = Path.Combine(videoDirectory, dto.DurianVideo.FileName);
            using (var stream = new FileStream(videoPath, FileMode.Create))
            {
                await dto.DurianVideo.CopyToAsync(stream);
            }
            string videoUrl = $"/videos/{dto.DurianVideo.FileName}";

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



        //[HttpPut("UpdateDurianProfile/{id}")]
        //public async Task<IActionResult> UpdateDurianProfile(int id, [FromBody] UpdateDurianProfileRequestDto dto)
        //{
        //    var profile = await _context.DurianProfiles.FindAsync(id);
        //    if (profile == null)
        //    {
        //        return NotFound("Durian profile not found");
        //    }

        //    var video = await _context.DurianVideos.FindAsync(profile.DurianVideoId);
        //    if (video != null)
        //    {
        //        video.UpdateDurianVideoFromDto(dto);
        //        await _context.SaveChangesAsync();
        //    }

        //    profile.UpdateDurianProfileFromDto(dto, profile.DurianVideoId);
        //    await _context.SaveChangesAsync();

        //    return Ok(profile.ToDurianProfileDto());
        //}


        [HttpPut("UpdateDurianProfile/{id}")]
        public async Task<IActionResult> UpdateDurianProfile(int id, [FromForm] UpdateDurianProfileRequestDto dto)
        {

            Console.WriteLine($"Received Durian ID: {id}");
            Console.WriteLine($"Payload received: {JsonConvert.SerializeObject(dto)}");

            var profile = await _context.DurianProfiles.Include(p => p.DurianVideo).FirstOrDefaultAsync(p => p.DurianId == id);
            if (profile == null)
            {
                return NotFound("Durian profile not found");
            }

            // Update Durian Image if provided
            if (dto.DurianImage != null)
            {
                string imageDirectory = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/images");
                if (!Directory.Exists(imageDirectory))
                {
                    Directory.CreateDirectory(imageDirectory);
                }
                string imagePath = Path.Combine(imageDirectory, dto.DurianImage.FileName);
                using (var stream = new FileStream(imagePath, FileMode.Create))
                {
                    await dto.DurianImage.CopyToAsync(stream);
                }
                profile.DurianImage = $"/images/{dto.DurianImage.FileName}";
            }

            // Update Durian Video if provided
            if (dto.DurianVideo != null)
            {
                string videoDirectory = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/videos");
                if (!Directory.Exists(videoDirectory))
                {
                    Directory.CreateDirectory(videoDirectory);
                }
                string videoPath = Path.Combine(videoDirectory, dto.DurianVideo.FileName);
                using (var stream = new FileStream(videoPath, FileMode.Create))
                {
                    await dto.DurianVideo.CopyToAsync(stream);
                }
                profile.DurianVideo.VideoUrl = $"/videos/{dto.DurianVideo.FileName}";
            }

            // Update Video Description if provided
            if (!string.IsNullOrEmpty(dto.VideoDescription) && profile.DurianVideo != null)
            {
                profile.DurianVideo.Description = dto.VideoDescription;
            }

            // Update other fields
            profile.DurianName = dto.DurianName;
            profile.Characteristics = dto.Characteristics;
            profile.TasteProfile = dto.TasteProfile;
            profile.DurianDescription = dto.DurianDescription;

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

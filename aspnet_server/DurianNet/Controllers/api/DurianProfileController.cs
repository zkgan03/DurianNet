using DurianNet.Data;
using DurianNet.Dtos.Request.DurianProfile;
using DurianNet.Helpers;
using DurianNet.Mappers;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

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
        public async Task<IActionResult> AddDurianProfile([FromBody] AddDurianProfileRequestDto dto)
        {
            var video = dto.ToDurianVideoFromAddRequest();
            _context.DurianVideos.Add(video);
            await _context.SaveChangesAsync();

            var profile = dto.ToDurianProfileFromAddRequest(video.VideoId);
            _context.DurianProfiles.Add(profile);
            await _context.SaveChangesAsync();

            return Ok(profile.ToDurianProfileDto());
        }

        [HttpPut("UpdateDurianProfile/{id}")]
        public async Task<IActionResult> UpdateDurianProfile(int id, [FromBody] UpdateDurianProfileRequestDto dto)
        {
            var profile = await _context.DurianProfiles.FindAsync(id);
            if (profile == null)
            {
                return NotFound("Durian profile not found");
            }

            var video = await _context.DurianVideos.FindAsync(profile.DurianVideoId);
            if (video != null)
            {
                video.UpdateDurianVideoFromDto(dto);
                await _context.SaveChangesAsync();
            }

            profile.UpdateDurianProfileFromDto(dto, profile.DurianVideoId);
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

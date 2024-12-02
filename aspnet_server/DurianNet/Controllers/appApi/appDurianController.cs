using DurianNet.Controllers.api;
using DurianNet.Data;
using DurianNet.Models.DataModels;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace DurianNet.Controllers.appApi
{
    [Route("appApi/durian")]
    [ApiController]
    public class appDurianController : ControllerBase
    {
        private readonly ApplicationDBContext _context;

        public appDurianController(ApplicationDBContext context)
        {
            _context = context;
        }

        // search favorite durian
        //search durian profile
        [HttpGet("appGetAllDurianProfiles")]
        public async Task<IActionResult> appGetAllDurianProfiles([FromQuery] string? searchQuery)
        {
            var durianProfiles = await _context.DurianProfiles
                .Where(dp => string.IsNullOrEmpty(searchQuery) || dp.DurianName.Contains(searchQuery))
                .Include(dp => dp.DurianVideo)
                .ToListAsync();

            if (!durianProfiles.Any())
            {
                return NotFound("No durian profiles found.");
            }

            return Ok(durianProfiles.Select(dp => new
            {
                dp.DurianId,
                dp.DurianName,
                dp.DurianCode,
                dp.DurianDescription,
                dp.Characteristics,
                dp.TasteProfile,
                dp.DurianImage,
                VideoUrl = dp.DurianVideo?.VideoUrl,
                VideoDescription = dp.DurianVideo?.Description
            }));
        }

        //display favorite durian
        //display durian profile
        [HttpGet("appGetAllDurianProfilesForUser")]
        public async Task<IActionResult> appGetAllDurianProfilesForUser()
        {
            var durians = await _context.DurianProfiles.ToListAsync();
            return Ok(durians.Select(d => new { d.DurianId, d.DurianName, d.DurianImage }));
        }

        //display durian profile details
        [HttpGet("appGetDurianProfile/{id}")]
        public async Task<IActionResult> appGetDurianProfile(int id)
        {
            var durian = await _context.DurianProfiles.Include(d => d.DurianVideo).FirstOrDefaultAsync(d => d.DurianId == id);
            if (durian == null) return NotFound("Durian not found");

            return Ok(new
            {
                durian.DurianName,
                durian.DurianCode,
                durian.DurianDescription,
                durian.Characteristics,
                durian.TasteProfile,
                durian.DurianImage,
                durian.DurianVideo.VideoUrl,
                durian.DurianVideo.Description
            });
        }

        //favorite durian for profile
        [HttpGet("appGetFavoriteDurians/{username}")]
        public async Task<IActionResult> appGetFavoriteDurians(string username)
        {
            var user = await _context.Users.Include(u => u.FavoriteDurians).ThenInclude(fd => fd.DurianProfile)
                .FirstOrDefaultAsync(u => u.UserName.ToLower() == username.ToLower());

            if (user == null) return NotFound("User not found");

            var favorites = user.FavoriteDurians.Select(fd => new
            {
                fd.DurianProfile.DurianId,
                fd.DurianProfile.DurianName,
                fd.DurianProfile.DurianImage
            });

            return Ok(favorites);
        }

        //favorite durian for profile
        [HttpPost("appAddFavoriteDurian")]
        public async Task<IActionResult> appAddFavoriteDurian([FromBody] AddFavoriteDurianRequest request)
        {
            var user = await _context.Users.Include(u => u.FavoriteDurians)
                .FirstOrDefaultAsync(u => u.UserName.ToLower() == request.Username.ToLower());

            if (user == null) return NotFound("User not found");

            var durian = await _context.DurianProfiles.FirstOrDefaultAsync(dp => dp.DurianName.ToLower() == request.DurianName.ToLower());
            if (durian == null) return NotFound("Durian not found");

            if (user.FavoriteDurians.Any(fd => fd.DurianId == durian.DurianId))
                return BadRequest("Durian already in favorites");

            user.FavoriteDurians.Add(new FavoriteDurian { UserId = user.Id, DurianId = durian.DurianId });
            await _context.SaveChangesAsync();

            return Ok("Favorite durian added successfully");
        }

        [HttpPost("appRemoveFavoriteDurian")]
        public async Task<IActionResult> appRemoveFavoriteDurian([FromBody] RemoveFavoriteDurianRequest request)
        {
            var user = await _context.Users
                .Include(u => u.FavoriteDurians)
                .ThenInclude(fd => fd.DurianProfile)
                .FirstOrDefaultAsync(u => u.UserName.ToLower() == request.Username.ToLower());

            if (user == null) return NotFound("User not found");

            var favorite = user.FavoriteDurians.FirstOrDefault(fd => fd.DurianProfile.DurianName.ToLower() == request.DurianName.ToLower());
            if (favorite == null) return NotFound("Favorite durian not found");

            user.FavoriteDurians.Remove(favorite);
            await _context.SaveChangesAsync();

            return Ok("Favorite durian removed successfully");
        }

    }
}

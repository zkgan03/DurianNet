//using DurianNet.Extensions;
//using DurianNet.Models.DataModels;
//using DurianNet.Services.FavoriteDurianService;
//using Microsoft.AspNetCore.Authorization;
//using Microsoft.AspNetCore.Identity;
//using Microsoft.AspNetCore.Mvc;

//namespace DurianNet.Controllers.api
//{
//    [Route("api/favoriteDurian")]
//    [ApiController]
//    public class FavoriteDurianController : ControllerBase
//    {
//        private readonly UserManager<User> _userManager;
//        private readonly Services.DurianProfileService.IDurianProfileRepository _durianProfileRepo;
//        private readonly IFavoriteDurian _favoriteDurianRepo;

//        public FavoriteDurianController(UserManager<User> userManager,
//        Services.DurianProfileService.IDurianProfileRepository durianProfileRepo, IFavoriteDurian favoriteDurianRepo)
//        {
//            _userManager = userManager;
//            _durianProfileRepo = durianProfileRepo;
//            _favoriteDurianRepo = favoriteDurianRepo;
//        }

//        [HttpGet]
//        [Authorize]
//        public async Task<IActionResult> GetUserFavoriteDurian()
//        {
//            var username = User.GetUsername();
//            var appUser = await _userManager.FindByNameAsync(username);
//            var userFavoriteDurian = await _favoriteDurianRepo.GetUserFavoriteDurian(appUser);
//            return Ok(userFavoriteDurian);
//        }

//        [HttpPost]
//        [Authorize]
//        public async Task<IActionResult> AddFavoriteDurian(string symbol)
//        {
//            var username = User.GetUsername();
//            var appUser = await _userManager.FindByNameAsync(username);
//            var durianProfile = await _durianProfileRepo.GetBySymbolAsync(symbol);

//            if (durianProfile == null) return BadRequest("Favorite Durian not found");

//            var userFavoriteDurian = await _favoriteDurianRepo.GetUserFavoriteDurian(appUser);

//            if (userFavoriteDurian.Any(e => e.DurianName.ToLower() == symbol.ToLower())) return BadRequest("Cannot add same durian to favorite durian");

//            var favoriteDurianModel = new FavoriteDurian
//            {
//                DurianId = durianProfile.DurianId,
//                UserId = appUser.Id
//            };

//            await _favoriteDurianRepo.CreateAsync(favoriteDurianModel);

//            if (favoriteDurianModel == null)
//            {
//                return StatusCode(500, "Could not create");
//            }
//            else
//            {
//                return Created();
//            }
//        }

//        [HttpDelete]
//        [Authorize]
//        public async Task<IActionResult> DeleteFavoriteDurian(string symbol)
//        {
//            var username = User.GetUsername();
//            var appUser = await _userManager.FindByNameAsync(username);

//            var userFavoriteDurian = await _favoriteDurianRepo.GetUserFavoriteDurian(appUser);

//            var filteredDurianProfile = userFavoriteDurian.Where(s => s.DurianName.ToLower() == symbol.ToLower()).ToList();

//            if (filteredDurianProfile.Count() == 1)
//            {
//                await _favoriteDurianRepo.DeleteFavoriteDurian(appUser, symbol);
//            }
//            else
//            {
//                return BadRequest("Durian not in your Favorite Durian");
//            }

//            return Ok();
//        }
//    }
//}
using DurianNet.Models.DataModels;
using DurianNet.Data;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace DurianNet.Controllers.api
{
    [Route("api/favoriteDurian")]
    [ApiController]
    public class FavoriteDurianController : ControllerBase
    {
        private readonly ApplicationDBContext _context;

        public FavoriteDurianController(ApplicationDBContext context)
        {
            _context = context;
        }

        // GET: api/favoriteDurian/{username}
        [HttpGet("{username}")]
        public async Task<IActionResult> GetFavoriteDurians(string username)
        {
            var user = await _context.Users
                .Include(u => u.FavoriteDurians)
                .ThenInclude(f => f.DurianProfile)
                .FirstOrDefaultAsync(u => u.UserName.ToLower() == username.ToLower());

            if (user == null) return NotFound("User not found.");

            var favoriteDurians = user.FavoriteDurians.Select(fd => new
            {
                DurianId = fd.DurianId,
                DurianName = fd.DurianProfile.DurianName,
                DurianDescription = fd.DurianProfile.DurianDescription,
                DurianImage = fd.DurianProfile.DurianImage
            });

            return Ok(favoriteDurians);
        }

        // POST: api/favoriteDurian
        [HttpPost]
        public async Task<IActionResult> AddFavoriteDurian([FromBody] AddFavoriteDurianRequest request)
        {
            var user = await _context.Users
                .Include(u => u.FavoriteDurians)
                .FirstOrDefaultAsync(u => u.UserName.ToLower() == request.Username.ToLower());

            if (user == null) return NotFound("User not found.");

            var durianProfile = await _context.DurianProfiles
                .FirstOrDefaultAsync(dp => dp.DurianName.ToLower() == request.DurianName.ToLower());

            if (durianProfile == null) return NotFound("Durian not found.");

            if (user.FavoriteDurians.Any(fd => fd.DurianId == durianProfile.DurianId))
                return BadRequest("This durian is already in the user's favorite list.");

            var favoriteDurian = new FavoriteDurian
            {
                UserId = user.Id,
                DurianId = durianProfile.DurianId
            };

            _context.FavoriteDurians.Add(favoriteDurian);
            await _context.SaveChangesAsync();

            return Ok("Durian added to favorites.");
        }

        //// DELETE: api/favoriteDurian
        //[HttpDelete]
        //public async Task<IActionResult> RemoveFavoriteDurian([FromBody] RemoveFavoriteDurianRequest request)
        //{
        //    var user = await _context.Users
        //        .Include(u => u.FavoriteDurians)
        //        .FirstOrDefaultAsync(u => u.UserName.ToLower() == request.Username.ToLower());

        //    if (user == null) return NotFound("User not found.");

        //    var favoriteDurian = user.FavoriteDurians.FirstOrDefault(fd => fd.DurianProfile.DurianName.ToLower() == request.DurianName.ToLower());

        //    if (favoriteDurian == null) return BadRequest("This durian is not in the user's favorite list.");

        //    _context.FavoriteDurians.Remove(favoriteDurian);
        //    await _context.SaveChangesAsync();

        //    return Ok("Durian removed from favorites.");
        //}

        [HttpDelete]
        public async Task<IActionResult> RemoveFavoriteDurian([FromBody] RemoveFavoriteDurianRequest request)
        {
            var user = await _context.Users
                .Include(u => u.FavoriteDurians)
                .ThenInclude(fd => fd.DurianProfile) // Ensure navigation property is included
                .FirstOrDefaultAsync(u => u.UserName.ToLower() == request.Username.ToLower());

            if (user == null) return NotFound("User not found.");

            if (user.FavoriteDurians == null)
                return BadRequest("Favorite durians not loaded or do not exist for this user.");

            var favoriteDurian = user.FavoriteDurians
                .FirstOrDefault(fd => fd.DurianProfile?.DurianName.ToLower() == request.DurianName.ToLower());

            if (favoriteDurian == null) return BadRequest("This durian is not in the user's favorite list.");

            _context.FavoriteDurians.Remove(favoriteDurian);
            await _context.SaveChangesAsync();

            return Ok("Durian removed from favorites.");
        }

    }

    // Request DTOs
    public class AddFavoriteDurianRequest
    {
        public string Username { get; set; }
        public string DurianName { get; set; }
    }

    public class RemoveFavoriteDurianRequest
    {
        public string Username { get; set; }
        public string DurianName { get; set; }
    }
}

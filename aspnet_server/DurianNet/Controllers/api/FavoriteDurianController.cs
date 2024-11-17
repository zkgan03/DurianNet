using DurianNet.Extensions;
using DurianNet.Interfaces;
using DurianNet.Models.DataModels;
using DurianNet.Repository;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;

namespace DurianNet.Controllers.api
{
    [Route("api/favoriteDurian")]
    [ApiController]
    public class FavoriteDurianController : ControllerBase
    {
        private readonly UserManager<User> _userManager;
        private readonly IDurianProfileRepository _durianProfileRepo;
        private readonly IFavoriteDurian _favoriteDurianRepo;

        public FavoriteDurianController(UserManager<User> userManager,
        IDurianProfileRepository durianProfileRepo, IFavoriteDurian favoriteDurianRepo)
        {
            _userManager = userManager;
            _durianProfileRepo = durianProfileRepo;
            _favoriteDurianRepo = favoriteDurianRepo;
        }

        [HttpGet]
        [Authorize]
        public async Task<IActionResult> GetUserFavoriteDurian()
        {
            var username = User.GetUsername();
            var appUser = await _userManager.FindByNameAsync(username);
            var userFavoriteDurian = await _favoriteDurianRepo.GetUserFavoriteDurian(appUser);
            return Ok(userFavoriteDurian);
        }

        [HttpPost]
        [Authorize]
        public async Task<IActionResult> AddFavoriteDurian(string symbol)
        {
            var username = User.GetUsername();
            var appUser = await _userManager.FindByNameAsync(username);
            var durianProfile = await _durianProfileRepo.GetBySymbolAsync(symbol);

            if (durianProfile == null) return BadRequest("Stock not found");

            var userFavoriteDurian = await _favoriteDurianRepo.GetUserFavoriteDurian(appUser);

            if (userFavoriteDurian.Any(e => e.DurianName.ToLower() == symbol.ToLower())) return BadRequest("Cannot add same durian to favorite durian");

            var favoriteDurianModel = new FavoriteDurian
            {
                DurianId = durianProfile.DurianId,
                UserId = appUser.Id
            };

            await _favoriteDurianRepo.CreateAsync(favoriteDurianModel);

            if (favoriteDurianModel == null)
            {
                return StatusCode(500, "Could not create");
            }
            else
            {
                return Created();
            }
        }

        [HttpDelete]
        [Authorize]
        public async Task<IActionResult> DeleteFavoriteDurian(string symbol)
        {
            var username = User.GetUsername();
            var appUser = await _userManager.FindByNameAsync(username);

            var userFavoriteDurian = await _favoriteDurianRepo.GetUserFavoriteDurian(appUser);

            var filteredDurianProfile = userFavoriteDurian.Where(s => s.DurianName.ToLower() == symbol.ToLower()).ToList();

            if (filteredDurianProfile.Count() == 1)
            {
                await _favoriteDurianRepo.DeleteFavoriteDurian(appUser, symbol);
            }
            else
            {
                return BadRequest("Durian not in your Favorite Durian");
            }

            return Ok();
        }
    }
}

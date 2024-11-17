using DurianNet.Data;
using DurianNet.Interfaces;
using DurianNet.Models.DataModels;
using Microsoft.EntityFrameworkCore;

namespace DurianNet.Repository
{
    public class FavoriteDurianRepository : IFavoriteDurian
    {
        private readonly ApplicationDBContext _context;
        
        public FavoriteDurianRepository(ApplicationDBContext context)
        {
            _context = context;
        }

        public async Task<FavoriteDurian> CreateAsync(FavoriteDurian favoriteDurian)
        {
            await _context.FavoriteDurians.AddAsync(favoriteDurian);
            await _context.SaveChangesAsync();
            return favoriteDurian;
        }

        public async Task<FavoriteDurian> DeleteFavoriteDurian(User user, string symbol)
        {
            var favoriteDurianModel = await _context.FavoriteDurians.FirstOrDefaultAsync(x => x.UserId == user.Id && x.DurianProfile.DurianName.ToLower() == symbol.ToLower());

            if (favoriteDurianModel == null)
            {
                return null;
            }

            _context.FavoriteDurians.Remove(favoriteDurianModel);
            await _context.SaveChangesAsync();
            return favoriteDurianModel;
        }

        public async Task<List<DurianProfile>> GetUserFavoriteDurian(User user)
        {
            return await _context.FavoriteDurians.Where(u => u.UserId == user.Id)
            .Select(durianProfile => new DurianProfile
            {
                DurianId = durianProfile.DurianId,
                DurianName = durianProfile.DurianProfile.DurianName,
                DurianDescription = durianProfile.DurianProfile.DurianDescription,
                Characteristics = durianProfile.DurianProfile.Characteristics,
                TasteProfile = durianProfile.DurianProfile.TasteProfile,
                DurianImage = durianProfile.DurianProfile.DurianImage
            }).ToListAsync();
        }

    }
}

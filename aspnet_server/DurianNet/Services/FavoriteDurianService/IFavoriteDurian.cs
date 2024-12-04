using DurianNet.Models.DataModels;

namespace DurianNet.Services.FavoriteDurianService
{
    public interface IFavoriteDurian
    {
        Task<List<DurianProfile>> GetUserFavoriteDurian(User user);
        Task<FavoriteDurian> CreateAsync(FavoriteDurian favoriteDurian);
        Task<FavoriteDurian> DeleteFavoriteDurian(User user, string symbol);
    }
}

using DurianNet.Models.DataModels;
using DurianNet.Repository;

namespace DurianNet.Interfaces
{
    public interface IFavoriteDurian
    {
        Task<List<DurianProfile>> GetUserFavoriteDurian(User user);
        Task<FavoriteDurian> CreateAsync(FavoriteDurian favoriteDurian);
        Task<FavoriteDurian> DeleteFavoriteDurian(User user, string symbol);
    }
}

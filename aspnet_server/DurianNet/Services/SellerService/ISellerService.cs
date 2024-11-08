using DurianNet.Models.DataModels;

namespace DurianNet.Services.SellerService
{
    public interface ISellerService
    {
        Task<List<Seller>> GetSellersAddedByUserAsync(string userId);
        Task<List<Seller>> SearchSellerByNameAsync(string name);
        Task<List<Seller>> GetAllSellersAsync();
        Task<Seller> GetSellerByIdAsync(int id);
        Task<Seller> AddSellerAsync(Seller seller);
        Task<Seller> UpdateSellerAsync(int id, Seller seller);
        Task RemoveSellerAsync(int id);
    }
}

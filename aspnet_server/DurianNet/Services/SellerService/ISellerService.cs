using DurianNet.Models.DataModels;

namespace DurianNet.Services.SellerService
{
    public interface ISellerService
    {
        Task<List<Seller>> GetAllSellersAsync();
        Task<Seller> GetSellerByIdAsync(int id);
        Task AddSellerAsync(Seller seller);
        Task UpdateSellerAsync(int id, Seller seller);
        Task RemoveSellerAsync(int id);
    }
}

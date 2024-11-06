using DurianNet.Models.DataModels;
using DurianNet.Utils;

namespace DurianNet.Services.SellerService
{
    public class FakeSellerService : ISellerService
    {
        public async Task AddSellerAsync(Seller seller)
        {
            FakeAppData.dummySellers.Add(seller);
        }

        public async Task<List<Seller>> GetAllSellersAsync()
        {
            return FakeAppData.dummySellers;
        }

        public async Task<Seller> GetSellerByIdAsync(int id)
        {
            var seller = FakeAppData
                .dummySellers
                .FirstOrDefault(s => s.SellerId == id);

            if (seller == null)
            {
                throw new Exception("Seller not found");
            }

            return seller;
        }

        public async Task RemoveSellerAsync(int id)
        {
            var seller = FakeAppData
               .dummySellers
               .FirstOrDefault(s => s.SellerId == id);

            if (seller == null)
            {
                throw new Exception("Seller not found");
            }

            FakeAppData.dummySellers.Remove(seller);
        }

        public async Task UpdateSellerAsync(int id, Seller seller)
        {
            var sellerInDb = FakeAppData
               .dummySellers
               .FirstOrDefault(s => s.SellerId == id);

            if (sellerInDb == null)
            {
                throw new Exception("Seller not found");
            }

            sellerInDb.Name = seller.Name;
            sellerInDb.Description = seller.Description;
            sellerInDb.Image = seller.Image;
            sellerInDb.Rating = seller.Rating;
            sellerInDb.Latitude = seller.Latitude;
            sellerInDb.Longitude = seller.Longitude;
            sellerInDb.DurianSold = seller.DurianSold;
            sellerInDb.Comments = seller.Comments;
        }



    }
}

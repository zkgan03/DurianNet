using DurianNet.Dtos.Response;
using DurianNet.Models.DataModels;

namespace DurianNet.Mappers
{
    public static class SellerMapper
    {
        public static SellerDtoResponse ToSellerDtoResponse(this Seller seller)
        {
            return new SellerDtoResponse
            {
                SellerId = seller.SellerId,
                Name = seller.Name,
                Description = seller.Description,
                Image = seller.Image,
                Rating = seller.Rating,
                Latitude = seller.Latitude,
                Longitude = seller.Longitude,
                DurianTypes = seller.DurianSold.Select(durian => durian.DurianName).ToList()
            };
        }
    }
}

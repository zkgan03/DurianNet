using System.Text.Json.Serialization;

namespace DurianNet.Dtos.Response
{
    public class SellerDtoResponse
    {
        [JsonPropertyName("sellerId")] public int SellerId { get; set; }
        [JsonPropertyName("name")] public string Name { get; set; }
        [JsonPropertyName("description")] public string Description { get; set; }
        [JsonPropertyName("imageUrl")] public string ImageUrl { get; set; }
        [JsonPropertyName("rating")] public double Rating { get; set; }
        [JsonPropertyName("latitude")] public double Latitude { get; set; }
        [JsonPropertyName("longitude")] public double Longitude { get; set; }
        [JsonPropertyName("durianTypes")] public List<DurianTypeDto> DurianTypes { get; set; }
        [JsonPropertyName("addByUser")] public SellerUserDto User { get; set; }

        public class DurianTypeDto
        {
            [JsonPropertyName("durianId")] public int DurianId { get; set; }
            [JsonPropertyName("name")] public string Name { get; set; }
        }

        public class SellerUserDto
        {
            [JsonPropertyName("userId")] public string UserId { get; set; }
            [JsonPropertyName("username")] public string Username { get; set; }
        }
    }


}

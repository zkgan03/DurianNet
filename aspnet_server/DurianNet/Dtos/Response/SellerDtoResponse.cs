using System.Text.Json.Serialization;

namespace DurianNet.Dtos.Response
{
    public class SellerDtoResponse
    {
        [JsonPropertyName("sellerId")] public int SellerId { get; set; }
        [JsonPropertyName("name")] public string Name { get; set; }
        [JsonPropertyName("description")] public string Description { get; set; }
        [JsonPropertyName("image")] public string Image { get; set; }
        [JsonPropertyName("rating")] public double Rating { get; set; }
        [JsonPropertyName("latitude")] public double Latitude { get; set; }
        [JsonPropertyName("longitude")] public double Longitude { get; set; }
        [JsonPropertyName("durianTypes")] public List<string> DurianTypes { get; set; }
    }
}

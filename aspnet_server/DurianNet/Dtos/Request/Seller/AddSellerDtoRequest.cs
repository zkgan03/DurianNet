using Microsoft.Extensions.Configuration.UserSecrets;
using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace DurianNet.Dtos.Request.Seller
{
    public class AddSellerDtoRequest
    {
        [Required]
        [JsonPropertyName("userId")]
        public string UserId { get; set; } // user added this seller

        [Required]
        [JsonPropertyName("name")]
        public string Name { get; set; }

        [Required]
        [JsonPropertyName("description")]
        public string Description { get; set; }

        [Required]
        [JsonPropertyName("base64Image")]
        public string Image { get; set; }

        [Required]
        [JsonPropertyName("latitude")]
        public double Latitude { get; set; }

        [Required]
        [JsonPropertyName("longitude")]
        public double Longitude { get; set; }

        [Required]
        [JsonPropertyName("durianProfileId")]
        public List<int> DurianProfileId { get; set; }
    }
}

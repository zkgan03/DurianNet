using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace DurianNet.Dtos.Request.Seller
{
    public class UpdateSellerDtoRequest
    {
        [Required]
        [JsonPropertyName("name")]
        public string Name { get; set; }
        [Required]
        [JsonPropertyName("description")]
        public string Description { get; set; }
        [Required]
        [JsonPropertyName("durianProfileId")]
        public List<int> DurianProfileId { get; set; }
    }
}

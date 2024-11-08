using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace DurianNet.Dtos.Request.Seller
{
    public class SearchSellersRequest
    {
        [Required]
        [JsonPropertyName("query")]
        public string query { get; set; } = string.Empty;
    }
}

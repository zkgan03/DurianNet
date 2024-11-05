using DurianNet.Models.DataModels;
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
        [JsonPropertyName("durianTypes")] public HashSet<string> DurianTypes { get; set; }
        [JsonPropertyName("comments")] public List<CommentDto> Comments { get; set; }
    }

    public class CommentDto
    {
        [JsonPropertyName("commentId")] public int CommentId { get; set; }
        [JsonPropertyName("rating")] public float Rating { get; set; }
        [JsonPropertyName("content")] public string Content { get; set; } = string.Empty;
        [JsonPropertyName("user")] public UserDto User { get; set; }
    }

    public class UserDto
    {
        [JsonPropertyName("userId")] public int UserId { get; set; }
        [JsonPropertyName("username")] public string Username { get; set; }
    }
}

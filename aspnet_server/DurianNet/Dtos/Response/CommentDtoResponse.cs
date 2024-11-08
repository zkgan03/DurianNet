using System.Text.Json.Serialization;

namespace DurianNet.Dtos.Response
{
    public class CommentDtoResponse
    {

        [JsonPropertyName("commentId")] public int CommentId { get; set; }
        [JsonPropertyName("rating")] public float Rating { get; set; }
        [JsonPropertyName("content")] public string Content { get; set; } = string.Empty;
        [JsonPropertyName("user")] public CommentUserDto User { get; set; }
        [JsonPropertyName("seller")] public CommentSellerDto Seller { get; set; }


        public class CommentUserDto
        {
            [JsonPropertyName("userId")] public string UserId { get; set; }
            [JsonPropertyName("username")] public string Username { get; set; }
            [JsonPropertyName("imageUrl")] public string ImageUrl { get; set; }
        }

        public class CommentSellerDto
        {
            [JsonPropertyName("sellerId")] public int SellerId { get; set; }
            [JsonPropertyName("name")] public string Name { get; set; }
        }
    }
}

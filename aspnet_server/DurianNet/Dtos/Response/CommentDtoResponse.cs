using System.Text.Json.Serialization;

namespace DurianNet.Dtos.Response
{
    public class CommentDtoResponse
    {

        [JsonPropertyName("commentId")] public int CommentId { get; set; }
        [JsonPropertyName("rating")] public float Rating { get; set; }
        [JsonPropertyName("content")] public string Content { get; set; } = string.Empty;
        [JsonPropertyName("user")] public UserDto User { get; set; }


        public class UserDto
        {
            [JsonPropertyName("userId")] public int UserId { get; set; }
            [JsonPropertyName("username")] public string Username { get; set; }
        }
    }
}

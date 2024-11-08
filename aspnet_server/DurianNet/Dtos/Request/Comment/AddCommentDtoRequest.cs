using System.ComponentModel.DataAnnotations;

namespace DurianNet.Dtos.Request.Comment
{
    public class AddCommentDtoRequest
    {
        [Required] public string UserId { get; set; } // user added this comment
        [Required] public int SellerId { get; set; } // seller commented
        [Required] public float Rating { get; set; }
        [Required] public string Content { get; set; }
    }
}

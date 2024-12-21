using System.ComponentModel.DataAnnotations;

namespace DurianNet.Dtos.Request.Comment
{
    public class AddCommentDtoRequest
    {
        [Required] public int SellerId { get; set; } // seller commented
        [Required] public float Rating { get; set; }
        [Required] public string Content { get; set; }
    }
}

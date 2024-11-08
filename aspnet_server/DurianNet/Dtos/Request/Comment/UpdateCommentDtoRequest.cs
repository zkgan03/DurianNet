using System.ComponentModel.DataAnnotations;

namespace DurianNet.Dtos.Request.Comment
{
    public class UpdateCommentDtoRequest
    {
        [Required] public float Rating { get; set; }
        [Required] public string Content { get; set; }
    }
}

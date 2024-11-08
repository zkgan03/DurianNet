using DurianNet.Dtos.Request.Comment;
using DurianNet.Mappers;
using DurianNet.Services.CommentService;
using DurianNet.Services.SellerService;
using Microsoft.AspNetCore.Mvc;

namespace DurianNet.Controllers.api
{
    [Route("api/[controller]")]
    [ApiController]
    public class CommentController : ControllerBase
    {
        private readonly ICommentService _commentService;
        private readonly ISellerService _sellerService;

        public CommentController(ICommentService commentService, ISellerService sellerService)
        {
            _commentService = commentService;
            _sellerService = sellerService;
        }


        [HttpGet("GetSellerComments/{sellerId}")]
        public async Task<IActionResult> GetSellerComments([FromRoute] int sellerId)
        {
            Console.WriteLine("GetSellerComments called");

            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            var seller = await _sellerService.GetSellerByIdAsync(sellerId);

            if (seller == null)
            {
                return BadRequest(ModelState);
            }

            var comments = await _commentService.GetCommentsBySellerIdAsync(sellerId);

            var response = comments.Select(c => c.ToCommentDtoResponse()).ToList();

            return Ok(response);
        }

        [HttpGet("GetComment/{commentId}")]
        public async Task<IActionResult> GetCommentById([FromRoute] int commentId)
        {
            Console.WriteLine("GetComment called");

            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            var comment = await _commentService.GetCommentByIdAsync(commentId);

            if (comment == null)
            {
                return BadRequest(ModelState);
            }

            var response = comment.ToCommentDtoResponse();

            return Ok(response);
        }


        [HttpPost("AddComment")]
        public async Task<IActionResult> AddComment([FromBody] AddCommentDtoRequest request)
        {
            Console.WriteLine("AddComment called");

            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            var addedComment = await _commentService.AddCommentAsync(request.ToCommentFromAdd());

            return Ok(addedComment.ToCommentDtoResponse());
        }

        [HttpPut("UpdateComment/{commentId}")]
        public async Task<IActionResult> UpdateComment([FromRoute] int commentId, [FromBody] UpdateCommentDtoRequest request)
        {
            Console.WriteLine("UpdateComment called");

            if (!ModelState.IsValid) return BadRequest(ModelState);

            var updatedComment = await _commentService.UpdateCommentAsync(commentId, request.ToCommentFromUpdate());

            return Ok(updatedComment.ToCommentDtoResponse());
        }

        [HttpDelete("RemoveComment/{commentId}")]
        public async Task<IActionResult> RemoveComment([FromRoute] int commentId)
        {
            Console.WriteLine("RemoveComment called");

            if (!ModelState.IsValid) return BadRequest(ModelState);

            await _commentService.RemoveCommentAsync(commentId);

            return Ok();
        }
    }
}

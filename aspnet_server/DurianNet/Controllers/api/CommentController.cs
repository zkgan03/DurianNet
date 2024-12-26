using DurianNet.Dtos.Request.Comment;
using DurianNet.Mappers;
using DurianNet.Services.CommentService;
using DurianNet.Services.SellerService;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace DurianNet.Controllers.api
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
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
                return NotFound("Comment not found");
            }

            var response = comment.ToCommentDtoResponse();

            return Ok(response);
        }



        [HttpPost("AddComment")]
        public async Task<IActionResult> AddComment([FromBody] AddCommentDtoRequest request)
        {
            Console.WriteLine("AddComment called");

            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (userId == null) return Unauthorized();

            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            var comment = request.ToCommentFromAdd();
            comment.UserId = userId;
            var addedComment = await _commentService.AddCommentAsync(comment);

            return Ok(addedComment.ToCommentDtoResponse());
        }



        [HttpPut("UpdateComment/{commentId}")]
        public async Task<IActionResult> UpdateComment([FromRoute] int commentId, [FromBody] UpdateCommentDtoRequest request)
        {
            Console.WriteLine("UpdateComment called");

            if (!ModelState.IsValid) return BadRequest(ModelState);

            var comment = await _commentService.GetCommentByIdAsync(commentId);
            if (comment == null) return BadRequest(ModelState);

            // get the user id from the token (authentication)
            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (userId != comment.UserId || userId == null) return Unauthorized("User not authorized to update comment");

            var updatedComment = await _commentService.UpdateCommentAsync(commentId, request.ToCommentFromUpdate());

            return Ok(updatedComment.ToCommentDtoResponse());
        }

        [HttpDelete("RemoveComment/{commentId}")]
        public async Task<IActionResult> RemoveComment([FromRoute] int commentId)
        {
            Console.WriteLine("RemoveComment called");

            if (!ModelState.IsValid) return BadRequest(ModelState);

            var comment = await _commentService.GetCommentByIdAsync(commentId);
            if (comment == null) return BadRequest(ModelState);

            // get the user id from the token (authentication)
            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (userId != comment.UserId || userId == null) return Unauthorized("User not authorized to remove comment");

            await _commentService.RemoveCommentAsync(commentId);

            return Ok();
        }
    }
}

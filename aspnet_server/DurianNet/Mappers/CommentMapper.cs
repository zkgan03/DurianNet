using DurianNet.Dtos.Request.Comment;
using DurianNet.Dtos.Response;
using DurianNet.Models.DataModels;

namespace DurianNet.Mappers
{
    public static class CommentMapper
    {
        public static CommentDtoResponse ToCommentDtoResponse(this Comment comment)
        {
            return new CommentDtoResponse
            {
                CommentId = comment.CommentId,
                Rating = comment.Rating,
                Content = comment.Content,
                User = new CommentDtoResponse.CommentUserDto
                {
                    UserId = comment.UserId,
                    Username = comment.User.UserName,
                    ImageUrl = comment.User.ProfilePicture
                },
                Seller = new CommentDtoResponse.CommentSellerDto
                {
                    SellerId = comment.SellerId,
                    Name = comment.Seller.Name
                }
            };
        }

        public static Comment ToCommentFromAdd(this AddCommentDtoRequest addCommentDtoRequest)
        {
            return new Comment
            {
                Rating = addCommentDtoRequest.Rating,
                Content = addCommentDtoRequest.Content,
                UserId = addCommentDtoRequest.UserId,
                SellerId = addCommentDtoRequest.SellerId
            };
        }

        public static Comment ToCommentFromUpdate(this UpdateCommentDtoRequest updateCommentDtoRequest)
        {
            return new Comment
            {
                Rating = updateCommentDtoRequest.Rating,
                Content = updateCommentDtoRequest.Content,
            };
        }
    }
}

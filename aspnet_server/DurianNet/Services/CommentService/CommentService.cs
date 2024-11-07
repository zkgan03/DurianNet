using DurianNet.Models.DataModels;

namespace DurianNet.Services.CommentService
{
    public class CommentService : ICommentService
    {
        public Task AddCommentAsync(Comment sellerComment)
        {
            throw new NotImplementedException();
        }

        public Task<List<Comment>> GetAllCommentsBySellerIdAsync(int id)
        {
            throw new NotImplementedException();
        }

        public Task<Comment> GetCommentByIdAsync(int id)
        {
            throw new NotImplementedException();
        }

        public Task RemoveCommentAsync(int id)
        {
            throw new NotImplementedException();
        }

        public Task UpdateCommentAsync(int id, Comment sellerComment)
        {
            throw new NotImplementedException();
        }
    }
}

using DurianNet.Models.DataModels;

namespace DurianNet.Services.CommentService
{
    public interface ICommentService
    {
        Task<List<Comment>> GetCommentsBySellerIdAsync(int id);
        Task<Comment> GetCommentByIdAsync(int id);
        Task<Comment> AddCommentAsync(Comment sellerComment);
        Task<Comment> UpdateCommentAsync(int id, Comment sellerComment);
        Task RemoveCommentAsync(int id);
    }
}

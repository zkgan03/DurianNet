using DurianNet.Models.DataModels;

namespace DurianNet.Services.CommentService
{
    public interface ICommentService
    {
        Task<List<Comment>> GetAllCommentsBySellerIdAsync(int id);
        Task<Comment> GetCommentByIdAsync(int id);
        Task AddCommentAsync(Comment sellerComment);
        Task UpdateCommentAsync(int id, Comment sellerComment);
        Task RemoveCommentAsync(int id);
    }
}

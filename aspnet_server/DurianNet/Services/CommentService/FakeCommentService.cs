using DurianNet.Models.DataModels;
using DurianNet.Utils;

namespace DurianNet.Services.CommentService
{
    public class FakeCommentService : ICommentService
    {
        public async Task AddCommentAsync(Comment sellerComment)
        {
            FakeAppData.dummyComments.Add(sellerComment);
        }

        public async Task<List<Comment>> GetAllCommentsBySellerIdAsync(int id)
        {
            return FakeAppData.dummyComments.Where(c => c.SellerId == id).ToList();
        }

        public async Task<Comment> GetCommentByIdAsync(int id)
        {
            var comment = FakeAppData
                .dummyComments
                .FirstOrDefault(c => c.CommentId == id);

            if (comment == null)
            {
                throw new Exception("Comment not found");
            }

            return comment;
        }

        public async Task RemoveCommentAsync(int id)
        {
            FakeAppData.dummyComments.RemoveAll(c => c.CommentId == id);
        }

        public async Task UpdateCommentAsync(int id, Comment sellerComment)
        {
            var comment = FakeAppData
              .dummyComments
              .FirstOrDefault(c => c.CommentId == id);

            if (comment == null)
            {
                throw new Exception("Comment not found");
            }


            comment.Content = sellerComment.Content;
        }
    }
}

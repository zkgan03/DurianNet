using DurianNet.Data;
using DurianNet.Exceptions;
using DurianNet.Models.DataModels;
using DurianNet.Utils;
using Microsoft.EntityFrameworkCore;

namespace DurianNet.Services.CommentService
{
    public class CommentService : ICommentService
    {
        private readonly ApplicationDBContext _context;

        public CommentService(ApplicationDBContext context)
        {
            _context = context;
        }

        public async Task<Comment> AddCommentAsync(Comment comment)
        {
            await _context.Comments.AddAsync(comment);
            await _context.SaveChangesAsync();

            // Load related entities
            await _context.Entry(comment).Reference(c => c.User).LoadAsync();
            await _context.Entry(comment).Reference(c => c.Seller).LoadAsync();

            return comment;
        }

        public async Task<List<Comment>> GetCommentsBySellerIdAsync(int id)
        {
            var comments = await _context.Comments
                                 .Where(c => c.SellerId == id)
                                 .Include(c => c.User)
                                 .ToListAsync();

            return comments;
        }

        public async Task<Comment> GetCommentByIdAsync(int id)
        {
            var comment = await _context.Comments
                .Include(c => c.User)
                .Include(c => c.Seller)
                .Where(c => c.CommentId == id)
                .FirstOrDefaultAsync();

            if (comment == null)
            {
                throw new DataNotFoundException("Comment Not Found");
            }

            return comment;
        }

        public async Task RemoveCommentAsync(int id)
        {
            var comment = await _context.Comments.FindAsync(id);

            if (comment == null)
            {
                throw new DataNotFoundException("Comment Not Found");
            }

            _context.Comments.Remove(comment);

            await _context.SaveChangesAsync();
        }

        public async Task<Comment> UpdateCommentAsync(int id, Comment newComment)
        {
            var comment = await _context.Comments.FindAsync(id);

            if (comment == null)
            {
                throw new DataNotFoundException("Comment Not Found");
            }

            comment.Content = newComment.Content;
            comment.Rating = newComment.Rating;

            await _context.SaveChangesAsync();

            _context.Entry(comment).Reference(c => c.User).Load();
            _context.Entry(comment).Reference(c => c.Seller).Load();

            return comment;
        }
    }
}

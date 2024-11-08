using System.ComponentModel.DataAnnotations;

namespace DurianNet.Models.DataModels
{
    public class Comment
    {
        [Key]
        public int CommentId { get; set; }
        public float Rating { get; set; }
        public string Content { get; set; } = string.Empty;
        public string UserId { get; set; } // userId in identity framework is string
        public int SellerId { get; set; }

        public User User { get; set; }
        public Seller Seller { get; set; }

        public override bool Equals(object obj)
        {
            if (obj is Comment otherComment)
            {
                return this.CommentId == otherComment.CommentId;
            }
            return false;
        }
    }
}

using System.ComponentModel.DataAnnotations.Schema;

namespace DurianNet.Models.DataModels
{
    [Table("FavoriteDurian")]
    public class FavoriteDurian
    {
        public string UserId { get; set; }
        public int DurianId { get; set; }
        public User User { get; set; }
        public DurianProfile DurianProfile { get; set; }
    }
}

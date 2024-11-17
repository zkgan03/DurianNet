using DurianNet.Repository;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace DurianNet.Models.DataModels
{
    [Table("DurianProfile")]
    public class DurianProfile
    {
        [Key]
        public int DurianId { get; set; }
        public string DurianName { get; set; }
        public string DurianDescription { get; set; }
        public string Characteristics { get; set; }
        public string TasteProfile { get; set; }
        public string DurianImage { get; set; }
        public int DurianVideoId { get; set; }

        public DurianVideo DurianVideo { get; set; }
        public ICollection<Seller> Sellers { get; set; } //To indicate many to many relationship
        public ICollection<User> Users { get; set; }//To indicate many to many relationship

        public List<FavoriteDurian> FavoriteDurians { get; set; } = new List<FavoriteDurian>();
    }
}

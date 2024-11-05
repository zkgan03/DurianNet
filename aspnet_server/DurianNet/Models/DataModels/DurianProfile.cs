using System.ComponentModel.DataAnnotations;

namespace DurianNet.Models.DataModels
{
    public class DurianProfile
    {
        [Key]
        public string DurianId { get; set; }
        public string DurianName { get; set; }
        public string DurianDescription { get; set; }
        public string Characteristics { get; set; }
        public string TasteProfile { get; set; }
        public string DurianImage { get; set; }
        public int DurianVideoId { get; set; }

        public DurianVideo DurianVideo { get; set; }
        public ICollection<Seller> Sellers { get; set; } //To indicate many to many relationship
        public ICollection<User> Users { get; set; }//To indicate many to many relationship
    }
}

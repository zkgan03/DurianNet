using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace DurianNet.Models.DataModels
{
    public class Seller
    {
        [Key]
        public int SellerId { get; set; }
        public string Name { get; set; }
        public string Description { get; set; }
        public string Image { get; set; }
        public double Rating { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public Boolean IsDeleted { get; set; } = false;
        public string UserId { get; set; } // userId in identity framework is string

        public User User { get; set; }
        public ICollection<DurianProfile> DurianSold { get; set; }
        public ICollection<Comment> Comments { get; set; }
    }
}

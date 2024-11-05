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

        public ICollection<DurianProfile> DurianSold { get; set; }
        public ICollection<Comments> Comments { get; set; }
    }
}

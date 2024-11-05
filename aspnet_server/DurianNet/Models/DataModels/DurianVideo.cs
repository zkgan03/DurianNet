using System.ComponentModel.DataAnnotations;

namespace DurianNet.Models.DataModels
{
    public class DurianVideo
    {
        [Key]
        public int VideoId { get; set; }
        public string Description { get; set; } = string.Empty;
        public string VideoUrl { get; set; } = string.Empty;

    }
}

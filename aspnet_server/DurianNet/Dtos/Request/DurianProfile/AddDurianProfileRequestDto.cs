using Microsoft.AspNetCore.Http;

namespace DurianNet.Dtos.Request.DurianProfile
{
    public class AddDurianProfileRequestDto
    {
        public string DurianName { get; set; }
        public string DurianDescription { get; set; }
        public string Characteristics { get; set; } // Updated to match mapper
        public string TasteProfile { get; set; }
        public string? DurianImage { get; set; }
        public string? DurianVideo { get; set; }
        public string? VideoDescription { get; set; }
    }
}


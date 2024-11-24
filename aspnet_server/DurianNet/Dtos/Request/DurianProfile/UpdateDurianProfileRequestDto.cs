namespace DurianNet.Dtos.Request.DurianProfile
{
    public class UpdateDurianProfileRequestDto
    {
        public string DurianName { get; set; }
        public string Characteristics { get; set; }
        public string TasteProfile { get; set; }
        public string DurianDescription { get; set; }
        public string? VideoDescription { get; set; }
        public string? DurianImage { get; set; } // Base64-encoded image
        public string? DurianVideo { get; set; } // Base64-encoded video

    }
}

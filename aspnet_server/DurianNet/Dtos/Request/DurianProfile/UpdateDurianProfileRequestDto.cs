namespace DurianNet.Dtos.Request.DurianProfile
{
    public class UpdateDurianProfileRequestDto
    {
        public string DurianName { get; set; }
        public string DurianDescription { get; set; }
        public string Characteristics { get; set; }
        public string TasteProfile { get; set; }
        public string DurianImage { get; set; }
        public string DurianVideoUrl { get; set; }
        public string DurianVideoDescription { get; set; } // Add this for updating the video description
    }
}

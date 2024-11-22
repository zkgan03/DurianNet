namespace DurianNet.Dtos.Request.DurianProfile
{
    public class UpdateDurianProfileRequestDto
    {
        //public string DurianName { get; set; }
        //public string DurianDescription { get; set; }
        //public string Characteristics { get; set; }
        //public string TasteProfile { get; set; }
        //public string DurianImage { get; set; }
        //public string DurianVideoUrl { get; set; }
        //public string DurianVideoDescription { get; set; } // Add this for updating the video description


        //public string DurianName { get; set; }
        //public string DurianDescription { get; set; }
        //public string Characteristics { get; set; }
        //public string TasteProfile { get; set; }
        //public IFormFile? DurianImage { get; set; } // Optional file upload for Durian Image
        //public IFormFile? DurianVideo { get; set; } // Optional file upload for Durian Video
        //public string? VideoDescription { get; set; } // Optional description update for the video

        public string DurianName { get; set; }
        public string Characteristics { get; set; }
        public string TasteProfile { get; set; }
        public string DurianDescription { get; set; }
        public string? VideoDescription { get; set; }
        public string? DurianImage { get; set; } // Base64-encoded image
        public string? DurianVideo { get; set; } // Base64-encoded video

    }
}

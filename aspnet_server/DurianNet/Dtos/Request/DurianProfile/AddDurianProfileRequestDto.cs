//namespace DurianNet.Dtos.Request.DurianProfile
//{
//    public class AddDurianProfileRequestDto
//    {
//        public string DurianName { get; set; }
//        public string DurianDescription { get; set; }
//        public string Characteristics { get; set; }
//        public string TasteProfile { get; set; }
//        public string DurianImage { get; set; }
//        public string DurianVideoUrl { get; set; }
//        public string DurianVideoDescription { get; set; } // New field for video description
//    }
//}

//}

using Microsoft.AspNetCore.Http;

namespace DurianNet.Dtos.Request.DurianProfile
{
    public class AddDurianProfileRequestDto
    {
        public string DurianName { get; set; }
        public string DurianDescription { get; set; }
        public string Characteristics { get; set; } // Updated to match mapper
        public string TasteProfile { get; set; }
        public IFormFile DurianImage { get; set; }
        public IFormFile DurianVideo { get; set; }
        public string VideoDescription { get; set; }
    }
}


namespace DurianNet.Dtos.Response.DurianProfile
{
    public class DurianListResponseDto
    {
        public int DurianId { get; set; }
        public string DurianName { get; set; } // Add DurianName for clarity in favorites list
        public string DurianImage { get; set; }
    }
}

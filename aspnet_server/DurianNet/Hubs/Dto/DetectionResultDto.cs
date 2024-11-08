using MessagePack;

namespace DurianNet.Hubs.Dto
{

    public class DetectionResultDto
    {
        public required string Label { get; set; }
        public double Confidence { get; set; }
        public int Top { get; set; }
        public int Left { get; set; }
        public int Width { get; set; }
        public int Height { get; set; }
    }
}

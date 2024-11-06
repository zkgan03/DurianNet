using MessagePack;

namespace DurianNet.Dtos
{

    [MessagePackObject]
    public class DetectionResultDto
    {
        [Key("Label")]
        public required string Label { get; set; }
        [Key("Confidence")]
        public double Confidence { get; set; }
        [Key("Top")]
        public int Top { get; set; }
        [Key("Left")]
        public int Left { get; set; }
        [Key("Width")]
        public int Width { get; set; }
        [Key("Height")]
        public int Height { get; set; }
    }
}

using DurianNet.Services.DetectionService.YOLO.v10.Metadata;
using SixLabors.ImageSharp;

namespace DurianNet.Services.DetectionService.YOLO.v10.Data.Detection
{
    public class BoundingBox 
    {
        public required YoloV10Class Class { get; init; }

        public required Rectangle Bounds { get; init; }

        public required float Confidence { get; init; }

        public override string ToString()
        {
            return $"{Class.Name} ({Confidence:N})";
        }
    }
}

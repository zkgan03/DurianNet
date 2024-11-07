using DurianNet.Services.DetectionService.YOLO.v10.Extensions;

namespace DurianNet.Services.DetectionService.YOLO.v10.Data.Detection
{
    public class DetectionResult : YoloV10Result
    {
        public required BoundingBox[] Boxes { get; init; }

        public override string ToString() => Boxes.Summary();
    }
}

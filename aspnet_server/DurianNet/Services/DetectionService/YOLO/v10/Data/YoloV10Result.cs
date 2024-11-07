using SixLabors.ImageSharp;

namespace DurianNet.Services.DetectionService.YOLO.v10.Data
{
    public abstract class YoloV10Result
    {
        public required Size Image { get; init; }
        public required SpeedResult Speed { get; init; }
    }

   
}

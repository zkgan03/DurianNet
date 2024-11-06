using DurianNet.Services.DetectionService.YOLO.v10.Metadata;

namespace DurianNet.Services.DetectionService.YOLO.v10.Data.Classification
{
    public class ClassProbability
    {
        public required YoloV10Class Name { get; init; }

        public required float Confidence { get; init; }

        public override string ToString()
        {
            return $"{Name.Name} ({Confidence:N})";
        }
    }
}

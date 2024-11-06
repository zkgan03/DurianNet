namespace DurianNet.Services.DetectionService.YOLO.v10.Data.Classification
{
    public class ClassificationResult : YoloV10Result
    {
        public required ClassProbability TopClass { get; init; }

        public required ClassProbability[] Probabilities { get; init; }

        public override string ToString()
        {
            return TopClass.ToString();
        }
    }
}

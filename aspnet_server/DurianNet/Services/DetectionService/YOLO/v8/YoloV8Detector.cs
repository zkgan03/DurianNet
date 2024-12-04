using Compunet.YoloV8;
using Compunet.YoloV8.Data;
using DurianNet.Hubs.Dto;

namespace DurianNet.Services.DetectionService.YOLO.v8
{
    public class YoloV8Detector : IDetector
    {
        private readonly YoloV8Predictor _predictor;

        public YoloV8Detector()
        {
            _predictor = YoloV8Predictor.Create("./Assets/Detection_Model/durian_2_v8s_640.onnx");
        }

        public void UpdateConfiguration(float confidence, float iouThreshold)
        {
            _predictor.Configuration.Confidence = confidence;
            _predictor.Configuration.IoU = iouThreshold;
        }

        public async Task<DetectionResultDto[]> DetectAsync(byte[] image)
        {
            var result = await _predictor.DetectAsync(image);

            var detectResults = DetectionResultToDto(result);

            return detectResults;
        }

        public async Task<DetectionResultDto[]> DetectAsync(string imagePath)
        {
            var result = await _predictor.DetectAsync(imagePath);

            var detectResults = DetectionResultToDto(result);

            return detectResults;
        }

        private DetectionResultDto[] DetectionResultToDto(DetectionResult result)
        {
            var detectResults = new DetectionResultDto[result.Boxes.Length];

            for (int i = 0; i < result.Boxes.Length; i++)
            {
                detectResults[i] = new DetectionResultDto
                {
                    Label = result.Boxes[i].Class.Name,
                    Confidence = Math.Round(result.Boxes[i].Confidence, 2),
                    Top = result.Boxes[i].Bounds.Top,
                    Left = result.Boxes[i].Bounds.Left,
                    Width = result.Boxes[i].Bounds.Width,
                    Height = result.Boxes[i].Bounds.Height,
                };
            }

            return detectResults;
        }

        public void Dispose()
        {
            _predictor.Dispose();
        }
    }
}

using DurianNet.Services.DetectionService.YOLO.v10.Base;
using DurianNet.Services.DetectionService.YOLO.v10.Data.Classification;
using DurianNet.Services.DetectionService.YOLO.v10.Data.Detection;
using DurianNet.Services.DetectionService.YOLO.v10.Metadata;
using DurianNet.Services.DetectionService.YOLO.v10.Parsers;
using DurianNet.Services.DetectionService.YOLO.v10.Selector;

namespace DurianNet.Services.DetectionService.YOLO.v10.Extensions
{
    public static partial class PredictorExtensions
    {

        public static DetectionResult Detect(
            this YoloV10Predictor predictor,
            ImageSelector selector,
            YoloV10Configuration? configuration = null)
        {
            configuration ??= predictor.Configuration;

            predictor.ValidateTask(YoloV10Task.Detect);

            return predictor.Run(selector, (outputs, image, timer) =>
            {
                // Postprocess

                var output = outputs[0].AsTensor<float>();

                var parser = new DetectionOutputParser(predictor.Metadata, configuration);

                var boxes = parser.Parse(output, image);

                var speed = timer.Stop();

                return new DetectionResult
                {
                    Boxes = boxes,
                    Image = image,
                    Speed = speed,
                };
            }, configuration);
        }

        public static ClassificationResult Classify(
            this YoloV10Predictor predictor,
            ImageSelector selector,
            YoloV10Configuration? configuration = null)
        {
            configuration ??= predictor.Configuration;

            predictor.ValidateTask(YoloV10Task.Classify);

            return predictor.Run(selector, (outputs, image, timer) =>
            {
                var output = outputs[0].AsEnumerable<float>().ToList();

                var probs = new ClassProbability[output.Count];

                for (int i = 0; i < output.Count; i++)
                {
                    var name = predictor.Metadata.Names[i];
                    var confidence = output[i];

                    probs[i] = new ClassProbability
                    {
                        Name = name,
                        Confidence = confidence,
                    };
                }

                var top = probs.MaxBy(x => x.Confidence) ?? throw new Exception();

                var speed = timer.Stop();

                return new ClassificationResult
                {
                    TopClass = top,
                    Probabilities = probs,
                    Image = image,
                    Speed = speed,
                };
            }, configuration);
        }

        #region Async Operations


        public static async Task<DetectionResult> DetectAsync(
            this YoloV10Predictor predictor,
            ImageSelector selector,
            YoloV10Configuration? configuration = null)
        {
            return await Task.Run(() => predictor.Detect(selector, configuration));
        }

        public static async Task<ClassificationResult> ClassifyAsync(this YoloV10Predictor predictor, ImageSelector selector, YoloV10Configuration? configuration = null)
        {
            return await Task.Run(() => predictor.Classify(selector, configuration));
        }

        #endregion

        private static void ValidateTask(this YoloV10Predictor predictor, YoloV10Task task)
        {
            if (predictor.Metadata.Task != task)
            {
                throw new InvalidOperationException("The loaded model does not support this task");
            }
        }

    }
}

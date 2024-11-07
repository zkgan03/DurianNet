using DurianNet.Services.DetectionService.YOLO.v10.Base;
using DurianNet.Services.DetectionService.YOLO.v10.Data;
using DurianNet.Services.DetectionService.YOLO.v10.Data.Classification;
using DurianNet.Services.DetectionService.YOLO.v10.Data.Detection;
using DurianNet.Services.DetectionService.YOLO.v10.Metadata;
using DurianNet.Services.DetectionService.YOLO.v10.Plotting;
using DurianNet.Services.DetectionService.YOLO.v10.Plotting.Classification;
using DurianNet.Services.DetectionService.YOLO.v10.Plotting.Detection;
using SixLabors.ImageSharp;
using Path = System.IO.Path;

namespace DurianNet.Services.DetectionService.YOLO.v10.Extensions
{

    public static class YoloV8PlottingExtensions
    {
        #region TaskAndSave Sync


        public static DetectionResult DetectAndSave(
            this YoloV10Predictor predictor,
            string path, string? output = null,
            YoloV10Configuration? configuration = null,
            DetectionPlottingOptions? options = null)
        {
            using var image = Image.Load(path);

            var result = predictor.Detect(image, configuration);

            using var plotted = result.PlotImage(image, options);

            output ??= CreateImageOutputPath(path, predictor.Metadata.Task);

            plotted.Save(output);

            return result;
        }

        public static ClassificationResult ClassifyAndSave(
            this YoloV10Predictor predictor, string path,
            string? output = null, YoloV10Configuration? configuration = null,
            ClassificationPlottingOptions? options = null)
        {
            using var image = Image.Load(path);

            var result = predictor.Classify(image, configuration);

            using var plotted = result.PlotImage(image, options);

            output ??= CreateImageOutputPath(path, predictor.Metadata.Task);

            plotted.Save(output);

            return result;
        }

        #endregion

        #region TaskAndSaveAsync


        public static async Task<DetectionResult> DetectAndSaveAsync(
            this YoloV10Predictor predictor,
            string path,
            string? output = null,
            YoloV10Configuration? configuration = null,
            DetectionPlottingOptions? options = null)
        {
            using var image = Image.Load(path);

            var result = await predictor.DetectAsync(image, configuration);

            using var plotted = await result.PlotImageAsync(image, options);

            output ??= CreateImageOutputPath(path, predictor.Metadata.Task);

            await plotted.SaveAsync(output);

            return result;
        }


        public static async Task<ClassificationResult> ClassifyAndSaveAsync(
            this YoloV10Predictor predictor,
            string path,
            string? output = null,
            YoloV10Configuration? configuration = null,
            ClassificationPlottingOptions? options = null)
        {
            using var image = Image.Load(path);

            var result = await predictor.ClassifyAsync(image, configuration);

            using var plotted = await result.PlotImageAsync(image, options);

            output ??= CreateImageOutputPath(path, predictor.Metadata.Task);

            await plotted.SaveAsync(output);

            return result;
        }

        #endregion

        #region PredictAndSave

        public static YoloV10Result PredictAndSave(
            this YoloV10Predictor predictor,
            string path, string? output = null,
            YoloV10Configuration? configuration = null,
            PlottingOptions? options = null)
        {
            return predictor.Metadata.Task switch
            {
                YoloV10Task.Detect => DetectAndSave(predictor, path, output, configuration, options as DetectionPlottingOptions),
                YoloV10Task.Classify => ClassifyAndSave(predictor, path, output, configuration, options as ClassificationPlottingOptions),
                _ => throw new NotSupportedException("Unsupported YOLOv10 task")
            };
        }

        public static async Task<YoloV10Result> PredictAndSaveAsync(
            this YoloV10Predictor predictor, 
            string path,
            string? output = null, 
            YoloV10Configuration? configuration = null,
            PlottingOptions? options = null)
        {
            return predictor.Metadata.Task switch
            {
                YoloV10Task.Detect => await DetectAndSaveAsync(predictor, path, output, configuration, options as DetectionPlottingOptions),
                YoloV10Task.Classify => await ClassifyAndSaveAsync(predictor, path, output, configuration, options as ClassificationPlottingOptions),
                _ => throw new NotSupportedException("Unsupported YOLOv10 task")
            };
        }

        #endregion

        private static string CreateImageOutputPath(string path, YoloV10Task task)
        {
            var baseDirectory = Path.GetDirectoryName(path) ?? Environment.CurrentDirectory;

            var plotDirectory = Path.Combine(baseDirectory, task.ToString().ToLower());

            if (Directory.Exists(plotDirectory) == false)
            {
                Directory.CreateDirectory(plotDirectory);
            }

            var extn = Path.GetExtension(path);
            var name = Path.GetFileNameWithoutExtension(path);

            var index = 0;

            while (true)
            {
                var filename = index == 0 ? $"{name}{extn}" : $"{name}_{index}{extn}";

                var fullpath = Path.Combine(plotDirectory, filename);

                if (File.Exists(fullpath))
                {
                    index++;
                }
                else
                {
                    return fullpath;
                }
            }
        }
    }
}

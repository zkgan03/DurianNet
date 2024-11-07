

using DurianNet.Services.DetectionService.YOLO.v10.Data.Classification;
using DurianNet.Services.DetectionService.YOLO.v10.Data.Detection;
using DurianNet.Services.DetectionService.YOLO.v10.Plotting.Classification;
using DurianNet.Services.DetectionService.YOLO.v10.Plotting.Detection;
using SixLabors.ImageSharp;

namespace DurianNet.Services.DetectionService.YOLO.v10.Plotting
{

    public static class PlottingAsyncOperationExtensions
    {

        public static async Task<Image> PlotImageAsync(this DetectionResult result, Image originImage, DetectionPlottingOptions? options = null)
        {
            return await Task.Run(() => result.PlotImage(originImage, options));
        }

        public static async Task<Image> PlotImageAsync(this ClassificationResult result, Image originImage, ClassificationPlottingOptions? options = null)
        {
            return await Task.Run(() => result.PlotImage(originImage, options));
        }
    }
}

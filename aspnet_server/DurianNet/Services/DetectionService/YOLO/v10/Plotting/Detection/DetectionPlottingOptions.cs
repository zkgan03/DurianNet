
namespace DurianNet.Services.DetectionService.YOLO.v10.Plotting.Detection
{

    public class DetectionPlottingOptions : PlottingOptions
    {
        public static DetectionPlottingOptions Default { get; } = new DetectionPlottingOptions();

        public float TextHorizontalPadding { get; set; }

        public float BoxBorderThickness { get; set; }

        public ColorPalette ColorPalette { get; set; }

        public DetectionPlottingOptions()
        {
            TextHorizontalPadding = 5F;
            BoxBorderThickness = 1F;
            ColorPalette = ColorPalette.Default;
        }
    }
}

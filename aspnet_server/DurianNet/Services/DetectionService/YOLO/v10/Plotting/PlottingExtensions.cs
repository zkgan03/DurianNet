using SixLabors.Fonts;
using SixLabors.ImageSharp.Drawing.Processing;
using SixLabors.ImageSharp.Drawing;
using SixLabors.ImageSharp.PixelFormats;
using SixLabors.ImageSharp.Processing;
using SixLabors.ImageSharp;
using DurianNet.Services.DetectionService.YOLO.v10.Selector;
using DurianNet.Services.DetectionService.YOLO.v10.Data.Detection;
using DurianNet.Services.DetectionService.YOLO.v10.Plotting.Detection;
using DurianNet.Services.DetectionService.YOLO.v10.Data.Classification;
using DurianNet.Services.DetectionService.YOLO.v10.Plotting.Classification;

namespace DurianNet.Services.DetectionService.YOLO.v10.Plotting
{

    public static class PlottingExtensions
    {

        public static Image PlotImage(
            this DetectionResult result,
            ImageSelector<Rgba32> originImage,
            DetectionPlottingOptions? options = null)
        {
            options ??= DetectionPlottingOptions.Default;

            var process = originImage.Load(true);

            process.Mutate(x => x.AutoOrient());

            EnsureSize(process.Size, result.Image);

            var size = result.Image;

            var ratio = Math.Max(size.Width, size.Height) / 640F;

            var textOptions = new TextOptions(options.FontFamily.CreateFont(options.FontSize * ratio));

            var textPadding = options.TextHorizontalPadding * ratio;

            var thickness = options.BoxBorderThickness * ratio;

            foreach (var box in result.Boxes)
            {
                var label = $"{box.Class.Name} {box.Confidence:N}";
                var color = options.ColorPalette.GetColor(box.Class.Id);

                var points = GetPoints(box);
                var textLocation = points[0]; // The first point is top left

                process.Mutate(context =>
                {
                    DrawBoundingBox(context, points, color, thickness, .1f);

                    DrawTextLabel(context, label, textLocation, color, thickness, textPadding, textOptions);
                });
            }

            return process;
        }

        public static Image PlotImage(
            this ClassificationResult result,
            ImageSelector<Rgba32> originImage,
            ClassificationPlottingOptions? options = null)
        {
            options ??= ClassificationPlottingOptions.Default;

            var process = originImage.Load(true);

            EnsureSize(process.Size, result.Image);

            var size = result.Image;

            var ratio = Math.Max(size.Width, size.Height) / 640F;

            var textOptions = new TextOptions(options.FontFamily.CreateFont(options.FontSize * ratio));

            var label = result.ToString();

            var classId = result.TopClass.Name.Id;

            var fill = options.FillColorPalette.GetColor(classId);
            var border = options.BorderColorPalette.GetColor(classId);

            var pen = new SolidPen(border, options.BorderThickness * ratio);
            var brush = new SolidBrush(fill);
            var location = new PointF(options.XOffset * ratio, options.YOffset * ratio);

            process.Mutate(x => x.DrawText(label, textOptions.Font, brush, pen, location));

            return process;
        }

        #region Private Methods

        private static void DrawBoundingBox(IImageProcessingContext context, PointF[] points, Color color, float thickness, float opacity)
        {
            var polygon = new Polygon(points);

            context.Draw(color, thickness, polygon);

            if (opacity > 0f)
            {
                context.Fill(color.WithAlpha(opacity), polygon);
            }
        }

        private static void DrawTextLabel(IImageProcessingContext context, string text, PointF location, Color color, float thickness, float padding, TextOptions options)
        {
            var rendered = TextMeasurer.MeasureSize(text, options);
            var renderedSize = new Size((int)(rendered.Width + padding), (int)rendered.Height);

            location.Offset(0, -renderedSize.Height);

            var textLocation = new PointF(location.X + padding / 2, location.Y);

            var textBoxPolygon = new RectangularPolygon(location, renderedSize);

            context.Fill(color, textBoxPolygon);
            context.Draw(color, thickness, textBoxPolygon);

            context.DrawText(text, options.Font, Color.White, textLocation);
        }

        private static PointF[] GetPoints(BoundingBox box)
        {
            var rect = box.Bounds;

            return
            [
                new PointF(rect.Left, rect.Top),
            new PointF(rect.Right, rect.Top),
            new PointF(rect.Right, rect.Bottom),
            new PointF(rect.Left, rect.Bottom),
        ];
        }

        private static void EnsureSize(Size origin, Size result)
        {
            if (origin != result)
            {
                throw new InvalidOperationException("Original image size must to be equals to prediction result image size");
            }
        }

        #endregion
    }
}

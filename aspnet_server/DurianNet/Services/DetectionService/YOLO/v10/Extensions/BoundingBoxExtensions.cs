using DurianNet.Services.DetectionService.YOLO.v10.Data.Detection;

namespace DurianNet.Services.DetectionService.YOLO.v10.Extensions
{
    internal static class BoundingBoxesExtensions
    {
        // Summary method for BoundingBoxes
        public static string Summary(this IEnumerable<BoundingBox> boxes)
        {
            var sort = boxes.Select(x => x.Class)
                            .GroupBy(x => x.Id)
                            .OrderBy(x => x.Key)
                            .Select(x => $"{x.Count()} {x.First().Name}");

            return string.Join(", ", sort);
        }
    }
}

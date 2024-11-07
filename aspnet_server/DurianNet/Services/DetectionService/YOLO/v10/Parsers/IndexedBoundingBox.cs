using DurianNet.Services.DetectionService.YOLO.v10.Metadata;
using SixLabors.ImageSharp;

namespace DurianNet.Services.DetectionService.YOLO.v10.Parsers
{
    internal readonly struct IndexedBoundingBox : IComparable<IndexedBoundingBox>
    {
        public bool IsEmpty => Bounds.IsEmpty;

        public required int Index { get; init; }

        public required YoloV10Class Class { get; init; }

        public required Rectangle Bounds { get; init; }

        public required float Confidence { get; init; }

        public int CompareTo(IndexedBoundingBox other) => Confidence.CompareTo(other.Confidence);
    }
}

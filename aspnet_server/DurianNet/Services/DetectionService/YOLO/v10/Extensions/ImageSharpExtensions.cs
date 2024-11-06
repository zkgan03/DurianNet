﻿using SixLabors.ImageSharp.PixelFormats;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Advanced;

namespace DurianNet.Services.DetectionService.YOLO.v10.Extensions
{
    internal static class ImageSharpExtensions
    {
        public static void EnumeratePixels<TPixel>(
            this Image<TPixel> image,
            Action<Point, TPixel> iterator) where TPixel : unmanaged, IPixel<TPixel>
        {
            var width = image.Width;
            var height = image.Height;

            if (image.DangerousTryGetSinglePixelMemory(out var memory))
            {
                Parallel.For(0, width * height, index =>
                {
                    int x = index % width;
                    int y = index / width;

                    var point = new Point(x, y);
                    var pixel = memory.Span[index];

                    iterator(point, pixel);
                });
            }
            else
            {
                Parallel.For(0, image.Height, y =>
                {
                    var row = image.DangerousGetPixelRowMemory(y).Span;

                    for (int x = 0; x < image.Width; x++)
                    {
                        var point = new Point(x, y);
                        var pixel = row[x];

                        iterator(point, pixel);
                    }
                });
            }
        }
    }
}

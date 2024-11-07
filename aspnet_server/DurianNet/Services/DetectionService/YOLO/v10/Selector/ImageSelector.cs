using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;

namespace DurianNet.Services.DetectionService.YOLO.v10.Selector
{

    public class ImageSelector : ImageSelector<Rgb24>
    {
        public ImageSelector(Image image)
            : base(image) { }

        public ImageSelector(string path)
            : base(path) { }

        public ImageSelector(byte[] data)
            : base(data) { }

        public ImageSelector(Stream stream)
            : base(stream) { }

        public static implicit operator ImageSelector(Image image) => new(image);

        public static implicit operator ImageSelector(string path) => new(path);

        public static implicit operator ImageSelector(byte[] data) => new(data);

        public static implicit operator ImageSelector(Stream stream) => new(stream);
    }
}

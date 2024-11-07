using SixLabors.ImageSharp;

namespace DurianNet.Services.DetectionService.YOLO.v10.Metadata
{
    public class YoloV10Metadata(
        string author,
        string description,
        string version,
        YoloV10Task task,
        int batch,
        Size imageSize,
        IReadOnlyList<YoloV10Class> names)
    {
        public static YoloV10Metadata Parse(IDictionary<string, string> metadata)
        {
            var author = metadata["author"];
            var description = metadata["description"];
            var version = metadata["version"];

            var task = metadata["task"] switch
            {
                "detect" => YoloV10Task.Detect,
                "classify" => YoloV10Task.Classify,
                _ => throw new InvalidOperationException("Unknow YoloV10 'task' value")
            };

            var batch = int.Parse(metadata["batch"]);

            var imageSize = ParseSize(metadata["imgsz"]);
            var classes = ParseNames(metadata["names"]);

            return new YoloV10Metadata(
                author,
                description,
                version,
                task,
                batch,
                imageSize,
                classes);
        }


        #region Metadata

        public string Author { get; } = author;

        public string Description { get; } = description;

        public string Version { get; } = version;

        public YoloV10Task Task { get; } = task;

        public int Batch { get; } = batch;

        public Size ImageSize { get; } = imageSize;

        public IReadOnlyList<YoloV10Class> Names { get; } = names;

        #endregion


        #region Static Parsers

        private static Size ParseSize(string text)
        {
            text = text[1..^1]; // '[640, 641]' => '640, 640'

            var split = text.Split(", ");

            var y = int.Parse(split[0]);
            var x = int.Parse(split[1]);

            return new Size(x, y);
        }

        private static YoloV10Class[] ParseNames(string text)
        {
            text = text[1..^1];

            var split = text.Split(", ");
            var count = split.Length;

            var names = new YoloV10Class[count];

            for (int i = 0; i < count; i++)
            {
                var value = split[i];

                var valueSplit = value.Split(": ");

                var id = int.Parse(valueSplit[0]);
                var name = valueSplit[1][1..^1].Replace('_', ' ');

                names[i] = new YoloV10Class(id, name);
            }

            return names;
        }

        #endregion
    }
}

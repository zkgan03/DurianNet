using Microsoft.ML.OnnxRuntime.Tensors;
using DurianNet.Services.DetectionService.YOLO.v10.Base;
using DurianNet.Services.DetectionService.YOLO.v10.Data.Detection;
using DurianNet.Services.DetectionService.YOLO.v10.Metadata;
using SixLabors.ImageSharp;

namespace DurianNet.Services.DetectionService.YOLO.v10.Parsers
{

    internal readonly ref struct DetectionOutputParser(
        YoloV10Metadata metadata,
        YoloV10Configuration configuration)
    {
        public BoundingBox[] Parse(Tensor<float> output, Size originSize)
        {
            // Parse the output tensor to get the bounding boxes
            var boxes = new IndexedBoundingBoxParser(metadata, configuration).Parse(output, originSize);

            // Convert the indexed bounding boxes to bounding boxes
            var result = new BoundingBox[boxes.Length];

            for (int i = 0; i < boxes.Length; i++)
            {
                var box = boxes[i];

                result[i] = new BoundingBox
                {
                    Class = box.Class,
                    Bounds = box.Bounds,
                    Confidence = box.Confidence,
                };
            }

            return result;
        }
    }
}

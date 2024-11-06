using Microsoft.ML.OnnxRuntime.Tensors;
using DurianNet.Services.DetectionService.YOLO.v10.Base;
using DurianNet.Services.DetectionService.YOLO.v10.Metadata;
using DurianNet.Services.DetectionService.YOLO.v10.Utilities;
using SixLabors.ImageSharp;
using System.Collections.Concurrent;

namespace DurianNet.Services.DetectionService.YOLO.v10.Parsers
{
    internal readonly ref struct IndexedBoundingBoxParser(
        YoloV10Metadata metadata,
        YoloV10Configuration configuration)
    {
        public IndexedBoundingBox[] Parse(
            Tensor<float> output,
            Size originSize)
        {
            // get the padding added to the image to keep the aspect ratio
            // if the image was resized to fit the model input size

            int xPadding;
            int yPadding;

            if (configuration.KeepOriginalAspectRatio)
            {
                //same as gain in YoloV8 (gain = old / min)
                var reductionRatio = Math.Min(metadata.ImageSize.Width / (float)originSize.Width,
                                              metadata.ImageSize.Height / (float)originSize.Height);

                xPadding = (int)((metadata.ImageSize.Width - originSize.Width * reductionRatio) / 2);
                yPadding = (int)((metadata.ImageSize.Height - originSize.Height * reductionRatio) / 2);
            }
            else
            {
                xPadding = 0;
                yPadding = 0;
            }

            return Parse(output, originSize, xPadding, yPadding);
        }

        public IndexedBoundingBox[] Parse(
            Tensor<float> output,
            Size originSize,
            int xPadding,
            int yPadding)
        {

            // get the ratio of the image size to the model input size
            // this is used to scale the bounding box back to the original image size
            // if the image was resized to fit the model input size

            var xRatio = (float)originSize.Width / metadata.ImageSize.Width;
            var yRatio = (float)originSize.Height / metadata.ImageSize.Height;

            if (configuration.KeepOriginalAspectRatio)
            {
                var maxRatio = Math.Max(xRatio, yRatio);

                xRatio = maxRatio;
                yRatio = maxRatio;
            }

            return Parse(output, originSize, xPadding, yPadding, xRatio, yRatio);
        }

        public IndexedBoundingBox[] Parse(
            Tensor<float> output,
            Size originSize,
            int xPadding,
            int yPadding,
            float xRatio,
            float yRatio)
        {
            // Extract the bounding boxes from the output tensor
            // and convert them to IndexedBoundingBoxes
            // The IndexedBoundingBoxes are then filtered by confidence
            // and returned as an array

            var _metadata = metadata;
            var _configuration = configuration;

            var boxes = new ConcurrentBag<IndexedBoundingBox>();

            // YOLOv10 output Tensor shape : float32[1,300,6]
            // 1 : batch size
            // 300 : number of bounding boxes 
            // 6 : [x1, y1, x2, y2, confidence, class]

            // iterate over the bounding boxes (index 1)
            Parallel.For(0, output.Dimensions[1], i =>
            {
                var x1 = output[0, i, 0];
                var y1 = output[0, i, 1];
                var x2 = output[0, i, 2];
                var y2 = output[0, i, 3];
                var confidence = output[0, i, 4];
                var classIndex = (int)output[0, i, 5];

                // if the confidence is less than the threshold, skip the bounding box
                if (confidence <= _configuration.Confidence)
                {
                    return;
                }

                // calculate the bounding box coordinates
                // with respect to the original image size (multiply by the ratio)
                var xMin = (int)((x1 - xPadding) * xRatio);
                var yMin = (int)((y1 - yPadding) * yRatio);
                var xMax = (int)((x2 - xPadding) * xRatio);
                var yMax = (int)((y2 - yPadding) * yRatio);

                // clamp the bounding box to the image bounds
                xMin = Math.Clamp(xMin, 0, originSize.Width);
                yMin = Math.Clamp(yMin, 0, originSize.Height);
                xMax = Math.Clamp(xMax, 0, originSize.Width);
                yMax = Math.Clamp(yMax, 0, originSize.Height);

                var name = _metadata.Names[classIndex]; // get the class name from the metadata
                var bounds = Rectangle.FromLTRB(xMin, yMin, xMax, yMax);

                // if the bounding box out of the image bounds, skip it
                if (bounds.Width > 0 && bounds.Height > 0)
                {
                    boxes.Add(new IndexedBoundingBox
                    {
                        Index = i,
                        Class = name,
                        Bounds = bounds,
                        Confidence = confidence
                    });
                }
            });

            // **NMS is not needded in YOLOv10

            //return NonMaxSuppressionHelper.Suppress(boxes.ToArray(), _configuration.IoU);
            return boxes.ToArray();
        }


        //public IndexedBoundingBox[] Parse(
        //    Tensor<float> output,
        //    Size originSize,
        //    int xPadding,
        //    int yPadding,
        //    float xRatio,
        //    float yRatio)
        //{
        //    var _metadata = metadata;
        //    var _configuration = configuration;

        //    var boxes = new IndexedBoundingBox[output.Dimensions[2]];

        //    Parallel.For(0, output.Dimensions[2], i =>
        //    {
        //        for (int j = 0; j < _metadata.Names.Count; j++)
        //        {
        //            var confidence = output[0, j + 4, i];

        //            if (confidence <= _configuration.Confidence)
        //            {
        //                continue;
        //            }

        //            var x = output[0, 0, i];
        //            var y = output[0, 1, i];
        //            var w = output[0, 2, i];
        //            var h = output[0, 3, i];

        //            var xMin = (int)((x - w / 2 - xPadding) * xRatio);
        //            var yMin = (int)((y - h / 2 - yPadding) * yRatio);
        //            var xMax = (int)((x + w / 2 - xPadding) * xRatio);
        //            var yMax = (int)((y + h / 2 - yPadding) * yRatio);

        //            xMin = Math.Clamp(xMin, 0, originSize.Width);
        //            yMin = Math.Clamp(yMin, 0, originSize.Height);
        //            xMax = Math.Clamp(xMax, 0, originSize.Width);
        //            yMax = Math.Clamp(yMax, 0, originSize.Height);

        //            var name = _metadata.Names[j];
        //            var bounds = Rectangle.FromLTRB(xMin, yMin, xMax, yMax);

        //            if (bounds.Width == 0 || bounds.Height == 0)
        //            {
        //                continue;
        //            }

        //            boxes[i] = new IndexedBoundingBox
        //            {
        //                Index = i,
        //                Class = name,
        //                Bounds = bounds,
        //                Confidence = confidence
        //            };
        //        }
        //    });

        //    var count = 0;

        //    for (int i = 0; i < boxes.Length; i++)
        //    {
        //        if (boxes[i].IsEmpty == false)
        //        {
        //            count++;
        //        }
        //    }

        //    var topBoxes = new IndexedBoundingBox[count];

        //    var topIndex = 0;

        //    for (int i = 0; i < boxes.Length; i++)
        //    {
        //        var box = boxes[i];

        //        if (box.IsEmpty)
        //        {
        //            continue;
        //        }

        //        topBoxes[topIndex++] = box;
        //    }

        //    return NonMaxSuppressionHelper.Suppress(topBoxes, configuration.IoU);
        //}
    }
}

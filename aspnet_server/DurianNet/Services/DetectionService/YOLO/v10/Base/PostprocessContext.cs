using Microsoft.ML.OnnxRuntime;
using DurianNet.Services.DetectionService.YOLO.v10.Data;
using DurianNet.Services.DetectionService.YOLO.v10.Timer;
using SixLabors.ImageSharp;

namespace DurianNet.Services.DetectionService.YOLO.v10.Base
{
    public delegate TResult PostprocessContext<TResult>(
        IReadOnlyList<NamedOnnxValue> outputs,
        Size imageSize,
        SpeedTimer timer) where TResult : YoloV10Result;
}

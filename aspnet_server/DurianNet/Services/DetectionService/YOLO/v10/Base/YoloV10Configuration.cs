namespace DurianNet.Services.DetectionService.YOLO.v10.Base
{
    /// Configuration for YOLOv10
    public class YoloV10Configuration
    {
        public static readonly YoloV10Configuration Default = new();

        /// <summary>
        /// Specify the minimum confidence value for including a result. Default is 0.3f.
        /// </summary>
        public float Confidence { get; set; } = .3f;

        /// <summary>
        /// Specify whether to keep the image aspect ratio when resizing. Default is true.
        /// </summary>
        public bool KeepOriginalAspectRatio { get; set; } = true;

        /// <summary>
        /// Specify whether to suppress parallel inference (pre-processing and post-processing will run in parallelly). Default is false.
        /// </summary>
        public bool SuppressParallelInference { get; set; } = false;

        /// <summary>
        /// Specify the minimum IoU value for Non-Maximum Suppression (NMS). Default is 0.45f.
        /// </summary>
        public float IoU { get; set; } = .45f;
    }
}

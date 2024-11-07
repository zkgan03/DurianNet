
using Microsoft.ML.OnnxRuntime;
using DurianNet.Services.DetectionService.YOLO.v10.Base;
using DurianNet.Services.DetectionService.YOLO.v10.Metadata;
using DurianNet.Services.DetectionService.YOLO.v10.Selector;

namespace DurianNet.Services.DetectionService.YOLO.v10.Builder
{
    public interface IYoloV10Builder
    {
        public IYoloV10Builder UseOnnxModel(BinarySelector model);


        public IYoloV10Builder UseCuda(int deviceId = 0);
        public IYoloV10Builder UseCuda(OrtCUDAProviderOptions options);

        public IYoloV10Builder UseRocm(int deviceId = 0);
        public IYoloV10Builder UseRocm(OrtROCMProviderOptions options);

        public IYoloV10Builder UseTensorrt(int deviceId = 0);
        public IYoloV10Builder UseTensorrt(OrtTensorRTProviderOptions options);

        public IYoloV10Builder UseTvm(string settings = "");


        public IYoloV10Builder WithMetadata(YoloV10Metadata metadata);

        public IYoloV10Builder WithConfiguration(Action<YoloV10Configuration> configure);

        public IYoloV10Builder WithSessionOptions(Microsoft.ML.OnnxRuntime.SessionOptions sessionOptions);

        public YoloV10Predictor Build();
    }
}

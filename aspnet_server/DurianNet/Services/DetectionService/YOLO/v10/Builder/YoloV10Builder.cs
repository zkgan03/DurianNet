
using Microsoft.ML.OnnxRuntime;
using DurianNet.Services.DetectionService.YOLO.v10.Base;
using DurianNet.Services.DetectionService.YOLO.v10.Metadata;
using DurianNet.Services.DetectionService.YOLO.v10.Selector;
using SessionOptions = Microsoft.ML.OnnxRuntime.SessionOptions;

namespace DurianNet.Services.DetectionService.YOLO.v10.Builder
{
    public class YoloV10Builder : IYoloV10Builder
    {
        private BinarySelector? _model;

        private SessionOptions? _sessionOptions;

        private YoloV10Metadata? _metadata;
        private YoloV10Configuration? _configuration;

        public static IYoloV10Builder CreateDefaultBuilder()
        {
            var builder = new YoloV10Builder();

            builder.UseCuda(0);


            return builder;
        }

        public YoloV10Predictor Build()
        {
            if (_model is null)
            {
                throw new ApplicationException("No model selected");
            }

            return new YoloV10Predictor(_model, _metadata, _configuration, _sessionOptions);
        }

        public IYoloV10Builder UseOnnxModel(BinarySelector model)
        {
            _model = model;

            return this;
        }


        public IYoloV10Builder UseCuda(int deviceId) 
            => WithSessionOptions(SessionOptions.MakeSessionOptionWithCudaProvider(deviceId));

        public IYoloV10Builder UseCuda(OrtCUDAProviderOptions options) 
            => WithSessionOptions(SessionOptions.MakeSessionOptionWithCudaProvider(options));

        public IYoloV10Builder UseRocm(int deviceId) 
            => WithSessionOptions(SessionOptions.MakeSessionOptionWithRocmProvider(deviceId));

        public IYoloV10Builder UseRocm(OrtROCMProviderOptions options) 
            => WithSessionOptions(SessionOptions.MakeSessionOptionWithRocmProvider(options));

        public IYoloV10Builder UseTensorrt(int deviceId) 
            => WithSessionOptions(SessionOptions.MakeSessionOptionWithTensorrtProvider(deviceId));

        public IYoloV10Builder UseTensorrt(OrtTensorRTProviderOptions options) 
            => WithSessionOptions(SessionOptions.MakeSessionOptionWithTensorrtProvider(options));

        public IYoloV10Builder UseTvm(string settings = "") 
            => WithSessionOptions(SessionOptions.MakeSessionOptionWithTvmProvider(settings));



        public IYoloV10Builder WithMetadata(YoloV10Metadata metadata)
        {
            _metadata = metadata;

            return this;
        }

        public IYoloV10Builder WithConfiguration(Action<YoloV10Configuration> configure)
        {
            var configuration = new YoloV10Configuration();

            configure(configuration);

            _configuration = configuration;

            return this;
        }

        public IYoloV10Builder WithSessionOptions(SessionOptions sessionOptions)
        {
            _sessionOptions = sessionOptions;

            return this;
        }
    }
}

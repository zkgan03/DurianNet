using Microsoft.ML.OnnxRuntime.Tensors;
using Microsoft.ML.OnnxRuntime;
using DurianNet.Services.DetectionService.YOLO.v10.Utilities;
using SixLabors.ImageSharp.PixelFormats;
using SixLabors.ImageSharp;
using DurianNet.Services.DetectionService.YOLO.v10.Metadata;
using SessionOptions = Microsoft.ML.OnnxRuntime.SessionOptions;
using DurianNet.Services.DetectionService.YOLO.v10.Selector;
using DurianNet.Services.DetectionService.YOLO.v10.Builder;
using DurianNet.Services.DetectionService.YOLO.v10.Timer;
using DurianNet.Services.DetectionService.YOLO.v10.Data;

namespace DurianNet.Services.DetectionService.YOLO.v10.Base
{
    public class YoloV10Predictor : IDisposable
    {
        private readonly InferenceSession _inference;

        private readonly object _locker = new();

        private bool _disposed;

        public YoloV10Metadata Metadata { get; }

        public YoloV10Configuration Configuration { get; }

        public static YoloV10Predictor Create(BinarySelector model)
            => YoloV10Builder.CreateDefaultBuilder().UseOnnxModel(model).Build();

        internal YoloV10Predictor(
            BinarySelector model,
            YoloV10Metadata? metadata,
            YoloV10Configuration? configuration,
            SessionOptions? options)
        {
            _inference = new InferenceSession(model.Load(), options ?? new SessionOptions());

            Metadata = metadata ?? YoloV10Metadata.Parse(_inference.ModelMetadata.CustomMetadataMap);
            Configuration = configuration ?? YoloV10Configuration.Default;
        }

        // This method is used to run the model on the input image and return the result
        public TResult Run<TResult>(
            ImageSelector selector,
            PostprocessContext<TResult> postprocess,
            YoloV10Configuration? configuration = null)
            where TResult : YoloV10Result
        {
            configuration ??= Configuration; // if configuration is null, use the default configuration
            using var image = selector.Load(true); // load the image
            var originSize = image.Size; // get the size of the image

            var timer = new SpeedTimer(); // create a new speed timer object

            // Preprocess
            timer.StartPreprocess(); // start the preprocess timer
            var input = Preprocess(image, configuration); //preprocess the image, return the input tensor
            var inputs = CreateInputAndMapNames([input]); // create the input tensor and map the input tensor to the input names

            // Inference
            timer.StartInference(); // stop the preprocess timer, start the inference timer
            using var outputs = Infer(inputs, configuration); // run the inference session on the input tensor and return the output tensor
            var list = new List<NamedOnnxValue>(outputs); // create a new list of named onnx values from the output tensor

            // Postprocess and return the result
            timer.StartPostprocess(); // stop the inference timer, strat the postprocess timer

            return postprocess(list, originSize, timer);
        }

        private IDisposableReadOnlyCollection<DisposableNamedOnnxValue> Infer(
            IReadOnlyCollection<NamedOnnxValue> inputs,
            YoloV10Configuration configuration)
        {
            // This method is used to run the inference session on the input tensor
            // by ONNX Runtime and return the output tensor

            // If the configuration is set to suppress parallel inference,
            // lock the inference process when running
            // because the inference session is not thread safe
            if (configuration.SuppressParallelInference)
            {
                lock (_locker)
                {
                    return _inference.Run(inputs);
                }
            }

            return _inference.Run(inputs);
        }

        private Tensor<float> Preprocess(Image<Rgb24> image, YoloV10Configuration configuration)
        {
            var modelSize = Metadata.ImageSize;

            // Create a new tensor with the model input size
            var dimensions = new int[] { 1, 3, modelSize.Height, modelSize.Width };
            var input = new DenseTensor<float>(dimensions);

            // Preprocess the image, resize it to the model input size and convert to 0 - 1 pixel values 
            // NOTE : image is passed by reference, so the image will be modified from the caller
            PreprocessHelper.ProcessToTensor(image, modelSize, configuration.KeepOriginalAspectRatio, input, 0);

            return input;
        }

        // This method is used to create the input tensor and map the input tensor to the input names
        private NamedOnnxValue[] CreateInputAndMapNames(ReadOnlySpan<Tensor<float>> inputs)
        {
            var length = inputs.Length;

            var values = new NamedOnnxValue[length];

            for (int i = 0; i < length; i++)
            {
                var name = _inference.InputNames[i];

                var value = NamedOnnxValue.CreateFromTensor(name, inputs[i]);

                values[i] = value;
            }

            return values;
        }

        public void Dispose()
        {
            if (_disposed)
            {
                return;
            }

            _inference.Dispose();
            _disposed = true;

            GC.SuppressFinalize(this);
        }
    }
}

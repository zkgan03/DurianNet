using DurianNet.Dtos;
using DurianNet.Hubs.Dto;
using DurianNet.Services.DetectionService;
using Microsoft.AspNetCore.SignalR;
using System.Text.Json;

namespace DurianNet.Hubs
{
    public class ObjectDetectionHub : Hub
    {

        private readonly IDetector _predictor;

        public ObjectDetectionHub(IDetector predictor)
        {
            _predictor = predictor;
        }

        public async Task Init(ConfigurationRequest request)
        {

            Console.WriteLine("Init request received");
            Console.WriteLine(JsonSerializer.Serialize(request));

            _predictor.UpdateConfiguration(
                confidence: request.ConfidenceThreshold,
                iouThreshold: request.IoUThreshold
                );

            // initialize the model and heat up the model
            await _predictor.DetectAsync("./Assets/Image/bus.jpg");

            await Clients.Caller.SendAsync("Initialized", "Initalization Completed");
        }

        public async Task<DetectionResultDto[]> DetectImage(string requestId, byte[] image)
        {
            Console.WriteLine("Got image from android");
            try
            {
                var result = await _predictor.DetectAsync(image);
                Console.WriteLine(JsonSerializer.Serialize(result));
                return result;

            }
            catch (Exception ex)
            {
                await Clients.Caller.SendAsync("DetectionError", ex.Message);
                return [new DetectionResultDto{
                    Label = "Error",
                }];
            }
        }

        public async Task DetectLiveStream(string requestId, byte[] image)
        {
            //Console.WriteLine("Got image from android");
            try
            {
                var result = await _predictor.DetectAsync(image);

                //var plotted = result.PlotImage(image);
                //plotted.Save("./Image/detect/plotted.jpg");

                await Clients.Caller.SendAsync(
                    "DetectionResult",
                    requestId,
                    result
                    );

                Console.WriteLine(JsonSerializer.Serialize(result));
            }
            catch (Exception ex)
            {
                await Clients.Caller.SendAsync("DetectionError", ex.Message);

            }
        }

        public void UpdateConfiguration(ConfigurationRequest request)
        {
            Console.WriteLine("Update Configuration request received");
            Console.WriteLine(JsonSerializer.Serialize(request));

            _predictor.UpdateConfiguration(
                confidence: request.ConfidenceThreshold,
                iouThreshold: request.IoUThreshold
                );
        }

        public override async Task OnConnectedAsync()
        {
            Console.WriteLine("Client connected");
            await base.OnConnectedAsync();
        }

        public override async Task OnDisconnectedAsync(Exception exception)
        {
            Console.WriteLine("Client disconnected");
            await base.OnDisconnectedAsync(exception);
        }
    }
}

using DurianNet.Hubs.Dto;

namespace DurianNet.Services.DetectionService
{
    public interface IDetector
    {
        void UpdateConfiguration(float confidence, float iouThreshold, int maxNumberDetection);
        Task<DetectionResultDto[]> DetectAsync(byte[] image);
        Task<DetectionResultDto[]> DetectAsync(string imagePath);
        void Dispose();

    }
}

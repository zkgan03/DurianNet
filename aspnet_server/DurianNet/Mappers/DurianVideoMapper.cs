using DurianNet.Dtos.Request.DurianProfile;
using DurianNet.Dtos.Response.DurianVideo;
using DurianNet.Models.DataModels;

namespace DurianNet.Mappers
{
    public static class DurianVideoMapper
    {
        public static DurianVideoResponseDto ToDurianVideoDto(this DurianVideo video)
        {
            return new DurianVideoResponseDto
            {
                VideoId = video.VideoId,
                Description = video.Description,
                VideoUrl = video.VideoUrl
            };
        }

        public static DurianVideo ToDurianVideoFromAddRequest(this AddDurianProfileRequestDto dto, string videoPath)
        {
            return new DurianVideo
            {
                //VideoUrl = dto.DurianVideoUrl,
                //Description = dto.DurianVideoDescription

                VideoUrl = videoPath, // Use the uploaded video's URL
                Description = dto.VideoDescription
            };
        }

        public static void UpdateDurianVideoFromDto(this DurianVideo video, UpdateDurianProfileRequestDto dto, string videoPath)
        {
            video.VideoUrl = videoPath;
            video.Description = dto.VideoDescription;
        }
    }
}

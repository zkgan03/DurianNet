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

        public static DurianVideo ToDurianVideoFromAddRequest(this AddDurianProfileRequestDto dto)
        {
            return new DurianVideo
            {
                VideoUrl = dto.DurianVideoUrl,
                Description = dto.DurianVideoDescription
            };
        }

        public static void UpdateDurianVideoFromDto(this DurianVideo video, UpdateDurianProfileRequestDto dto)
        {
            video.VideoUrl = dto.DurianVideoUrl;
            video.Description = dto.DurianVideoDescription;
        }
    }
}

using DurianNet.Dtos.Request.DurianProfile;
using DurianNet.Dtos.Response.DurianProfile;
using DurianNet.Models.DataModels;

namespace DurianNet.Mappers
{
    public static class DurianProfileMapper
    {
        public static DurianProfileResponseDto ToDurianProfileDto(this DurianProfile profile)
        {
            return new DurianProfileResponseDto
            {
                DurianId = profile.DurianId,
                DurianName = profile.DurianName,
                DurianDescription = profile.DurianDescription,
                Characteristics = profile.Characteristics,
                TasteProfile = profile.TasteProfile,
                DurianImage = profile.DurianImage,
                DurianVideoUrl = profile.DurianVideo?.VideoUrl
            };
        }

        public static DurianListResponseDto ToDurianListDto(this DurianProfile profile)
        {
            return new DurianListResponseDto
            {
                DurianId = profile.DurianId,
                DurianName = profile.DurianName,
                DurianImage = profile.DurianImage
            };
        }

        public static DurianProfile ToDurianProfileFromAddRequest(this AddDurianProfileRequestDto dto, int videoId)
        {
            return new DurianProfile
            {
                DurianName = dto.DurianName,
                DurianDescription = dto.DurianDescription,
                Characteristics = dto.Characteristics,
                TasteProfile = dto.TasteProfile,
                DurianImage = dto.DurianImage,
                DurianVideoId = videoId
            };
        }

        public static void UpdateDurianProfileFromDto(this DurianProfile profile, UpdateDurianProfileRequestDto dto, int videoId)
        {
            profile.DurianName = dto.DurianName;
            profile.DurianDescription = dto.DurianDescription;
            profile.Characteristics = dto.Characteristics;
            profile.TasteProfile = dto.TasteProfile;
            profile.DurianImage = dto.DurianImage;
            profile.DurianVideoId = videoId;
        }

        public static DurianProfileForUserResponseDto ToDurianProfileForUserDto(this DurianProfile profile)
        {
            return new DurianProfileForUserResponseDto
            {
                DurianId = profile.DurianId,
                DurianName = profile.DurianName,
                DurianImage = profile.DurianImage
            };
        }

        public static DurianProfileForAdminResponseDto ToDurianProfileForAdminDto(this DurianProfile profile)
        {
            return new DurianProfileForAdminResponseDto
            {
                DurianId = profile.DurianId,
                DurianName = profile.DurianName,
                DurianDescription = profile.DurianDescription,
                Characteristics = profile.Characteristics,
                TasteProfile = profile.TasteProfile
            };
        }
    }
}

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
                DurianCode = profile.DurianCode,
                DurianDescription = profile.DurianDescription,
                Characteristics = profile.Characteristics,
                TasteProfile = profile.TasteProfile,
                DurianImage = profile.DurianImage,
                DurianVideoUrl = profile.DurianVideo.VideoUrl,
                DurianVideoDescription = profile.DurianVideo.Description
            };
        }

        public static DurianProfileResponseDto ToDurianProfileDtoWithNullCheck(this DurianProfile profile)
        {
            return new DurianProfileResponseDto
            {
                DurianId = profile.DurianId,
                DurianName = profile.DurianName,
                DurianCode = profile.DurianCode,
                DurianDescription = profile.DurianDescription,
                Characteristics = profile.Characteristics,
                TasteProfile = profile.TasteProfile,
                DurianImage = profile.DurianImage,
                DurianVideoUrl = profile.DurianVideo?.VideoUrl ?? string.Empty, // Return empty string if null
                DurianVideoDescription = profile.DurianVideo?.Description ?? "No description available" // Return fallback if null
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

        public static DurianProfile ToDurianProfileFromAddRequest(this AddDurianProfileRequestDto dto, string imagePath, int videoId)
        {
            return new DurianProfile
            {
                DurianName = dto.DurianName,
                DurianCode = dto.DurianCode,
                DurianDescription = dto.DurianDescription,
                Characteristics = dto.Characteristics, // Match property names
                TasteProfile = dto.TasteProfile,
                DurianImage = imagePath, // Use the uploaded image's URL
                DurianVideoId = videoId
            };
        }

        public static void UpdateDurianProfileFromDto(this DurianProfile profile, UpdateDurianProfileRequestDto dto, string imagePath, int videoId)
        {
            profile.DurianName = dto.DurianName;
            profile.DurianDescription = dto.DurianDescription;
            profile.Characteristics = dto.Characteristics;
            profile.TasteProfile = dto.TasteProfile;
            profile.DurianImage = imagePath;
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

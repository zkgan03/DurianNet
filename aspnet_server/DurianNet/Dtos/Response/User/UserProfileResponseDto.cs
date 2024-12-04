using DurianNet.Dtos.Response.DurianProfile;

namespace DurianNet.Dtos.Response.User
{
    public class UserProfileResponseDto
    {
        public string Username { get; set; }
        public string FullName { get; set; }
        public string Email { get; set; }
        public string PhoneNumber { get; set; }
        public string ProfilePicture { get; set; }
        public List<DurianListResponseDto> FavoriteDurians { get; set; }
    }
}

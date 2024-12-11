namespace DurianNet.Dtos.Request.User
{
    /*public class UpdateUserProfileRequestDto
    {
        public string FullName { get; set; }
        public string Email { get; set; }
        public string PhoneNumber { get; set; }
        public string ProfilePicture { get; set; }
    }*/

    public class UpdateUserProfileRequestDto
    {
        public string FullName { get; set; }
        public string Email { get; set; }
        public string PhoneNumber { get; set; }
        public IFormFile ProfilePicture { get; set; }
    }

    public class UpdateUserWithoutImageRequestDto
    {
        public string? FullName { get; set; }
        public string? Email { get; set; }
        public string? PhoneNumber { get; set; }
        public string? ProfilePicture { get; set; } // "noImage" logic handled here
    }
}

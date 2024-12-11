namespace DurianNet.Dtos.Request.User
{
    public class AdminUpdateUserProfileRequestDto
    {
        public string FullName { get; set; }
        public string Email { get; set; }
        public string PhoneNumber { get; set; }
        public string ProfilePicture { get; set; } // String to store the file path
    }

}

namespace DurianNet.Dtos.Request.User
{
    public class UserDetailsDto
    {
        public string Id { get; set; }
        public string FullName { get; set; }  // Assuming full name is a separate field
        public string Email { get; set; }
        public string Username { get; set; }
        public string PhoneNumber { get; set; }
        public string Password { get; set; }  // Optional: Avoid exposing password in response
        public string UserType { get; set; }
        public string Status { get; set; }  // Enum: Active, Deleted, Inactive

        public string ProfilePicture { get; set; }  // URL or file path of the profile picture
    }

}

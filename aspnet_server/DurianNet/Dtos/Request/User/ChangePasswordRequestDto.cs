namespace DurianNet.Dtos.Request.User
{
    public class ChangePasswordRequestDto
    {
        public string CurrentPassword { get; set; } // Used only for verification, not stored in User model
        public string Password { get; set; } // New password to be updated in the model
    }
}

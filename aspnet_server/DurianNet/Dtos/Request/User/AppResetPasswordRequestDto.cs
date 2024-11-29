namespace DurianNet.Dtos.Request.User
{
    public class AppResetPasswordRequestDto
    {
        public string Email { get; set; }
        public string NewPassword { get; set; }
    }
}

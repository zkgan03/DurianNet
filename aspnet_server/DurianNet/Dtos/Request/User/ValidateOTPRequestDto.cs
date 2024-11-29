namespace DurianNet.Dtos.Request.User
{
    public class ValidateOTPRequestDto
    {
        public string Email { get; set; }
        public string OTP { get; set; }
    }
}

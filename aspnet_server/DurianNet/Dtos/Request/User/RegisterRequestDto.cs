namespace DurianNet.Dtos.Request.User
{
    public class RegisterRequestDto
    {
        public string Username { get; set; }
        public string Email { get; set; }
        public string Password { get; set; } // Only send the final password
    }
}

using DurianNet.Data;
using DurianNet.Dtos.Account;
using DurianNet.Dtos.Request.User;
using DurianNet.Models.DataModels;
using DurianNet.Services.EmailService;
using DurianNet.Services.TokenService;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace DurianNet.Controllers.appApi
{
    [Route("appApi/user")]
    [ApiController]
    public class appUserController : ControllerBase
    {
        private readonly UserManager<User> _userManager;
        private readonly ApplicationDBContext _context;
        private readonly ITokenService _tokenService; // Add ITokenService

        public appUserController(UserManager<User> userManager, ApplicationDBContext context, ITokenService tokenService)
        {
            _userManager = userManager;
            _context = context;
            _tokenService = tokenService; // Assign it in the constructor
        }

        //login
        [HttpPost("appLogin")]
        public async Task<IActionResult> appLogin([FromBody] LoginDto loginDto)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);

            var user = await _userManager.Users.FirstOrDefaultAsync(u => u.UserName.ToLower() == loginDto.Username.ToLower());
            if (user == null) return Unauthorized("Invalid username or password");

            var result = await _userManager.CheckPasswordAsync(user, loginDto.Password);
            if (!result) return Unauthorized("Invalid username or password");

            // Generate Token
            var token = _tokenService.CreateToken(user);

            return Ok(new
            {
                UserName = user.UserName,
                Email = user.Email,
                Token = token // Include the token in the response
            });
        }

        //signup
        [HttpPost("appRegister")]
        public async Task<IActionResult> appRegister([FromBody] RegisterDto registerDto)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);

            var appUser = new User
            {
                UserName = registerDto.Username,
                Email = registerDto.Email,
                ProfilePicture = "defaultProfilePicture.jpg",
                UserStatus = UserStatus.Active,
                UserType = UserType.User
            };

            var result = await _userManager.CreateAsync(appUser, registerDto.Password);
            if (!result.Succeeded) return StatusCode(500, result.Errors);

            return Ok(new { appUser.UserName, appUser.Email });
        }

        //change password
        [HttpPut("appChangePassword/{username}")]
        public async Task<IActionResult> appChangePassword(string username, [FromBody] ChangePasswordRequestDto dto)
        {
            if (string.IsNullOrWhiteSpace(dto.CurrentPassword) || string.IsNullOrWhiteSpace(dto.Password))
                return BadRequest("Current password or new password is missing.");

            var user = await _userManager.FindByNameAsync(username);
            if (user == null) return NotFound("User not found");

            var result = await _userManager.ChangePasswordAsync(user, dto.CurrentPassword, dto.Password);
            if (!result.Succeeded) return BadRequest(result.Errors);

            return Ok("Password changed successfully");
        }

        ////forgot password
        //[HttpPost("appForgotPassword")]
        //public async Task<IActionResult> appForgotPassword([FromBody] ForgotPasswordRequestDto dto)
        //{
        //    if (string.IsNullOrWhiteSpace(dto.Email))
        //        return BadRequest("Email cannot be empty.");

        //    //var user = await _userManager.FindByEmailAsync(dto.Email);
        //    var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == dto.Email);
        //    if (user == null) return NotFound("No user found with the provided email");

        //    // Determine whether the user is an admin or regular user
        //    bool isAdmin = user.UserType == UserType.Admin || user.UserType == UserType.SuperAdmin;

        //    try
        //    {
        //        // Use the EmailService to send the password recovery email
        //        EmailService.SendPasswordRecoveryEmail(user.Email, isAdmin);

        //        // Return success response
        //        return Ok("Password recovery email sent successfully.");
        //    }
        //    catch (Exception ex)
        //    {
        //        // Handle errors during email sending
        //        return StatusCode(500, $"An error occurred while sending the email: {ex.Message}");
        //    }
        //}

        [HttpPost("appForgotPassword")]
        public async Task<IActionResult> appForgotPassword([FromBody] ForgotPasswordRequestDto dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Email))
                return BadRequest("Email cannot be empty.");

            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == dto.Email);
            if (user == null) return NotFound("No user found with the provided email.");

            // Generate a new OTP
            var otp = new Random().Next(1000, 9999).ToString();
            var expiry = DateTime.UtcNow.AddMinutes(10); // OTP expires in 10 minutes

            // Update user with OTP and expiry
            user.OTP = otp;
            user.OTPExpiry = expiry;

            _context.Users.Update(user);
            await _context.SaveChangesAsync();

            // Send the OTP via email
            try
            {
                EmailService.SendOtpEmail(user.Email, otp);
                return Ok("OTP sent successfully.");
            }
            catch (Exception ex)
            {
                return StatusCode(500, $"An error occurred while sending the email: {ex.Message}");
            }
        }


        [HttpPost("validateOTP")]
        public async Task<IActionResult> ValidateOTP([FromBody] ValidateOTPRequestDto dto)
        {
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == dto.Email);

            if (user == null) return NotFound("User not found.");
            if (user.OTP != dto.OTP) return BadRequest("Invalid OTP.");
            if (user.OTPExpiry == null || user.OTPExpiry < DateTime.UtcNow) return BadRequest("OTP has expired.");

            // Clear OTP after validation
            user.OTP = null;
            user.OTPExpiry = null;
            _context.Users.Update(user);
            await _context.SaveChangesAsync();

            return Ok("OTP validated successfully.");
        }



        //reset password (get email and new password)
        [HttpPost("appResetPassword")]
        public async Task<IActionResult> appResetPassword([FromBody] AppResetPasswordRequestDto dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Email) || string.IsNullOrWhiteSpace(dto.NewPassword))
                return BadRequest("Email and new password cannot be empty");

            // Find the user by email
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == dto.Email);
            if (user == null) return NotFound("User not found");

            // Reset the password
            user.PasswordHash = _userManager.PasswordHasher.HashPassword(user, dto.NewPassword);
            var result = await _userManager.UpdateAsync(user);

            if (!result.Succeeded)
                return StatusCode(500, $"Failed to reset password: {string.Join(", ", result.Errors.Select(e => e.Description))}");

            return Ok("Password reset successfully");
        }

        //display edit profile
        //display user profile
        [HttpGet("appGetUserByUsername/{username}")]
        public async Task<IActionResult> appGetUserByUsername(string username)
        {
            var user = await _userManager.Users.SingleOrDefaultAsync(u => u.UserName == username);
            if (user == null) return NotFound("User not found");

            return Ok(new { user.UserName, user.Email, user.FullName, user.PhoneNumber, user.ProfilePicture });
        }

        //edit profile
        [HttpPut("appUpdateUserByUsername/{username}")]
        public async Task<IActionResult> appUpdateUserByUsername(string username, [FromBody] UpdateUserProfileRequestDto dto)
        {
            var user = await _userManager.Users.SingleOrDefaultAsync(u => u.UserName == username);
            if (user == null) return NotFound("User not found");

            user.FullName = dto.FullName ?? user.FullName;
            user.Email = dto.Email ?? user.Email;
            user.PhoneNumber = dto.PhoneNumber ?? user.PhoneNumber;

            await _userManager.UpdateAsync(user);
            return Ok("User profile updated successfully");
        }
    }
}

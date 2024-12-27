using DurianNet.Data;
using DurianNet.Dtos.Account;
using DurianNet.Dtos.Request.User;
using DurianNet.Mappers;
using DurianNet.Models.DataModels;
using DurianNet.Services.EmailService;
using DurianNet.Services.TokenService;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using DurianNet.Dtos.Request.User;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authentication;


namespace DurianNet.Controllers.api
{
    //[Route("api/[controller]")]
    [Route("api/user")]
    [ApiController]
    public class UserController : ControllerBase
    {
        private readonly ApplicationDBContext _context;
        private readonly UserManager<User> _userManager;
        private readonly ITokenService _tokenService; // Add ITokenService

        public UserController(UserManager<User> userManager, ApplicationDBContext context, ITokenService tokenService)
        {
            _userManager = userManager;
            _context = context;
            _tokenService = tokenService; // Assign it in the constructor
        }

        [HttpGet("GetEverything")]
        public async Task<IActionResult> GetEverything()
        {
            var users = await _context.Users.ToListAsync();  // Retrieve all users

            if (users == null || !users.Any())
            {
                return NotFound("No users found.");
            }

            // Map the users to a list of UserDetailsDto
            var userDtos = users.Select(user => user.UserDetailsDto()).ToList();

            return Ok(userDtos);
        }

        [HttpGet("GetSessionEmail")]
        public IActionResult GetSessionEmail()
        {
            var email = HttpContext.Session.GetString("ResetPasswordEmail");
            if (string.IsNullOrEmpty(email))
                return NotFound("No email found in session.");

            return Ok($"Email in session: {email}");
        }

        [HttpPost("appLogin")]
        public async Task<IActionResult> appLogin([FromBody] LoginDto loginDto)
        {
            try
            {
                if (!ModelState.IsValid)
                    return BadRequest(ModelState);

                var user = await _userManager.Users.FirstOrDefaultAsync(u => u.UserName.ToLower() == loginDto.Username.ToLower());
                if (user == null)
                    return Unauthorized("Invalid username or password");

                // Check if the user is deleted
                if (user.UserStatus == UserStatus.Deleted)
                    return Unauthorized("User account is deleted. Contact support.");

                var result = await _userManager.CheckPasswordAsync(user, loginDto.Password);
                if (!result)
                    return Unauthorized("Invalid username or password");

                // Generate access token
                var accessToken = _tokenService.GenerateAccessToken(user);

                // Session-based authentication (for web)
                var claims = new[] {
                    new Claim(ClaimTypes.NameIdentifier, user.Id),
                    new Claim(ClaimTypes.Email, user.Email),
                    new Claim(ClaimTypes.Name, user.UserName),
                    new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString())
                };

                var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);
                var principal = new ClaimsPrincipal(identity);

                await HttpContext.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme, principal, new AuthenticationProperties
                {
                    IsPersistent = true,
                    ExpiresUtc = DateTime.UtcNow.AddMinutes(30)
                });

                return Ok(new
                {
                    UserName = user.UserName,
                    Email = user.Email,
                    AccessToken = accessToken
                });
            }
            catch (Exception ex)
            {
                // Log the actual error
                Console.WriteLine($"Error in /appLogin: {ex.Message} - Stack Trace: {ex.StackTrace}");
                return StatusCode(500, $"An unexpected fault happened: {ex.Message}");
            }
        }



        [HttpPut("appDeleteAccount/{username}")]
        public async Task<IActionResult> appDeleteAccount(string username)
        {
            var user = await _userManager.FindByNameAsync(username);
            if (user == null) return NotFound("User not found");

            // Update user status to deleted
            user.UserStatus = UserStatus.Deleted;
            await _userManager.UpdateAsync(user);

            return Ok("User account deleted successfully");
        }

        [HttpPost("appLogout")]
        public async Task<IActionResult> appLogout()
        {
            await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);

            return Ok("Logged out successfully");
        }

        [HttpPost("appRegister")]
        public async Task<IActionResult> appRegister([FromBody] RegisterDto registerDto)
        {
            // 1. Validate the ModelState
            if (!ModelState.IsValid)
                return BadRequest("Invalid input. Please check the provided data.");

            // 2. Check if the username already exists
            var existingUserByUsername = await _userManager.FindByNameAsync(registerDto.Username);
            if (existingUserByUsername != null)
                return BadRequest("Username is already taken. Please choose a different one.");

            // 3. Check if the email already exists
            var existingUserByEmail = await _userManager.Users.FirstOrDefaultAsync(u => u.Email.ToLower() == registerDto.Email.ToLower());
            if (existingUserByEmail != null)
                return BadRequest("Email is already registered. Please use a different email.");

            // 4. Create the new user
            var appUser = new User
            {
                UserName = registerDto.Username,
                Email = registerDto.Email,
                ProfilePicture = "defaultProfilePicture.jpg",
                UserStatus = UserStatus.Active,
                UserType = UserType.User,
                FullName = "-", // Provide default value
                PhoneNumber = "-" // Provide default value
            };

            // 5. Create the user using the UserManager
            var result = await _userManager.CreateAsync(appUser, registerDto.Password);
            if (!result.Succeeded)
                return StatusCode(500, "An error occurred while creating the user.");

            // 6. Return the success response
            return Ok("Registration successful.");
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

        [HttpPut("appUpdateUserByUsername/{username}")]
        public async Task<IActionResult> appUpdateUserByUsername(string username, [FromForm] UpdateUserProfileRequestDto dto)
        {
            var user = await _context.Users.SingleOrDefaultAsync(u => u.UserName == username);
            if (user == null) return NotFound("User not found");

            user.FullName = dto.FullName ?? user.FullName;
            user.Email = dto.Email ?? user.Email;
            user.PhoneNumber = dto.PhoneNumber ?? user.PhoneNumber;

            // Handle profile picture upload
            if (dto.ProfilePicture != null)
            {
                var uploadsFolder = Path.Combine("wwwroot", "images");
                Directory.CreateDirectory(uploadsFolder);

                var fileName = $"{Guid.NewGuid()}_{dto.ProfilePicture.FileName}";
                var filePath = Path.Combine(uploadsFolder, fileName);

                using (var stream = new FileStream(filePath, FileMode.Create))
                {
                    await dto.ProfilePicture.CopyToAsync(stream);
                }

                user.ProfilePicture = $"/images/{fileName}"; // Save relative path
            }

            _context.Users.Update(user);
            await _context.SaveChangesAsync();

            return Ok("User profile updated successfully");
        }

        [HttpPut("appUpdateUserWithoutImage/{username}")]
        public async Task<IActionResult> appUpdateUserWithoutImage(string username, [FromBody] UpdateUserWithoutImageRequestDto dto)
        {
            var user = await _context.Users.SingleOrDefaultAsync(u => u.UserName == username);
            if (user == null) return NotFound("User not found");

            // Update user's Full Name, Email, and Phone Number
            user.FullName = dto.FullName ?? user.FullName;
            user.Email = dto.Email ?? user.Email;
            user.PhoneNumber = dto.PhoneNumber ?? user.PhoneNumber;

            // Retain the existing profile image
            if (dto.ProfilePicture == "noImage")
            {
                user.ProfilePicture = user.ProfilePicture; // No changes to profile picture
            }

            // Save changes in the database
            _context.Users.Update(user);
            await _context.SaveChangesAsync();

            return Ok("User profile updated successfully without changing the profile image");
        }


        [HttpPut("appUpdateUserWithImage/{username}")]
        public async Task<IActionResult> UpdateUserWithImage(string username, [FromForm] UpdateUserProfileRequestDto dto)
        {
            var user = await _userManager.Users.SingleOrDefaultAsync(u => u.UserName == username);
            if (user == null)
                return NotFound("User not found");

            user.FullName = dto.FullName ?? user.FullName;
            user.Email = dto.Email ?? user.Email;
            user.PhoneNumber = dto.PhoneNumber ?? user.PhoneNumber;

            if (dto.ProfilePicture != null)
            {
                var uploadsFolder = Path.Combine("wwwroot", "images");
                Directory.CreateDirectory(uploadsFolder);

                var fileName = $"{Guid.NewGuid()}_{dto.ProfilePicture.FileName}";
                var filePath = Path.Combine(uploadsFolder, fileName);

                using (var stream = new FileStream(filePath, FileMode.Create))
                {
                    await dto.ProfilePicture.CopyToAsync(stream);
                }

                user.ProfilePicture = $"/images/{fileName}";
            }

            await _userManager.UpdateAsync(user);
            return Ok("Profile updated successfully");
        }

        // This method mainly is used to refresh the access token, from the android client
        [HttpPost("refreshToken")]
        public async Task<IActionResult> RefreshToken([FromBody] RefreshTokenRequest request)
        {
            Console.WriteLine("Refresh token called");

            var user = await _userManager.Users
                .Include(u => u.RefreshTokens)
                .SingleOrDefaultAsync(u => u.RefreshTokens.Any(rt => rt.Token == request.RefreshToken));

            if (user == null)
                return Unauthorized("Invalid refresh token");

            var refreshToken = user.RefreshTokens.Single(rt => rt.Token == request.RefreshToken);

            if (refreshToken.Expiration < DateTime.UtcNow || refreshToken.IsRevoked)
            {
                return Unauthorized("Expired or revoked refresh token");
            }

            // Revoke old refresh token and issue a new one
            refreshToken.IsRevoked = true;
            var newRefreshToken = _tokenService.GenerateRefreshToken(); // rotate refresh token

            user.RefreshTokens.Add(newRefreshToken);
            await _userManager.UpdateAsync(user);

            var newAccessToken = _tokenService.GenerateAccessToken(user); // refresh access token

            return Ok(new { AccessToken = newAccessToken, RefreshToken = newRefreshToken.Token });
        }

        [HttpGet("authorizedAction")]
        public IActionResult AuthorizedAction()
        {
            Console.WriteLine("Authorized action called");

            //Get everything from the token
            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            var email = User.FindFirstValue(ClaimTypes.Email);
            var jti = User.FindFirstValue(JwtRegisteredClaimNames.Jti);
            var exp = User.FindFirstValue(JwtRegisteredClaimNames.Exp);

            // return as anonymous object
            return Ok(new
            {
                userId,
                email,
                jti,
                exp
            });
        }
    }
}

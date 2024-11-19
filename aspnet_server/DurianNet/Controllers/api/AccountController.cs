using DurianNet.Dtos.Account;
using DurianNet.Dtos.Request.User;
using DurianNet.Interfaces;
using DurianNet.Models.DataModels;
using DurianNet.Repository;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;

namespace DurianNet.Controllers.api
{
    [Route("api/account")]
    [ApiController]
    public class AccountController : ControllerBase
    {
        private readonly UserManager<User> _userManager;
        private readonly ITokenService _tokenService;
        private readonly SignInManager<User> _signinManager;
        private readonly IUserRepository _userRepository;

        public AccountController(UserManager<User> userManager, ITokenService tokenService, SignInManager<User> signInManager, IUserRepository userRepository)
        {
            _userManager = userManager;
            _tokenService = tokenService;
            _signinManager = signInManager;
            _userRepository = userRepository;
        }

        //    [HttpPost("login")]
        //    public async Task<IActionResult> Login(LoginDto loginDto)
        //    {
        //        if (!ModelState.IsValid)
        //            return BadRequest(ModelState);

        //        var user = await _userManager.Users.FirstOrDefaultAsync(x => x.UserName == loginDto.Username.ToLower());

        //        if (user == null) return Unauthorized("Invalid username!");

        //        if (user.UserType != UserType.User)
        //            return Unauthorized("Only regular users can log in to the app.");

        //        var result = await _signinManager.CheckPasswordSignInAsync(user, loginDto.Password, false);

        //        if (!result.Succeeded) return Unauthorized("Username not found and/or password incorrect");

        //        // Create cookie
        //        var claims = new List<Claim>
        //{
        //    new Claim(ClaimTypes.Name, user.UserName),
        //    new Claim(ClaimTypes.NameIdentifier, user.Id),
        //    new Claim(ClaimTypes.Role, user.UserType.ToString())
        //};

        //        var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);
        //        var principal = new ClaimsPrincipal(identity);

        //        await HttpContext.SignInAsync(
        //            CookieAuthenticationDefaults.AuthenticationScheme,
        //            principal,
        //            new AuthenticationProperties
        //            {
        //                IsPersistent = true,  // Keep the cookie across sessions
        //                ExpiresUtc = DateTime.UtcNow.AddMinutes(30)  // Cookie expiration
        //            });

        //        return Ok(
        //            new NewUserDto
        //            {
        //                UserName = user.UserName,
        //                Email = user.Email,
        //                Token = _tokenService.CreateToken(user)
        //            }
        //        );
        //    }


        //    [HttpPost("loginAdmin")]
        //    public async Task<IActionResult> LoginAdmin(LoginDto loginDto)
        //    {
        //        if (!ModelState.IsValid)
        //            return BadRequest(ModelState);

        //        var user = await _userManager.Users.FirstOrDefaultAsync(x => x.UserName == loginDto.Username.ToLower());

        //        if (user == null) return Unauthorized("Invalid username!");

        //        if (user.UserType != UserType.Admin && user.UserType != UserType.SuperAdmin)
        //            return Unauthorized("Only admins and super admins can log in to the admin web interface.");

        //        var result = await _signinManager.CheckPasswordSignInAsync(user, loginDto.Password, false);

        //        if (!result.Succeeded) return Unauthorized("Username not found and/or password incorrect");

        //        // Create cookie
        //        var claims = new List<Claim>
        //{
        //    new Claim(ClaimTypes.Name, user.UserName),
        //    new Claim(ClaimTypes.NameIdentifier, user.Id),
        //    new Claim(ClaimTypes.Role, user.UserType.ToString())
        //};

        //        var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);
        //        var principal = new ClaimsPrincipal(identity);

        //        await HttpContext.SignInAsync(
        //            CookieAuthenticationDefaults.AuthenticationScheme,
        //            principal,
        //            new AuthenticationProperties
        //            {
        //                IsPersistent = true,  // Keep the cookie across sessions
        //                ExpiresUtc = DateTime.UtcNow.AddMinutes(30)  // Cookie expiration
        //            });

        //        return Ok(
        //            new NewUserDto
        //            {
        //                UserName = user.UserName,
        //                Email = user.Email,
        //                Token = _tokenService.CreateToken(user)
        //            }
        //        );
        //    }

        [HttpPost("login")]
        public async Task<IActionResult> Login(LoginDto loginDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var user = await _userManager.Users.FirstOrDefaultAsync(x => x.UserName == loginDto.Username.ToLower());

            if (user == null) return Unauthorized("Invalid username!");

            if (user.UserType != UserType.User)
                return Unauthorized("Only regular users can log in to the app.");

            var result = await _signinManager.CheckPasswordSignInAsync(user, loginDto.Password, false);

            if (!result.Succeeded) return Unauthorized("Username not found and/or password incorrect");

            // Generate Token
            var token = _tokenService.CreateToken(user);

            // Create cookie for session-based authentication
            var claims = new List<Claim>
    {
        new Claim(ClaimTypes.Name, user.UserName),
        new Claim(ClaimTypes.NameIdentifier, user.Id),
        new Claim(ClaimTypes.Role, user.UserType.ToString())
    };

            var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);
            var principal = new ClaimsPrincipal(identity);

            await HttpContext.SignInAsync(
                CookieAuthenticationDefaults.AuthenticationScheme,
                principal,
                new AuthenticationProperties
                {
                    IsPersistent = true,  // Keep the cookie across sessions
                    ExpiresUtc = DateTime.UtcNow.AddMinutes(30)  // Cookie expiration
                });

            // Save JWT in a secure HTTP-only cookie
            HttpContext.Response.Cookies.Append("AuthToken", token, new CookieOptions
            {
                HttpOnly = true, // Prevent access via JavaScript
                Secure = true,   // Use HTTPS
                SameSite = SameSiteMode.Strict, // Strict SameSite policy
                Expires = DateTime.UtcNow.AddMinutes(30) // Matches the session expiration
            });

            return Ok(
                new NewUserDto
                {
                    UserName = user.UserName,
                    Email = user.Email,
                    Token = token // Return token to the client as well
                }
            );
        }

        [HttpPost("loginAdmin")]
        public async Task<IActionResult> LoginAdmin(LoginDto loginDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var user = await _userManager.Users.FirstOrDefaultAsync(x => x.UserName == loginDto.Username.ToLower());

            if (user == null) return Unauthorized("Invalid username!");

            if (user.UserType != UserType.Admin && user.UserType != UserType.SuperAdmin)
                return Unauthorized("Only admins and super admins can log in to the admin web interface.");

            var result = await _signinManager.CheckPasswordSignInAsync(user, loginDto.Password, false);

            if (!result.Succeeded) return Unauthorized("Username not found and/or password incorrect");

            // Generate Token
            var token = _tokenService.CreateToken(user);

            // Create cookie for session-based authentication
            var claims = new List<Claim>
        {
            new Claim(ClaimTypes.Name, user.UserName),
            new Claim(ClaimTypes.NameIdentifier, user.Id),
            new Claim(ClaimTypes.Role, user.UserType.ToString())
        };

            var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);
            var principal = new ClaimsPrincipal(identity);

            await HttpContext.SignInAsync(
                CookieAuthenticationDefaults.AuthenticationScheme,
                principal,
                new AuthenticationProperties
                {
                    IsPersistent = true,  // Keep the cookie across sessions
                    ExpiresUtc = DateTime.UtcNow.AddMinutes(30)  // Cookie expiration
                });

            // Save JWT in a secure HTTP-only cookie
            HttpContext.Response.Cookies.Append("AuthToken", token, new CookieOptions
            {
                HttpOnly = true, // Prevent access via JavaScript
                Secure = true,   // Use HTTPS
                SameSite = SameSiteMode.Strict, // Strict SameSite policy
                Expires = DateTime.UtcNow.AddMinutes(30) // Matches the session expiration
            });

            return Ok(
                new NewUserDto
                {
                    UserName = user.UserName,
                    Email = user.Email,
                    Token = token // Return token to the client as well
                }
            );
        }


        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegisterDto registerDto)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(ModelState);
                }

                var appUser = new User
                {
                    UserName = registerDto.Username,
                    Email = registerDto.Email,
                    ProfilePicture = "defaultProfilePicture.jpg", // Set default profile picture
                    UserStatus = UserStatus.Active, // Set default status
                    UserType = UserType.User // Set default type
                };

                var createdUser = await _userManager.CreateAsync(appUser, registerDto.Password);

                if (createdUser.Succeeded)
                {
                    var roleResult = await _userManager.AddToRoleAsync(appUser, "User");
                    if (roleResult.Succeeded)
                    {
                        return Ok(
                        new NewUserDto
                        {
                            UserName = appUser.UserName,
                            Email = appUser.Email,
                            Token = _tokenService.CreateToken(appUser)
                        });
                    }
                    else
                    {
                        return StatusCode(500, roleResult.Errors);
                    }
                }
                else
                {
                    return StatusCode(500, createdUser.Errors);
                }
            }
            catch (Exception e)
            {
                Console.WriteLine($"Error during registration: {e.Message}");
                return StatusCode(500, "An unexpected error occurred during registration.");
            }
        }

        [HttpPost("registerAdmin")]
        public async Task<IActionResult> RegisterAdmin([FromBody] RegisterDto registerDto)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(ModelState);
                }

                var appUser = new User
                {
                    UserName = registerDto.Username,
                    Email = registerDto.Email,
                    ProfilePicture = "default.jpg", // Set default profile picture
                    UserStatus = UserStatus.Active, // Set default status
                    UserType = UserType.Admin // Set default type for admin
                };

                var createdUser = await _userManager.CreateAsync(appUser, registerDto.Password);

                if (createdUser.Succeeded)
                {
                    var roleResult = await _userManager.AddToRoleAsync(appUser, "Admin");
                    if (roleResult.Succeeded)
                    {
                        return Ok(
                        new NewUserDto
                        {
                            UserName = appUser.UserName,
                            Email = appUser.Email,
                            Token = _tokenService.CreateToken(appUser)
                        });
                    }
                    else
                    {
                        return StatusCode(500, roleResult.Errors);
                    }
                }
                else
                {
                    return StatusCode(500, createdUser.Errors);
                }
            }
            catch (Exception e)
            {
                Console.WriteLine($"Error during admin registration: {e.Message}");
                return StatusCode(500, "An unexpected error occurred during admin registration.");
            }
        }

        [HttpPost("ResetPassword")]
        public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordRequestDto dto)
        {
            if (string.IsNullOrWhiteSpace(dto.NewPassword))
                return BadRequest("New password cannot be empty.");

            // Retrieve email from session
            var email = HttpContext.Session.GetString("ResetPasswordEmail");
            if (string.IsNullOrEmpty(email))
                return BadRequest("No email found for password reset. Please initiate the Forgot Password process again.");

            // Find the user by email
            var user = await _userManager.FindByEmailAsync(email);
            if (user == null)
                return BadRequest("No user found with the provided email.");

            try
            {
                // Reset the password
                user.PasswordHash = _userManager.PasswordHasher.HashPassword(user, dto.NewPassword);
                var result = await _userManager.UpdateAsync(user);

                if (!result.Succeeded)
                {
                    var errors = string.Join(", ", result.Errors.Select(e => e.Description));
                    Console.WriteLine($"Password reset failed: {errors}");
                    return StatusCode(500, $"An error occurred while resetting the password: {errors}");
                }

                Console.WriteLine("Password reset successful.");
                return Ok("Password reset successfully.");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Exception in ResetPassword: {ex.Message}");
                return StatusCode(500, "An unexpected error occurred while resetting the password.");
            }
        }

        [HttpPut("ChangePassword")]
        [Authorize]
        public async Task<IActionResult> ChangePassword([FromBody] ChangePasswordRequestDto dto)
        {
            if (string.IsNullOrWhiteSpace(dto.CurrentPassword) || string.IsNullOrWhiteSpace(dto.Password))
                return BadRequest("Current password or new password is missing.");

            // Retrieve the username from ClaimTypes.Name
            var username = User.Identity?.Name;
            if (string.IsNullOrEmpty(username))
                return Unauthorized("User not found1.");

            var user = await _userManager.FindByNameAsync(username);
            if (user == null)
                return Unauthorized("User not found.");

            var result = await _userManager.ChangePasswordAsync(user, dto.CurrentPassword, dto.Password);
            if (!result.Succeeded)
                return BadRequest(result.Errors);

            return Ok("Password changed successfully.");
        }

        [HttpPost("refresh-token")]
        public async Task<IActionResult> RefreshToken([FromBody] RefreshTokenRequestDto request)
        {
            var user = await _userManager.Users
                .Include(u => u.RefreshTokens)
                .FirstOrDefaultAsync(u => u.RefreshTokens.Any(rt => rt.Token == request.RefreshToken));

            if (user == null)
                return Unauthorized("Invalid refresh token");

            var refreshToken = user.RefreshTokens.Single(rt => rt.Token == request.RefreshToken);
            if (refreshToken.Expiration < DateTime.UtcNow || refreshToken.IsRevoked)
                return Unauthorized("Expired or revoked refresh token");

            refreshToken.IsRevoked = true;
            var newRefreshToken = _tokenService.GenerateRefreshToken();
            user.RefreshTokens.Add(newRefreshToken);
            await _userManager.UpdateAsync(user);

            var newAccessToken = _tokenService.CreateToken(user);

            return Ok(new { AccessToken = newAccessToken, RefreshToken = newRefreshToken.Token });
        }

        [HttpPost("logout")]
        public async Task<IActionResult> Logout([FromBody] LogoutRequestDto request)
        {
            // Get the current user from the UserManager
            var user = await _userManager.GetUserAsync(User);

            if (user == null)
                return Unauthorized("User not found or not logged in.");

            // Revoke the refresh token (if applicable)
            if (!string.IsNullOrEmpty(request.RefreshToken))
            {
                await _tokenService.RevokeRefreshToken(user, request.RefreshToken);
            }

            // Sign out the user and remove the authentication cookie
            await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);

            // Delete the AuthToken cookie
            HttpContext.Response.Cookies.Delete("AuthToken");

            return Ok("Logged out successfully.");
        }


        [Authorize(Policy = "AdminPolicy")]
        [HttpGet("admin-action")]
        public IActionResult AdminAction()
        {
            return Ok("This is an admin-only action.");
        }



    }
}


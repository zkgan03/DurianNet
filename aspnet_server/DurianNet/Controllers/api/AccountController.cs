using DurianNet.Dtos.Account;
using DurianNet.Dtos.Request.User;
using DurianNet.Models.DataModels;
using DurianNet.Services.TokenService;
using DurianNet.Services.UserService;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text.RegularExpressions;

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

        //[HttpPost("loginAdmin")]
        //public async Task<IActionResult> LoginAdmin(LoginDto loginDto)
        //{
        //    if (!ModelState.IsValid)
        //        return BadRequest(ModelState);

        //    var user = await _userManager.Users.FirstOrDefaultAsync(x => x.UserName == loginDto.Username.ToLower());

        //    if (user == null) return Unauthorized("Invalid username!");

        //    if (user.UserType != UserType.Admin && user.UserType != UserType.SuperAdmin)
        //        return Unauthorized("Only admins and super admins can log in to the admin web interface.");

        //    if (user.UserStatus == UserStatus.Deleted)
        //        return Unauthorized("This admin account is deleted.");

        //    var result = await _signinManager.CheckPasswordSignInAsync(user, loginDto.Password, false);

        //    if (!result.Succeeded) return Unauthorized("Invalid username or password!");

        //    // Generate JWT token
        //    var token = _tokenService.CreateToken(user);

        //    // Set the session cookie with the username
        //    HttpContext.Session.SetString("Username", user.UserName);

        //    // Save the token in an HTTP-only cookie
        //    HttpContext.Response.Cookies.Append("AuthToken", token, new CookieOptions
        //    {
        //        HttpOnly = true, // Prevents JavaScript access
        //        Secure = true,   // Use HTTPS in production
        //        SameSite = SameSiteMode.Strict,
        //        Expires = DateTime.UtcNow.AddMinutes(30)
        //    });

        //    return Ok(new
        //    {
        //        UserName = user.UserName,
        //        Email = user.Email
        //    });
        //}

        [HttpPost("loginAdmin")]
        public async Task<IActionResult> LoginAdmin(LoginDto loginDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(new { message = "Invalid input. Please check your username and password." });

            var user = await _userManager.Users
                .FirstOrDefaultAsync(x => x.UserName.ToLower() == loginDto.Username.ToLower());

            if (user == null)
                return Unauthorized(new { message = "Invalid username!" });

            if (user.UserType != UserType.Admin && user.UserType != UserType.SuperAdmin)
                return Unauthorized(new { message = "Only admins and super admins can log in to the admin web interface." });

            if (user.UserStatus == UserStatus.Deleted)
                return Unauthorized(new { message = "This admin account is deleted." });

            var result = await _signinManager.CheckPasswordSignInAsync(user, loginDto.Password, false);

            if (!result.Succeeded)
                return Unauthorized(new { message = "Invalid username or password!" });

            // Generate JWT token
            var token = _tokenService.CreateToken(user);

            // Set the session cookie with the username
            HttpContext.Session.SetString("Username", user.UserName);

            // create claims
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.Name, user.UserName),
                new Claim(ClaimTypes.NameIdentifier, user.Id),
                new Claim(ClaimTypes.Role, user.UserType.ToString())
            };

            // create identity
            var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);

            // create principal
            var principal = new ClaimsPrincipal(identity);

            // sign in
            await HttpContext.SignInAsync(
                CookieAuthenticationDefaults.AuthenticationScheme,
                principal,
                new AuthenticationProperties
                {
                    IsPersistent = true,
                    ExpiresUtc = DateTime.UtcNow.AddMinutes(30)
                });


            //// Save the token in an HTTP-only cookie
            //HttpContext.Response.Cookies.Append("AuthToken", token, new CookieOptions
            //{
            //    HttpOnly = true,
            //    Secure = true,
            //    SameSite = SameSiteMode.Strict,
            //    Expires = DateTime.UtcNow.AddMinutes(30)
            //});

            return Ok(new { UserName = user.UserName, Email = user.Email, message = "Login successful!" });
        }

        [Authorize(policy: "AdminPolicy")]
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
                // Check if ModelState is valid
                if (!ModelState.IsValid)
                {
                    return BadRequest("Invalid input.");
                }

                // Validate username length
                if (registerDto.Username.Length < 5)
                {
                    return BadRequest("Username must be at least 5 characters long.");
                }

                // Validate password strength
                var passwordPattern = @"^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&_])[A-Za-z\d@$!%*?&_]{8,}$";

                if (!Regex.IsMatch(registerDto.Password, passwordPattern))
                {
                    return BadRequest("Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.");
                }

                // Check if username or email already exists
                if (await _userManager.Users.AnyAsync(u => u.UserName == registerDto.Username))
                {
                    return BadRequest("Username is already taken.");
                }
                if (await _userManager.Users.AnyAsync(u => u.Email == registerDto.Email))
                {
                    return BadRequest("Email is already in use.");
                }

                // Create user
                var appUser = new User
                {
                    UserName = registerDto.Username,
                    Email = registerDto.Email,
                    ProfilePicture = "default.jpg", // Default profile picture
                    UserStatus = UserStatus.Active,
                    UserType = UserType.Admin
                };

                var createdUser = await _userManager.CreateAsync(appUser, registerDto.Password);

                if (!createdUser.Succeeded)
                {
                    return StatusCode(500, createdUser.Errors);
                }

                // Add user to Admin role
                var roleResult = await _userManager.AddToRoleAsync(appUser, "Admin");
                if (!roleResult.Succeeded)
                {
                    return StatusCode(500, roleResult.Errors);
                }

                return Ok(new NewUserDto
                {
                    UserName = appUser.UserName,
                    Email = appUser.Email,
                    Token = _tokenService.CreateToken(appUser)
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error during admin registration: {ex.Message}");
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




        [HttpPut("ChangePassword/{username?}")]
        public async Task<IActionResult> ChangePassword(string? username, [FromBody] ChangePasswordRequestDto dto)
        {
            if (string.IsNullOrWhiteSpace(dto.CurrentPassword) || string.IsNullOrWhiteSpace(dto.Password))
                return BadRequest("Current password or new password is missing.");

            // If the username is not provided in the route, retrieve it from the session
            if (string.IsNullOrEmpty(username))
            {
                username = HttpContext.Session.GetString("Username");
                if (string.IsNullOrEmpty(username))
                    return Unauthorized("User not found.");
            }

            // Find the user by username
            var user = await _userManager.FindByNameAsync(username);
            if (user == null)
                return NotFound("User not found.");

            // Attempt to change the password
            var result = await _userManager.ChangePasswordAsync(user, dto.CurrentPassword, dto.Password);
            if (!result.Succeeded)
            {
                var errorMessages = string.Join(", ", result.Errors.Select(e => e.Description));
                Console.WriteLine($"Change password failed: {errorMessages}");
                return BadRequest(result.Errors);
            }

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



        //This is admin logout
        [Authorize(policy: "AdminPolicy")]
        [HttpPost("logout")]
        public async Task<IActionResult> Logout()
        {
            // Retrieve the username from session
            //var username = HttpContext.Session.GetString("Username");
            //if (string.IsNullOrEmpty(username))
            //    return Unauthorized("User not logged in.");

            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);

            // get the user
            var user = await _userManager.FindByIdAsync(userId);

            if (user == null)
            {
                return Unauthorized("User not logged in.");
            }

            // Clear session data
            HttpContext.Session.Remove("Username");
            HttpContext.Session.Remove("ResetPasswordEmail");

            // Sign out and remove cookies
            //await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
            HttpContext.Response.Cookies.Delete("AuthToken");

            await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);

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


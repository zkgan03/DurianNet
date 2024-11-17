using DurianNet.Dtos.Account;
using DurianNet.Dtos.Request.User;
using DurianNet.Interfaces;
using DurianNet.Models.DataModels;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.IdentityModel.Tokens.Jwt;

namespace DurianNet.Controllers.api
{
    [Route("api/account")]
    [ApiController]
    public class AccountController : ControllerBase
    {
        private readonly UserManager<User> _userManager;
        private readonly ITokenService _tokenService;
        private readonly SignInManager<User> _signinManager;

        public AccountController(UserManager<User> userManager, ITokenService tokenService, SignInManager<User> signInManager)
        {
            _userManager = userManager;
            _tokenService = tokenService;
            _signinManager = signInManager;
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

            return Ok(
                new NewUserDto
                {
                    UserName = user.UserName,
                    Email = user.Email,
                    Token = _tokenService.CreateToken(user)
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

            return Ok(
                new NewUserDto
                {
                    UserName = user.UserName,
                    Email = user.Email,
                    Token = _tokenService.CreateToken(user)
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
                    ProfilePicture = "default.jpg", // Set default profile picture
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
        public async Task<IActionResult> ResetPassword(string token, [FromBody] ResetPasswordRequestDto dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Password))
                return BadRequest("Password cannot be empty.");

            var user = await _userManager.Users.SingleOrDefaultAsync(u => u.Id == token); // Assuming the token is user's Id for simplicity
            if (user == null)
                return BadRequest("Invalid or expired reset token.");

            var result = await _userManager.ResetPasswordAsync(user, token, dto.Password);
            if (!result.Succeeded)
                return StatusCode(500, result.Errors);

            return Ok("Password reset successfully.");
        }

        //[HttpPut("ChangePassword")]
        //[Authorize]
        //public async Task<IActionResult> ChangePassword([FromBody] ChangePasswordRequestDto dto)
        //{
        //    if (string.IsNullOrWhiteSpace(dto.CurrentPassword) || string.IsNullOrWhiteSpace(dto.Password))
        //        return BadRequest("Current password or new password is missing.");

        //    // Retrieve username from JwtRegisteredClaimNames.GivenName
        //    var username = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.GivenName)?.Value;
        //    //var user = await _userManager.FindByNameAsync(username);
        //    //if (user == null)
        //    //    return Unauthorized("User not found.");
        //    if (string.IsNullOrEmpty(username))
        //        return Unauthorized("User not found.");

        //    var user = await _userManager.FindByNameAsync(username);
        //    if (user == null)
        //        return Unauthorized("User not found.");

        //    var result = await _userManager.ChangePasswordAsync(user, dto.CurrentPassword, dto.Password);
        //    if (!result.Succeeded)
        //        return BadRequest(result.Errors);

        //    return Ok("Password changed successfully.");
        //}

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



    }
}


using DurianNet.Data;
using DurianNet.Models.DataModels;
using DurianNet.Services.TokenService;
using DurianNet.Services.UserService;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using DurianNet.Dtos.Request.User;
using DurianNet.Mappers;
using System.Text.RegularExpressions;
using Microsoft.AspNetCore.Authorization;

[Authorize(Policy = "AdminPolicy")]
[ApiController]
[Route("adminprofile")]
public class AdminProfileWebController : Controller
{
    private readonly UserManager<User> _userManager;
    private readonly ITokenService _tokenService;
    private readonly SignInManager<User> _signinManager;
    private readonly IUserRepository _userRepository;
    private readonly ApplicationDBContext _context;

    public AdminProfileWebController(UserManager<User> userManager, ITokenService tokenService, SignInManager<User> signInManager, IUserRepository userRepository, ApplicationDBContext context)
    {
        _userManager = userManager;
        _tokenService = tokenService;
        _signinManager = signInManager;
        _userRepository = userRepository;
        _context = context;
    }


    [HttpGet("ProfilePage")]
    public IActionResult ProfilePage()
    {
        return View("~/Views/AdminProfile/AdminProfile.cshtml");
    }

    [HttpGet("EditProfilePage")]
    public IActionResult EditProfilePage()
    {
        return View("~/Views/AdminProfile/EditAdminProfile.cshtml");
    }

    [HttpGet("ChangePasswordPage")]
    public IActionResult ChangePasswordPage()
    {
        return View("~/Views/AdminProfile/ChangePassword.cshtml");
    }

    [HttpPost("ChangePasswordAction")]
    public IActionResult ChangePasswordAction(string currentPassword, string newPassword, string confirmPassword)
    {
        // For now, just redirect back to the Profile Page.
        return RedirectToAction("ProfilePage");
    }

    [HttpPost("EditProfileAction")]
    public IActionResult EditProfileAction(string fullname, string email, string phone)
    {
        // Redirect back to the Admin Profile page after saving.
        return RedirectToAction("ProfilePage");
    }

    [HttpGet("GetUserByUsername/{username?}")]
    public async Task<IActionResult> GetUserByUsername(string? username)
    {
        // If username is not provided in the route, use the session value
        if (string.IsNullOrEmpty(username))
        {
            username = HttpContext.Session.GetString("Username");

            if (string.IsNullOrEmpty(username))
            {
                return Unauthorized("Session expired or username not found.");
            }
        }

        // Find the user by username
        var user = await _context.Users.SingleOrDefaultAsync(u => u.UserName == username);

        if (user == null)
        {
            return NotFound("User not found.");
        }

        return Ok(user.ToUserDetailsDto());
    }

    [HttpPut("DeleteAdmin/{username?}")]
    public async Task<IActionResult> DeleteAdminByUsername(string? username)
    {
        // Retrieve username from session if not provided in the route
        if (string.IsNullOrEmpty(username))
        {
            username = HttpContext.Session.GetString("Username");
            if (string.IsNullOrEmpty(username))
            {
                return Unauthorized("Session expired or username not found.");
            }
        }

        // Find the user by username
        var user = await _context.Users.SingleOrDefaultAsync(u => u.UserName == username);
        if (user == null)
        {
            return NotFound("User not found");
        }

        // Clear session data
        HttpContext.Session.Remove("Username");
        HttpContext.Session.Remove("ResetPasswordEmail");
        HttpContext.Response.Cookies.Delete("AuthToken");

        // Change the user status to "deleted"
        user.UserStatus = UserStatus.Deleted;

        // Save the changes to the database
        await _context.SaveChangesAsync();

        // Return success response with the updated user details
        return Ok(user.ToUserDetailsDto());
    }

    [HttpPut("ChangePassword/{username?}")]
    public async Task<IActionResult> ChangePassword(string? username, [FromBody] ChangePasswordRequestDto dto)
    {
        if (string.IsNullOrWhiteSpace(dto.CurrentPassword) || string.IsNullOrWhiteSpace(dto.Password))
            return BadRequest(new { message = "Current password or new password is missing." });

        // Validate password strength
        var passwordPattern = @"^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&_])[A-Za-z\d@$!%*?&_]{8,}$";
        if (!Regex.IsMatch(dto.Password, passwordPattern))
        {
            return BadRequest(new { message = "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character." });
        }

        // If the username is not provided in the route, retrieve it from the session
        if (string.IsNullOrEmpty(username))
        {
            username = HttpContext.Session.GetString("Username");
            if (string.IsNullOrEmpty(username))
                return Unauthorized(new { message = "Session expired or username not found." });
        }

        // Find the user by username
        var user = await _userManager.FindByNameAsync(username);
        if (user == null)
            return NotFound(new { message = "User not found." });

        // Attempt to change the password
        var result = await _userManager.ChangePasswordAsync(user, dto.CurrentPassword, dto.Password);
        if (!result.Succeeded)
        {
            var errors = result.Errors.Select(e =>
                e.Code == "PasswordMismatch" ? "Incorrect current password." : e.Description // Replace error message
            );

            var errorMessages = string.Join(", ", errors);
            return BadRequest(new { message = errorMessages });
        }

        return Ok(new { message = "Password changed successfully." });
    }

    [HttpPut("UpdateAdminProfileByUsername/{username?}")]
    public async Task<IActionResult> UpdateAdminProfileByUsername(string? username, [FromBody] AdminUpdateUserProfileRequestDto dto)
    {
        if (string.IsNullOrEmpty(username))
        {
            username = HttpContext.Session.GetString("Username");
            if (string.IsNullOrEmpty(username))
                return Unauthorized(new { message = "Session expired or username not found." });
        }

        var user = await _context.Users.SingleOrDefaultAsync(u => u.UserName == username);
        if (user == null)
            return NotFound(new { message = "User not found." });

        try
        {
            if (string.IsNullOrWhiteSpace(dto.FullName) || string.IsNullOrWhiteSpace(dto.Email) || string.IsNullOrWhiteSpace(dto.PhoneNumber))
            {
                return BadRequest(new { message = "All fields are required." });
            }

            user.FullName = dto.FullName;
            user.Email = dto.Email;
            user.PhoneNumber = dto.PhoneNumber;

            if (!string.IsNullOrEmpty(dto.ProfilePicture))
            {
                user.ProfilePicture = dto.ProfilePicture;
            }

            await _context.SaveChangesAsync();
            return Ok(new { message = "Profile updated successfully.", profilePicture = user.ProfilePicture });
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error updating profile: {ex.Message}");
            return StatusCode(500, new { message = "An unexpected error occurred while updating the profile." });
        }
    }


    [HttpPost("UploadProfilePicture")]
    public async Task<IActionResult> UploadProfilePicture([FromBody] UploadProfilePictureRequestDto dto)
    {
        if (string.IsNullOrEmpty(dto.FileName) || string.IsNullOrEmpty(dto.FileContent))
            return BadRequest("Invalid file data.");

        try
        {
            var fileBytes = Convert.FromBase64String(dto.FileContent);
            var fileName = $"{Guid.NewGuid()}_{dto.FileName}";
            var filePath = Path.Combine("wwwroot/images", fileName);

            Directory.CreateDirectory(Path.GetDirectoryName(filePath));
            await System.IO.File.WriteAllBytesAsync(filePath, fileBytes);

            return Ok(new { filePath = $"/images/{fileName}" });
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Upload Error: {ex.Message}");
            return StatusCode(500, "Failed to upload profile picture.");
        }
    }




}

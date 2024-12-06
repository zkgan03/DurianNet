using DurianNet.Data;
using DurianNet.Models.DataModels;
using DurianNet.Services.TokenService;
using DurianNet.Services.UserService;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using DurianNet.Dtos.Request.User;
using DurianNet.Mappers;

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

    /*[HttpPut("UpdateUserByUsername/{username?}")]
    public async Task<IActionResult> UpdateUserByUsername(string? username, [FromForm] UpdateUserProfileRequestDto dto)
    {
        if (string.IsNullOrEmpty(username))
        {
            username = HttpContext.Session.GetString("Username");
            if (string.IsNullOrEmpty(username)) return Unauthorized("Session expired or username not found.");
        }

        var user = await _context.Users.SingleOrDefaultAsync(u => u.UserName == username);
        if (user == null) return NotFound("User not found");

        // Update fields
        user.FullName = dto.FullName;
        user.Email = dto.Email;
        user.PhoneNumber = dto.PhoneNumber;

        // Handle profile picture upload
        if (dto.ProfilePicture != null)
        {
            // Generate a unique file name
            var fileName = $"{Guid.NewGuid()}_{dto.ProfilePicture.FileName}";

            // Define the file path
            var filePath = Path.Combine("wwwroot/images", fileName);

            // Save the file
            using (var stream = new FileStream(filePath, FileMode.Create))
            {
                await dto.ProfilePicture.CopyToAsync(stream);
            }

            // Store the relative path in the database
            user.ProfilePicture = $"/uploads/{fileName}";
        }

        await _context.SaveChangesAsync();
        return Ok(user.ToUserDetailsDto());
    }*/

    /*[HttpPut("UpdateUserByUsername/{username?}")]
    public async Task<IActionResult> UpdateUserByUsername(string? username, [FromForm] UpdateUserProfileRequestDto dto)
    {
        if (string.IsNullOrEmpty(username))
        {
            username = HttpContext.Session.GetString("Username");
            if (string.IsNullOrEmpty(username)) return Unauthorized("Session expired or username not found.");
        }

        var user = await _context.Users.SingleOrDefaultAsync(u => u.UserName == username);
        if (user == null) return NotFound("User not found");

        // Update fields
        user.FullName = dto.FullName;
        user.Email = dto.Email;
        user.PhoneNumber = dto.PhoneNumber;

        // Handle profile picture upload
        if (dto.ProfilePicture != null)
        {
            var fileName = $"{Guid.NewGuid()}_{dto.ProfilePicture.FileName}";
            var filePath = Path.Combine("wwwroot/images", fileName);

            // Ensure directory exists
            Directory.CreateDirectory(Path.GetDirectoryName(filePath));

            using (var stream = new FileStream(filePath, FileMode.Create))
            {
                await dto.ProfilePicture.CopyToAsync(stream);
            }

            // Store the relative path in the database
            user.ProfilePicture = $"/images/{fileName}";
        }

        await _context.SaveChangesAsync();
        return Ok(user.ToUserDetailsDto());
    }*/

    /*[HttpPut("UpdateUserByUsername/{username?}")]
    public async Task<IActionResult> UpdateUserByUsername(string? username, [FromForm] UpdateUserProfileRequestDto dto, [FromForm] string? ExistingProfilePicture)
    {
        if (string.IsNullOrEmpty(username))
        {
            username = HttpContext.Session.GetString("Username");
            if (string.IsNullOrEmpty(username)) return Unauthorized("Session expired or username not found.");
        }

        var user = await _context.Users.SingleOrDefaultAsync(u => u.UserName == username);
        if (user == null) return NotFound("User not found");

        // Update fields
        user.FullName = dto.FullName;
        user.Email = dto.Email;
        user.PhoneNumber = dto.PhoneNumber;

        // Handle profile picture upload
        if (dto.ProfilePicture != null)
        {
            // Save the new profile picture
            var fileName = $"{Guid.NewGuid()}_{dto.ProfilePicture.FileName}";
            var filePath = Path.Combine("wwwroot/images", fileName);

            // Ensure directory exists
            Directory.CreateDirectory(Path.GetDirectoryName(filePath));

            using (var stream = new FileStream(filePath, FileMode.Create))
            {
                await dto.ProfilePicture.CopyToAsync(stream);
            }

            // Update the profile picture path
            user.ProfilePicture = $"/images/{fileName}";
        }
        else if (!string.IsNullOrEmpty(ExistingProfilePicture))
        {
            // Use the existing profile picture
            user.ProfilePicture = ExistingProfilePicture;
        }

        await _context.SaveChangesAsync();
        return Ok(user.ToUserDetailsDto());
    }*/

    /*[HttpPut("UpdateUserByUsername/{username?}")]
    public async Task<IActionResult> UpdateUserByUsername(string? username, [FromForm] UpdateUserProfileRequestDto dto, [FromForm] string? ExistingProfilePicture)
    {
        if (string.IsNullOrEmpty(username))
        {
            username = HttpContext.Session.GetString("Username");
            if (string.IsNullOrEmpty(username)) return Unauthorized("Session expired or username not found.");
        }

        Console.WriteLine($"Username: {username}");
        Console.WriteLine($"FullName: {dto.FullName}");
        Console.WriteLine($"Email: {dto.Email}");
        Console.WriteLine($"PhoneNumber: {dto.PhoneNumber}");
        Console.WriteLine($"ExistingProfilePicture: {ExistingProfilePicture}");
        Console.WriteLine($"ProfilePicture: {dto.ProfilePicture?.FileName}");

        var user = await _context.Users.SingleOrDefaultAsync(u => u.UserName == username);
        if (user == null) return NotFound("User not found");

        // Update other fields
        user.FullName = dto.FullName;
        user.Email = dto.Email;
        user.PhoneNumber = dto.PhoneNumber;

        // Handle profile picture
        if (dto.ProfilePicture != null && dto.ProfilePicture.Length > 0)
        {
            // Save new profile picture
            var fileName = $"{Guid.NewGuid()}_{dto.ProfilePicture.FileName}";
            var filePath = Path.Combine("wwwroot/images", fileName);

            Directory.CreateDirectory(Path.GetDirectoryName(filePath));

            using (var stream = new FileStream(filePath, FileMode.Create))
            {
                await dto.ProfilePicture.CopyToAsync(stream);
            }

            user.ProfilePicture = $"/images/{fileName}"; // Update with new path
        }
        else if (!string.IsNullOrEmpty(ExistingProfilePicture))
        {
            // Use existing profile picture path
            user.ProfilePicture = ExistingProfilePicture;
        }
        else
        {
            // If no profile picture provided at all, return an error or handle it
            return BadRequest("Profile picture is required.");
        }

        await _context.SaveChangesAsync();
        return Ok(user.ToUserDetailsDto());
    }*/

    [HttpPut("UpdateAdminProfileByUsername/{username?}")]
    public async Task<IActionResult> UpdateAdminProfileByUsername(string? username, [FromBody] AdminUpdateUserProfileRequestDto dto)
    {
        if (string.IsNullOrEmpty(username))
        {
            username = HttpContext.Session.GetString("Username");
            if (string.IsNullOrEmpty(username)) return Unauthorized("Session expired or username not found.");
        }

        var user = await _context.Users.SingleOrDefaultAsync(u => u.UserName == username);
        if (user == null) return NotFound("User not found");

        try
        {
            user.FullName = dto.FullName;
            user.Email = dto.Email;
            user.PhoneNumber = dto.PhoneNumber;

            if (!string.IsNullOrEmpty(dto.ProfilePicture))
            {
                user.ProfilePicture = dto.ProfilePicture;
            }

            await _context.SaveChangesAsync();
            Console.WriteLine($"Updated Profile Picture: {user.ProfilePicture}");
            return Ok(user.ToUserDetailsDto());
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Update Error: {ex.Message}");
            return StatusCode(500, "Failed to update profile.");
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

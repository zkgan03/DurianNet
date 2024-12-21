using DurianNet.Dtos.Account;
using DurianNet.Models.DataModels;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Authorization;
using DurianNet.Services.TokenService;
using DurianNet.Services.UserService;
using System.IdentityModel.Tokens.Jwt;
using DurianNet.Dtos.Request.User;
using Microsoft.AspNetCore.Identity;
using DurianNet.Services.EmailService;
using DurianNet.Data;
using System.Text.RegularExpressions;

[ApiController]
[Route("account")]
public class AccountWebController : Controller
{

    private readonly UserManager<User> _userManager;
    private readonly ITokenService _tokenService;
    private readonly SignInManager<User> _signinManager;
    private readonly IUserRepository _userRepository;
    private readonly ApplicationDBContext _context;

    public AccountWebController(UserManager<User> userManager, ITokenService tokenService, SignInManager<User> signInManager, IUserRepository userRepository, ApplicationDBContext context)
    {
        _userManager = userManager;
        _tokenService = tokenService;
        _signinManager = signInManager;
        _userRepository = userRepository;
        _context = context;
    }

    [HttpGet("LoginPage")]
    public IActionResult LoginPage()
    {
        return View("~/Views/Login/Login.cshtml"); 
    }

    [HttpGet("ForgotPassword")]
    public IActionResult ForgotPassword()
    {
        return View("~/Views/Login/ForgetPassword.cshtml"); 
    }

    [HttpGet("ResetPassword")]
    public IActionResult ResetPassword()
    {
        return View("~/Views/Login/ResetPassword.cshtml");
    }

    [Authorize(Policy = "SuperAdminPolicy")]
    [HttpGet("Registration")]
    public IActionResult Registration()
    {
        return View("~/Views/Login/Registration.cshtml");
    }

    [Authorize(Policy = "SuperAdminPolicy")]
    [HttpPost("Register")]
    public IActionResult Register(string username, string email, string password, string confirmPassword)
    {

        // Redirect back to the registration page
        return RedirectToAction("Registration");
    }

    [HttpPost("ResetPasswordAction")]
    public IActionResult ResetPasswordAction(string newPassword, string confirmPassword)
    {

        // Redirect to login page after successful password reset
        return RedirectToAction("LoginPage");
    }

    [HttpPost("ForgetPasswordAction")]
    public IActionResult ForgetPasswordAction(string email)
    {

        // Redirect to the Reset Password page.
        return RedirectToAction("ResetPassword");
    }

    [HttpPost("LoginAction")]
    public IActionResult LoginAction(string username, string password)
    {


        // Redirect to DurianProfile page after successful login.
        return RedirectToAction("DurianProfilePage", "durianprofile");
    }


    //// TODO : add admin policy
    //[Authorize(policy: "AdminPolicy")]
    //[HttpPost("loginAdmin")]
    //public async Task<IActionResult> LoginAdmin(LoginDto loginDto)
    //{
    //    if (!ModelState.IsValid)
    //        return BadRequest(new { message = "Invalid input. Please check your username and password." });

    //    var user = await _userManager
    //        .Users
    //        .FirstOrDefaultAsync(x => x.UserName.ToLower() == loginDto.Username.ToLower());

    //    if (user == null)
    //        return Unauthorized(new { message = "Invalid username!" });

    //    if (user.UserType != UserType.Admin && user.UserType != UserType.SuperAdmin)
    //        return Unauthorized(new { message = "Only admins and super admins can log in to the admin web interface." });

    //    if (user.UserStatus == UserStatus.Deleted)
    //        return Unauthorized(new { message = "This admin account is deleted." });

    //    var result = await _signinManager.CheckPasswordSignInAsync(user, loginDto.Password, false);

    //    if (!result.Succeeded)
    //        return Unauthorized(new { message = "Invalid username or password!" });


    //    // Set the session cookie with the username
    //    HttpContext.Session.SetString("Username", user.UserName);

    //    // create claims
    //    var claims = new List<Claim>
    //        {
    //            new Claim(ClaimTypes.Name, user.UserName),
    //            new Claim(ClaimTypes.NameIdentifier, user.Id),
    //            new Claim(ClaimTypes.Role, user.UserType.ToString())
    //        };

    //    // create identity
    //    var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);

    //    // create principal
    //    var principal = new ClaimsPrincipal(identity);

    //    // sign in
    //    await HttpContext.SignInAsync(
    //        CookieAuthenticationDefaults.AuthenticationScheme,
    //        principal,
    //        new AuthenticationProperties
    //        {
    //            IsPersistent = true,
    //            ExpiresUtc = DateTime.UtcNow.AddMinutes(30)
    //        });

    //    // Pass success message using TempData
    //    //TempData["SuccessMessage"] = "Successfully logged in!";
    //    //return RedirectToAction("DurianProfilePage", "durianprofile");
    //    return Ok(new { message = "Login successful!", UserName = user.UserName, Email = user.Email });
    //}


    //web api start here
    [HttpPost("loginAdmin")]
    public async Task<IActionResult> LoginAdmin(LoginDto loginDto)
    {
        if (!ModelState.IsValid)
            return BadRequest(new { message = "Invalid input. Please check your username and password." });

        var user = await _userManager.Users
            .FirstOrDefaultAsync(x => x.UserName.ToLower() == loginDto.Username.ToLower());

        if (user == null)
            return Unauthorized(new { message = "Invalid username or password!" });

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
                IsPersistent = false, // <-- Make the cookie session-based (only lives until the browser is closed)
                //ExpiresUtc = DateTime.UtcNow.AddMinutes(30) // Cookie expiration time, but only for this session
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

    [HttpPost("ForgotPassword")]
    public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordRequestDto dto)
    {
        if (string.IsNullOrEmpty(dto.Email))
            return BadRequest(new { message = "Email cannot be empty." });

        // Validate email format
        var emailPattern = @"^[^@\s]+@[^@\s]+\.[^@\s]+$";
        if (!Regex.IsMatch(dto.Email, emailPattern))
            return BadRequest(new { message = "Invalid email format." });

        // Find the user by email
        var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == dto.Email);
        if (user == null)
            return NotFound(new { message = "No user found with the provided email address." });

        // Save the email in the session
        HttpContext.Session.SetString("ResetPasswordEmail", dto.Email);

        // Determine whether the user is an admin or regular user
        bool isAdmin = user.UserType == UserType.Admin || user.UserType == UserType.SuperAdmin;

        try
        {
            // Use the EmailService to send the password recovery email
            EmailService.SendPasswordRecoveryEmail(user.Email, isAdmin);

            // Return success response
            return Ok("Password recovery email sent successfully.");
        }
        catch (Exception ex)
        {
            // Handle errors during email sending
            return StatusCode(500, new { message = $"An error occurred while sending the email: {ex.Message}" });
        }
    }

    [HttpPost("ResetPassword")]
    public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordRequestDto dto)
    {
        if (string.IsNullOrWhiteSpace(dto.NewPassword))
            return BadRequest(new { message = "New password cannot be empty." });

        // Validate password strength
        var passwordPattern = @"^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&_])[A-Za-z\d@$!%*?&_]{8,}$";
        if (!Regex.IsMatch(dto.NewPassword, passwordPattern))
        {
            return BadRequest(new { message = "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character." });
        }

        // Retrieve email from session
        var email = HttpContext.Session.GetString("ResetPasswordEmail");
        if (string.IsNullOrEmpty(email))
            return BadRequest(new { message = "No email found for password reset. Please initiate the Forgot Password process again." });

        // Find the user by email
        var user = await _userManager.FindByEmailAsync(email);
        if (user == null)
            return BadRequest(new { message = "No user found with the provided email." });

        try
        {
            // Reset the password
            user.PasswordHash = _userManager.PasswordHasher.HashPassword(user, dto.NewPassword);
            var result = await _userManager.UpdateAsync(user);

            if (!result.Succeeded)
            {
                var errors = string.Join(", ", result.Errors.Select(e => e.Description));
                Console.WriteLine($"Password reset failed: {errors}");
                return StatusCode(500, new { message = $"An error occurred while resetting the password: {errors}" });
            }

            Console.WriteLine("Password reset successful.");
            return Ok(new { message = "Password reset successfully." });
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Exception in ResetPassword: {ex.Message}");
            return StatusCode(500, new { message = "An unexpected error occurred while resetting the password." });
        }
    }

    [Authorize(Policy = "SuperAdminPolicy")]
    [HttpPost("registerAdmin")]
    public async Task<IActionResult> RegisterAdmin([FromBody] RegisterDto registerDto)
    {
        try
        {
            // Check if ModelState is valid
            if (!ModelState.IsValid)
            {
                return BadRequest(new { message = "Invalid input." });
            }

            // Validate username length
            if (registerDto.Username.Length < 5)
            {
                return BadRequest(new { message = "Username must be at least 5 characters long." });
            }

            // Validate password strength
            var passwordPattern = @"^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&_])[A-Za-z\d@$!%*?&_]{8,}$";

            if (!Regex.IsMatch(registerDto.Password, passwordPattern))
            {
                return BadRequest(new { message = "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character." });
            }

            // Check if username or email already exists
            if (await _userManager.Users.AnyAsync(u => u.UserName == registerDto.Username))
            {
                return BadRequest(new { message = "Username is already taken." });
            }
            if (await _userManager.Users.AnyAsync(u => u.Email == registerDto.Email))
            {
                return BadRequest(new { message = "Email is already in use." });
            }

            // Create user
            var appUser = new User
            {
                UserName = registerDto.Username,
                Email = registerDto.Email,
                ProfilePicture = "/images/defaultProfilePicture.jpg", // Default profile picture
                UserStatus = UserStatus.Active,
                UserType = UserType.Admin
            };

            var createdUser = await _userManager.CreateAsync(appUser, registerDto.Password);

            if (!createdUser.Succeeded)
            {
                var errors = string.Join(", ", createdUser.Errors.Select(e => e.Description));
                return StatusCode(500, new { message = errors });
            }

            // Add user to Admin role
            var roleResult = await _userManager.AddToRoleAsync(appUser, "Admin");
            if (!roleResult.Succeeded)
            {
                var errors = string.Join(", ", roleResult.Errors.Select(e => e.Description));
                return StatusCode(500, new { message = errors });
            }

            return Ok(new { message = "Registration successful." });
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error during admin registration: {ex.Message}");
            return StatusCode(500, new { message = "An unexpected error occurred during registration." });
        }
    }

    [Authorize(Policy = "AdminPolicy")]
    [HttpPost("logout")]
    public async Task<IActionResult> Logout()
    {
        //Retrieve the username from session
        var username = HttpContext.Session.GetString("Username");
        if (string.IsNullOrEmpty(username))
            return Unauthorized("User not logged in.");

        // Clear session data
        //HttpContext.Session.Remove("Username");
        //HttpContext.Session.Remove("ResetPasswordEmail");
        // Clear all session data
        HttpContext.Session.Clear();

        //Clear all old cookies
        HttpContext.Response.Cookies.Delete(".AspNetCore.Cookies");


        // Sign out and remove cookies
        //await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
        HttpContext.Response.Cookies.Delete("AuthToken");

        await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);

        return Ok("Logged out successfully.");
    }

    ////This is admin logout
    //[Authorize(policy: "AdminPolicy")]
    //[HttpPost("logout")]
    //public async Task<IActionResult> Logout()
    //{
    //    // Retrieve the username from session
    //    //var username = HttpContext.Session.GetString("Username");
    //    //if (string.IsNullOrEmpty(username))
    //    //    return Unauthorized("User not logged in.");

    //    var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);

    //    // get the user
    //    var user = await _userManager.FindByIdAsync(userId);

    //    if (user == null)
    //    {
    //        return Unauthorized("User not logged in.");
    //    }

    //    // Clear session data
    //    HttpContext.Session.Remove("Username");
    //    HttpContext.Session.Remove("ResetPasswordEmail");

    //    // Sign out and remove cookies
    //    //await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
    //    HttpContext.Response.Cookies.Delete("AuthToken");

    //    await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);

    //    return Ok("Logged out successfully.");
    //}

    //[Authorize(Policy = "AdminPolicy")]
    //[HttpGet("admin-action")]
    //public IActionResult AdminAction()
    //{
    //    return Ok("This is an admin-only action.");
    //}
}

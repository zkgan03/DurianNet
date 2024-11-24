using DurianNet.Dtos.Account;
using DurianNet.Models.DataModels;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using Microsoft.EntityFrameworkCore;

[Route("account")]
public class AccountWebController : Controller
{

    private readonly UserManager<User> _userManager;
    private readonly SignInManager<User> _signinManager;

    public AccountWebController(UserManager<User> userManager, SignInManager<User> signinManager)
    {
        _userManager = userManager;
        _signinManager = signinManager;
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

    [HttpGet("Registration")]
    public IActionResult Registration()
    {
        return View("~/Views/Login/Registration.cshtml");
    }

    [HttpPost("Register")]
    public IActionResult Register(string username, string email, string password, string confirmPassword)
    {
        // TODO: Add registration logic here (e.g., save to the database)

        // Redirect back to the registration page
        return RedirectToAction("Registration");
    }

    [HttpPost("ResetPasswordAction")]
    public IActionResult ResetPasswordAction(string newPassword, string confirmPassword)
    {
        // TODO: Add reset password logic here (e.g., validate and update the password in the database)

        // Redirect to login page after successful password reset
        return RedirectToAction("LoginPage");
    }

    [HttpPost("ForgetPasswordAction")]
    public IActionResult ForgetPasswordAction(string email)
    {
        // TODO: Add logic to validate the email and send a reset email if necessary.
        // You can also log whether the email exists or not for debugging.

        // Redirect to the Reset Password page.
        return RedirectToAction("ResetPassword");
    }

    [HttpPost("LoginAction")]
    public IActionResult LoginAction(string username, string password)
    {
        // TODO: Add logic to validate the username and password.
        // Example: Check if username and password match an entry in the database.
        // For now, assume login is successful.

        // Redirect to DurianProfile page after successful login.
        return RedirectToAction("DurianProfilePage", "durianprofile");
    }

    // TODO : add admin policy
    [HttpPost("loginAdmin")]
    public async Task<IActionResult> LoginAdmin(LoginDto loginDto)
    {
        if (!ModelState.IsValid)
            return BadRequest(new { message = "Invalid input. Please check your username and password." });

        var user = await _userManager
            .Users
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

        return RedirectToAction("DurianProfilePage", "durianprofile");
    }

}

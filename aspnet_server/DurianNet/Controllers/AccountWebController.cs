using Microsoft.AspNetCore.Mvc;

[Route("account")]
public class AccountWebController : Controller
{
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

}

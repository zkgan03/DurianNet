using Microsoft.AspNetCore.Mvc;

[Route("adminprofile")]
public class AdminProfileWebController : Controller
{
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
        // TODO: Add logic to validate the current password and update it to the new password.
        // Example: Check if the current password matches the one in the database, 
        // and ensure newPassword == confirmPassword, then save it to the database.

        // For now, just redirect back to the Profile Page.
        return RedirectToAction("ProfilePage");
    }

    [HttpPost("EditProfileAction")]
    public IActionResult EditProfileAction(string fullname, string email, string phone)
    {
        // TODO: Add logic to update the admin profile in the database.
        // Example: Validate the input and update the respective fields in the database.

        // Redirect back to the Admin Profile page after saving.
        return RedirectToAction("ProfilePage");
    }
}

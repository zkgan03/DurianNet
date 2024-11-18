using Microsoft.AspNetCore.Mvc;

[Route("useraccount")]
public class UserAccountWebController : Controller
{
    [HttpGet("UserAccountPage")]
    public IActionResult UserAccountPage()
    {
        // Load the main User Account page
        return View("~/Views/UserAccount/UserAccount.cshtml");
    }

    [HttpGet("ViewUser")]
    public IActionResult ViewUser(int id)
    {
        // TODO: Fetch user details by ID and pass it to the view
        // For now, just load the page
        return View("~/Views/UserAccount/UserAccountDetails.cshtml");
    }

    [HttpGet("DeleteUser")]
    public IActionResult DeleteUser(int id)
    {
        // TODO: Implement logic to delete the user by ID
        // Redirect to User Account page after deletion
        return RedirectToAction("UserAccountPage");
    }

    [HttpGet("RecoverUser")]
    public IActionResult RecoverUser(int id)
    {
        // TODO: Implement logic to recover the user by ID
        // Redirect to User Account page after recovery
        return RedirectToAction("UserAccountPage");
    }
}

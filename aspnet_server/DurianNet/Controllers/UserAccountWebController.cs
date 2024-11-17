using Microsoft.AspNetCore.Mvc;

[Route("useraccount")]
public class UserAccountWebController : Controller
{
    [HttpGet("UserAccountPage")]
    public IActionResult UserAccountPage()
    {
        return View("~/Views/UserAccount/UserAccount.cshtml");
    }
}

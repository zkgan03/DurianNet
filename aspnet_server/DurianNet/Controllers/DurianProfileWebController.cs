using Microsoft.AspNetCore.Mvc;

[Route("durianprofile")]
public class DurianProfileWebController : Controller
{
    [HttpGet("DurianProfilePage")]
    public IActionResult DurianProfilePage()
    {
        return View("~/Views/DurianProfile/DurianProfile.cshtml");
    }
}

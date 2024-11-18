using Microsoft.AspNetCore.Mvc;

[Route("durianprofile")]
public class DurianProfileWebController : Controller
{
    [HttpGet("DurianProfilePage")]
    public IActionResult DurianProfilePage()
    {
        // This loads the main Durian Profile page.
        return View("~/Views/DurianProfile/DurianProfile.cshtml");
    }

    [HttpGet("AddDurianProfilePage")]
    public IActionResult AddDurianProfilePage()
    {
        return View("~/Views/DurianProfile/AddDurianProfileDetails.cshtml");
    }


    [HttpGet("UpdateDurianProfileDetailsPage")]
    public IActionResult UpdateDurianProfileDetailsPage(int id)
    {
        // TODO: Fetch durian details by ID and pass it to the view for editing.
        // For now, just load the page.
        return View("~/Views/DurianProfile/UpdateDurianProfileDetails.cshtml");
    }

    [HttpGet("Delete")]
    public IActionResult Delete(int id)
    {
        // TODO: Implement delete logic for the durian by ID.
        // Redirect to the Durian Profile page after deletion.
        return RedirectToAction("DurianProfilePage");
    }
}

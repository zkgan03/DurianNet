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

    [HttpPost("SaveDurianProfile")]
    public IActionResult SaveDurianProfile(string durianName, string characteristic, string tasteProfile, string durianDescription, string videoDescription)
    {
        // TODO: Save the data to the database or perform other necessary logic.

        // Redirect to the Durian Profile page after saving.
        return RedirectToAction("DurianProfilePage");
    }

    [HttpPost("UpdateDurianProfile")]
    public IActionResult UpdateDurianProfile(int id, string durianName, string characteristic, string tasteProfile, string durianDescription, string videoDescription)
    {
        // TODO: Implement the logic to update the durian profile in the database using the provided data.

        // Redirect back to the Durian Profile page after updating.
        return RedirectToAction("DurianProfilePage");
    }


}

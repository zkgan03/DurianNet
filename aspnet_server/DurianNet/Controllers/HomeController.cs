using DurianNet.Data;
using DurianNet.Models.DataModels;
using DurianNet.Models.ViewModels;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Diagnostics;

namespace DurianNet.Controllers
{
    [ApiExplorerSettings(IgnoreApi = true)] // Exclude the whole controller from Swagger
    [Route("")]
    public class HomeController : Controller
    {
        private readonly ILogger<HomeController> _logger;
        private readonly ApplicationDBContext _context;

        public HomeController(ILogger<HomeController> logger, ApplicationDBContext context)
        {
            _logger = logger;
            _context = context;

        }

        [HttpGet("")]
        public IActionResult RedirectToLoginPage()
        {
            // Redirect to the login page
            return RedirectToAction("LoginPage", "Account");
        }

        [HttpGet("privacy")]
        public IActionResult Privacy()
        {
            return View();
        }

        [HttpGet("error")]
        [ResponseCache(Duration = 0, Location = ResponseCacheLocation.None, NoStore = true)]
        public IActionResult Error()
        {
            return View(new ErrorViewModel { RequestId = Activity.Current?.Id ?? HttpContext.TraceIdentifier });
        }
    }
}

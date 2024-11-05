using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace DurianNet.Controllers.api
{
    [Route("api/[controller]")]
    [ApiController]
    public class SellerLocatorController : ControllerBase
    {
        [HttpGet]
        public IActionResult Get()
        {
            return Ok("Hello World");
        }
    }
}

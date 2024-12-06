using DurianNet.Data;
using DurianNet.Mappers;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace DurianNet.Controllers.api
{
    [Route("api/[controller]")]
    [ApiController]
    public class DurianVideoController : ControllerBase
    {
        private readonly ApplicationDBContext _context;

        public DurianVideoController(ApplicationDBContext context)
        {
            _context = context;
        }

        /*[HttpGet("GetAllDurianVideos")]
        public async Task<IActionResult> GetAllDurianVideos()
        {
            var durianVideos = await _context.DurianVideos.ToListAsync();
            var videoDtos = durianVideos.Select(dv => dv.ToDurianVideoDto()).ToList();
            return Ok(videoDtos);
        }

        [HttpGet("GetDurianVideo/{id}")]
        public async Task<IActionResult> GetDurianVideoById(int id)
        {
            var durianVideo = await _context.DurianVideos.FindAsync(id);
            if (durianVideo == null)
            {
                return NotFound("Durian video not found");
            }
            return Ok(durianVideo.ToDurianVideoDto());
        }

        [HttpDelete("DeleteDurianVideo/{id}")]
        public async Task<IActionResult> DeleteDurianVideo(int id)
        {
            var video = await _context.DurianVideos.FindAsync(id);
            if (video == null)
            {
                return NotFound("Durian video not found");
            }

            _context.DurianVideos.Remove(video);
            await _context.SaveChangesAsync();
            return NoContent();
        }*/

    }
}

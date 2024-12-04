using DurianNet.Data;
using DurianNet.Models.DataModels;
using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace DurianNet.Services.DurianVideoService
{
    public class DurianVideoRepository : IDurianVideoRepository
    {
        private readonly ApplicationDBContext _context;

        public DurianVideoRepository(ApplicationDBContext context)
        {
            _context = context;
        }

        public async Task<List<DurianVideo>> GetAllDurianVideosAsync()
        {
            return await _context.DurianVideos.ToListAsync();
        }

        public async Task<DurianVideo?> GetDurianVideoByIdAsync(int id)
        {
            return await _context.DurianVideos.FindAsync(id);
        }

        public async Task<DurianVideo?> DeleteDurianVideoAsync(int id)
        {
            var video = await _context.DurianVideos.FindAsync(id);
            if (video != null)
            {
                _context.DurianVideos.Remove(video);
                await _context.SaveChangesAsync();
                return video;
            }
            return null;
        }
    }
}

using DurianNet.Data;
using DurianNet.Models.DataModels;
using DurianNet.Dtos.Request.DurianProfile;
using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace DurianNet.Services.DurianProfileService
{
    public class DurianProfileRepository : IDurianProfileRepository
    {
        private readonly ApplicationDBContext _context;

        public DurianProfileRepository(ApplicationDBContext context)
        {
            _context = context;
        }

        public async Task<List<DurianProfile>> GetAllDurianProfilesAsync(DurianQueryRequestDto query)
        {
            var durianProfilesQuery = _context.DurianProfiles.AsQueryable();

            // Filter by DurianName if provided
            if (!string.IsNullOrWhiteSpace(query.DurianName))
            {
                durianProfilesQuery = durianProfilesQuery.Where(dp => dp.DurianName.Contains(query.DurianName));
            }

            return await durianProfilesQuery.Include(dp => dp.DurianVideo).ToListAsync();
        }


        public async Task<DurianProfile?> GetDurianProfileByIdAsync(int id)
        {
            return await _context.DurianProfiles.Include(dp => dp.DurianVideo).FirstOrDefaultAsync(dp => dp.DurianId == id);
        }

        public async Task<DurianProfile> AddDurianProfileAsync(AddDurianProfileRequestDto dto)
        {
            var video = new DurianVideo
            {
                // Map properties from dto
            };
            _context.DurianVideos.Add(video);
            await _context.SaveChangesAsync();

            var profile = new DurianProfile
            {
                // Map properties from dto and link video
            };
            _context.DurianProfiles.Add(profile);
            await _context.SaveChangesAsync();

            return profile;
        }

        public async Task<DurianProfile?> UpdateDurianProfileAsync(int id, UpdateDurianProfileRequestDto dto)
        {
            var profile = await _context.DurianProfiles.FindAsync(id);
            if (profile != null)
            {
                // Update properties based on dto
                await _context.SaveChangesAsync();
            }
            return profile;
        }

        public async Task<bool> DeleteDurianProfileAsync(int id)
        {
            var profile = await _context.DurianProfiles.FindAsync(id);
            if (profile != null)
            {
                _context.DurianProfiles.Remove(profile);
                await _context.SaveChangesAsync();
                return true;
            }
            return false;
        }

        public async Task<DurianProfile?> GetBySymbolAsync(string symbol)
        {
            return await _context.DurianProfiles.FirstOrDefaultAsync(s => s.DurianName == symbol);
        }
    }
}

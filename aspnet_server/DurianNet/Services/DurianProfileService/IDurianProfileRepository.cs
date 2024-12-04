using DurianNet.Models.DataModels;
using DurianNet.Dtos.Request.DurianProfile;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace DurianNet.Services.DurianProfileService
{
    public interface IDurianProfileRepository
    {
        Task<List<DurianProfile>> GetAllDurianProfilesAsync(DurianQueryRequestDto query);
        Task<DurianProfile?> GetDurianProfileByIdAsync(int id);
        Task<DurianProfile?> GetBySymbolAsync(string symbol);
        Task<DurianProfile> AddDurianProfileAsync(AddDurianProfileRequestDto dto);
        Task<DurianProfile?> UpdateDurianProfileAsync(int id, UpdateDurianProfileRequestDto dto);
        Task<bool> DeleteDurianProfileAsync(int id);
    }
}

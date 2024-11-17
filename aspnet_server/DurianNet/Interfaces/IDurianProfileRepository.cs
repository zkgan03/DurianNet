using DurianNet.Models.DataModels;
using DurianNet.Dtos.Request.DurianProfile;
using System.Collections.Generic;
using System.Threading.Tasks;
using DurianNet.Helpers;

namespace DurianNet.Interfaces
{
    public interface IDurianProfileRepository
    {
        Task<List<DurianProfile>> GetAllDurianProfilesAsync(DurianQueryObject query);
        Task<DurianProfile?> GetDurianProfileByIdAsync(int id);
        Task<DurianProfile?> GetBySymbolAsync(string symbol);
        Task<DurianProfile> AddDurianProfileAsync(AddDurianProfileRequestDto dto);
        Task<DurianProfile?> UpdateDurianProfileAsync(int id, UpdateDurianProfileRequestDto dto);
        Task<bool> DeleteDurianProfileAsync(int id);
    }
}

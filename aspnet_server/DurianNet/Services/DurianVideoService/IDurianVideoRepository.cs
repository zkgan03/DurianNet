using DurianNet.Models.DataModels;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace DurianNet.Services.DurianVideoService
{
    public interface IDurianVideoRepository
    {
        Task<List<DurianVideo>> GetAllDurianVideosAsync();
        Task<DurianVideo?> GetDurianVideoByIdAsync(int id);
        Task<DurianVideo?> DeleteDurianVideoAsync(int id);
    }
}

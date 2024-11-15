using DurianNet.Models.DataModels;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace DurianNet.Interfaces
{
    public interface IDurianVideoRepository
    {
        Task<List<DurianVideo>> GetAllDurianVideosAsync();
        Task<DurianVideo?> GetDurianVideoByIdAsync(int id);
        Task<DurianVideo?> DeleteDurianVideoAsync(int id);
    }
}

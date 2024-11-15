using DurianNet.Models.DataModels;
using DurianNet.Dtos.Request.User;
using System.Collections.Generic;
using System.Threading.Tasks;
using DurianNet.Helpers;

namespace DurianNet.Interfaces
{
    public interface IUserRepository
    {
        Task<List<User>> GetAllUsersAsync(QueryObject query);
        Task<User?> GetUserByIdAsync(string id);
        Task<User?> RegisterUserAsync(RegisterRequestDto dto);
        Task<User?> UpdateUserAsync(string id, UpdateUserProfileRequestDto dto);
        Task<User?> DeleteUserAsync(string id);
        Task<bool> UserExistsAsync(string id);
    }
}

using DurianNet.Data;
using DurianNet.Models.DataModels;
using DurianNet.Dtos.Request.User;
using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;
using System.Threading.Tasks;
using DurianNet.Helpers;

namespace DurianNet.Services.UserService
{
    public class UserRepository : IUserRepository
    {
        private readonly ApplicationDBContext _context;

        public UserRepository(ApplicationDBContext context)
        {
            _context = context;
        }

        public async Task<List<User>> GetAllUsersAsync(QueryObject query)
        {
            var users = _context.Users.AsQueryable();

            if (!string.IsNullOrWhiteSpace(query.Username))
            {
                users = users.Where(u => u.UserName.Contains(query.Username));
            }

            return await users.ToListAsync();
        }


        public async Task<User?> GetUserByIdAsync(string id)
        {
            return await _context.Users.FindAsync(id);
        }

        public async Task<User?> RegisterUserAsync(RegisterRequestDto dto)
        {
            var user = new User
            {
                UserName = dto.Username,
                Email = dto.Email,
                // Add other properties here as necessary
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            return user;
        }

        public async Task<User?> UpdateUserAsync(string id, UpdateUserProfileRequestDto dto)
        {
            var user = await _context.Users.FindAsync(id);
            if (user != null)
            {
                // Update user properties here
                user.FullName = dto.FullName;
                user.Email = dto.Email;
                // Update other properties if needed
                await _context.SaveChangesAsync();
            }
            return user;
        }

        public async Task<User?> DeleteUserAsync(string id)
        {
            var user = await _context.Users.FindAsync(id);
            if (user != null)
            {
                _context.Users.Remove(user);
                await _context.SaveChangesAsync();
            }
            return user;
        }

        public async Task<bool> UserExistsAsync(string id)
        {
            return await _context.Users.AnyAsync(u => u.Id == id);
        }
    }
}

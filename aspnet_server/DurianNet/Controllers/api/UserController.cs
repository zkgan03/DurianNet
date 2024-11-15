using DurianNet.Data;
using DurianNet.Dtos.Request.User;
using DurianNet.Helpers;
using DurianNet.Interfaces;
using DurianNet.Mappers;
using DurianNet.Models.DataModels;
using DurianNet.Services.EmailService;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace DurianNet.Controllers.api
{
    [Route("api/[controller]")]
    [ApiController]
    public class UserController : ControllerBase
    {
        private readonly ApplicationDBContext _context;

        public UserController(ApplicationDBContext context)
        {
            _context = context;
        }

        [HttpGet("GetEverythingFromUsers")]
        public async Task<IActionResult> GetEverythingFromUsers()
        {
            var users = await _context.Users.ToListAsync();  // Retrieve all users
            if (users == null || !users.Any())
            {
                return NotFound("No users found.");
            }

            // Map the users to a list of UserDetailsDto
            var userDtos = users.Select(user => user.UserDetailsDto()).ToList();

            return Ok(userDtos);
        }

        [HttpGet("GetAllUsers")]
        public async Task<IActionResult> GetAllUsers([FromQuery] QueryObject query)
        {
            // Check if query is null
            if (query == null)
            {
                return BadRequest("Query parameters are missing.");
            }

            // Start with a queryable collection of users
            var usersQuery = _context.Users.AsQueryable();

            // Filter by username if provided in the query parameters
            if (!string.IsNullOrWhiteSpace(query.Username))
            {
                usersQuery = usersQuery.Where(u => u.UserName.Contains(query.Username));
            }

            // Execute the query and get the result
            var users = await usersQuery.ToListAsync();

            // If no users found, return a NotFound response
            if (users == null || !users.Any())
            {
                return NotFound("No users found.");
            }

            // Map the users to DTOs (assuming you have an extension method to do this)
            var userDtos = users.Select(u => u.ToUserListDto()).ToList();

            // Return the list of user DTOs
            return Ok(userDtos);
        }



        [HttpGet("GetUser/{id}")]
        public async Task<IActionResult> GetUserById(string id)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null)
            {
                return NotFound("User not found");
            }
            return Ok(user.ToUserDetailsDto());
        }

        [HttpPost("Register")]
        public async Task<IActionResult> Register([FromBody] RegisterRequestDto dto)
        {
            var user = dto.ToUserFromRegisterRequest();
            _context.Users.Add(user);
            await _context.SaveChangesAsync();
            return Ok(user.ToUserDetailsDto());
        }

        [HttpPut("UpdateUser/{id}")]
        public async Task<IActionResult> UpdateUser(string id, [FromBody] UpdateUserProfileRequestDto dto)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null)
            {
                return NotFound("User not found");
            }

            user.UpdateUserFromDto(dto);
            await _context.SaveChangesAsync();
            return Ok(user.ToUserDetailsDto());
        }

        [HttpPost("ForgotPassword")]
        public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordRequestDto dto)
        {
            // Validate the input
            if (string.IsNullOrEmpty(dto.Email))
            {
                return BadRequest("Email cannot be empty.");
            }

            // Check if the email exists in the database
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == dto.Email);
            if (user == null)
            {
                return NotFound("No user found with the provided email address.");
            }

            // Determine whether the user is an admin or regular user
            bool isAdmin = user.UserType == UserType.Admin || user.UserType == UserType.SuperAdmin;

            try
            {
                // Use the EmailService to send the password recovery email
                EmailService.SendPasswordRecoveryEmail(user.Email, isAdmin);

                // Return success response
                return Ok("Password recovery email sent successfully.");
            }
            catch (Exception ex)
            {
                // Handle errors during email sending
                return StatusCode(500, $"An error occurred while sending the email: {ex.Message}");
            }
        }



        [HttpPut("SaveFavoriteDurians/{userId}")]
        public async Task<IActionResult> SaveFavoriteDurians(string userId, [FromBody] FavoriteDurianRequestDto dto)
        {
            var user = await _context.Users.Include(u => u.FavoriteDurian).FirstOrDefaultAsync(u => u.Id == userId);
            if (user == null)
            {
                return NotFound("User not found");
            }

            // Validate the durian IDs
            var durians = await _context.DurianProfiles.Where(dp => dto.DurianIds.Contains(dp.DurianId)).ToListAsync();
            if (durians.Count != dto.DurianIds.Count)
            {
                return BadRequest("Some durian IDs are invalid.");
            }

            // Initialize or clear the FavoriteDurian collection
            user.FavoriteDurian ??= new List<DurianProfile>();
            user.FavoriteDurian.Clear();

            // Add the new favorites
            foreach (var durian in durians)
            {
                user.FavoriteDurian.Add(durian);
            }

            await _context.SaveChangesAsync();
            return Ok("Favorite durians updated successfully");
        }



        [HttpPost("RegisterAdmin")]
        public async Task<IActionResult> RegisterAdmin([FromBody] RegisterRequestDto dto)
        {
            var admin = dto.ToUserFromRegisterRequest();
            admin.UserType = UserType.Admin;

            // Hash the password
            var passwordHasher = new PasswordHasher<User>();
            admin.PasswordHash = passwordHasher.HashPassword(admin, dto.Password);

            _context.Users.Add(admin);
            await _context.SaveChangesAsync();

            return Ok(admin.ToUserDetailsDto());
        }

        [HttpPut("DeleteUser/{id}")]
        public async Task<IActionResult> DeleteUser(string id)
        {
            // Find the user by ID
            var user = await _context.Users.FindAsync(id);
            if (user == null)
            {
                return NotFound("User not found");
            }

            // Change the user status to "deleted"
            user.UserStatus = UserStatus.Deleted;  // Assuming you have an enum or status field for user status

            // Save the changes to the database
            await _context.SaveChangesAsync();

            // Return success response with updated user status
            return Ok(user.ToUserDetailsDto());
        }

        [HttpPut("RecoverUser/{id}")]
        public async Task<IActionResult> RecoverUser(string id)
        {
            // Find the user by ID
            var user = await _context.Users.FindAsync(id);
            if (user == null)
            {
                return NotFound("User not found");
            }

            // Change the user status to "Active"
            if (user.UserStatus == UserStatus.Deleted)
            {
                user.UserStatus = UserStatus.Active;  // Change status to Active
            }
            else
            {
                return BadRequest("User status is already active or in an invalid state for recovery.");
            }

            // Save the changes to the database
            await _context.SaveChangesAsync();

            // Return success response with updated user status
            return Ok(user.ToUserDetailsDto());
        }




    }
}

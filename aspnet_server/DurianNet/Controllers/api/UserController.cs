using DurianNet.Data;
using DurianNet.Dtos.Request.User;
using DurianNet.Helpers;
using DurianNet.Interfaces;
using DurianNet.Mappers;
using DurianNet.Models.DataModels;
using DurianNet.Services.EmailService;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;

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
            if (query == null)
            {
                return BadRequest("Query parameters are missing.");
            }

            Console.WriteLine($"Query.Username: {query.Username}"); // Debugging

            var usersQuery = _context.Users.AsQueryable();

            if (!string.IsNullOrWhiteSpace(query.Username))
            {
                usersQuery = usersQuery.Where(u => u.UserName.ToLower().Contains(query.Username.ToLower())); // Case-insensitive
            }

            var users = await usersQuery.ToListAsync();

            if (users == null || !users.Any())
            {
                return NotFound("No users found.");
            }

            try
            {
                var userDtos = users.Select(u => u.ToUserListDto()).ToList();
                return Ok(userDtos);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error in DTO mapping: {ex.Message}"); // Debugging
                return StatusCode(500, "An error occurred while processing the data.");
            }
        }

        //GetUser
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


        [HttpGet("GetUserByUsername/{username?}")]
        public async Task<IActionResult> GetUserByUsername(string? username)
        {
            // If username is not provided in the route, use the session value
            if (string.IsNullOrEmpty(username))
            {
                username = HttpContext.Session.GetString("Username");

                if (string.IsNullOrEmpty(username))
                {
                    return Unauthorized("Session expired or username not found.");
                }
            }

            // Find the user by username
            var user = await _context.Users.SingleOrDefaultAsync(u => u.UserName == username);

            if (user == null)
            {
                return NotFound("User not found.");
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

        //UpdateUserByUsername
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

        
        [HttpPut("UpdateUserByUsername/{username?}")]
        public async Task<IActionResult> UpdateUserByUsername(string? username, [FromBody] UpdateUserProfileRequestDto dto)
        {
            if (string.IsNullOrEmpty(username))
            {
                username = HttpContext.Session.GetString("Username");
                if (string.IsNullOrEmpty(username)) return Unauthorized("Session expired or username not found.");
            }

            var user = await _context.Users.SingleOrDefaultAsync(u => u.UserName == username);
            if (user == null) return NotFound("User not found");

            // Update fields
            user.FullName = dto.FullName;
            user.Email = dto.Email;
            user.PhoneNumber = dto.PhoneNumber;

            // Update profile picture
            if (!string.IsNullOrEmpty(dto.ProfilePicture))
            {
                user.ProfilePicture = dto.ProfilePicture; // Save Base64 string or store it in a file
            }

            await _context.SaveChangesAsync();
            return Ok(user.ToUserDetailsDto());
        }

        [HttpPost("ForgotPassword")]
        public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordRequestDto dto)
        {
            if (string.IsNullOrEmpty(dto.Email))
                return BadRequest("Email cannot be empty.");

            // Find the user by email
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == dto.Email);
            if (user == null)
                return NotFound("No user found with the provided email address.");

            // Save the email in the session
            HttpContext.Session.SetString("ResetPasswordEmail", dto.Email);

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

        
        
        [HttpGet("GetSessionEmail")]
        public IActionResult GetSessionEmail()
        {
            var email = HttpContext.Session.GetString("ResetPasswordEmail");
            if (string.IsNullOrEmpty(email))
                return NotFound("No email found in session.");

            return Ok($"Email in session: {email}");
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

using DurianNet.Data;
using DurianNet.Dtos.Request.User;
using DurianNet.Models.DataModels;
using DurianNet.Services.TokenService;
using DurianNet.Services.UserService;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using DurianNet.Mappers;
using Microsoft.AspNetCore.Authorization;

[Authorize(Policy = "AdminPolicy")]
[ApiController]
[Route("useraccount")]
public class UserAccountWebController : Controller
{
    private readonly UserManager<User> _userManager;
    private readonly ITokenService _tokenService;
    private readonly SignInManager<User> _signinManager;
    private readonly IUserRepository _userRepository;
    private readonly ApplicationDBContext _context;

    public UserAccountWebController(UserManager<User> userManager, ITokenService tokenService, SignInManager<User> signInManager, IUserRepository userRepository, ApplicationDBContext context)
    {
        _userManager = userManager;
        _tokenService = tokenService;
        _signinManager = signInManager;
        _userRepository = userRepository;
        _context = context;
    }


    [HttpGet("UserAccountPage")]
    public IActionResult UserAccountPage()
    {
        // Load the main User Account page
        return View("~/Views/UserAccount/UserAccount.cshtml");
    }

    [HttpGet("ViewUser")]
    public IActionResult ViewUser(string id) // Changed id to string
    {
        if (string.IsNullOrEmpty(id))
        {
            return BadRequest("User ID is missing.");
        }
        ViewData["UserId"] = id; // Pass the ID to the view if needed
        return View("~/Views/UserAccount/UserAccountDetails.cshtml");
    }


    [HttpGet("DeleteUser")]
    public IActionResult DeleteUser(int id)
    {
        // Redirect to User Account page after deletion
        return RedirectToAction("UserAccountPage");
    }

    [HttpGet("RecoverUser")]
    public IActionResult RecoverUser(int id)
    {
        // Redirect to User Account page after recovery
        return RedirectToAction("UserAccountPage");
    }

    //search username
    [HttpGet("GetAllUsers")]
    public async Task<IActionResult> GetAllUsers([FromQuery] QueryObject query)
    {
        if (query == null)
        {
            return BadRequest("Query parameters are missing.");
        }

        Console.WriteLine($"Query.Username: {query.Username}"); // Debugging

        //var usersQuery = _context.Users.AsQueryable();
        // Initialize the query with the condition UserType = User
        var usersQuery = _context.Users.Where(u => u.UserType == UserType.User);

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

    [HttpGet("GetEverythingFromUsers")]
    public async Task<IActionResult> GetEverythingFromUsers()
    {
        //var users = await _context.Users.ToListAsync();  // Retrieve all users
        // Retrieve all users with UserType = User
        var users = await _context.Users
                                  .Where(user => user.UserType == UserType.User)
                                  .ToListAsync();

        if (users == null || !users.Any())
        {
            return NotFound("No users found.");
        }

        // Map the users to a list of UserDetailsDto
        var userDtos = users.Select(user => user.UserDetailsDto()).ToList();

        return Ok(userDtos);
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

    /*[HttpGet("GetUser/{id}")]
    public async Task<IActionResult> GetUserById(string id)
    {

        var user = await _context.Users.FindAsync(id);
        if (user == null)
        {
            return NotFound("User not found");
        }
        return Ok(user.ToUserDetailsDto());
    }*/

    [HttpGet("GetUser/{id}")]
    public async Task<IActionResult> GetUserById(string id)
    {
        if (string.IsNullOrEmpty(id))
        {
            return BadRequest("User ID is required.");
        }

        var user = await _context.Users.FindAsync(id);
        if (user == null)
        {
            return NotFound("User not found");
        }

        return Ok(user.ToUserDetailsDto());
    }



}

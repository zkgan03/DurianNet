using DurianNet.Dtos.Account;
using DurianNet.Dtos.Request.User;
using DurianNet.Models.DataModels;
using DurianNet.Services.TokenService;
using DurianNet.Services.UserService;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text.RegularExpressions;

namespace DurianNet.Controllers.api
{
    [Route("api/account")]
    [ApiController]
    public class AccountController : ControllerBase
    {
        private readonly UserManager<User> _userManager;
        private readonly ITokenService _tokenService;
        private readonly SignInManager<User> _signinManager;
        private readonly IUserRepository _userRepository;

        public AccountController(UserManager<User> userManager, ITokenService tokenService, SignInManager<User> signInManager, IUserRepository userRepository)
        {
            _userManager = userManager;
            _tokenService = tokenService;
            _signinManager = signInManager;
            _userRepository = userRepository;
        }

        [HttpPost("refresh-token")]
        public async Task<IActionResult> RefreshToken([FromBody] RefreshTokenRequestDto request)
        {
            var user = await _userManager.Users
                .Include(u => u.RefreshTokens)
                .FirstOrDefaultAsync(u => u.RefreshTokens.Any(rt => rt.Token == request.RefreshToken));

            if (user == null)
                return Unauthorized("Invalid refresh token");

            var refreshToken = user.RefreshTokens.Single(rt => rt.Token == request.RefreshToken);
            if (refreshToken.Expiration < DateTime.UtcNow || refreshToken.IsRevoked)
                return Unauthorized("Expired or revoked refresh token");

            refreshToken.IsRevoked = true;
            var newRefreshToken = _tokenService.GenerateRefreshToken();
            user.RefreshTokens.Add(newRefreshToken);
            await _userManager.UpdateAsync(user);

            var newAccessToken = _tokenService.CreateToken(user);

            return Ok(new { AccessToken = newAccessToken, RefreshToken = newRefreshToken.Token });
        }



        






    }
}


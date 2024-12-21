using DurianNet.Models.DataModels;

namespace DurianNet.Services.TokenService
{
    public interface ITokenService
    {
        string CreateToken(User user);
        RefreshToken GenerateRefreshToken();
        Task RevokeRefreshToken(User user, string refreshToken);

        string GenerateAccessToken(User user);
    }
}

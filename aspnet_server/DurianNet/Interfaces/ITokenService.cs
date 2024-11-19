using DurianNet.Models.DataModels;

namespace DurianNet.Interfaces
{
    public interface ITokenService
    {
        string CreateToken(User user);
        RefreshToken GenerateRefreshToken();
        Task RevokeRefreshToken(User user, string refreshToken);
    }
}

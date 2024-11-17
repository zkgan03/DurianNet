using DurianNet.Models.DataModels;

namespace DurianNet.Interfaces
{
    public interface ITokenService
    {
        string CreateToken(User user);
    }
}

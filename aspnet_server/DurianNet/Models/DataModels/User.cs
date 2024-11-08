using Microsoft.AspNetCore.Identity;
using System.ComponentModel.DataAnnotations;

namespace DurianNet.Models.DataModels
{
    public class User : IdentityUser
    {
        public string ProfilePicture { get; set; }
        public ICollection<DurianProfile> FavoriteDurian { get; set; }
    }
}

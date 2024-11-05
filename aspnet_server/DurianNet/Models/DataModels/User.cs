using Microsoft.AspNetCore.Identity;
using System.ComponentModel.DataAnnotations;

namespace DurianNet.Models.DataModels
{
    public class User : IdentityUser
    {

        public ICollection<DurianProfile> favoriteDurian { get; set; }
    }
}

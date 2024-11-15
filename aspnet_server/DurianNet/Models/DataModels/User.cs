using Microsoft.AspNetCore.Identity;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace DurianNet.Models.DataModels
{
    public class User : IdentityUser
    {
        [Required]
        public string FullName { get; set; } // User's full name

        public string ProfilePicture { get; set; } // Profile picture URL or path

        [Required]
        public UserType UserType { get; set; } // Enum for user types (User, Admin, SuperAdmin)

        [Required]
        public UserStatus UserStatus { get; set; } // Enum for user status (Active, Deleted)

        // Navigation properties if needed
        public ICollection<DurianProfile> FavoriteDurian { get; set; }
    }

    public enum UserType
    {
        User,
        Admin,
        SuperAdmin
    }

    public enum UserStatus
    {
        Active,
        Deleted
    }
}


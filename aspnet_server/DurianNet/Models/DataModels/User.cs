using Microsoft.AspNetCore.Identity;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace DurianNet.Models.DataModels
{
    [Table("User")]
    public class User : IdentityUser
    {
        public string FullName { get; set; } = string.Empty;// User's full name

        public string ProfilePicture { get; set; } // Profile picture URL or path

        [Required]
        public UserType UserType { get; set; } // Enum for user types (User, Admin, SuperAdmin)

        [Required]
        public UserStatus UserStatus { get; set; } // Enum for user status (Active, Deleted)

        // Navigation properties if needed
        public ICollection<DurianProfile> FavoriteDurian { get; set; }

        public List<FavoriteDurian> FavoriteDurians { get; set; } = new List<FavoriteDurian>();

        public ICollection<RefreshToken> RefreshTokens { get; set; } = new List<RefreshToken>();

        //no use
        public string? PasswordResetToken { get; set; } // Make it nullable

        //no use
        public DateTime? ResetTokenExpires { get; set; }

        public string? OTP { get; set; }
        public DateTime? OTPExpiry { get; set; }

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


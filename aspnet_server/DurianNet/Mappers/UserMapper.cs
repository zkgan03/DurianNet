using DurianNet.Dtos.Request.User;
using DurianNet.Dtos.Response.User;
using DurianNet.Models.DataModels;

namespace DurianNet.Mappers
{
    public static class UserMapper
    {
        public static UserDetailsResponseDto ToUserDetailsDto(this User user)
        {
            return new UserDetailsResponseDto
            {
                Username = user.UserName,
                FullName = user.FullName,
                Email = user.Email,
                PhoneNumber = user.PhoneNumber,
                ProfilePicture = user.ProfilePicture,
                UserStatus = user.UserStatus.ToString()
            };
        }

        public static UserListResponseDto ToUserListDto(this User user)
        {
            return new UserListResponseDto
            {
                UserId = user.Id,
                Username = user.UserName,
                Email = user.Email,
                PhoneNumber = user.PhoneNumber,
                UserStatus = user.UserStatus.ToString()
            };
        }

        public static User ToUserFromRegisterRequest(this RegisterRequestDto dto)
        {
            return new User
            {
                UserName = dto.Username,
                Email = dto.Email,
                FullName = string.Empty,
                PhoneNumber = string.Empty,
                ProfilePicture = string.Empty,
                UserType = UserType.User,
                UserStatus = UserStatus.Active,
                PasswordHash = dto.Password // Password hashing should be done in the service layer
            };
        }

        //admin no use
        /*public static void UpdateUserFromDto(this User user, UpdateUserProfileRequestDto dto)
        {
            user.FullName = dto.FullName;
            user.Email = dto.Email;
            user.PhoneNumber = dto.PhoneNumber;
            user.ProfilePicture = dto.ProfilePicture;
        }*/

        public static UserDetailsDto UserDetailsDto(this User user)
        {
            return new UserDetailsDto
            {
                Id = user.Id,
                FullName = user.FullName,  // Assuming the FullName is a field in the User model
                Email = user.Email,
                Username = user.UserName,
                PhoneNumber = user.PhoneNumber,
                Password = user.PasswordHash,  // Optional: Avoid exposing password in response
                UserType = user.UserType.ToString(),  // Assuming UserType is an Enum
                Status = user.UserStatus.ToString(),  // Assuming Status is an Enum
                ProfilePicture = user.ProfilePicture // Assuming ProfilePicture is a field in the User model
            };
        }


    }
}

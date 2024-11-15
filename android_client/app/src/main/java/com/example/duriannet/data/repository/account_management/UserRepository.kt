package com.example.duriannet.data.repository.account_management

import com.example.duriannet.models.User
import com.example.duriannet.models.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {

    //login
    suspend fun login(username: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            // Implement your login logic here
            // For example, make a network request to authenticate the user
            // Return Result.success(user) if successful, or Result.failure(exception) if failed
        }
    }

    //signUp
    suspend fun register(username: String, email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            // Implement your registration logic here
            // For example, make a network request to register the user
            // Return Result.success(user) if successful, or Result.failure(exception) if failed
        }
    }

    //frogetPassword
    suspend fun sendResetEmail(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            // Implement your email sending logic here
            // For example, make a network request to send the reset email
            // Return Result.success(Unit) if successful, or Result.failure(exception) if failed
        }
    }

    //resetPassword
    suspend fun resetPassword(newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            // Implement your password reset logic here
            // For example, make a network request to reset the password
            // Return Result.success(Unit) if successful, or Result.failure(exception) if failed
        }
    }

    //Profile
    suspend fun getProfile(): Result<Profile> {
        return withContext(Dispatchers.IO) {
            // Implement your profile fetching logic here
            // For example, make a network request to fetch the profile
            // Return Result.success(profile) if successful, or Result.failure(exception) if failed
        }
    }

    //editProfile
    suspend fun updateProfile(fullname: String, email: String, phoneNumber: String): Result<Unit> {
        // Implement the logic to update the profile without the username
    }

    //changePassword
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        // Implement the logic to change the password using currentPassword and newPassword
        // Return a Result object indicating success or failure
    }

    //editProfileFavoriteDurian
    suspend fun getAllDurians(): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            // Implement your logic to fetch all durians here
            // For example, make a network request to fetch the durians
            // Return Result.success(durians) if successful, or Result.failure(exception) if failed
        }
    }
}
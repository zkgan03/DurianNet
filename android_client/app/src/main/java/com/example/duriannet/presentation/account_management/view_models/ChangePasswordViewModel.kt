package com.example.duriannet.presentation.account_management.view_models

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.presentation.account_management.state.ChangePasswordState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sharedPreferences: SharedPreferences // Inject shared preferences
) : ViewModel() {

    private val _changePasswordState = MutableStateFlow(ChangePasswordState())
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState

    private val _serverMessage = MutableStateFlow<String?>(null)
    val serverMessage: StateFlow<String?> = _serverMessage

    fun postMessage(message: String) {
        _serverMessage.value = message
    }

    fun clearMessage() {
        _serverMessage.value = null
    }

    /*fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordState(loading = true)

            val username = sharedPreferences.getString("username", null)
            if (username.isNullOrEmpty()) {
                _changePasswordState.value = ChangePasswordState(error = "User not logged in")
                postMessage("User not logged in")
                return@launch
            }
            val result = userRepository.changePassword(username, currentPassword, newPassword)

            if (result.isSuccess) {
                _changePasswordState.value = ChangePasswordState(isPasswordChanged = true)
                postMessage("Password changed successfully")
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Failed to change password"
                _changePasswordState.value = ChangePasswordState(error = errorMessage)
                postMessage(errorMessage)
            }
        }
    }*/

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordState(loading = true)

            val username = sharedPreferences.getString("username", null)
            if (username.isNullOrEmpty()) {
                _changePasswordState.value = ChangePasswordState(error = "User not logged in")
                postMessage("User not logged in")
                return@launch
            }
            val result = userRepository.changePassword(username, currentPassword, newPassword)

            if (result.isSuccess) {
                _changePasswordState.value = ChangePasswordState(isPasswordChanged = true)
                postMessage("Password changed successfully")
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Failed to change password"
                _changePasswordState.value = ChangePasswordState(error = errorMessage)
                postMessage(errorMessage)
            }

            // Clear the message after setting it
            clearMessage()
        }
    }

}


/*
package com.example.duriannet.presentation.account_management.view_models

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.presentation.account_management.state.ChangePasswordState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sharedPreferences: SharedPreferences // Inject shared preferences
) : ViewModel() {

    private val _changePasswordState = MutableStateFlow(ChangePasswordState())
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState

    */
/*fun getUsername(): String {
        return sharedPreferences.getString("username", "") ?: ""
    }*//*


    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            // Validate new password and confirmation
            if (newPassword != confirmPassword) {
                _changePasswordState.value = ChangePasswordState(error = "Passwords do not match")
                return@launch
            }

            // Retrieve username from shared preferences
            val username = sharedPreferences.getString("username", null)
            if (username.isNullOrEmpty()) {
                _changePasswordState.value = ChangePasswordState(error = "User not logged in")
                return@launch
            }

            // Call repository method to change password
            val result = userRepository.changePassword(username, currentPassword, newPassword)
            if (result.isSuccess) {
                _changePasswordState.value = ChangePasswordState(isPasswordChanged = true)
            } else {
                _changePasswordState.value = ChangePasswordState(error = result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}
*/

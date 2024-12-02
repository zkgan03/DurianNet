package com.example.duriannet.presentation.account_management.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.presentation.account_management.state.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState

    private val _serverMessage = MutableStateFlow<String?>(null)
    val serverMessage: StateFlow<String?> = _serverMessage

    fun postMessage(message: String) {
        _serverMessage.value = message
    }

    fun clearMessage() {
        _serverMessage.value = null
    }

    fun clearLoginError() {
        _loginState.value = _loginState.value.copy(error = "")
    }


    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val result = userRepository.login(username, password)
                if (result.isSuccess) {
                    val token = result.getOrNull()?.token.orEmpty()
                    _loginState.value = LoginState(isLoggedIn = true, username = username, token = token)
                    postMessage("Login successful")
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e("LoginError", error)
                    _loginState.value = LoginState(error = error)
                    postMessage(error)
                }
            } catch (e: Exception) {
                Log.e("LoginError", "Login failed", e)
                _loginState.value = LoginState(error = e.message ?: "Unknown error")
                postMessage(e.message ?: "Unknown error occurred")
            }
        }
    }
}


/*
package com.example.duriannet.presentation.account_management.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.presentation.account_management.state.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState

    fun login(username: String, password: String) {

        viewModelScope.launch {
            try {
                val result = userRepository.login(username, password)
                if (result.isSuccess) {
                    _loginState.value = LoginState(isLoggedIn = true, username = username)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e("LoginError", error) // Log the error message
                    _loginState.value = LoginState(error = error)
                }
            } catch (e: Exception) {
                Log.e("LoginError", "Login failed", e) // Log the exception
                _loginState.value = LoginState(error = e.message ?: "Unknown error")
            }
        }

    }
}
*/

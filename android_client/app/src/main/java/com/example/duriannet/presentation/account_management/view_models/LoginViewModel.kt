package com.example.duriannet.presentation.account_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.presentation.account_management.state.LoginState
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.utils.Event
import com.example.duriannet.utils.EventBus.sendEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.login(username, password)
            result.onSuccess {
                _loginState.value = LoginState(isLoggedIn = true)
            }.onFailure {
                _loginState.value = LoginState(error = it.message ?: "Login failed")
            }
        }
    }
}
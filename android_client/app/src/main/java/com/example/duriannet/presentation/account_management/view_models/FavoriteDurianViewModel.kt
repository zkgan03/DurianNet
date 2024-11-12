package com.example.duriannet.presentation.account_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.presentation.account_management.state.FavoriteDurianState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteDurianViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _favoriteDurianState = MutableStateFlow(FavoriteDurianState())
    val favoriteDurianState: StateFlow<FavoriteDurianState> = _favoriteDurianState

    fun loadAllDurians() {
        viewModelScope.launch {
            val result = userRepository.getAllDurians()
            if (result.isSuccess) {
                _favoriteDurianState.value = FavoriteDurianState(favoriteDurians = result.getOrNull() ?: emptyList())
            } else {
                _favoriteDurianState.value = FavoriteDurianState(error = result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}
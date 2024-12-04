package com.example.duriannet.presentation.durian_dictionary.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.remote.dtos.response.DurianProfileResponseDto
import com.example.duriannet.data.repository.durian_dictionary.DurianRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DurianProfileDetailsViewModel @Inject constructor(
    private val repository: DurianRepository
) : ViewModel() {

    private val _durianDetailsState = MutableStateFlow<DurianProfileResponseDto?>(null)
    val durianDetailsState: StateFlow<DurianProfileResponseDto?> = _durianDetailsState

    fun loadDurianDetails(id: Int) {
        viewModelScope.launch {
            val result = repository.getDurianDetails(id)
            if (result.isSuccess) {
                _durianDetailsState.value = result.getOrNull()
            } else {
                _durianDetailsState.value = null // Handle error if needed
            }
        }
    }
}


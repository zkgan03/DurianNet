package com.example.duriannet.presentation.durian_dictionary.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.durian_dictionary.DurianRepository
import com.example.duriannet.models.Durian
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DurianProfileDetailsViewModel @Inject constructor(
    private val durianRepository: DurianRepository
) : ViewModel() {

    private val _durianDetailsState = MutableStateFlow<Durian?>(null)
    val durianDetailsState: StateFlow<Durian?> = _durianDetailsState

    fun loadDurianDetails(durianId: String) {
        viewModelScope.launch {
            val result = durianRepository.getDurianDetails(durianId)
            if (result.isSuccess) {
                _durianDetailsState.value = result.getOrNull()
            } else {
                // Handle error
            }
        }
    }
}
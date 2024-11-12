package com.example.duriannet.presentation.durian_dictionary.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.durian_dictionary.DurianRepository
import com.example.duriannet.models.Durian
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DurianProfileViewModel @Inject constructor(
    private val durianRepository: DurianRepository
) : ViewModel() {

    private val _durianProfileState = MutableStateFlow<List<Durian>>(emptyList())
    val durianProfileState: StateFlow<List<Durian>> = _durianProfileState

    fun loadAllDurians() {
        viewModelScope.launch {
            val result = durianRepository.getAllDurians()
            if (result.isSuccess) {
                _durianProfileState.value = result.getOrNull() ?: emptyList()
            } else {
                // Handle error
            }
        }
    }
}
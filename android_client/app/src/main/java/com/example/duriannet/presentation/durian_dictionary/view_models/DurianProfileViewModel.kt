package com.example.duriannet.presentation.durian_dictionary.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.durian_dictionary.DurianRepository
import com.example.duriannet.presentation.durian_dictionary.state.DurianProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DurianProfileViewModel @Inject constructor(
    private val repository: DurianRepository
) : ViewModel() {

    private val _durianProfileState = MutableStateFlow(DurianProfileState())
    val durianProfileState: StateFlow<DurianProfileState> = _durianProfileState

    fun loadAllDurians() {
        viewModelScope.launch {
            val result = repository.getAllDurianProfilesForUser()
            if (result.isSuccess) {
                val durians = result.getOrNull() ?: emptyList()
                _durianProfileState.value = DurianProfileState(
                    allDurians = durians,
                    filteredDurians = durians
                )
            } else {
                _durianProfileState.value = DurianProfileState(error = "Failed to load durians")
            }
        }
    }

    fun filterDurians(query: String) {
        _durianProfileState.value = _durianProfileState.value.copy(
            filteredDurians = _durianProfileState.value.allDurians.filter {
                it.durianName.contains(query, ignoreCase = true)
            }
        )
    }
}



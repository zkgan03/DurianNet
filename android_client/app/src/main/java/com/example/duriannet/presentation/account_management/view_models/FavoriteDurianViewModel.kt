package com.example.duriannet.presentation.account_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.durian_dictionary.DurianRepository
import com.example.duriannet.presentation.account_management.state.FavoriteDurianState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteDurianViewModel @Inject constructor(
    private val durianRepository: DurianRepository
) : ViewModel() {

    private val _favoriteDurianState = MutableStateFlow(FavoriteDurianState())
    val favoriteDurianState: StateFlow<FavoriteDurianState> = _favoriteDurianState

    private val favoriteChanges = mutableMapOf<Int, Boolean>()

    fun loadDurians(username: String) {
        viewModelScope.launch {
            try {
                val allDuriansResult = durianRepository.getAllDurians()
                val favoriteDuriansResult = durianRepository.getFavoriteDurians(username)

                if (allDuriansResult.isSuccess && favoriteDuriansResult.isSuccess) {
                    val allDurians = allDuriansResult.getOrNull() ?: emptyList()
                    val favoriteDurianIds = favoriteDuriansResult.getOrNull()?.map { it.durianId } ?: emptyList()

                    _favoriteDurianState.value = FavoriteDurianState(
                        allDurians = allDurians,
                        filteredDurians = allDurians, // Ensure consistency with filteredDurians
                        favoriteDurianIds = favoriteDurianIds
                    )
                } else {
                    _favoriteDurianState.value = FavoriteDurianState(error = "Failed to load durians")
                }
            } catch (e: Exception) {
                _favoriteDurianState.value = FavoriteDurianState(error = e.message ?: "Unknown error")
            }
        }
    }

    fun filterDurians(query: String) {
        val filteredList = if (query.isEmpty()) {
            _favoriteDurianState.value.allDurians // Reset to allDurians if query is empty
        } else {
            _favoriteDurianState.value.allDurians.filter {
                it.durianName.contains(query, ignoreCase = true)
            }
        }
        _favoriteDurianState.value = _favoriteDurianState.value.copy(filteredDurians = filteredList)
    }

    fun onFavoriteChange(durianId: Int, isFavorite: Boolean) {
        favoriteChanges[durianId] = isFavorite
    }

    fun saveFavoriteChanges(username: String) {
        if (favoriteChanges.isEmpty()) return // No changes to save

        viewModelScope.launch {
            favoriteChanges.forEach { (durianId, isFavorite) ->
                val durianName = _favoriteDurianState.value.allDurians
                    .find { it.durianId == durianId }?.durianName ?: return@forEach

                if (isFavorite) {
                    durianRepository.addFavoriteDurian(username, durianName)
                } else {
                    durianRepository.removeFavoriteDurian(username, durianName)
                }
            }
            // Clear changes after saving
            favoriteChanges.clear()
        }
    }
}



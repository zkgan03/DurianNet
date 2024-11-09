package com.example.duriannet.presentation.seller_locator.view_models

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.seller.ISellerRepository
import com.example.duriannet.models.DurianType
import com.example.duriannet.models.Seller
import com.example.duriannet.presentation.seller_locator.state.SellerInputState
import com.example.duriannet.utils.Event
import com.example.duriannet.utils.EventBus
import com.example.duriannet.utils.EventBus.sendEvent
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageSellerViewModel @Inject constructor(
    private val sellerRepository: ISellerRepository,
) : ViewModel() {

    private val _addedSellers: MutableStateFlow<List<Seller>> = MutableStateFlow(emptyList())
    val addedSellers
        get() = _addedSellers.asStateFlow()

    private val _selectedSeller: MutableStateFlow<Seller?> = MutableStateFlow(null)
    val selectedSellerState
        get() = _selectedSeller.asStateFlow()


    init {
        getSellersAddedByUser()
    }

    private fun getSellersAddedByUser() {
        viewModelScope.launch {
            // TODO : Replace with actual user id
            val request = sellerRepository.getSellersAddedByUser("1")

            if (request.isSuccess) {
                Log.e("ManageSellerViewModel", "Sellers fetched successfully : ${request.getOrNull()}")
                _addedSellers.update { request.getOrNull() ?: emptyList() }
            } else {
                EventBus.sendEvent(Event.Toast("Failed to fetch sellers"))
            }
        }
    }

    fun filterAddedSeller(q: String): List<Seller> {
        return _addedSellers.value.filter { it.name.contains(q, ignoreCase = true) }
    }


    fun removeSeller() {
        viewModelScope.launch(Dispatchers.IO) {

            if (_selectedSeller.value == null) {
                EventBus.sendEvent(Event.Toast("No seller selected to be deleted"))
                return@launch
            }

            Log.e("ManageSellerViewModel", "Deleting seller : ${_selectedSeller.value}")

            val request = sellerRepository.deleteSeller(_selectedSeller.value!!.sellerId)

            if (request.isSuccess) {
                EventBus.sendEvent(Event.Toast("Seller deleted successfully"))
            } else {
                EventBus.sendEvent(Event.Toast("Failed to delete seller"))
            }

            // fetch all sellers again
            getSellersAddedByUser()
            _selectedSeller.update { null }
        }
    }

    fun selectSeller(seller: Seller) {
        _selectedSeller.update { seller }
    }

    fun unselectSeller() {
        _selectedSeller.update { null }
    }

    fun updateSeller(onSuccess: () -> Unit) {

        viewModelScope.launch(Dispatchers.IO) {
            if (_selectedSeller.value == null) {
                sendEvent(Event.Toast("No seller selected to be updated"))
                return@launch
            }

            _selectedSeller.value!!.apply {
                if (name.isEmpty() || description.isEmpty() || durianTypes.isEmpty()) {
                    sendEvent(Event.Toast("Please fill in all fields"))
                    return@launch
                }
            }

            Log.e("ManageSellerViewModel", "Updating seller : ${_selectedSeller.value}")

            val result = sellerRepository.updateSeller(
                _selectedSeller.value!!.sellerId,
                _selectedSeller.value!!.name,
                _selectedSeller.value!!.description,
                _selectedSeller.value!!.durianTypes.toList()
            )

            result.onSuccess {
                sendEvent(Event.Toast("Seller updated successfully"))
                onSuccess()
            }.onFailure { ex ->
                Log.e("AddSellerViewModel", ex.message ?: "Failed to update seller")
                sendEvent(Event.Toast(ex.message ?: "Failed to update seller"))
            }


            // fetch all sellers again
            getSellersAddedByUser()
            _selectedSeller.update { null }
        }
    }


    fun inputSellerName(name: String) {
        _selectedSeller.update { _selectedSeller.value?.copy(name = name) }
    }

    fun inputSellerDescription(description: String) {
        _selectedSeller.update { _selectedSeller.value?.copy(description = description) }
    }

    fun inputSellerDurianType(durianType: Set<DurianType>) {
        _selectedSeller.update { _selectedSeller.value?.copy(durianTypes = durianType.toHashSet()) }
    }


}
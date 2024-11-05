package com.example.duriannet.presentation.seller_locator.view_models

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.seller_locator.ISellerLocator
import com.example.duriannet.models.DurianType
import com.example.duriannet.models.Seller
import com.example.duriannet.utils.Event
import com.example.duriannet.utils.EventBus
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageSellerViewModel @Inject constructor(
    private val sellerLocatorRepository: ISellerLocator,
) : ViewModel() {

    private val _addedSellers: MutableStateFlow<List<Seller>> = MutableStateFlow(emptyList())
    val addedSellers
        get() = _addedSellers.asStateFlow()

    private val _selectedSeller: MutableStateFlow<Seller?> = MutableStateFlow(null)
    val selectedSellerState
        get() = _selectedSeller.asStateFlow()


    init {

        viewModelScope.launch {
            val request = sellerLocatorRepository.getAllSellers()
            if (request.isSuccess) {
                //get only first 5 sellers for testing purposes
                // TODO : update with actual data
                _addedSellers.update { request.getOrNull()!!.take(5) }

            } else {
                EventBus.sendEvent(Event.Toast("Failed to fetch sellers"))
            }
        }

    }

    fun filterAddedSeller(q: String): List<Seller> {
        return _addedSellers.value.filter { it.name.contains(q, ignoreCase = true) }
    }


    fun removeSeller(seller: Seller) {
        _addedSellers.update { it - seller }
        //TODO: Remove seller from database, notify server


    }

    fun selectSeller(seller: Seller) {
        _selectedSeller.update { seller }
    }

    fun updateSeller() {
        // update in added sellers list
        _addedSellers.update {
            it.map { seller ->
                if (seller.sellerId == _selectedSeller.value?.sellerId) {
                    _selectedSeller.value!!
                } else {
                    seller
                }
            }
        }

        // TODO : update in server side
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


    fun getDummySellers(): List<Seller> {
        val dummyImage: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Placeholder image
        return listOf(
            Seller(
                sellerId = 1,
                name = "Seller 1",
                description = "Description 1",
                durianTypes = hashSetOf(DurianType.MusangKing),
                image = dummyImage,
                rating = 4.5f,
                latLng = LatLng(1.0, 1.0)
            ),
            Seller(
                sellerId = 2,
                name = "Seller 2",
                description = "Description 2",
                durianTypes = hashSetOf(DurianType.D24),
                image = dummyImage,
                rating = 4.0f,
                latLng = LatLng(2.0, 2.0)
            ),
            Seller(
                sellerId = 3,
                name = "Seller 3",
                description = "Description 3",
                durianTypes = hashSetOf(DurianType.RedPrawn),
                image = dummyImage,
                rating = 3.5f,
                latLng = LatLng(3.0, 3.0)
            ),
            Seller(
                sellerId = 4,
                name = "Seller 4",
                description = "Description 4",
                durianTypes = hashSetOf(DurianType.BlackThorn),
                image = dummyImage,
                rating = 4.8f,
                latLng = LatLng(4.0, 4.0)
            ),
            Seller(
                sellerId = 5,
                name = "Seller 5",
                description = "Description 5",
                durianTypes = hashSetOf(DurianType.MusangKing, DurianType.D24),
                image = dummyImage,
                rating = 4.2f,
                latLng = LatLng(5.0, 5.0)
            )
        )
    }
}
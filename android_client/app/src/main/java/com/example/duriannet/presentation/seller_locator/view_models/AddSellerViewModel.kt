package com.example.duriannet.presentation.seller_locator.view_models

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.seller.SellerRepository
import com.example.duriannet.models.DetectionResult
import com.example.duriannet.models.DurianType
import com.example.duriannet.presentation.seller_locator.state.SellerInputState
import com.example.duriannet.utils.BitmapHelper
import com.example.duriannet.utils.Event
import com.example.duriannet.utils.EventBus.sendEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import drawResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddSellerViewModel @Inject constructor(
    private val sellerRepository: SellerRepository,
) : ViewModel() {
    var imageResult: Bitmap? = null
        get() = field?.drawResults(detectionResults, detectionSize)

    var detectionResults: Array<DetectionResult> = emptyArray()

    var detectionSize = Pair(0, 0)

    private val _inputState = MutableStateFlow(SellerInputState())
    val inputState = _inputState.asStateFlow()


    fun addSeller(onSuccess : () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                inputState.value.apply {
                    if (sellerName.isEmpty() || sellerDescription.isEmpty() || sellerDurianType.isEmpty()) {
                        sendEvent(Event.Toast("Please fill in all fields"))
                        return@withContext
                    }
                }

                val name = _inputState.value.sellerName
                val description = _inputState.value.sellerDescription
                val durianType = _inputState.value.sellerDurianType
                val latitude = _inputState.value.sellerLocation.first
                val longitude = _inputState.value.sellerLocation.second
                val image = BitmapHelper.encodeBase64Image(imageResult!!)

                //TODO : Replace with actual user id
                val result = sellerRepository.addSeller(
                    userId = "1",
                    name = name,
                    description = description,
                    base64Image = image,
                    latitude = latitude,
                    longitude = longitude,
                    durianTypes = durianType.toList()
                )

                result.onSuccess {
                    sendEvent(Event.Toast("Seller added successfully"))
                    onSuccess()
                }.onFailure {
                    val exception = result.exceptionOrNull()
                    Log.e("AddSellerViewModel", exception?.message ?: "Failed to add seller")
                    sendEvent(Event.Toast(exception?.message ?: "Failed to add seller"))
                }
            }
        }
    }

    fun inputSellerName(name: String) {
        _inputState.update { _inputState.value.copy(sellerName = name) }
    }

    fun inputSellerDescription(description: String) {
        _inputState.update { _inputState.value.copy(sellerDescription = description) }
    }

    fun inputSellerDurianType(durianType: Set<DurianType>) {
        _inputState.update { _inputState.value.copy(sellerDurianType = durianType.toHashSet()) }
    }

    fun inputSellerLocation(latitude: Double, longitude: Double) {
        _inputState.update { _inputState.value.copy(sellerLocation = Pair(latitude, longitude)) }
    }

}
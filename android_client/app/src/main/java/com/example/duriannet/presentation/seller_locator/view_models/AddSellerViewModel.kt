package com.example.duriannet.presentation.seller_locator.view_models

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.duriannet.data.repository.seller_locator.SellerLocatorRepository
import com.example.duriannet.models.DetectionResult
import com.example.duriannet.models.DurianType
import com.example.duriannet.presentation.seller_locator.state.SellerInputState
import dagger.hilt.android.lifecycle.HiltViewModel
import drawResults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AddSellerViewModel @Inject constructor(
    private val sellerLocatorRepository: SellerLocatorRepository,
) : ViewModel() {
    var imageResult: Bitmap? = null
        get() = field?.drawResults(detectionResults, detectionSize)

    var detectionResults: Array<DetectionResult> = emptyArray()

    var detectionSize = Pair(0, 0)

    private val _inputState = MutableStateFlow(SellerInputState())
    val inputState = _inputState.asStateFlow()

    fun addSeller() {
        // TODO: Add seller to database
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

    fun inputSellerLocation(location: Pair<Double, Double>) {
        _inputState.update { _inputState.value.copy(sellerLocation = Pair(location.first, location.second)) }
    }

}
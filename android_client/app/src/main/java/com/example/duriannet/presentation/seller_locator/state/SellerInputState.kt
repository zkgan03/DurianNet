package com.example.duriannet.presentation.seller_locator.state

import com.example.duriannet.models.DurianType

data class SellerInputState(
    val sellerName: String = "",
    val sellerDescription: String = "",
    val sellerDurianType: HashSet<DurianType> = hashSetOf(),
    val sellerLocation: Pair<Double, Double> = Pair(0.0, 0.0),
)
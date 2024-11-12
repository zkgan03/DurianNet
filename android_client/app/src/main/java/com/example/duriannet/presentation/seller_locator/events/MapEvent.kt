package com.example.duriannet.presentation.seller_locator.events

sealed class MapEvent {
    data class UpdateQuery(val query: String) : MapEvent()

    data class SelectSeller(val sellerId: Int) : MapEvent()
    data object RefreshSellers : MapEvent()
    data object RetryLoading : MapEvent()

    data object RefreshComments : MapEvent()
    data class AddComment(val content: String, val rating: Float) : MapEvent()
    data class EditComment(val content: String, val rating: Float) : MapEvent()
    data object DeleteComment : MapEvent()
}
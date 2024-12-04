package com.example.duriannet.presentation.seller_locator.state

import com.example.duriannet.models.Comment
import com.example.duriannet.models.Seller
import com.example.duriannet.models.SellerSearchResult

sealed class MapScreenState {
    data object Loading : MapScreenState()
    data class Error(val message: String) : MapScreenState()

    data class Success(
        val sellers: List<Seller> = emptyList(), // all sellers
        val searchResults: List<SellerSearchResult> = emptyList(), // search results for the query
        val selectedSeller: Seller? = null, // selected seller
        val sellerComments: List<Comment> = emptyList(), // comments of the selected seller
        val userComment: Comment? = null, // current user comment on the selected seller
        val isBottomSheetLoading: Boolean = false,
        val query: String = "",
    ) : MapScreenState()
}
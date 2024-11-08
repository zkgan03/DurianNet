package com.example.duriannet.presentation.seller_locator.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.comment.ICommentRepository
import com.example.duriannet.data.repository.seller.ISellerRepository
import com.example.duriannet.models.Seller
import com.example.duriannet.models.Comment
import com.example.duriannet.utils.Event
import com.example.duriannet.utils.EventBus.sendEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO : Refactor this view model to use the new state management, since it is too cluttered

@HiltViewModel
class MapViewModel @Inject constructor(
    private val sellerRepository: ISellerRepository,
    private val commentRepository: ICommentRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val queryFlow = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 1,
        BufferOverflow.DROP_OLDEST
    )

    private val _sellers = MutableStateFlow(emptyList<Seller>())
    val sellers = _sellers.asStateFlow()

    private val _searchResults = MutableStateFlow(emptyList<Seller>())
    val searchResults = _searchResults.asStateFlow()

    private val _sellerComments = MutableStateFlow(emptyList<Comment>())
    val sellerComments = _sellerComments.asStateFlow()

    private val _userComment = MutableStateFlow<Comment?>(null)
    val userComment = _userComment.asStateFlow()

    private val _bottomSheetProgress = MutableStateFlow(false)
    val bottomSheetProgress = _bottomSheetProgress.asStateFlow()


    private val _selectedSeller = MutableStateFlow<Seller?>(null)
    val selectedSeller = _selectedSeller.asStateFlow()

    init {
        setUpQueryFlow()
        getAllSellers()
    }

    private fun getAllSellers() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = sellerRepository.getAllSellers()

            if (result.isSuccess) {
                val placesResponse = result.getOrNull()

                updateSellers(placesResponse ?: emptyList())

            } else if (result.isFailure) {
                val exception = result.exceptionOrNull()
                Log.e("SellerLocatorViewModel", exception?.message ?: "Failed to get all places")
                sendEvent(Event.Toast(exception?.message ?: "Failed to get all places"))

                // retry in 5 seconds
                delay(5000)
                getAllSellers()
            }

        }
    }

    private fun setUpQueryFlow() {
        queryFlow
            .debounce(300)
            .distinctUntilChanged()
            .mapLatest { query ->

                if (query.isEmpty()) {
                    updateSearchResults(sellers.value)
                    return@mapLatest
                }

                Log.e("SellerLocatorViewModel", "Invoked, Searching for $query")


                val result = sellerRepository.searchSellers(query)

                if (result.isSuccess) {
                    val searchResults = result.getOrNull()

                    updateSearchResults(searchResults ?: emptyList())

                } else if (result.isFailure) {
                    val exception = result.exceptionOrNull()
                    Log.e("SellerLocatorViewModel", exception?.message ?: "Failed to search places")
                    sendEvent(Event.Toast(exception?.message ?: "Failed to search places"))

                }

                return@mapLatest
            }
            .launchIn(viewModelScope)
    }

    fun selectSeller(sellerId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _bottomSheetProgress.update {
                true
            }

            getSellerComments(sellerId)

            val result = sellerRepository.getSellerById(sellerId)

            if (result.isSuccess) {
                val seller = result.getOrNull()

                _selectedSeller.update {
                    seller
                }

            } else if (result.isFailure) {
                val exception = result.exceptionOrNull()
                Log.e("SellerLocatorViewModel", exception?.message ?: "Failed to get seller by id")
                sendEvent(Event.Toast(exception?.message ?: "Failed to get seller by id"))
            }

            _bottomSheetProgress.update {
                false
            }
        }

    }

    private fun getSellerComments(sellerId: Int) {

        viewModelScope.launch(Dispatchers.IO) {

            val result = commentRepository.getSellerComments(sellerId)

            if (result.isSuccess) {
                val comments = result.getOrNull()

                //TODO: If it is current user comment, remove from the list
                // val currentUser = getCurrentUser()
                val (filteredComments, currentUserCommentList) = comments
                    .orEmpty()
                    .partition { it.userId != "1" } //TODO: Replace with currentUser.userId

                _sellerComments.update {
                    filteredComments
                }

                _userComment.update {
                    currentUserCommentList.firstOrNull()
                }

            } else if (result.isFailure) {
                val exception = result.exceptionOrNull()
                Log.e("SellerLocatorViewModel", exception?.message ?: "Failed to get seller comments")
                sendEvent(Event.Toast(exception?.message ?: "Failed to get seller comments"))
            }
        }


    }

    fun updateQueryAndSearch(query: String) {
        _query.update {
            query
        }
        queryFlow.tryEmit(query)
    }

    fun updateQuery(query: String) {
        _query.update {
            query
        }
    }

    private fun updateSellers(sellers: List<Seller>) {
        _sellers.update {
            sellers
        }
    }

    private fun updateSearchResults(searchResults: List<Seller>) {
        _searchResults.update {
            searchResults
        }
    }

    fun addComment(content: String, rating: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_selectedSeller.value == null) {
                sendEvent(Event.Toast("No seller selected to add comment"))
                return@launch
            }

            val result = commentRepository.addComment(
                userId = "1", //TODO : Replace with real user ID
                sellerId = selectedSeller.value!!.sellerId,
                content = content,
                rating = rating
            )

            if (result.isSuccess) {
                sendEvent(Event.Toast("Comment added successfully"))

                getSellerComments(selectedSeller.value!!.sellerId)
                selectSeller(selectedSeller.value!!.sellerId)

            } else if (result.isFailure) {
                val exception = result.exceptionOrNull()
                Log.e("SellerLocatorViewModel", exception?.message ?: "Failed to add comment")
                sendEvent(Event.Toast(exception?.message ?: "Failed to add comment"))
            }
        }
    }

    //TODO : Remove and edit comment
}
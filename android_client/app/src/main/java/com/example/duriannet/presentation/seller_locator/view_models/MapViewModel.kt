package com.example.duriannet.presentation.seller_locator.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.seller_locator.ISellerLocator
import com.example.duriannet.models.Seller
import com.example.duriannet.models.SellerComment
import com.example.duriannet.utils.Event
import com.example.duriannet.utils.EventBus.sendEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val sellerLocatorRepository: ISellerLocator,
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

    private val _initProgress = MutableStateFlow(false)
    val initProgress = _initProgress.asStateFlow()

    private val _sellerComments = MutableStateFlow(emptyList<SellerComment>())
    val sellerComments = _sellerComments.asStateFlow()

    private val _bottomSheetProgress = MutableStateFlow(false)
    val bottomSheetProgress = _bottomSheetProgress.asStateFlow()

    init {
        setUpQueryFlow()
        initPlaces()
    }

    private fun initPlaces() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                val result = sellerLocatorRepository.getAllSellers()

                if (result.isSuccess) {
                    val placesResponse = result.getOrNull()

                    updateSellers(placesResponse ?: emptyList())

                } else if (result.isFailure) {
                    val exception = result.exceptionOrNull()
                    Log.e("SellerLocatorViewModel", exception?.message ?: "Failed to get all places")
                    sendEvent(Event.Toast(exception?.message ?: "Failed to get all places"))
                }

                _initProgress.update {
                    true
                }

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


                val result = sellerLocatorRepository.searchSellers(query)

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

    fun getSellerComments(sellerId: Int) {
        _bottomSheetProgress.update {
            true
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {


                val result = sellerLocatorRepository.getSellerComments(sellerId)

                if (result.isSuccess) {
                    val comments = result.getOrNull()

                    _sellerComments.update {
                        comments ?: emptyList()
                    }

                } else if (result.isFailure) {
                    val exception = result.exceptionOrNull()
                    Log.e("SellerLocatorViewModel", exception?.message ?: "Failed to get seller comments")
                    sendEvent(Event.Toast(exception?.message ?: "Failed to get seller comments"))
                }


            }
        }

        _bottomSheetProgress.update {
            false
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

    private fun updateSellers(places: List<Seller>) {
        _sellers.update {
            places
        }
    }

    private fun updateSearchResults(searchResults: List<Seller>) {
        _searchResults.update {
            searchResults
        }
    }

//    private var searchJob: Job? = null
//    fun searchPlace(query: String) {
//        searchJob?.cancel()
//        searchJob = viewModelScope.launch(Dispatchers.IO) {
//
//            delay(300) // debounce time
//
//            if (query.isEmpty()) {
////                adapter.submitList(emptyList())
//                return@launch
//            }
//
//            val result = sellerLocatorRepository.searchPlaces(query)
//
//            if (result.isSuccess) {
//                val places = result.getOrNull()
//
//                withContext(Dispatchers.Main) {
//                    val searchResults = places?.map {
//                        it.toPlace()
//                    }?.let {
//                        List(it.size) { index ->
//                            SearchItem(index, name = it.getOrNull(index)?.name ?: "")
//                        }
//                    } ?: emptyList()
//
//                    updateSearchResults(searchResults)
//                }
//
//            } else if (result.isFailure) {
//                val exception = result.exceptionOrNull()
//                Log.e("SellerLocatorViewModel", exception?.message ?: "Failed to search places")
//                sendEvent(Event.Toast(exception?.message ?: "Failed to search places"))
//            }
//        }
//    }
}
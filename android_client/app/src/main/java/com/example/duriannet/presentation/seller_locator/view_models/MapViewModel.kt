package com.example.duriannet.presentation.seller_locator.view_models

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.comment.ICommentRepository
import com.example.duriannet.data.repository.seller.ISellerRepository
import com.example.duriannet.models.toSellerSearchResult
import com.example.duriannet.presentation.seller_locator.events.MapEvent
import com.example.duriannet.presentation.seller_locator.state.MapScreenState
import com.example.duriannet.services.common.GoogleMapManager
import com.example.duriannet.utils.Event
import com.example.duriannet.utils.EventBus.sendEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO : Refactor this view model to use the new state management, since it is too cluttered

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sellerRepository: ISellerRepository,
    private val commentRepository: ICommentRepository,
) : ViewModel() {

    private var _state = MutableStateFlow<MapScreenState>(MapScreenState.Loading)
    val state get() = _state.asStateFlow()

    private var isInitialized = false

    private val queryFlow = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 1,
        BufferOverflow.DROP_OLDEST
    )

    private val _currentUserLocation = MutableStateFlow(Pair(0.0, 0.0))

    fun initViewModel() {

        if (isInitialized) {
            refreshSellers()
        }

        isInitialized = true

        // run on main thread
        viewModelScope.launch(Dispatchers.IO) {
            val currentLocation = GoogleMapManager.getUserLocationAwait(context)
            _currentUserLocation.value = Pair(currentLocation!!.latitude, currentLocation.longitude)

            setUpQueryFlow()
            initSellers()
        }
    }

    private val maxAttempts = 5
    private val delay = 5000L
    private fun initSellers() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = MapScreenState.Loading

            var currentAttempt = 0

            while (currentAttempt < maxAttempts) {
                val task = loadSellersWithRetry()
                if (task) {
                    break
                }
                delay(delay)
                currentAttempt++
            }

            if (currentAttempt == maxAttempts) {
                _state.value = MapScreenState.Error("Failed to load sellers")
            }
        }
    }

    private suspend fun loadSellersWithRetry(): Boolean {
        val result = sellerRepository.getAllSellers()

        result.onSuccess { sellers ->
            _state.value = MapScreenState.Success(
                sellers = sellers,
                searchResults = sellers.map {
                    it.toSellerSearchResult(
                        _currentUserLocation.value
                    )
                }.sortedBy { it.distanceFromCurrentLocationInKm }
            )
            return true
        }.onFailure {
            val exception = result.exceptionOrNull()
            Log.e("SellerLocatorViewModel", exception?.message ?: "Failed to load the sellers")
            sendEvent(Event.Toast(exception?.message ?: "Failed to load the sellers"))

            return false
        }

        return false
    }

    private fun getAllSellers() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = (_state.value as? MapScreenState.Success) ?: return@launch

            val result = sellerRepository.getAllSellers()

            result.onSuccess { sellers ->
                if (sellers == currentState.sellers) return@launch // no need to update the state if the data is the same

                updateState { state ->
                    state.copy(sellers = sellers)
                }

            }.onFailure {
                val exception = result.exceptionOrNull()
                Log.e("SellerLocatorViewModel", exception?.message ?: "Failed to get all places")
                sendEvent(Event.Toast(exception?.message ?: "Failed to get all places"))
            }
        }
    }

    private fun setUpQueryFlow() {
        queryFlow
            .debounce(300)
            .distinctUntilChanged()
            .mapLatest { query ->

                val result = if (query.isEmpty()) {
                    sellerRepository.getAllSellers()
                } else {
                    sellerRepository.searchSellers(query)
                }

                result.onSuccess { sellers ->
                    updateState { state ->
                        state.copy(
                            searchResults = sellers
                                .map {
                                    it.toSellerSearchResult(_currentUserLocation.value)
                                }
                                .sortedBy { it.distanceFromCurrentLocationInKm })
                    }
                }.onFailure {
                    val exception = it
                    Log.e("SellerLocatorViewModel", exception.message ?: "Failed to search places")
                    sendEvent(Event.Toast(exception.message ?: "Failed to search places"))
                }

                return@mapLatest
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: MapEvent) {
        when (event) {
            is MapEvent.RetryLoading -> initSellers()
            is MapEvent.UpdateQuery -> handleQueryUpdate(event.query)

            is MapEvent.SelectSeller -> handleSellerSelection(event.sellerId)
            is MapEvent.RefreshSellers -> refreshSellers()

            is MapEvent.RefreshComments -> TODO("Not yet implemented")
            is MapEvent.AddComment -> handleAddComment(event.content, event.rating)
            is MapEvent.EditComment -> handleEditComment(event.content, event.rating)
            is MapEvent.DeleteComment -> handleDeleteComment()
        }
    }


    private fun refreshSellers() {
        if (_state.value is MapScreenState.Success) {
            Log.e("MapViewModel", "refreshSellers")
            getAllSellers()
        }
    }

    // only update the state if the current state is Success
    private fun updateState(update: (MapScreenState.Success) -> MapScreenState.Success) {
        val currentState = _state.value as? MapScreenState.Success ?: return
        _state.value = update(currentState)
    }

    private fun handleQueryUpdate(query: String) {
        Log.e("MapViewModel", "handleQueryUpdate : $query")

        updateState { state ->
            state.copy(query = query)
        }

        queryFlow.tryEmit(query)
    }

    private fun handleSellerSelection(sellerId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            updateState {
                it.copy(isBottomSheetLoading = true)
            }

            try {
                val seller = sellerRepository.getSellerById(sellerId).getOrThrow()
                val comments = commentRepository.getSellerComments(sellerId).getOrThrow()

                val (filteredComments, userComments) = comments.partition { it.userId != "1" }

                updateState {
                    it.copy(
                        selectedSeller = seller,
                        sellerComments = filteredComments,
                        userComment = userComments.firstOrNull(),
                        isBottomSheetLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("SellerLocatorViewModel", e.message ?: "Failed to get seller comments")
                sendEvent(Event.Toast(e.message ?: "Failed to load seller details"))
                updateState { it.copy(isBottomSheetLoading = false) }
            }

        }
    }

    private fun handleAddComment(content: String, rating: Float) {
        viewModelScope.launch(Dispatchers.IO) {

            val currentState = (_state.value as? MapScreenState.Success) ?: return@launch

            val selectedSeller = currentState.selectedSeller ?: run {
                sendEvent(Event.Toast("No seller selected"))
                return@launch
            }

            val result = commentRepository.addComment(
                userId = "1", //TODO : Replace with real user ID
                sellerId = selectedSeller.sellerId,
                content = content,
                rating = rating
            )

            result.onSuccess {
                sendEvent(Event.Toast("Comment added successfully"))
                handleSellerSelection(selectedSeller.sellerId)
            }.onFailure {
                val exception = result.exceptionOrNull()
                Log.e("SellerLocatorViewModel", exception?.message ?: "Failed to add comment")
                sendEvent(Event.Toast(exception?.message ?: "Failed to add comment"))
            }

        }
    }

    private fun handleEditComment(content: String, rating: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = (_state.value as? MapScreenState.Success) ?: return@launch

            val commentId = currentState.userComment?.commentId ?: run {
                sendEvent(Event.Toast("No comment to edit"))
                return@launch
            }

            val result = commentRepository.updateComment(
                commentId = commentId,
                content = content,
                rating = rating
            )

            result.onSuccess {
                sendEvent(Event.Toast("Comment updated successfully"))
                handleSellerSelection(currentState.selectedSeller?.sellerId ?: -1)
            }.onFailure {
                val exception = result.exceptionOrNull()
                Log.e("SellerLocatorViewModel", exception?.message ?: "Failed to update comment")
                sendEvent(Event.Toast(exception?.message ?: "Failed to update comment"))
            }
        }
    }

    private fun handleDeleteComment() {

        viewModelScope.launch(Dispatchers.IO) {
            val currentState = (_state.value as? MapScreenState.Success) ?: return@launch

            val commentId = currentState.userComment?.commentId ?: run {
                sendEvent(Event.Toast("No comment to delete"))
                return@launch
            }

            val result = commentRepository.deleteComment(commentId)

            result.onSuccess {
                sendEvent(Event.Toast("Comment deleted successfully"))
                handleSellerSelection(currentState.selectedSeller?.sellerId ?: -1)
            }.onFailure {
                val exception = result.exceptionOrNull()
                Log.e("SellerLocatorViewModel", exception?.message ?: "Failed to delete comment")
                sendEvent(Event.Toast(exception?.message ?: "Failed to delete comment"))
            }
        }
    }
}
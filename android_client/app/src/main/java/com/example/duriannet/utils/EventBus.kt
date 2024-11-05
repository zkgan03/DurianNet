package com.example.duriannet.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object EventBus {
    private val _events = Channel<Event>()
    val events = _events.receiveAsFlow()

    suspend fun sendEvent(event: Event) {
        _events.send(event)
    }
}

sealed interface Event {
    data class Toast(val message: String) : Event
//    data class Dialog(val title: String, val text: String) : Event
}
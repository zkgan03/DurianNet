package com.example.duriannet.presentation.durian_dictionary.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.remote.dtos.request.durian.ChatRequestDto
import com.example.duriannet.data.repository.durian_dictionary.ChatbotRepository
import com.example.duriannet.models.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DurianChatbotViewModel @Inject constructor(
    private val chatbotRepository: ChatbotRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(Message(userMessage, true)) // Add user message
        _messages.value = currentMessages

        val messageDtos = currentMessages.map {
            ChatRequestDto.MessageDto(
                role = if (it.isSent) "Human" else "AI",
                content = it.text
            )
        }

        viewModelScope.launch {
            chatbotRepository.streamChat(messageDtos).collect { cleanedResponse ->
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(Message(cleanedResponse, false)) // Add combined response
                _messages.value = updatedMessages
            }
        }
    }



    /*fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(Message(userMessage, true))
        _messages.value = currentMessages

        val messageDtos = currentMessages.map {
            ChatRequestDto.MessageDto(
                role = if (it.isSent) "Human" else "AI",
                content = it.text
            )
        }

        viewModelScope.launch {
            chatbotRepository.streamChat(messageDtos).collect { response ->
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(Message(response, false))
                _messages.value = updatedMessages
            }
        }
    }*/
}


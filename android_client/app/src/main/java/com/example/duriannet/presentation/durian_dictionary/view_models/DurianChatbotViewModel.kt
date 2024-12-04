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

    private fun cleanResponse(response: String): String {
        return response.replace("**", "").trim()
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(Message(userMessage, true)) // Add user's message
        currentMessages.add(Message("", isSent = false, isLoading = true)) // Add loading message
        _messages.value = currentMessages

        // Prepare the message DTOs (excluding loading indicators)
        val messageDtos = currentMessages.filterNot { it.isLoading }.map {
            ChatRequestDto.MessageDto(
                role = if (it.isSent) "Human" else "AI",
                content = it.text
            )
        }

        // Collect the response from the repository
        viewModelScope.launch {
            chatbotRepository.streamChat(messageDtos).collect { rawResponse ->
                val cleanedResponse = cleanResponse(rawResponse) // Sanitize the response
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.removeLast() // Remove the loading message
                updatedMessages.add(Message(cleanedResponse, isSent = false)) // Add the cleaned response
                _messages.value = updatedMessages
            }
        }
    }
}


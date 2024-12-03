package com.example.duriannet.data.remote.dtos.request.durian

data class ChatRequestDto(
    val messages: List<MessageDto>
) {
    data class MessageDto(
        val role: String,
        val content: String
    )
}

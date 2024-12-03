package com.example.duriannet.data.repository.durian_dictionary

import com.example.duriannet.data.remote.api.DurianApi
import com.example.duriannet.data.remote.dtos.request.durian.ChatRequestDto
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import javax.inject.Inject

class ChatbotRepository @Inject constructor(
    private val durianApi: DurianApi
) {
    // Create an instance of ObjectMapper
    private val objectMapper = ObjectMapper()

    fun streamChat(messages: List<ChatRequestDto.MessageDto>) = callbackFlow<String> {
        try {
            val chatRequest = ChatRequestDto(messages)
            val response = durianApi.chatWithHistory(chatRequest)

            if (!response.isSuccessful) {
                close(Exception("API Error: ${response.code()} ${response.message()}"))
            }

            val reader = response.body()?.byteStream()?.bufferedReader(Charsets.UTF_8)
            val stringBuilder = StringBuilder()

            reader?.forEachLine { line ->
                if (line.startsWith("data:")) {
                    val rawData = line.substringAfter("data: ").trim()
                    try {
                        // Use ObjectMapper to parse the JSON and extract the "data" field
                        val jsonNode = objectMapper.readTree(rawData)
                        val extractedText = jsonNode["data"]?.asText() ?: ""
                        stringBuilder.append(extractedText)
                    } catch (e: Exception) {
                        println("Error parsing line: $line")
                    }
                }
            }

            // Emit the combined message
            trySend(stringBuilder.toString())
            reader?.close()
        } catch (e: Exception) {
            close(e)
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)
}


/*import com.example.duriannet.data.remote.api.DurianApi
import com.example.duriannet.data.remote.dtos.request.durian.ChatRequestDto
import com.fasterxml.jackson.core.JsonParser
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import okhttp3.ResponseBody
import java.net.SocketTimeoutException
import javax.inject.Inject

class ChatbotRepository @Inject constructor(
    private val durianApi: DurianApi
) {
    fun streamChat(messages: List<ChatRequestDto.MessageDto>) = callbackFlow<String> {
        try {
            val chatRequest = ChatRequestDto(messages)
            val response = durianApi.chatWithHistory(chatRequest)

            if (!response.isSuccessful) {
                close(Exception("API Error: ${response.code()} ${response.message()}"))
            }

            val reader = response.body()?.byteStream()?.bufferedReader(Charsets.UTF_8)
            val stringBuilder = StringBuilder()

            reader?.forEachLine { line ->
                if (line.startsWith("data:")) {
                    val rawData = line.substringAfter("data: ").trim()
                    try {
                        // Use ObjectMapper to parse the JSON and extract the "data" field
                        val jsonNode = objectMapper.readTree(rawData)
                        val extractedText = jsonNode["data"]?.asText() ?: ""
                        stringBuilder.append(extractedText)
                    } catch (e: Exception) {
                        println("Error parsing line: $line")
                    }
                }
            }

            // Emit the combined message
            trySend(stringBuilder.toString())
            reader?.close()
        } catch (e: Exception) {
            close(e)
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

}*/
/*//*
/ This function is used to stream chat messages to the server and receive responses.
    fun streamChat(messages: List<ChatRequestDto.MessageDto>) = callbackFlow<String> {
        try {
            val chatRequest = ChatRequestDto(messages)
            val response = durianApi.chatWithHistory(chatRequest)

            if (!response.isSuccessful) {
                close(Exception("API Error: ${response.code()} ${response.message()}"))
            }

            val reader = response.body()?.byteStream()?.bufferedReader(Charsets.UTF_8)
            reader?.forEachLine { line ->
                if (line.startsWith("data:")) {
                    trySend(line.substringAfter("data: ").trim())
                }
            }
            reader?.close()
        } catch (e: SocketTimeoutException) {
            close(Exception("Request timed out. Please try again later."))
        } catch (e: Exception) {
            close(e)
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)*/

}*/
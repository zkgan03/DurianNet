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
                trySend("API Error: ${response.code()} - ${response.message()}")
                close(Exception("API Error: ${response.code()} ${response.message()}"))
            }

            /*if (!response.isSuccessful) {
                trySend("API Error: ${response.code()} - ${response.message()}")
                close(Exception("API Error: ${response.code()} - ${response.message()}"))
                return@callbackFlow
            }*/

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
                        trySend("Error parsing the chatbot response.")
                    }
                }
            }

            /*reader?.forEachLine { line ->
                if (line.startsWith("data:")) {
                    val rawData = line.substringAfter("data: ").trim()
                    try {
                        val jsonNode = objectMapper.readTree(rawData)
                        val extractedText = jsonNode["data"]?.asText() ?: ""
                        if (extractedText.isNotEmpty()) {
                            trySend(extractedText) // Send the response
                        }
                    } catch (e: Exception) {
                        trySend("Error parsing the chatbot response.")
                        println("Error parsing line: $line")
                    }
                }
            }*/

            // Emit the combined message
            trySend(stringBuilder.toString())
            reader?.close()
        } catch (e: Exception) {
            trySend("Server is not available. Please try again later.")
            close(e)
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)
}


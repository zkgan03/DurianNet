package com.example.duriannet.presentation.durian_dictionary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.duriannet.databinding.FragmentDurianChatbotBinding
import com.example.duriannet.models.Message
import com.example.duriannet.presentation.durian_dictionary.adapters.ChatbotAdapter

class DurianChatbotFragment : Fragment() {
    private lateinit var binding: FragmentDurianChatbotBinding
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: ChatbotAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDurianChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatbotAdapter(messages)
        binding.rvChatbot.layoutManager = LinearLayoutManager(context)
        binding.rvChatbot.adapter = adapter

        binding.send.setOnClickListener {
            val messageText = binding.messageBox.text.toString()
            if (messageText.isNotBlank()) {
                val message = Message(text = messageText, isSent = true)
                messages.add(message)
                adapter.notifyItemInserted(messages.size - 1)
                binding.rvChatbot.scrollToPosition(messages.size - 1)
                binding.messageBox.text.clear()

                // Simulate receiving a response
                val response = Message(text = "Received: $messageText", isSent = false)
                messages.add(response)
                adapter.notifyItemInserted(messages.size - 1)
                binding.rvChatbot.scrollToPosition(messages.size - 1)
            }
        }
    }
}
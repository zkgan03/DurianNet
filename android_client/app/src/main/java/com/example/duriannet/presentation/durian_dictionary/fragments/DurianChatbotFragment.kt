package com.example.duriannet.presentation.durian_dictionary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.duriannet.databinding.FragmentDurianChatbotBinding
import com.example.duriannet.models.Message
import com.example.duriannet.presentation.durian_dictionary.adapters.ChatbotAdapter
import com.example.duriannet.presentation.durian_dictionary.view_models.DurianChatbotViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DurianChatbotFragment : Fragment() {

    private lateinit var binding: FragmentDurianChatbotBinding
    private val viewModel: DurianChatbotViewModel by viewModels()
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

        adapter = ChatbotAdapter()
        binding.rvChatbot.layoutManager = LinearLayoutManager(context)
        binding.rvChatbot.adapter = adapter

        lifecycleScope.launchWhenStarted {
            viewModel.messages.collect { messages ->
                adapter.submitList(messages)
                binding.rvChatbot.scrollToPosition(messages.size - 1)
            }
        }

        binding.send.setOnClickListener {
            val messageText = binding.messageBox.text.toString()
            viewModel.sendMessage(messageText)
            binding.messageBox.text.clear()
        }
    }
}

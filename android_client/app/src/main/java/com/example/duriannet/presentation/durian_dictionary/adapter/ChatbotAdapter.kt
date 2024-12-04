package com.example.duriannet.presentation.durian_dictionary.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.duriannet.databinding.ItemMessageReceivedBinding
import com.example.duriannet.databinding.ItemMessageSentBinding
import com.example.duriannet.models.Message

class ChatbotAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isSent) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is SentMessageViewHolder) holder.bind(message)
        else if (holder is ReceivedMessageViewHolder) holder.bind(message)
    }

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    class SentMessageViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.txtSentMessage.text = message.text
        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            if (message.isLoading) {
                binding.txtReceiveMessage.visibility = View.GONE
                binding.loadingContainer.visibility = View.VISIBLE

                // Start dots animation
                animateDots(binding.loadingDots)
            } else {
                binding.txtReceiveMessage.visibility = View.VISIBLE
                binding.loadingContainer.visibility = View.GONE

                val cleanText = message.text.replace("**", "") // Remove bold markers
                val cleanText2 = cleanText.replace("*", "") // Remove italic markers
                binding.txtReceiveMessage.text = cleanText2

                //binding.txtReceiveMessage.text = message.text
            }
        }

        private fun animateDots(textView: TextView) {
            val dotsArray = arrayOf("", ".", "..", "...")
            var index = 0

            // Animate dots every 500ms
            textView.postDelayed(object : Runnable {
                override fun run() {
                    textView.text = dotsArray[index]
                    index = (index + 1) % dotsArray.size
                    textView.postDelayed(this, 500)
                }
            }, 500)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.text == newItem.text && oldItem.isSent == newItem.isSent
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}

package com.example.duriannet.presentation.account_management.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.duriannet.databinding.ItemDurianProfileBinding

class DurianProfileAdapter : ListAdapter<String, DurianProfileAdapter.DurianProfileViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DurianProfileViewHolder {
        val binding = ItemDurianProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DurianProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DurianProfileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DurianProfileViewHolder(private val binding: ItemDurianProfileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(durianName: String) {
            binding.durianName.text = durianName
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }
}
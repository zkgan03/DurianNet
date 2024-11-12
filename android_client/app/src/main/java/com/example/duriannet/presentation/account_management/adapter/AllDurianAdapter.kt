package com.example.duriannet.presentation.account_management.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.duriannet.databinding.ItemFavoriteDurianBinding

class AllDurianAdapter : ListAdapter<String, AllDurianAdapter.AllDurianViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllDurianViewHolder {
        val binding = ItemFavoriteDurianBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllDurianViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AllDurianViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AllDurianViewHolder(private val binding: ItemFavoriteDurianBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(durianName: String) {
            binding.favoriteDurianName.text = durianName
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }
}
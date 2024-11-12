package com.example.duriannet.presentation.account_management.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.duriannet.databinding.ItemFavoriteDurianSelectedBinding

class FavoriteDurianAdapter : ListAdapter<String, FavoriteDurianAdapter.FavoriteDurianViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteDurianViewHolder {
        val binding = ItemFavoriteDurianSelectedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteDurianViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteDurianViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FavoriteDurianViewHolder(private val binding: ItemFavoriteDurianSelectedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(durianName: String) {
            binding.favoriteDurianName.text = durianName
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }
}
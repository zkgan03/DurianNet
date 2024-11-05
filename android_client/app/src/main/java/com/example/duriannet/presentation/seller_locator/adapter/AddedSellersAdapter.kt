package com.example.duriannet.presentation.seller_locator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.duriannet.R
import com.example.duriannet.databinding.ItemSellerAddedBinding
import com.example.duriannet.models.Seller

class AddedSellersAdapter
    : ListAdapter<Seller, AddedSellersAdapter.AddedSellersViewHolder>(object : DiffUtil.ItemCallback<Seller>() {
    override fun areItemsTheSame(oldItem: Seller, newItem: Seller): Boolean {
        return oldItem.sellerId == newItem.sellerId
    }

    override fun areContentsTheSame(oldItem: Seller, newItem: Seller): Boolean {
        return oldItem == newItem
    }
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddedSellersViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_seller_added, parent, false)
        return AddedSellersViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddedSellersViewHolder, position: Int) {
        val seller = getItem(position)
        holder.bind(seller)
    }

    private var onDeleteClicked: ((Seller) -> Unit)? = null
    fun setOnDeleteClickedListener(listener: (Seller) -> Unit) {
        onDeleteClicked = listener
    }

    private var onEditClicked: ((Seller) -> Unit)? = null
    fun setOnEditClickedListener(listener: (Seller) -> Unit) {
        onEditClicked = listener
    }


    inner class AddedSellersViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemSellerAddedBinding.bind(view)

        fun bind(seller: Seller) {
            binding.sellerName.text = seller.name
            binding.sellerAddress.text = seller.description

            binding.iconEdit.setOnClickListener {
                onEditClicked?.invoke(seller)
            }

            binding.iconDelete.setOnClickListener {
                onDeleteClicked?.invoke(seller)
            }
        }
    }
}
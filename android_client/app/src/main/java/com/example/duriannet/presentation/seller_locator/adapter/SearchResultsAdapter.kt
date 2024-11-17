package com.example.duriannet.presentation.seller_locator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.duriannet.R
import com.example.duriannet.databinding.ItemSearchResultBinding
import com.example.duriannet.models.Seller
import com.example.duriannet.models.SellerSearchResult
import com.example.duriannet.services.common.GoogleMapManager

class SearchResultsAdapter(
    private var userLocation: Pair<Double, Double> = Pair(0.0, 0.0),
    private var onItemClicked: (SellerSearchResult) -> Unit = {},
) : ListAdapter<SellerSearchResult, SearchResultsAdapter.ViewHolder>(object : DiffUtil.ItemCallback<SellerSearchResult>() {
    override fun areItemsTheSame(oldItem: SellerSearchResult, newItem: SellerSearchResult): Boolean {
        return oldItem.sellerId == newItem.sellerId
    }

    override fun areContentsTheSame(oldItem: SellerSearchResult, newItem: SellerSearchResult): Boolean {
        return oldItem == newItem
    }
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSearchResultBinding.bind(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_search_result, parent, false)
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchResult = getItem(position)
        holder.bind(searchResult)
    }

    fun updateUserLocation(userLocation: Pair<Double, Double>) {
        this.userLocation = userLocation
        notifyDataSetChanged()
    }

    fun setOnItemClickedListener(onItemClicked: (SellerSearchResult) -> Unit) {
        this.onItemClicked = onItemClicked
    }

    inner class ViewHolder(private val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(seller: SellerSearchResult) {
            binding.distanceFromCurrentLocation.text = String.format("%.2f km", seller.distanceFromCurrentLocationInKm)

            binding.sellerName.text = seller.name
            binding.sellerAddress.text = GoogleMapManager.getAddress(
                itemView.context,
                seller.latitude,
                seller.longitude
            )
            binding.itemView.setOnClickListener {
                onItemClicked(seller)
            }
        }
    }
}

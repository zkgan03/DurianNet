package com.example.duriannet.presentation.seller_locator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.duriannet.R
import com.example.duriannet.databinding.ItemAllUserCommentsBinding
import com.example.duriannet.models.SellerComment

class SellerCommentsAdapter
    : ListAdapter<SellerComment, SellerCommentsAdapter.SellerCommentViewHolder>(
    object : DiffUtil.ItemCallback<SellerComment>() {
        override fun areItemsTheSame(oldItem: SellerComment, newItem: SellerComment): Boolean {
            return oldItem.commentId == newItem.commentId
        }

        override fun areContentsTheSame(oldItem: SellerComment, newItem: SellerComment): Boolean {
            return oldItem == newItem
        }

    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellerCommentViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_all_user_comments, parent, false)

        return SellerCommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: SellerCommentViewHolder, position: Int) {
        val sellerComment = getItem(position)
        holder.bind(sellerComment)
    }


    inner class SellerCommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding: ItemAllUserCommentsBinding = ItemAllUserCommentsBinding.bind(view)

        fun bind(sellerComment: SellerComment) {
            binding.imageUser.setImageResource(R.drawable.image_1) // TODO : Change to user image
            binding.textUsername.text = sellerComment.user.username
            binding.textComment.text = sellerComment.content
            binding.ratingUser.rating = sellerComment.rating

            binding.iconEdit.visibility = View.GONE
            binding.iconDelete.visibility = View.GONE
        }
    }
}
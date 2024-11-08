package com.example.duriannet.presentation.seller_locator.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.duriannet.R
import com.example.duriannet.databinding.ItemAllUserCommentsBinding
import com.example.duriannet.models.Comment
import com.example.duriannet.utils.Common

class SellerCommentsAdapter(
    private val context: Context,
) : ListAdapter<Comment, SellerCommentsAdapter.SellerCommentViewHolder>(
    object : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.commentId == newItem.commentId
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
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

        fun bind(sellerComment: Comment) {
            Common.loadServerImageIntoView(
                context,
                sellerComment.userImage,
                binding.imageUser
            )
            binding.textUsername.text = sellerComment.username
            binding.textComment.text = sellerComment.content
            binding.ratingBar.rating = sellerComment.rating
        }
    }
}
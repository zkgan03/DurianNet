package com.example.duriannet.presentation.detector.adapters

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.duriannet.R
import com.example.duriannet.databinding.ItemMediaBinding
import com.example.duriannet.models.MediaStoreData

class GalleryItemAdapter : ListAdapter<MediaStoreData, GalleryItemAdapter.NewGalleryItemViewHolder>(
    object : DiffUtil.ItemCallback<MediaStoreData>() {
        override fun areItemsTheSame(oldItem: MediaStoreData, newItem: MediaStoreData): Boolean {
            return oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(oldItem: MediaStoreData, newItem: MediaStoreData): Boolean {
            return oldItem == newItem
        }
    }
) {

    private var selectedPosition = RecyclerView.NO_POSITION

    private var onMediaSelectedListener: ((MediaStoreData) -> Unit)? = null // Callback Method
    fun setOnMediaSelectedListener(listener: (MediaStoreData) -> Unit) {
        onMediaSelectedListener = listener
    }

    // Cache the dominant color of each image, so that we don't have to generate the palette every time
    private val colorCache = mutableMapOf<String, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewGalleryItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return NewGalleryItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewGalleryItemViewHolder, position: Int) {
        val mediaItem = getItem(position)

        // Load the image
        Glide.with(holder.imageView.context)
            .asBitmap()
            .load(mediaItem.uri)
            .override(200, 200)
            .into(object : CustomTarget<Bitmap>() {

                // we need to get the bitmap here because the image is lazy loaded
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    holder.imageView.setImageBitmap(resource)

                    val cachedColor = colorCache[mediaItem.uri.toString()] // Check if the color is cached

                    // If the color is not cached, generate the palette
                    if (cachedColor == null) {

                        Palette.from(resource).generate { palette ->
                            val vibrant = palette?.dominantSwatch
                            vibrant?.rgb?.let { color ->
                                holder.imageView.setBackgroundColor(color)
                                colorCache[mediaItem.uri.toString()] = color // Cache the color
                            }
                        }
                    }

                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    holder.imageView.setImageDrawable(placeholder)
                }
            })

        // Set duration for videos, hide for images
        holder.duration.visibility = View.GONE

        // set onClickListener for each item
        holder.itemView.setOnClickListener {
            selectedPosition = holder.layoutPosition
            // Notify MainActivity to display selected image
            onMediaSelectedListener?.invoke(mediaItem)
        }
    }

    inner class NewGalleryItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemMediaBinding.bind(view)
        val imageView: ImageView = binding.imageView
        val duration: TextView = binding.duration
    }
}
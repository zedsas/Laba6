package com.example.a6laba.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a6laba.R
import com.example.a6laba.data.ImageItem

class ImageAdapter(private var images: List<ImageItem>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    val items: List<ImageItem>
        get() = images

    var onItemClick: ((ImageItem) -> Unit)? = null
    var onItemLongClick: ((ImageItem) -> Unit)? = null

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val textView: TextView = itemView.findViewById(R.id.textViewDescription)

        fun bind(item: ImageItem) {
            Glide.with(itemView.context)
                .load(item.uri)
                .centerCrop()
                .into(imageView)
            textView.text = item.description
            itemView.setOnClickListener {
                onItemClick?.invoke(item)
            }
            itemView.setOnLongClickListener {
                onItemLongClick?.invoke(item)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size

    fun updateList(newList: List<ImageItem>) {
        images = newList
        notifyDataSetChanged()
    }
}
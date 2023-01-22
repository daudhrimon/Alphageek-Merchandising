package com.gdm.alphageek.view.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.databinding.ImageItemsBinding

class ImageViewAdapter(private val dataList: List<Uri>) : RecyclerView.Adapter<ImageViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ImageItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: ImageItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Uri) {
            binding.imageView.setImageURI(data)
        }
    }
}
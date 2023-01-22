package com.gdm.alphageek.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.data.local.planogram_image.PlanogramTempImage
import com.gdm.alphageek.databinding.PlanogramImageItemsBinding

class PlanogramImageAdapter(private val dataList: List<PlanogramTempImage>) : RecyclerView.Adapter<PlanogramImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PlanogramImageItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: PlanogramImageItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: PlanogramTempImage) {
            binding.beforeImage.setImageURI(data.beforeImage)
            binding.afterImage.setImageURI(data.afterImage)
        }
    }
}
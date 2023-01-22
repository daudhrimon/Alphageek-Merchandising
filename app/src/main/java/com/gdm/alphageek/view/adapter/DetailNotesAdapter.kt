package com.gdm.alphageek.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.databinding.DetailingNotesItemsBinding

class DetailNotesAdapter(private val dataList: List<String>) : RecyclerView.Adapter<DetailNotesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DetailingNotesItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: DetailingNotesItemsBinding) : RecyclerView.ViewHolder(binding.root)
}
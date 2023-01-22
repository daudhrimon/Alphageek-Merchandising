package com.gdm.alphageek.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.down_sync.Brief
import com.gdm.alphageek.databinding.BriefItemsBinding
import com.gdm.alphageek.utils.Utils

class BriefAdapter(private val dataList: List<Brief>) : RecyclerView.Adapter<BriefAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BriefItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: BriefItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Brief) {
            binding.title.text = data.title
            binding.description.text  = data.description

            binding.mainLayout.setOnClickListener{
                Utils.currentBrief = data
                Navigation.createNavigateOnClickListener(R.id.inboxDetailsFragment).onClick(binding.mainLayout)
            }
        }
    }
}
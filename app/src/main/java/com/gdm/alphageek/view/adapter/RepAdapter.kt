package com.gdm.alphageek.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.data.local.store_detailing.DetailingPeople
import com.gdm.alphageek.databinding.PromoDescriptionItemsBinding

class RepAdapter(private val dataList: ArrayList<DetailingPeople>) : RecyclerView.Adapter<RepAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PromoDescriptionItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: PromoDescriptionItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DetailingPeople) {
            binding.title.text = data.name

            binding.deleteBtn.setOnClickListener{
                dataList.remove(data)
                notifyDataSetChanged()
            }
        }
    }

    fun getListItems():List<DetailingPeople>{
        return dataList
    }

    fun clearData(){
        dataList.clear()
    }
}
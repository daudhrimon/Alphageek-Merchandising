package com.gdm.alphageek.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.data.local.competition_tracking.PromoDescriptionData
import com.gdm.alphageek.databinding.PromoDescriptionItemsBinding
import com.gdm.alphageek.utils.isGONE
import com.gdm.alphageek.utils.isVISIBLE

class PromoDescriptionAdapter(
    private val dataList: ArrayList<PromoDescriptionData>,
    private val share: Boolean
) : RecyclerView.Adapter<PromoDescriptionAdapter.ViewHolder>() {

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
        fun bind(data: PromoDescriptionData) {
            if (share) binding.deleteBtn.isGONE() else binding.deleteBtn.isVISIBLE()
            binding.title.text = data.title

            binding.deleteBtn.setOnClickListener{
                dataList.remove(data)
                notifyDataSetChanged()
            }
        }
    }

    fun getListItems():List<PromoDescriptionData>{
        return dataList
    }

    fun clearData(){
        dataList.clear()
    }

}
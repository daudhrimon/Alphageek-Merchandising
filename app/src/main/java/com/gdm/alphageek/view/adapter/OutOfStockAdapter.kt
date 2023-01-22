package com.gdm.alphageek.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.out_of_stock.OutOfStockData
import com.gdm.alphageek.databinding.OutOfStockItemsBinding
import com.gdm.alphageek.utils.isGONE
import com.gdm.alphageek.utils.isVISIBLE
import com.squareup.picasso.Picasso

class OutOfStockAdapter(
    private val dataList: ArrayList<OutOfStockData>,
    private val share: Boolean
) : RecyclerView.Adapter<OutOfStockAdapter.ViewHolder>() {

    private var itemList = ArrayList<OutOfStockData>()
    private var isBoxChecked = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OutOfStockItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: OutOfStockItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: OutOfStockData) {
            binding.productName.text = data.product_name
            if (data.is_checked){
                binding.checkbox.isEnabled = false
                binding.checkbox.isChecked = true
                binding.deleteBtn.isVISIBLE()
            } else {
                if (share) binding.deleteBtn.isGONE() else binding.deleteBtn.isVISIBLE()
                binding.checkbox.isEnabled = true
                binding.checkbox.isChecked = false
            }
            try { Picasso.get().load(data.product_image).placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.productImage) } catch (_:Exception){}

            binding.checkbox.setOnCheckedChangeListener { compoundButton, _ ->
                if (compoundButton.isChecked) {
                    compoundButton.isChecked = true
                    isBoxChecked = true
                    itemList.add(
                        OutOfStockData(
                            data.product_id,
                            data.category_id,
                            data.brand_id,
                            data.client_id,
                            data.product_name,
                            data.product_image,
                            true
                        )
                    )
                } else {
                    data.is_checked = true
                    isBoxChecked = false
                    itemList.remove(data)
                }
            }

            binding.deleteBtn.setOnClickListener {
                itemList.remove(data)
                dataList.remove(data)
                notifyDataSetChanged()
            }
        }
    }

    fun getListItems() = itemList

    fun getIsBoxChecked() = isBoxChecked

    fun clearData() {
        dataList.clear()
    }
}
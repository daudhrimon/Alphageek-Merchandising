package com.gdm.alphageek.view.adapter

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.down_sync.Brand
import com.gdm.alphageek.data.local.product_availability.ProductAvailableData
import com.gdm.alphageek.data.remote.InputFilterMinMax
import com.gdm.alphageek.databinding.ProductOrderItemsBinding
import com.gdm.alphageek.utils.isGONE
import com.gdm.alphageek.utils.isVISIBLE
import com.squareup.picasso.Picasso

class ProductAvailabilityAdapter(
    private val dataList: ArrayList<ProductAvailableData>,
    private val context: Context,
    private val type: String,
    private val share: Boolean
) : RecyclerView.Adapter<ProductAvailabilityAdapter.ViewHolder>() {

    private var itemList = ArrayList<ProductAvailableData>()
    private var brandList = ArrayList<Brand>().apply {
        add(Brand(1, "Quantity"))
        add(Brand(2, "Facing"))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ProductOrderItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: ProductOrderItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ProductAvailableData) {
            binding.checkbox.text = type
            binding.itemCount.filters = arrayOf<InputFilter>(InputFilterMinMax(0,100000000))
            if (data.availability_qty == 0){
                binding.deleteBtn.isGONE()
            } else {
                if (share) binding.deleteBtn.isGONE() else binding.deleteBtn.isVISIBLE()
                binding.itemCount.setText(data.availability_qty.toString())
                binding.checkbox.isChecked = true
                binding.itemCount.isEnabled = false
                binding.brandSpinner.isEnabled = false
            }
            binding.productName.text = data.product_name
            try { Picasso.get().load(data.product_image).placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.productImage) }catch (_:Exception){}

            binding.checkbox.setOnCheckedChangeListener { compoundButton, _ ->
                val count = binding.itemCount.text.toString()
                if (compoundButton.isChecked) {
                   if (count.isEmpty()){
                       binding.itemCount.requestFocus()
                       binding.itemCount.error = "Pieces"
                       compoundButton.isChecked = false
                   } else{
                       compoundButton.isChecked = true
                   }
                } else {
                    itemList.remove(data)
                }
            }

            binding.itemCount.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    val count = binding.itemCount.text.toString()
                    var bool = true
                    try {
                        if (itemList.size > 0) {
                            for (item in itemList) {
                                if (data.product_id == item.product_id) {
                                    bool = false
                                    itemList.remove(item)
                                    itemList.add(ProductAvailableData(data.product_id,data.category_id,data.brand_id,data.client_id,
                                        data.product_name,
                                        data.product_image,count.toInt(),
                                        brandList[binding.brandSpinner.selectedItemPosition].id)
                                    )
                                }
                            }
                            if (bool) {
                                itemList.add(ProductAvailableData(
                                        data.product_id,
                                        data.category_id,
                                        data.brand_id,
                                        data.client_id,
                                        data.product_name,
                                        data.product_image,
                                        count.toInt(),
                                        brandList[binding.brandSpinner.selectedItemPosition].id
                                    ))
                            }
                        } else {
                            itemList.add(ProductAvailableData(
                                    data.product_id,
                                    data.category_id,
                                    data.brand_id,
                                    data.client_id,
                                    data.product_name,
                                    data.product_image,
                                    count.toInt(),
                                    brandList[binding.brandSpinner.selectedItemPosition].id
                                ))
                        }
                    } catch (e: java.lang.Exception) {/**/}
                }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {/**/}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.isNotEmpty()){
                        binding.checkbox.isChecked = Integer.parseInt(s.toString())>0
                    }else{
                        binding.checkbox.isChecked=false
                    }
                }
            })

            binding.brandSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, brandList)
            binding.brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, i: Int, p3: Long) {
                    binding.itemCount.setText(binding.itemCount.text.toString())
                } override fun onNothingSelected(p0: AdapterView<*>?) {/**/}
            }
            data.facing_qty?.let { binding.brandSpinner.setSelection(it-1) }

            binding.deleteBtn.setOnClickListener{
                dataList.remove(data)
                notifyDataSetChanged()
            }
        }
    }

    fun getListItems() = itemList

    fun clearData(){
        itemList.clear()
    }


}
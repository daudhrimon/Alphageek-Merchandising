package com.gdm.alphageek.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.product_order.ProductOrderData
import com.gdm.alphageek.data.remote.InputFilterMinMax
import com.gdm.alphageek.databinding.ProductOrderMainItemsBinding
import com.gdm.alphageek.utils.isGONE
import com.gdm.alphageek.utils.isVISIBLE
import com.squareup.picasso.Picasso

class ProductOrderAdapter(
    private val dataList: ArrayList<ProductOrderData>,
    private val share: Boolean
) : RecyclerView.Adapter<ProductOrderAdapter.ViewHolder>() {

    private var itemList = ArrayList<ProductOrderData>()
    private var isBoxChecked = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ProductOrderMainItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: ProductOrderMainItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("NotifyDataSetChanged")
        fun bind(data: ProductOrderData) {
            binding.caseCount.filters = arrayOf<InputFilter>(InputFilterMinMax(0,100000000))
            binding.unitCount.filters = arrayOf<InputFilter>(InputFilterMinMax(0,100000000))
            if (data.order_case_qty == 0 && data.order_unit_qty == 0){
                binding.deleteBtn.isGONE()
            } else {
                if (share) binding.deleteBtn.isGONE() else binding.deleteBtn.isVISIBLE()
                binding.caseCount.setText(data.order_case_qty.toString())
                binding.unitCount.setText(data.order_unit_qty.toString())
                binding.caseCount.isEnabled = false
                binding.unitCount.isEnabled = false
                binding.checkbox.isEnabled = false
                binding.checkbox.isChecked = true
            }
            try { Picasso.get().load(data.product_image).placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.productImage) } catch (_:Exception){}

            binding.checkbox.setOnCheckedChangeListener { checkBox, _ ->
                var caseCount = binding.caseCount.text.toString()
                var unitCount = binding.unitCount.text.toString()
                when{ caseCount.isEmpty() -> caseCount = "0" }
                when{ unitCount.isEmpty() -> unitCount = "0" }

                if (checkBox.isChecked) {
                    if (caseCount =="0" && unitCount == "0"){
                        binding.caseCount.requestFocus()
                        binding.caseCount.error = "Quantity"
                        binding.unitCount.requestFocus()
                        binding.unitCount.error = "Quantity"
                        checkBox.isChecked = false
                        isBoxChecked = false
                    }else{
                        checkBox.isChecked = true
                        isBoxChecked = true
                        itemList.add(
                            ProductOrderData(
                                data.product_id,
                                data.category_id,
                                data.product_category_id,
                                data.brand_id,
                                data.client_id,
                                data.product_name,
                                data.product_image,
                                data.unit_price,
                                data.unit_per_case,
                                caseCount.toInt(),
                                unitCount.toInt()))
                    }
                }else {
                    data.order_case_qty = caseCount.toInt()
                    data.order_unit_qty = unitCount.toInt()
                    itemList.remove(data)
                }
            }


            binding.caseCount.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    var caseCount = binding.caseCount.text.toString()
                    var unitCount = binding.unitCount.text.toString()
                    when{ caseCount.isEmpty() -> caseCount = "0" }
                    when{ unitCount.isEmpty() -> unitCount = "0" }

                    var bool = true;
                    try {
                        if (itemList.size > 0) {
                            for (item in itemList) {
                                if (data.product_id == item.product_id) {
                                    bool = false
                                    itemList.remove(item)
                                    itemList.add(
                                        ProductOrderData(
                                            data.product_id,
                                            data.category_id,
                                            data.product_category_id,
                                            data.brand_id,
                                            data.client_id,
                                            data.product_name,
                                            data.product_image,
                                            data.unit_price,
                                            data.unit_per_case,
                                            caseCount.toInt(),
                                            unitCount.toInt()))
                                }
                                // body of loop
                            }
                            if (bool) {
                                itemList.add(

                                    ProductOrderData(
                                        data.product_id,
                                        data.category_id,
                                        data.product_category_id,
                                        data.brand_id,
                                        data.client_id,
                                        data.product_name,
                                        data.product_image,
                                        data.unit_price,
                                        data.unit_per_case,
                                        caseCount.toInt(),
                                        unitCount.toInt()))
                            }
                        } else {
                            itemList.add(
                                ProductOrderData(data.product_id,data.category_id,data.product_category_id,data.brand_id,data.client_id,data.product_name,data.product_image,
                                    data.unit_price,data.unit_per_case,caseCount.toInt(),unitCount.toInt())
                            )
                        }
                    } catch (e: java.lang.Exception) {/**/}
                }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {/**/}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    var unitCount =  binding.unitCount.text.toString()
                    when { unitCount.isEmpty() -> unitCount = "0"}
                    when {
                        s.isNotEmpty() -> binding.checkbox.apply {
                            isChecked = when { s.toString().toInt() > 0 || unitCount.toInt() > 0 -> true else -> false }
                            isBoxChecked = isChecked
                        }
                        else -> binding.checkbox.apply {
                            isChecked = when { unitCount.toInt() > 0 -> true else -> false }
                            isBoxChecked = isChecked
                        }
                    }
                }
            })


            binding.unitCount.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    var caseCount = binding.caseCount.text.toString()
                    var unitCount = binding.unitCount.text.toString()
                    when{ caseCount.isEmpty() -> caseCount = "0" }
                    when{ unitCount.isEmpty() -> unitCount = "0" }

                    var bool = true;
                    try {
                        if (itemList.size > 0) {
                            for (item in itemList) {
                                if (data.product_id == item.product_id) {
                                    bool = false
                                    itemList.remove(item)
                                    itemList.add(
                                        ProductOrderData(
                                            data.product_id,
                                            data.category_id,
                                            data.product_category_id,
                                            data.brand_id,
                                            data.client_id,
                                            data.product_name,
                                            data.product_image,
                                            data.unit_price,
                                            data.unit_per_case,
                                            caseCount.toInt(),
                                            unitCount.toInt()))
                                }
                                // body of loop
                            }
                            if (bool) {
                                itemList.add(

                                    ProductOrderData(
                                        data.product_id,
                                        data.category_id,
                                        data.product_category_id,
                                        data.brand_id,
                                        data.client_id,
                                        data.product_name,
                                        data.product_image,
                                        data.unit_price,
                                        data.unit_per_case,
                                        caseCount.toInt(),
                                        unitCount.toInt()))
                            }
                        } else {
                            itemList.add(
                                ProductOrderData(
                                    data.product_id,
                                    data.category_id,
                                    data.product_category_id,
                                    data.brand_id,
                                    data.client_id,
                                    data.product_name,
                                    data.product_image,
                                    data.unit_price,
                                    data.unit_per_case,
                                    caseCount.toInt(),
                                    unitCount.toInt()))
                        }
                    } catch (e: java.lang.Exception) {/**/}

                }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {/**/}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    var caseCount =  binding.caseCount.text.toString()
                    when { caseCount.isEmpty() -> caseCount = "0"}
                    when {
                        s.isNotEmpty() -> binding.checkbox.apply {
                            isChecked = when { s.toString().toInt() > 0 || caseCount.toInt() > 0 -> true else -> false }
                            isBoxChecked = isChecked
                        }
                        else -> binding.checkbox.apply {
                            isChecked = when { caseCount.toInt() > 0 -> true else -> false }
                            isBoxChecked = isChecked
                        }
                    }
                }
            })

            binding.productName.text = data.product_name

            binding.deleteBtn.setOnClickListener{
                itemList.remove(data)
                dataList.remove(data)
                notifyDataSetChanged()
            }

        }
    }

    fun getListItems() = itemList

    fun getIsBoxChecked() = isBoxChecked

    fun clearData(){
        itemList.clear()
    }
}
package com.gdm.alphageek.view.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.posm_products.PosmTrackingData
import com.gdm.alphageek.data.remote.InputFilterMinMax
import com.gdm.alphageek.databinding.PosmTrackingItemsBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.isGONE
import com.gdm.alphageek.utils.isVISIBLE
import com.squareup.picasso.Picasso

class PosmTrackingAdapter(
    private val dataList: ArrayList<PosmTrackingData>,
    private val share: Boolean = false
) : RecyclerView.Adapter<PosmTrackingAdapter.ViewHolder>() {

    private var itemList = ArrayList<PosmTrackingData>()
    private var isBoxChecked = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PosmTrackingItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: PosmTrackingItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(data: PosmTrackingData) {
            binding.itemCount.filters = arrayOf<InputFilter>(InputFilterMinMax(0,100000000))
            if (data.availability_qty == null) {
                binding.deleteBtn.isGONE()
            } else {
                if (share) binding.deleteBtn.isGONE() else binding.deleteBtn.isVISIBLE()
                binding.itemCount.setText(data.availability_qty.toString())
                binding.statusSpinner.setSelection(Utils.getSpinnerIndex(binding.statusSpinner, data.status))
                binding.checkbox.isChecked = true
                binding.itemCount.isEnabled = false
                binding.statusSpinner.isEnabled = false
                binding.checkbox.isEnabled = false
            }
            binding.productName.text = data.product_name
            try { Picasso.get().load(data.product_image).placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.productImage) } catch (_:Exception){}

            binding.deleteBtn.setOnClickListener {
                itemList.remove(data)
                dataList.remove(data)
                notifyDataSetChanged()
            }


            binding.checkbox.setOnCheckedChangeListener { checkBox, _ ->
                val count = binding.itemCount.text.toString()
                if (checkBox.isChecked) {
                    when { count.isEmpty()-> {
                        binding.itemCount.requestFocus()
                        binding.itemCount.error = "Pieces"
                        checkBox.isChecked = false
                        isBoxChecked = false
                    } else-> {
                        checkBox.isChecked = true
                        isBoxChecked = true
                        itemList.add(
                            PosmTrackingData(
                                data.product_id,
                                data.category_id,
                                data.brand_id,
                                data.client_id,
                                data.product_name,
                                data.product_image,
                                count.toInt(),
                                binding.statusSpinner.selectedItem.toString()
                            )
                        )
                    } }
                } else {
                    itemList.remove(data)
                }
            }

            binding.itemCount.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    val count = when {binding.itemCount.text.toString().isEmpty() -> "0" else-> binding.itemCount.text.toString()}
                    var bool = true
                    try {
                        if (itemList.size > 0) {
                            for (item in itemList) {
                                if (data.product_id == item.product_id) {
                                    bool = false
                                    itemList.remove(item)
                                    itemList.add(PosmTrackingData(
                                        data.product_id,
                                        data.category_id,
                                        data.brand_id,
                                        data.client_id,
                                        data.product_name,
                                        data.product_image,
                                        count.toInt(),
                                        binding.statusSpinner.selectedItem.toString()
                                    ))

                                }
                            }
                            if (bool) {
                                itemList.add(       PosmTrackingData(
                                    data.product_id,
                                    data.category_id,
                                    data.brand_id,
                                    data.client_id,
                                    data.product_name,
                                    data.product_image,
                                    count.toInt(),
                                    binding.statusSpinner.selectedItem.toString()
                                ))

                            }
                        } else {
                           itemList.add(PosmTrackingData(
                               data.product_id,
                               data.category_id,
                               data.brand_id,
                               data.client_id,
                               data.product_name,
                               data.product_image,
                               count.toInt(),
                               binding.statusSpinner.selectedItem.toString()
                           ))
                        }
                    } catch (e: java.lang.Exception) {/**/}

                }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {/**/}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.isNotEmpty()) {
                        binding.checkbox.isChecked = true
                        binding.statusSpinner.setSelection(when{s.toString()=="0"->2 else->1})
                        isBoxChecked = true
                    } else {
                        binding.checkbox.isChecked = false
                        binding.statusSpinner.setSelection(2)
                        isBoxChecked = false
                    }
                }
            })
        }
    }

    fun getListItems() = itemList

    fun getIsBoxChecked() = isBoxChecked

    fun clearData() {
        itemList.clear()
    }
}
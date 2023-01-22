package com.gdm.alphageek.view.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.price_check.PriceCheckData
import com.gdm.alphageek.data.remote.InputFilterMinMax
import com.gdm.alphageek.databinding.PriceCheckItemsBinding
import com.gdm.alphageek.utils.isGONE
import com.gdm.alphageek.utils.isVISIBLE
import com.squareup.picasso.Picasso

class PriceCheckAdapter(
    private val dataList: ArrayList<PriceCheckData>,
    private val share: Boolean
) : RecyclerView.Adapter<PriceCheckAdapter.ViewHolder>() {

    private var itemList = ArrayList<PriceCheckData>()
    private var isBoxSelected = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PriceCheckItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: PriceCheckItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(data: PriceCheckData) {
            binding.actualPrice.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 100000000))
            if (data.actual_price == 0.0) {
                binding.checkbox.isVISIBLE()
                binding.deleteBtn.isGONE()
            } else {
                if (share) binding.deleteBtn.isGONE() else binding.deleteBtn.isVISIBLE()
                binding.checkbox.isGONE()
                binding.actualPrice.setText(data.actual_price?.toInt().toString())
                binding.actualPrice.isEnabled = false
            }
            try { Picasso.get().load(data.product_image).placeholder(R.drawable.no_image).error(R.drawable.ic_product_freshness).into(binding.productImage) } catch (e: Exception) {/**/ }

            binding.actualPrice.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    val count = binding.actualPrice.text.toString()
                    var bool = true;
                    try {
                        if (itemList.size > 0) {
                            for (item in itemList) {
                                if (data.product_id == item.product_id) {
                                    bool = false
                                    itemList.remove(item)
                                    itemList.add(
                                        PriceCheckData(
                                            data.product_id,
                                            data.category_id,
                                            data.brand_id,
                                            data.client_id,
                                            data.product_name,
                                            data.product_image,
                                            data.unit_price,
                                            count.toDouble()
                                        )
                                    )

                                }
                                // body of loop
                            }
                            if (bool) {
                                itemList.add(
                                    PriceCheckData(
                                        data.product_id,
                                        data.category_id,
                                        data.brand_id,
                                        data.client_id,
                                        data.product_name,
                                        data.product_image,
                                        data.unit_price,
                                        count.toDouble()
                                    )
                                )


                            }
                        } else {
                            itemList.add(
                                PriceCheckData(
                                    data.product_id,
                                    data.category_id,
                                    data.brand_id,
                                    data.client_id,
                                    data.product_name,
                                    data.product_image,
                                    data.unit_price,
                                    count.toDouble()
                                )
                            )

                        }
                    } catch (e: java.lang.Exception) {/**/}

                }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {/**/}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.isNotEmpty()) {
                        if (Integer.parseInt(s.toString()) > 0) {
                            binding.checkbox.isChecked = true
                            isBoxSelected = true
                        } else {
                            binding.checkbox.isChecked = false
                            isBoxSelected = false
                        }
                    } else {
                        binding.checkbox.isChecked = false
                        isBoxSelected = false
                    }
                }
            })


            binding.checkbox.setOnCheckedChangeListener { compoundButton, _ ->
                val count = binding.actualPrice.text.toString()
                if (compoundButton.isChecked) {
                    if (count.isEmpty() || count == "0") {
                        binding.actualPrice.requestFocus()
                        binding.actualPrice.error = "Price"
                        compoundButton.isChecked = false
                        isBoxSelected = false
                    } else {
                        compoundButton.isChecked = true
                        isBoxSelected = true
                    }
                }
            }
            binding.unitPrice.text = data.unit_price.toString()
            binding.productName.text = data.product_name

            binding.deleteBtn.setOnClickListener {
                itemList.remove(data)
                dataList.remove(data)
                notifyDataSetChanged()
            }
        }
    }

    fun getListItems() = itemList

    fun getIsBoxChecked() = isBoxSelected

    fun clearData() {
        itemList.clear()
    }

}
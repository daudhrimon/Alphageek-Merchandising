package com.gdm.alphageek.view.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.posm_products.PosmDeploymentData
import com.gdm.alphageek.data.remote.InputFilterMinMax
import com.gdm.alphageek.databinding.PosmDeploymentItemsBinding
import com.gdm.alphageek.utils.isGONE
import com.gdm.alphageek.utils.isVISIBLE
import com.squareup.picasso.Picasso

class PosmDeploymentAdapter(
    private val dataList: ArrayList<PosmDeploymentData>,
    private val share: Boolean = false
) : RecyclerView.Adapter<PosmDeploymentAdapter.ViewHolder>() {

    private var itemList = ArrayList<PosmDeploymentData>()
    private var isBoxChecked = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PosmDeploymentItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: PosmDeploymentItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(data: PosmDeploymentData) {
            binding.unitCount.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 100000000))
            binding.caseCount.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 100000000))
            if (data.availability_qty == 0 && data.case_qty == 0) {
                binding.deleteBtn.isGONE()
            } else {
                if (share) binding.deleteBtn.isGONE() else binding.deleteBtn.isVISIBLE()
                binding.unitCount.setText(data.availability_qty.toString())
                binding.caseCount.setText(data.case_qty.toString())
                binding.checkbox.isChecked = true
                binding.unitCount.isEnabled = false
                binding.caseCount.isEnabled = false
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
                var case = binding.caseCount.text.toString()
                var unit = binding.unitCount.text.toString()
                when{ unit.isEmpty() -> unit = "0" }
                when{ case.isEmpty() -> case = "0" }

                if (checkBox.isChecked) {
                    when {
                        unit == "0" && case == "0" -> {
                            binding.caseCount.requestFocus()
                            binding.caseCount.error = "case"
                            binding.unitCount.requestFocus()
                            binding.unitCount.error = "unit"
                            checkBox.isChecked = false
                            isBoxChecked = false
                        }
                        else -> {
                            checkBox.isChecked = true
                            isBoxChecked = true
                            itemList.add(
                                PosmDeploymentData(
                                    data.product_id,
                                    data.category_id,
                                    data.brand_id,
                                    data.client_id,
                                    data.product_name,
                                    data.product_image,
                                    unit.toInt(),
                                    case.toInt()))
                        }
                    }
                } else {
                    itemList.remove(data)
                }
            }



            binding.unitCount.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    var unit = binding.unitCount.text.toString()
                    var case = binding.caseCount.text.toString()
                    when{ unit.isEmpty() -> unit = "0" }
                    when{ case.isEmpty() -> case = "0" }

                    var bool = true;
                    try {
                        if (itemList.size > 0) {
                            for (item in itemList) {
                                if (data.product_id == item.product_id) {
                                    bool = false
                                    itemList.remove(item)
                                    itemList.add(
                                        PosmDeploymentData(
                                            data.product_id,
                                            data.category_id,
                                            data.brand_id,
                                            data.client_id,
                                            data.product_name,
                                            data.product_image,
                                            unit.toInt(),
                                            case.toInt()))
                                }
                                // body of loop
                            }
                            if (bool) {
                                itemList.add(
                                    PosmDeploymentData(
                                        data.product_id,
                                        data.category_id,
                                        data.brand_id,
                                        data.client_id,
                                        data.product_name,
                                        data.product_image,
                                        unit.toInt(),
                                        case.toInt()))
                            }
                        } else {
                            itemList.add(
                                PosmDeploymentData(
                                    data.product_id,
                                    data.category_id,
                                    data.brand_id,
                                    data.client_id,
                                    data.product_name,
                                    data.product_image,
                                    unit.toInt(),
                                    case.toInt()))
                        }
                    } catch (e: java.lang.Exception) {/**/}
                }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {/* */}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    var case =  binding.caseCount.text.toString()
                    when { case.isEmpty() -> case = "0"}
                    when {
                        s.isNotEmpty() -> binding.checkbox.apply {
                            isChecked = when { s.toString().toInt() > 0 || case.toInt() > 0 -> true else -> false }
                            isBoxChecked = isChecked
                        }
                        else -> binding.checkbox.apply {
                            isChecked = when { case.toInt() > 0 -> true else -> false }
                            isBoxChecked = isChecked
                        }
                    }
                }
            })


            binding.caseCount.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    var unit = binding.unitCount.text.toString()
                    var case = binding.caseCount.text.toString()
                    when{ unit.isEmpty() -> unit = "0" }
                    when{ case.isEmpty() -> case = "0" }

                    var bool = true;
                    try {
                        if (itemList.size > 0) {
                            for (item in itemList) {
                                if (data.product_id == item.product_id) {
                                    bool = false
                                    itemList.remove(item)
                                    itemList.add(
                                        PosmDeploymentData(
                                            data.product_id,
                                            data.category_id,
                                            data.brand_id,
                                            data.client_id,
                                            data.product_name,
                                            data.product_image,
                                            unit.toInt(),
                                            case.toInt()))
                                }
                                // body of loop
                            }
                            if (bool) {
                                itemList.add(

                                    PosmDeploymentData(
                                        data.product_id,
                                        data.category_id,
                                        data.brand_id,
                                        data.client_id,
                                        data.product_name,
                                        data.product_image,
                                        unit.toInt(),
                                        case.toInt()))
                            }
                        } else {
                            itemList.add(
                                PosmDeploymentData(
                                    data.product_id,
                                    data.category_id,
                                    data.brand_id,
                                    data.client_id,
                                    data.product_name,
                                    data.product_image,
                                    unit.toInt(),
                                    case.toInt()))
                        }
                    } catch (e: java.lang.Exception) {/**/}
                }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {/**/}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    var unit =  binding.unitCount.text.toString()
                    when { unit.isEmpty() -> unit = "0"}
                    when {
                        s.isNotEmpty() -> binding.checkbox.apply {
                            isChecked = when { s.toString().toInt() > 0 || unit.toInt() > 0 -> true else -> false }
                            isBoxChecked = isChecked
                        }
                        else -> binding.checkbox.apply {
                            isChecked = when { unit.toInt() > 0 -> true else -> false }
                            isBoxChecked = isChecked
                        }
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
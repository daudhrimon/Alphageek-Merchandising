package com.gdm.alphageek.view.adapter

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.product_availability.ProductAvailableData
import com.gdm.alphageek.data.remote.InputFilterMinMax
import com.gdm.alphageek.databinding.ProductExpiryItemsBinding
import com.gdm.alphageek.utils.isGONE
import com.gdm.alphageek.utils.isVISIBLE
import com.squareup.picasso.Picasso

class ProductExpiryAdapter(
    private val dataList: ArrayList<ProductAvailableData>,
    private val type: String,
    private val share: Boolean
) : RecyclerView.Adapter<ProductExpiryAdapter.ViewHolder>() {

    private var itemList = ArrayList<ProductAvailableData>()
    private var isBoxSelected = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ProductExpiryItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: ProductExpiryItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ProductAvailableData) {
            binding.itemCount.filters = arrayOf<InputFilter>(InputFilterMinMax(0,100000000))
            binding.checkbox.text = type
            if (data.availability_qty == 0){
                binding.deleteBtn.isGONE()
            } else {
                if (share) binding.deleteBtn.isGONE() else binding.deleteBtn.isVISIBLE()
                binding.itemCount.setText(data.availability_qty.toString())
                binding.checkbox.isChecked = true
                binding.itemCount.isEnabled = false
            }
            binding.productName.text = data.product_name
            try { Picasso.get().load(data.product_image).placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.productImage) }catch (e:Exception){/**/}

            binding.deleteBtn.setOnClickListener{
                itemList.remove(data)
                dataList.remove(data)
                notifyDataSetChanged()
            }

            binding.checkbox.setOnCheckedChangeListener { compoundButton, _ ->
                val count = binding.itemCount.text.toString()
                if (compoundButton.isChecked) {
                   if (count.isEmpty()){
                       binding.itemCount.requestFocus()
                       binding.itemCount.error = "Pieces"
                       compoundButton.isChecked = false
                       isBoxSelected = false
                   }else{
                       compoundButton.isChecked = true
                       isBoxSelected = true
                   }
                }else {
                    itemList.remove(data)
                }
            }

            binding.itemCount.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {

                    val count = binding.itemCount.text.toString()


                    var bool = true;

                    try {
                        if (itemList.size > 0) {
                            for (item in itemList) {
                                if (data.product_id == item.product_id) {
                                    bool = false
                                    itemList.remove(item)
                                    itemList.add(
                                        ProductAvailableData(data.product_id,data.category_id,data.brand_id,data.client_id,
                                            data.product_name,data.product_image,count.toInt(), 0)
                                    )

                                }
                                // body of loop
                            }
                            if (bool) {
                                itemList.add(               ProductAvailableData(data.product_id,data.category_id,data.brand_id,data.client_id,
                                    data.product_name,data.product_image,count.toInt(), 0)
                                )

                            }
                        } else {
                            itemList.add(               ProductAvailableData(data.product_id,data.category_id,data.brand_id,data.client_id,
                                data.product_name,data.product_image,count.toInt(), 0)
                            )
                        }
                    } catch (e: java.lang.Exception) {/**/}
                }
                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
                    if (s.isNotEmpty()){
                    if(Integer.parseInt(s.toString())>0){
                        binding.checkbox.isChecked=true
                        isBoxSelected = true
                    }else{
                        binding.checkbox.isChecked=false
                        isBoxSelected = false
                    }}else{
                        binding.checkbox.isChecked=false
                        isBoxSelected = false
                    }
                }
            })



        }
    }

    fun getListItems() = itemList

    fun getIsBoxChecked() = isBoxSelected

    fun clearData(){
        itemList.clear()
    }
}
package com.gdm.alphageek.view.ui.module.price_check

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.down_sync.Brand
import com.gdm.alphageek.data.local.price_check.PriceCheckData
import com.gdm.alphageek.databinding.DialogProductSelectBinding
import com.gdm.alphageek.databinding.FragmentPriceCheckBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.showErrorToast
import com.gdm.alphageek.view.adapter.PriceCheckAdapter
import com.gdm.alphageek.viewmodels.ProductViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class PriceCheckFragment : Fragment() {
    private lateinit var binding: FragmentPriceCheckBinding
    private val productViewModel: ProductViewModel by viewModels()
    private var brandList: ArrayList<Brand> = ArrayList()
    private var finalProductList: ArrayList<PriceCheckData> = ArrayList()
    private var brandID: Int? = null
    private var priceCheckAdapter: PriceCheckAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPriceCheckBinding.inflate(layoutInflater)

        //start
        val currentTime: DateFormat = SimpleDateFormat("HH:mm:ss")
        val calendar = Calendar.getInstance()
        Utils.startTime = currentTime.format(calendar.time)
     //end

        binding.continueBtn.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("product_list", Gson().toJson(finalProductList))
            findNavController().navigate(R.id.priceCheckImageFragment, bundle)
        }

        binding.addProductLayout.setOnClickListener {
            productViewModel.productList.postValue(ArrayList())
            showProductSelectDialog()
        }

        return binding.root
    }


    private fun showProductSelectDialog() {
        val builder = AlertDialog.Builder(context,R.style.Calender_dialog_theme)
        val customView = DialogProductSelectBinding.inflate(layoutInflater)
        builder.setView(customView.root)
        val alertDialog: AlertDialog = builder.create()
        var adapter = PriceCheckAdapter(ArrayList(), false)


        // get brand list
        productViewModel.getBrandList()
        productViewModel.brandList.observe(viewLifecycleOwner) {
            if (it != null) {
                brandList.clear()
                brandList.add(Brand(-1, "Select Brand"))
                brandList.addAll(it)
                customView.brandSpinner.adapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_dropdown_item,
                    brandList
                )
            }
        }

        // listen for product list according to brand
        productViewModel.productList.observe(viewLifecycleOwner) {
            if (it != null) {
                // set up the information
                val itemList = ArrayList<PriceCheckData>()
                customView.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                if (it.isNotEmpty()) {
                    for (i in it.indices) {
                        itemList.add(
                            PriceCheckData(
                                it[i].id,
                                it[i].category_id!!,
                                it[i].brand_id,
                                it[i].client_id!!,
                                it[i].product_name!!,
                                it[i].product_image!!,
                                it[i].unit_price!!
                            )
                        )
                    }
                }

                adapter = PriceCheckAdapter(itemList, false)
                customView.recyclerview.adapter = adapter

            }
        }


        // get selected brand
        customView.brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    val selectedItem = adapterView?.selectedItem?.toString()
                    if (selectedItem != "Select Brand" && selectedItem != null) {
                        brandID = brandList[customView.brandSpinner.selectedItemPosition].id
                        productViewModel.getProductByBrand(brandID!!)
                        Log.wtf("brandID", brandID.toString())

                    } else {
                        productViewModel.getProductByBrand(0)
                    }

                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }

        // save temp list
        customView.saveBtn.setOnClickListener {

            Log.wtf("itemList", Gson().toJson(adapter.getListItems()))

            // set up final list
            if (adapter.getListItems().isNotEmpty() && adapter.getIsBoxChecked()) {
                val arrList = ArrayList<PriceCheckData>()
                arrList.addAll(adapter.getListItems())


                for (i in 0 until arrList.size) {
                    var bool = true
                    for (j in 0 until finalProductList.size) {
                        if (arrList.get(i).product_id == finalProductList.get(j).product_id) {
                            bool = false
                            finalProductList.removeAt(j)
                            finalProductList.add(arrList.get(i))
                        }

                    }
                    if (bool) {
                        finalProductList.add(arrList.get(i))
                    }
                }
                setFinalRecyclerview(finalProductList)
            } else {
                showErrorToast(requireContext(),"No product selected !")
            }
            adapter.clearData()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun setFinalRecyclerview(productList: ArrayList<PriceCheckData>) {
        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
        priceCheckAdapter = PriceCheckAdapter(productList,false)
        binding.recyclerview.adapter = priceCheckAdapter
    }
}
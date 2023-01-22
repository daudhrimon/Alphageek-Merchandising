package com.gdm.alphageek.view.ui.module.posm

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
import com.gdm.alphageek.data.local.posm_products.PosmTrackingData
import com.gdm.alphageek.databinding.DialogProductSelectBinding
import com.gdm.alphageek.databinding.FragmentPosmTrackingBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.showErrorToast
import com.gdm.alphageek.view.adapter.PosmTrackingAdapter
import com.gdm.alphageek.viewmodels.ProductViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class PosmTrackingFragment : Fragment() {
    private lateinit var binding:FragmentPosmTrackingBinding
    private val productViewModel: ProductViewModel by viewModels()
    private var brandList: ArrayList<Brand> = ArrayList()
    private var finalProductList: ArrayList<PosmTrackingData> = ArrayList()
    private var brandID: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPosmTrackingBinding.inflate(layoutInflater)

        val currentTime: DateFormat = SimpleDateFormat("HH:mm:ss")
        val calendar = Calendar.getInstance()
        Utils.startTime =  currentTime.format(calendar.time)

        binding.addProductLayout.setOnClickListener {
            productViewModel.productListPosm.postValue(ArrayList())
            showProductSelectDialog()
        }
        binding.continueBtn.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("posm_tracking_list", Gson().toJson(finalProductList))
            findNavController().navigate(R.id.posmDeploymentFragment,bundle)
        }




        return binding.root
    }


    private fun showProductSelectDialog() {
        val builder = AlertDialog.Builder(context,R.style.Calender_dialog_theme)
        val customView = DialogProductSelectBinding.inflate(layoutInflater)
        builder.setView(customView.root)

        val alertDialog: AlertDialog = builder.create()
        var trackingAdapter = PosmTrackingAdapter(ArrayList())

        // get brand list
        productViewModel.getBrandList()
        productViewModel.brandList.observe(viewLifecycleOwner) {
            if (it != null) {
                brandList.clear()
                brandList.add(Brand(-1, "Select Brand"))
                brandList.addAll(it)
                customView.brandSpinner.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item,brandList)
            }
        }

        // listen for product list according to brand
        productViewModel.productListPosm.observe(viewLifecycleOwner) {
            if (it != null) {
                // set up the information
                val itemList = ArrayList<PosmTrackingData>()
                customView.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                if (it.isNotEmpty()) {
                    for (i in it.indices) {
                        itemList.add(
                            PosmTrackingData(
                                it[i].id,
                                it[i].category_id!!,
                                it[i].brand_id,
                                it[i].client_id!!,
                                it[i].product_name!!,
                                it[i].product_image,
                                availability_qty = null,
                                status = ""
                            )
                        )
                    }
                }

                trackingAdapter = PosmTrackingAdapter(itemList)
                customView.recyclerview.adapter = trackingAdapter

            }
        }


        // get selected brand
        customView.brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                    val selectedItem = adapterView?.selectedItem?.toString()
                    if (selectedItem != "Select Brand" && selectedItem != null) {
                        brandID = brandList[customView.brandSpinner.selectedItemPosition].id
                        productViewModel.getProductPosmByBrand(brandID!!)
                        Log.wtf("brandID", brandID.toString())

                    }else{
                        productViewModel.getProductPosmByBrand(0)
                    }
                }
                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }

        // save temp list
        customView.saveBtn.setOnClickListener {
            // set up final list
            if (trackingAdapter?.getListItems().isNotEmpty() && trackingAdapter.getIsBoxChecked()) {
                val arrList =ArrayList<PosmTrackingData>()
                arrList.addAll(trackingAdapter.getListItems())
                for (i in 0 until  arrList.size) {
                    var  bool =true
                    for (j in 0 until finalProductList.size) {
                        if (arrList[i].product_id== finalProductList[j].product_id){
                            bool=false
                            finalProductList.removeAt(j)
                            finalProductList.add(arrList[i])
                        }
                    }
                    if (bool){
                        finalProductList.add(arrList[i])
                    }
                }
                binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                binding.recyclerview.adapter = PosmTrackingAdapter(finalProductList)
            } else {
                showErrorToast(requireContext(),"No item selected !")
            }
            trackingAdapter.clearData()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
}
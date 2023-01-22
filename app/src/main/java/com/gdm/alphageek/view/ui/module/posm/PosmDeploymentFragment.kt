package com.gdm.alphageek.view.ui.module.posm

import android.annotation.SuppressLint
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
import com.gdm.alphageek.data.local.down_sync.Outlet
import com.gdm.alphageek.data.local.posm_products.PosmDeploymentData
import com.gdm.alphageek.data.local.visit.ScheduleVisit
import com.gdm.alphageek.databinding.DialogProductSelectBinding
import com.gdm.alphageek.databinding.FragmentPosmDeploymentBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.showErrorToast
import com.gdm.alphageek.utils.showSuccessToast
import com.gdm.alphageek.view.adapter.PosmDeploymentAdapter
import com.gdm.alphageek.view.ui.module.common.ShareDialog
import com.gdm.alphageek.viewmodels.DashboardViewModel
import com.gdm.alphageek.viewmodels.OutletViewModel
import com.gdm.alphageek.viewmodels.ProductViewModel
import com.gdm.alphageek.viewmodels.VisitViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class PosmDeploymentFragment : Fragment() {
    private lateinit var binding: FragmentPosmDeploymentBinding
    private val productViewModel: ProductViewModel by viewModels()
    private val visitViewModel: VisitViewModel by viewModels()
    private val outletViewModel : OutletViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private var outletData : Outlet? = null
    private var brandList: ArrayList<Brand> = ArrayList()
    private var finalProductList: ArrayList<PosmDeploymentData> = ArrayList()
    private var brandID: Int? = null
    private var isInternetAvailable = false

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPosmDeploymentBinding.inflate(layoutInflater)


        // get current outlet info
        outletViewModel.getOutletById(Utils.currentSchedule.outlet_id)
        outletViewModel.outlet.observe(viewLifecycleOwner){
            if (it != null) {
                outletData = it
            }
        }

        //get product layout
        binding.addProductLayout.setOnClickListener {
            productViewModel.productListPosm.postValue(ArrayList())
            showProductSelectDialog()
        }

        binding.shareBtn.setOnClickListener {
            if (finalProductList.isNotEmpty()) {
                ShareDialog(
                    requireContext(),
                    "POSM Tracking & Deployment",
                    "Outlet Name: ${outletData?.outlet_name ?: ""},\n Outlet Address: ${outletData?.outlet_address ?: ""},\nOutlet Phone: ${outletData?.outlet_phone ?: ""}",
                    null,
                    null,
                    arguments?.getString("posm_tracking_list").toString(),
                    Gson().toJson(finalProductList),
                    "POSM"
                ).show()
            } else {
                showErrorToast(requireContext(),"No item selected !")
            }
        }

        binding.completeBtn.setOnClickListener {
            if (finalProductList.isNotEmpty()){
                val currentDate: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                val currentTime: DateFormat = SimpleDateFormat("HH:mm:ss")
                val calendar = Calendar.getInstance()
                Utils.endTime =  currentTime.format(calendar.time)

                val visitDistance = Utils.getDistance(
                    (outletData?.gio_lat ?: "0.0").toDouble(),
                    (outletData?.gio_long ?: "0.0").toDouble(),
                    (Utils.gio_lat ?: "0.0").toDouble(),
                    (Utils.gio_long ?: "0.0").toDouble()
                )
                val isException = when { visitDistance <= 100.00-> "0" else-> "1" }

                // current date and time
                if (Utils.checkForInternet(requireActivity())) {
                    isInternetAvailable=true
                }

                // save data into db
                when {
                    outletData?.type_id != null &&
                            outletData?.country_id != null &&
                            outletData?.state_id != null &&
                            outletData?.region_id != null &&
                            outletData?.location_id != null -> {

                        visitViewModel.insertVisitData(
                            ScheduleVisit(0,
                                Utils.currentSchedule.schedule_id,
                                Utils.currentSchedule.outlet_id,
                                outletData!!.type_id,
                                outletData!!.channel_id,
                                currentDate.format(calendar.time),
                                currentTime.format(calendar.time),
                                outletData!!.country_id,
                                outletData!!.state_id,
                                outletData!!.region_id,
                                outletData!!.location_id,
                                4,
                                stat_time= Utils.startTime,
                                end_time= Utils.endTime,
                                gio_lat= Utils.gio_lat,
                                gio_long= Utils.gio_long,
                                is_exception=isException,
                                visit_distance=visitDistance.toString(),
                                isInternetAvailable=isInternetAvailable,
                                posm_tracking_list = arguments?.getString("posm_tracking_list").toString(),
                                posm_deploy_list = Gson().toJson(finalProductList)))
                    }
                    else -> showErrorToast(requireContext(),"Something went wrong !")
                }

                // update schedule table
                val scheduleItem = Utils.currentSchedule
                scheduleItem.posm_visit = 1
                dashboardViewModel.updateScheduleData(scheduleItem)
            } else {
                showErrorToast(requireContext(),"No item selected !")
            }
        }

        // create visit response
        visitViewModel.insertVisitResponse.observe(requireActivity()) {
            if (it != null) {
                if (it.toInt() != -1) {
                    showSuccessToast(requireContext(),"Posm visit successfully Added")
                    findNavController().navigate(R.id.dashboardFragment)
                } else {
                    showErrorToast(requireContext(),"Failed to visit POSM")
                }
            }
        }



        return binding.root
    }

    private fun showProductSelectDialog() {
        val builder = AlertDialog.Builder(context, R.style.Calender_dialog_theme)
        val customView = DialogProductSelectBinding.inflate(layoutInflater)
        builder.setView(customView.root)


        var deploymentAdapter = PosmDeploymentAdapter(ArrayList())

        val alertDialog: AlertDialog = builder.create()


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
        productViewModel.productListPosm.observe(viewLifecycleOwner) {
            if (it != null) {
                // set up the information
                val itemList = ArrayList<PosmDeploymentData>()
                customView.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                if (it.isNotEmpty()) {
                    for (i in it.indices) {
                        itemList.add(
                            PosmDeploymentData(
                                it[i].id,
                                it[i].category_id!!,
                                it[i].brand_id,
                                it[i].client_id!!,
                                it[i].product_name!!,
                                it[i].product_image,
                                0,
                                0
                            )
                        )
                    }
                }
                deploymentAdapter = PosmDeploymentAdapter(itemList)
                customView.recyclerview.adapter = deploymentAdapter
            }
        }

        // get selected brand
        customView.brandSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
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
            if (deploymentAdapter?.getListItems().isNotEmpty() && deploymentAdapter?.getIsBoxChecked()) {
                val arrList =ArrayList<PosmDeploymentData>()
                arrList.addAll(deploymentAdapter.getListItems())

                for (i in 0 until  arrList.size) {
                    var  bool =true
                    for (j in 0 until finalProductList.size) {
                        if (arrList.get(i).product_id== finalProductList[j].product_id){
                            bool = false
                            finalProductList.removeAt(j)
                            finalProductList.add(arrList[i])
                        }

                    }
                    if (bool){
                        finalProductList.add(arrList[i])
                    }
                }
                // finalProductList.addAll(adapter.getListItems())
                binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                binding.recyclerview.adapter = PosmDeploymentAdapter(finalProductList)
            } else {
                showErrorToast(requireContext(),"No item selected !")
            }

            deploymentAdapter.clearData()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
}
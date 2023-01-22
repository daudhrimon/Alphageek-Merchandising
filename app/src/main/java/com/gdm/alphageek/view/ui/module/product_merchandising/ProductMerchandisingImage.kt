package com.gdm.alphageek.view.ui.module.product_merchandising

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.down_sync.Outlet
import com.gdm.alphageek.data.local.image_model.ImageModel
import com.gdm.alphageek.data.local.visit.ScheduleVisit
import com.gdm.alphageek.databinding.FragmentProductMerchandisingImageBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.showErrorToast
import com.gdm.alphageek.utils.showSuccessToast
import com.gdm.alphageek.view.adapter.ImageViewAdapter
import com.gdm.alphageek.view.ui.module.common.ShareDialog
import com.gdm.alphageek.viewmodels.DashboardViewModel
import com.gdm.alphageek.viewmodels.OutletViewModel
import com.gdm.alphageek.viewmodels.VisitViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ProductMerchandisingImage : Fragment() {
    private lateinit var binding: FragmentProductMerchandisingImageBinding
    private val visitViewModel: VisitViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val outletViewModel: OutletViewModel by viewModels()
    private var outletData: Outlet? = null
    private var imageList = arrayListOf<Uri>()
    private var isInternetAvailable = false

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductMerchandisingImageBinding.inflate(layoutInflater)
//        Toast.makeText(activity, .toString(),Toast.LENGTH_LONG).show()
        // get current outlet info
        outletViewModel.getOutletById(Utils.currentSchedule.outlet_id)
        outletViewModel.outlet.observe(viewLifecycleOwner) {
            if (it != null) {
                outletData = it
            }
        }
        binding.addImage.setOnClickListener {
            ImagePicker.with(this)
                .crop()                                    //Crop image(Optional), Check Customization for more option
                .compress(1024)                    //Final image size will be less than 1 MB(Optional)
                .cameraOnly()
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        binding.shareBtn.setOnClickListener {
            if (imageList.isEmpty()) {
                showErrorToast(requireContext(),"Please upload image first")
            } else {
                Utils.imageShare = imageList
                ShareDialog(
                    requireContext(),
                    "Product Merchandising",
                    "Outlet Name: ${outletData?.outlet_name ?: ""},\n Outlet Address: ${outletData?.outlet_address ?: ""},\nOutlet Phone: ${outletData?.outlet_phone ?: ""}",
                    null,
                    null,
                    arguments?.getString("product_list").toString(),
                    arguments?.getString("questions_data").toString(),
                    "MERCHANDISING"
                ).show()
            }
        }

        binding.completeBtn.setOnClickListener {
            val currentDate: DateFormat = SimpleDateFormat("yyyy-MM-dd")
            val currentTime: DateFormat = SimpleDateFormat("HH:mm:ss")
            val calendar = Calendar.getInstance()
            Utils.endTime = currentTime.format(calendar.time)

            val visitDistance = Utils.getDistance(
                (outletData?.gio_lat ?: "0.0").toDouble(),
                (outletData?.gio_long ?: "0.0").toDouble(),
                (Utils.gio_lat ?: "0.0").toDouble(),
                (Utils.gio_long ?: "0.0").toDouble()
            )
            val isException = when { visitDistance <= 100.00-> "0" else-> "1" }

            val images = ArrayList<ImageModel>()
            if (imageList.isEmpty()) {
                showErrorToast(requireContext(),"Please upload image first")
            } else {
                for (items in imageList) {
                    images.add(
                        ImageModel(
                            Utils.convertImageToBase64(
                                requireActivity().contentResolver,
                                items
                            ).replace("\n", "").replace(" ", "")
                        )
                    )
                }


                // current date and time

                if (Utils.checkForInternet(requireActivity())) {
                    isInternetAvailable = true
                }
                // save data into db
                visitViewModel.insertVisitData(
                    ScheduleVisit(
                        Utils.visitId,
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
                        2,
                        Gson().toJson(images),
                        stat_time = Utils.startTime,
                        end_time = Utils.endTime,
                        gio_lat = Utils.gio_lat,
                        gio_long = Utils.gio_long,
                        is_exception = isException,
                        visit_distance = visitDistance.toString(),
                        isInternetAvailable = isInternetAvailable,
                        planogram_list = arguments?.getString("questions_data").toString(),
                        available_list = arguments?.getString("product_list").toString()
                    ),

                    )
                Utils.visitId++
                // update schedule table
                val scheduleItem = Utils.currentSchedule
                scheduleItem.merchandising_visit = 1

                dashboardViewModel.updateScheduleData(scheduleItem)
            }


        }

        // create visit response
        visitViewModel.insertVisitResponse.observe(requireActivity()) {
            if (it != null) {
                if (it.toInt() != -1) {
                    showSuccessToast(requireContext(),"Merchandising visit successfully Added")
                    findNavController().navigate(R.id.dashboardFragment)
                } else {
                    showErrorToast(requireContext(),"Failed to visit Merchandising")
                }
            }
        }



        return binding.root
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    if (imageList.size == 3) {
                        showErrorToast(requireContext(),"You can't add more than 3 pictures")
                    } else {
                        imageList.add(fileUri)
                        setupRecyclerview()
                    }
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(
                        requireActivity(),
                        ImagePicker.getError(data),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    Toast.makeText(requireActivity(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun setupRecyclerview() {
        when(imageList.size) {
            1 -> binding.recyclerView.layoutManager = GridLayoutManager(context,1)
            2 -> binding.recyclerView.layoutManager = GridLayoutManager(context,2)
            3 -> binding.recyclerView.layoutManager = GridLayoutManager(context,3)
        }
        binding.recyclerView.adapter = ImageViewAdapter(imageList)
    }
}
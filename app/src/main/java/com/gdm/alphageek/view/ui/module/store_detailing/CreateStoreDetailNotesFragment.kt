package com.gdm.alphageek.view.ui.module.store_detailing

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.down_sync.DetailingDashboard
import com.gdm.alphageek.data.local.down_sync.Outlet
import com.gdm.alphageek.data.local.image_model.ImageModel
import com.gdm.alphageek.data.local.store_detailing.DetailingPeople
import com.gdm.alphageek.data.local.store_detailing.StoreDetailingData
import com.gdm.alphageek.data.local.visit.ScheduleVisit
import com.gdm.alphageek.databinding.FragmentCreateStoreDetailNotesBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.showErrorToast
import com.gdm.alphageek.utils.showSuccessToast
import com.gdm.alphageek.view.adapter.ImageViewAdapter
import com.gdm.alphageek.view.ui.module.common.ShareDialog
import com.gdm.alphageek.viewmodels.DashboardViewModel
import com.gdm.alphageek.viewmodels.DetailScheduleViewModel
import com.gdm.alphageek.viewmodels.OutletViewModel
import com.gdm.alphageek.viewmodels.VisitViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CreateStoreDetailNotesFragment : Fragment() {
    private lateinit var binding: FragmentCreateStoreDetailNotesBinding
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private lateinit var detailingDashboard: DetailingDashboard
    private val detailScheduleViewModel: DetailScheduleViewModel by viewModels()
    private val outletViewModel: OutletViewModel by viewModels()
    private val visitViewModel: VisitViewModel by viewModels()
    private var scheduleId: Long = 0;
    private var outletData: Outlet? = null
    private lateinit var storeDetailingData: StoreDetailingData
    private lateinit var storeDetailingDataVisit: ScheduleVisit
    private var statusID = 0
    private var insertType: Int = 0
    private var imageList: ArrayList<Uri> = ArrayList()
    private var isInternetAvailable = false
    private var detailingStatus = 0
    private lateinit var imageUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateStoreDetailNotesBinding.inflate(layoutInflater)
        // check visit for update or insert

        scheduleId = arguments?.getLong("schedule_id")!!
        when(arguments?.getInt("insert_type")) {
            1 -> {
                insertType = 1
                visitViewModel.getVisitData(Utils.currentDetailingSchedule.schedule_id)
            }
            0 -> insertType = 0
        }

        // get current outlet info
        outletViewModel.getOutletById(Utils.currentDetailingSchedule.outlet_id)
        outletViewModel.outlet.observe(viewLifecycleOwner) {
            if (it != null) {
                outletData = it
                Log.i("outletg", it.outlet_id.toString())
                Log.i("outletg", it.type_id.toString())
                Log.i("outletg", it.channel_id.toString())
                Log.i("outletg", it.outlet_id.toString())
            }
        }


        binding.completeBtn.setOnClickListener {
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

            val status = binding.statusSpinner.selectedItem?.toString()
            val notes = binding.notes.text.toString()
            val images = ArrayList<ImageModel>()
            when {
                status == "Select Detailing Status" || status == null -> {
                    showErrorToast(requireContext(),"Select Detailing Status")
                }
                imageList.isEmpty() -> {
                    showErrorToast(requireContext(),"Please upload image first")
                }
                notes.isEmpty() -> {
                    binding.notes.requestFocus()
                    binding.notes.error = "Notes"
                }

                else -> {
                    for (items in imageList) {
                        images.add(
                            ImageModel(
                                Utils.convertImageToBase64(requireActivity().contentResolver, items)
                            )
                        )
                    }

                    var imageBitmap = Utils.convertImageToBase64(requireActivity().contentResolver, imageUri)
                    imageBitmap = imageBitmap.replace("\n", "")
                    // current date and time
                    val currentDate: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                    val currentTime: DateFormat = SimpleDateFormat("HH:mm:ss")
                    val calendar = Calendar.getInstance()

                    if (Utils.checkForInternet(requireActivity())) {
                        isInternetAvailable = true
                    }
                    if (insertType == 1) {
                        if (detailingStatus == statusID) {
                            showErrorToast(requireContext(),"Please change detailing status")
                        } else {
                            // Completed
                            try {
                                //end
                                val detailVisit = storeDetailingDataVisit
                                detailVisit.id = Utils.currentDetailingSchedule.schedule_id + 1
                                detailVisit.schedule_id = Utils.currentDetailingSchedule.schedule_id
                                detailVisit.outlet_id = Utils.currentDetailingSchedule.outlet_id
                                detailVisit.outlet_type_id = outletData?.type_id
                                detailVisit.outlet_channel_id = outletData?.channel_id
                                detailVisit.visit_date = currentDate.format(calendar.time)
                                detailVisit.visit_time = currentTime.format(calendar.time)
                                detailVisit.country_id = outletData?.country_id
                                detailVisit.state_id = outletData?.state_id
                                detailVisit.region_id = outletData?.region_id
                                detailVisit.location_id = outletData?.location_id
                                detailVisit.visit_type = 3
                                detailVisit.image_list = imageBitmap
                                detailVisit.notes = notes
                                detailVisit.stat_time = Utils.startTime
                                detailVisit.end_time = Utils.endTime
                                detailVisit.gio_lat = Utils.gio_lat
                                detailVisit.gio_long = Utils.gio_long
                                detailVisit.is_exception = isException
                                detailVisit.visit_distance = visitDistance.toString()
                                detailVisit.isInternetAvailable = isInternetAvailable
                                detailVisit.store_detailing_visit = Gson().toJson(
                                    outletData?.outlet_id?.let { it1 ->
                                        StoreDetailingData(
                                            storeDetailingData.id,
                                            Utils.currentDetailingSchedule.schedule_id,
                                            statusID.toString(),
                                            notes,
                                            outletData!!.outlet_name.toString(),
                                            it1,
                                            outletData!!.outlet_address,
                                            1,
                                            insertType
                                        )
                                    }
                                )
                                visitViewModel.insertVisitData(detailVisit)
                            } catch (e: Exception) {/**/ }
                        }
                    } else {
                        if (statusID == 1) {
                            // inProgress
                            visitViewModel.insertVisitData(
                                ScheduleVisit(
                                    Utils.currentDetailingSchedule.schedule_id,
                                    Utils.currentDetailingSchedule.schedule_id,
                                    Utils.currentDetailingSchedule.outlet_id,
                                    outletData?.type_id,
                                    outletData?.channel_id,
                                    currentDate.format(calendar.time),
                                    currentTime.format(calendar.time),
                                    outletData?.country_id,
                                    outletData?.state_id,
                                    outletData?.region_id,
                                    outletData?.location_id,
                                    3,
                                    image_list = imageBitmap,
                                    notes,
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    store_detailing_visit = Gson().toJson(
                                        outletData?.outlet_id?.let { it1 ->
                                            StoreDetailingData(
                                                0,
                                                Utils.currentDetailingSchedule.schedule_id,
                                                statusID.toString(),
                                                notes,
                                                outletData?.outlet_name.toString(),
                                                it1,
                                                outletData?.outlet_address,
                                                1,
                                                insertType
                                            )

                                        }
                                    ),
                                    stat_time = Utils.startTime,
                                    end_time = Utils.endTime,
                                    gio_lat = Utils.gio_lat,
                                    gio_long = Utils.gio_long,
                                    is_exception = isException,
                                    visit_distance = visitDistance.toString(),
                                    isInternetAvailable = isInternetAvailable,
                                ))
                        } else {
                            showErrorToast(requireContext(),"Select in progress")
                        }
                    }

                    // update schedule table
                    val scheduleItem = Utils.currentDetailingSchedule
                    scheduleItem.is_local = 1
                    scheduleItem.visit_status = statusID

                    detailScheduleViewModel.updateDetailSchedule(scheduleItem)
                }
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
                .createIntent { intent -> startForProfileImageResult.launch(intent) }
        }



        binding.shareBtn.setOnClickListener {
            val status = binding.statusSpinner.selectedItem?.toString()
            val notes = binding.notes.text.toString()
            when {
                status == "Select Detailing Status" || status == null -> {
                    showErrorToast(requireContext(),"Select Detailing Status")
                }
                imageList.isEmpty() -> {
                    showErrorToast(requireContext(),"Please upload image first")
                }
                notes.isEmpty() -> {
                    binding.notes.requestFocus()
                    binding.notes.error = "Notes"
                }
                else -> {
                    if (insertType == 1) {
                        if (detailingStatus == statusID) {
                            showErrorToast(requireContext(),"Please change detailing status")
                        } else {
                            // Completed
                            Utils.imageShare = imageList
                            ShareDialog(
                                requireContext(),
                                "Store Detailing ($status)",
                                "Outlet Name: ${outletData?.outlet_name ?: ""},\n Outlet Address: ${outletData?.outlet_address ?: ""},\nOutlet Phone: ${outletData?.outlet_phone ?: ""}",
                                null,
                                notes,
                                null,
                                null,
                                "DETAILING"
                            ).show()
                        }
                    } else {
                        if (statusID == 1) {
                            // inProgress
                            Utils.imageShare = imageList
                            ShareDialog(
                                requireContext(),
                                "Store Detailing ($status)",
                                "Outlet Name: ${outletData?.outlet_name ?: ""},\n Outlet Address: ${outletData?.outlet_address ?: ""},\nOutlet Phone: ${outletData?.outlet_phone ?: ""}",
                                null,
                                notes,
                                null,
                                null,
                                "DETAILING"
                            ).show()
                        } else {
                            showErrorToast(requireContext(),"Select in progress")
                        }
                    }
                }
            }
        }
        // dashboard data
        dashboardViewModel.getDetailDashboardData()
        dashboardViewModel.detailDashboardData.observe(viewLifecycleOwner) {
            if (it != null) {
                detailingDashboard = it
            }
        }
        // create visit response
        visitViewModel.insertVisitResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.toInt() != -1) {
                    // status items
                    if (statusID == 1) {
                        val dashboardItem = detailingDashboard
                        try {
                            dashboardItem.open_details = (dashboardItem.open_details?:0)+1
                            dashboardItem.total_added = (dashboardItem.total_added?:0)+1
                            val repsList = Gson().fromJson(Utils.currentDetailingSchedule.reps, Array<DetailingPeople>::class.java).asList()
                            dashboardItem.reps_details = (dashboardItem.reps_details?:0)+repsList.size
                            dashboardViewModel.updateDetailingDashboard(dashboardItem)
                        } catch (e: Exception) {/**/}
                    } else if (statusID == 2) {
                        val dashboardItem = detailingDashboard
                        try {
                            if ((dashboardItem.open_details ?: 0).toString().toInt() > 0) {
                                dashboardItem.open_details = (dashboardItem.open_details?:0)-1
                            }
                            dashboardItem.close_details = (dashboardItem.close_details?:0)+1
                            dashboardViewModel.updateDetailingDashboard(dashboardItem)
                        } catch (e: Exception) {/**/ }
                    }
                    showSuccessToast(requireContext(),"visit data successfully Added")
                    findNavController().navigate(R.id.storeDetailingDashboardFragment)
                } else {
                    showErrorToast(requireContext(),"Failed to insert visit data")
                }
            }
        }

        // current visit data
        visitViewModel.visitDataResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                Utils.currentDetailingSchedule.outlet_id
                try {
                    storeDetailingDataVisit = it
                    storeDetailingData =
                        Gson().fromJson(it.store_detailing_visit, StoreDetailingData::class.java)
                    // set spinner
                    binding.statusSpinner.setSelection(storeDetailingData.status.toInt())
                    detailingStatus = storeDetailingData.status.toInt()
                    binding.notes.setText(storeDetailingData.note)
                } catch (e: Exception) {/**/}
            }
        }

        binding.statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (selectedItem == "InProgress") {
                    statusID = 1
                } else if (selectedItem == "Completed") {
                    statusID = 2
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        //visit data response has no data

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
                        imageUri = fileUri
                        setupRecyclerview()
                    }
                    //binding.addImage.setImageResource(R.drawable.ic_camera_shape)
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
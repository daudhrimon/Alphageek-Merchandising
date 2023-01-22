package com.gdm.alphageek.view.ui.module.outlet_recruitment

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.down_sync.Dashboard
import com.gdm.alphageek.data.local.down_sync.Outlet
import com.gdm.alphageek.data.local.image_model.ImageModel
import com.gdm.alphageek.data.local.visit.ScheduleVisit
import com.gdm.alphageek.databinding.FragmentOutletRecruitmentVisitBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.showErrorToast
import com.gdm.alphageek.utils.showSuccessToast
import com.gdm.alphageek.view.ui.module.common.ShareDialog
import com.gdm.alphageek.viewmodels.DashboardViewModel
import com.gdm.alphageek.viewmodels.OutletViewModel
import com.gdm.alphageek.viewmodels.VisitViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class OutletRecruitmentVisit : Fragment() {
    private lateinit var binding: FragmentOutletRecruitmentVisitBinding
    private lateinit var dashboardData: Dashboard
    private var isInternetAvailable = false
    private var imageUri: Uri? = null
    private val visitViewModel: VisitViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val outletViewModel : OutletViewModel by viewModels()
    private var outletData : Outlet? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOutletRecruitmentVisitBinding.inflate(layoutInflater)

        val currentTime: DateFormat = SimpleDateFormat("HH:mm:ss")
        val calendar = Calendar.getInstance()
        Utils.startTime =  currentTime.format(calendar.time)

        // get current outlet info
        outletViewModel.getOutletById(Utils.currentSchedule.outlet_id)
        outletViewModel.outlet.observe(viewLifecycleOwner){
            if (it != null) {
                outletData = it
            }
        }
        // dashboard data
        dashboardViewModel.getDashboardData()
        dashboardViewModel.dashboardData.observe(requireActivity()){
            if (it != null){
                dashboardData=it
            }
        }


        binding.image.setOnClickListener {
            ImagePicker.with(this)
                .crop()                                    //Crop image(Optional), Check Customization for more option
                .compress(1024)                    //Final image size will be less than 1 MB(Optional)
                .cameraOnly()
                .maxResultSize(1080,1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        binding.shareBtn.setOnClickListener {
            when {
                imageUri == null -> { showErrorToast(requireContext(),"Please upload image first") }
                binding.notes.text.isEmpty() -> { showErrorToast(requireContext(),"Write something.....") }
                else -> {
                    ShareDialog(
                        requireContext(),
                        "Outlet Recruitment Visit",
                        "Outlet Name: ${outletData?.outlet_name ?: ""},\n Outlet Address: ${outletData?.outlet_address ?: ""},\nOutlet Phone: ${outletData?.outlet_phone ?: ""}",
                        null,
                        binding.notes.text.toString(),
                        null,
                        null,
                        "OUTLET"
                    ).show()
                }
            }
        }

        binding.completeBtn.setOnClickListener {
            // current date and time
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

            val file = imageUri?.path?.let { File(it) }

            when {
                imageUri == null -> { showErrorToast(requireContext(),"Please upload image first") }
                binding.notes.text.isEmpty() -> { showErrorToast(requireContext(),"Write something.....") }
                else -> {
                    val imageList = ArrayList<ImageModel>()
                    imageList.add(ImageModel(Utils.convertImageToBase64(requireActivity().contentResolver,imageUri!!).replace("\n","").replace(" ",""),))

                    if (Utils.checkForInternet(requireActivity())) {
                        isInternetAvailable=true
                    }
                    visitViewModel.insertVisitData(
                        ScheduleVisit(Utils.visitId,
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
                            1,
                            Gson().toJson(imageList),
                            binding.notes.text.toString(),
                            stat_time=Utils.startTime,
                            end_time= Utils.endTime,
                            gio_lat= Utils.gio_lat,
                            gio_long= Utils.gio_long,
                            is_exception=isException,
                            visit_distance=visitDistance.toString(),
                            isInternetAvailable=isInternetAvailable,
                        )
                    )

                    val dashboardItem = dashboardData
                    dashboardItem.pending_merch = (dashboardItem.pending_merch?:0)-1
                    dashboardItem.visited_merch = (dashboardItem.visited_merch?:0)+1
                    dashboardViewModel.updateDashboardData(dashboardItem)
                    Utils.visitId++
                    val scheduleItem = Utils.currentSchedule
                    scheduleItem.outlet_visit = 1
                    dashboardViewModel.updateScheduleData(scheduleItem)
                }
            }
        }


        // create visit response
        visitViewModel.insertVisitResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.toInt() != -1) {
                    showSuccessToast(requireContext(),"Recruitment visit successfully Added")
                    findNavController().navigate(R.id.dashboardFragment)
                } else {
                    showErrorToast(requireContext(),"Failed to visit Recruitment")
                }
            }
        }



        return binding.root
    }

    private val startForProfileImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!

                    imageUri = fileUri
                    val imageList = arrayListOf<Uri>()
                    imageList.add(fileUri)
                    Utils.imageShare = imageList
                    binding.image.setImageURI(fileUri)
                    Utils.convertImageToBase64(requireActivity().contentResolver,fileUri)
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
}
package com.gdm.alphageek.view.ui.module.common.notes

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.down_sync.Outlet
import com.gdm.alphageek.data.local.visit.ScheduleVisit
import com.gdm.alphageek.databinding.FragmentNotesBinding
import com.gdm.alphageek.utils.Constants
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.showErrorToast
import com.gdm.alphageek.utils.showSuccessToast
import com.gdm.alphageek.view.ui.module.common.ShareDialog
import com.gdm.alphageek.viewmodels.DashboardViewModel
import com.gdm.alphageek.viewmodels.OutletViewModel
import com.gdm.alphageek.viewmodels.VisitViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class NotesFragment : Fragment() {
    private lateinit var binding: FragmentNotesBinding
    private val visitViewModel: VisitViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val outletViewModel: OutletViewModel by viewModels()
    private var outletData: Outlet? = null
    private var isInternetAvailable = false

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotesBinding.inflate(layoutInflater)

        // get current outlet info
        outletViewModel.getOutletById(Utils.currentSchedule.outlet_id)
        outletViewModel.outlet.observe(viewLifecycleOwner) {
            if (it != null) {
                outletData = it
            }
        }

        // submit button action
        binding.completeBtn.setOnClickListener {
            if (Utils.checkForInternet(requireActivity())) { isInternetAvailable = true }
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

            val notes = binding.notes.text.toString()
            if (notes.isEmpty()) {
                showErrorToast(requireContext(),"Write Something.........")
            } else {
                when (Utils.currentPage) {
                    Constants.NAVIGATION_COMPETING_TRACKING -> {

                        // save data into db
                        visitViewModel.insertVisitData(
                            ScheduleVisit(
                                Utils.visitId,
                                Utils.currentSchedule.schedule_id,
                                Utils.currentSchedule.outlet_id,
                                outletData?.type_id,
                                outletData?.channel_id,
                                currentDate.format(calendar.time),
                                currentTime.format(calendar.time),
                                outletData?.country_id,
                                outletData?.state_id,
                                outletData?.region_id,
                                outletData?.location_id,
                                visit_type = 5,
                                stat_time= Utils.startTime,
                                end_time= Utils.endTime,
                                gio_lat= Utils.gio_lat,
                                gio_long= Utils.gio_long,
                                is_exception=isException,
                                visit_distance=visitDistance.toString(),
                                isInternetAvailable=isInternetAvailable,
                                image_list = arguments?.getString("image_list").toString(),
                                available_list = arguments?.getString("product_list").toString(),
                                promo_description_list = arguments?.getString("promo_description_list").toString(),
                                notes = notes
                            )
                        )
                        Utils.visitId++
                        // update schedule table
                        val scheduleItem = Utils.currentSchedule
                        scheduleItem.competition_visit = 1

                        dashboardViewModel.updateScheduleData(scheduleItem)
                    }
                    Constants.NAVIGATION_PRODUCT_FRESHNESS -> {
                        // save data into db
                        visitViewModel.insertVisitData(
                            ScheduleVisit(
                                0,
                                Utils.currentSchedule.schedule_id,
                                Utils.currentSchedule.outlet_id,
                                outletData?.type_id,
                                outletData?.channel_id,
                                currentDate.format(calendar.time),
                                currentTime.format(calendar.time),
                                outletData?.country_id,
                                outletData?.state_id,
                                outletData?.region_id,
                                outletData?.location_id,
                                6,
                                stat_time= Utils.startTime,
                                end_time= Utils.endTime,
                                gio_lat= Utils.gio_lat,
                                gio_long= Utils.gio_long,
                                is_exception=isException,
                                visit_distance=visitDistance.toString(),
                                isInternetAvailable=isInternetAvailable,
                                image_list = arguments?.getString("image_list").toString(),
                                available_list = arguments?.getString("product_list").toString(),
                                notes = notes
                            )

                        )

                        // update schedule table
                        val scheduleItem = Utils.currentSchedule
                        scheduleItem.freshness_visit = 1

                        dashboardViewModel.updateScheduleData(scheduleItem)
                    }
                    Constants.NAVIGATION_OUT_OF_STOCK -> {
                        // save data into db
                        visitViewModel.insertVisitData(
                            ScheduleVisit(
                                0,
                                Utils.currentSchedule.schedule_id,
                                Utils.currentSchedule.outlet_id,
                                outletData?.type_id,
                                outletData?.channel_id,
                                currentDate.format(calendar.time),
                                currentTime.format(calendar.time),
                                outletData?.country_id,
                                outletData?.state_id,
                                outletData?.region_id,
                                outletData?.location_id,
                                7,
                                stat_time= Utils.startTime,
                                end_time= Utils.endTime,
                                gio_lat= Utils.gio_lat,
                                gio_long= Utils.gio_long,
                                is_exception=isException,
                                visit_distance=visitDistance.toString(),
                                isInternetAvailable=isInternetAvailable,
                                available_list = arguments?.getString("product_list").toString(),
                                notes = notes
                            )

                        )

                        // update schedule table
                        val scheduleItem = Utils.currentSchedule
                        scheduleItem.oos_visit = 1

                        dashboardViewModel.updateScheduleData(
                            scheduleItem
                        )
                    }
                    Constants.NAVIGATION_PLANOGRAM -> {
                        // save data into db
                        visitViewModel.insertVisitData(
                            ScheduleVisit(
                                0,
                                Utils.currentSchedule.schedule_id,
                                Utils.currentSchedule.outlet_id,
                                outletData?.type_id,
                                outletData?.channel_id,
                                currentDate.format(calendar.time),
                                currentTime.format(calendar.time),
                                outletData?.country_id,
                                outletData?.state_id,
                                outletData?.region_id,
                                outletData?.location_id,
                                8,
                                stat_time= Utils.startTime,
                                end_time= Utils.endTime,
                                gio_lat= Utils.gio_lat,
                                gio_long= Utils.gio_long,
                                is_exception=isException,
                                visit_distance=visitDistance.toString(),
                                isInternetAvailable=isInternetAvailable,
                                planogram_list = arguments?.getString("questions_data").toString(),
                                image_list = arguments?.getString("image_list").toString(),
                                notes = notes
                            )

                        )

                        // update schedule table
                        val scheduleItem = Utils.currentSchedule
                        scheduleItem.planogram_visit = 1

                        dashboardViewModel.updateScheduleData(scheduleItem)
                    }
                    Constants.NAVIGATION_PRODUCT_ORDER -> {
                        // save data into db
                        visitViewModel.insertVisitData(
                            ScheduleVisit(
                                0,
                                Utils.currentSchedule.schedule_id,
                                Utils.currentSchedule.outlet_id,
                                outletData?.type_id,
                                outletData?.channel_id,
                                currentDate.format(calendar.time),
                                currentTime.format(calendar.time),
                                outletData?.country_id,
                                outletData?.state_id,
                                outletData?.region_id,
                                outletData?.location_id,
                                10,
                                stat_time= Utils.startTime,
                                end_time= Utils.endTime,
                                gio_lat= Utils.gio_lat,
                                gio_long= Utils.gio_long,
                                is_exception=isException,
                                visit_distance=visitDistance.toString(),
                                isInternetAvailable=isInternetAvailable,
                                available_list  = arguments?.getString("product_list").toString(),
                                notes = notes
                            )
                        )

                        // update schedule table
                        val scheduleItem = Utils.currentSchedule
                        scheduleItem.ordering_visit = 1

                        dashboardViewModel.updateScheduleData(
                            scheduleItem
                        )
                    }
                }

            }

        }


        // create visit response
        visitViewModel.insertVisitResponse.observe(requireActivity()) {
            if (it != null) {
                if (it.toInt() != -1) {
                    showSuccessToast(requireContext(),"visit data successfully Added")
                    findNavController().navigate(R.id.dashboardFragment)
                } else {
                    showErrorToast(requireContext(),"Failed to insert visit data")
                }
            }
        }


        // share
        binding.shareBtn.setOnClickListener {
            if (binding.notes.text.isNotEmpty()) {
                var module = ""
                var status = ""
                when(Utils.currentPage) {
                    Constants.NAVIGATION_PRODUCT_ORDER -> {
                        module = "Product Ordering"
                        status = "ORDERING"
                        Utils.planogramImgShare = null
                        Utils.imageShare = null
                    }
                    Constants.NAVIGATION_COMPETING_TRACKING -> {
                        module = "Competition Tracking"
                        status = "COMPETITION"
                        Utils.planogramImgShare = null
                    }
                    Constants.NAVIGATION_PRODUCT_FRESHNESS -> {
                        module = "Product Freshness"
                        status = "FRESHNESS"
                        Utils.planogramImgShare = null
                    }
                    Constants.NAVIGATION_OUT_OF_STOCK -> {
                        module = "Out-of-Stock Tracking"
                        status = "OUT_OF_STOCK"
                        Utils.planogramImgShare = null
                        Utils.imageShare = null
                    }
                    Constants.NAVIGATION_PLANOGRAM -> {
                        module = "Planogram Checks"
                        status = "PLANOGRAM"
                        Utils.imageShare = null
                    }
                }
                if (module.isNotEmpty() && status.isNotEmpty()) {
                    ShareDialog(
                        requireContext(),
                        module,
                        "Outlet Name: ${outletData?.outlet_name ?: ""},\n Outlet Address: ${outletData?.outlet_address ?: ""},\nOutlet Phone: ${outletData?.outlet_phone ?: ""}",
                        arguments?.getString("promo_description_list").toString(),
                        binding.notes.text.toString(),
                        arguments?.getString("product_list").toString(),
                        arguments?.getString("questions_data").toString(),
                        status
                    ).show()
                }
            } else {
                showErrorToast(requireContext(),"Write something.......!")
            }
        }


        return binding.root
    }
}

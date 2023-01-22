package com.gdm.alphageek.view.ui.module.common.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.down_sync.Outlet
import com.gdm.alphageek.data.local.down_sync.Schedule
import com.gdm.alphageek.databinding.FragmentCreateScheduleBinding
import com.gdm.alphageek.utils.showErrorToast
import com.gdm.alphageek.utils.showSuccessToast
import com.gdm.alphageek.viewmodels.DashboardViewModel
import com.gdm.alphageek.viewmodels.OutletViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CreateScheduleFragment : Fragment() {
    private lateinit var binding: FragmentCreateScheduleBinding
    private val outletViewModel: OutletViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private var outletList: ArrayList<Outlet> = ArrayList()
    private var outlet: Outlet? = null
    private var date: String = ""
    private var time: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateScheduleBinding.inflate(layoutInflater)

        // get all outlet
        outletViewModel.getAllOutlet()
        outletViewModel.outletList.observe(viewLifecycleOwner) {
            if (it != null) {
                outletList.clear()
                outletList.add(
                    Outlet(
                        -1,
                        -1,
                        "",
                        "",
                        "",
                        "",
                        "Select Outlet",
                        "",
                        -1,
                        -1,
                        -1,
                        -1,
                        "",
                        -1,
                        "",
                        "",
                        "",
                        "",
                        "",
                        0,
                        1,
                        0,
                        "",
                        null
                    )
                )
                outletList.addAll(it)
                binding.outletSpinner.adapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_dropdown_item,
                    outletList
                )
            }
        }

        // get selected outlet
        binding.outletSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (selectedItem != "Select Outlet" && selectedItem != null) {
                    outlet = outletList[binding.outletSpinner.selectedItemPosition]
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }


        //setup calender
        val myCalendar: Calendar = Calendar.getInstance()
        val dateSetListener: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { date_picker, year, monthOfYear, dayOfMonth ->
                date_picker.minDate = System.currentTimeMillis() - 1000
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "yyyy-MM-dd"
                val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)

                binding.dateTv.text = sdf.format(myCalendar.time)

                date = sdf.format(myCalendar.time)

            }

        binding.scheduleDateLayout.setOnClickListener {
            DatePickerDialog(
                requireActivity(),
                R.style.Calender_dialog_theme,
                dateSetListener,
                myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

        binding.scheduleTimeLayout.setOnClickListener {
            val hour = myCalendar[Calendar.HOUR_OF_DAY]
            val minute = myCalendar[Calendar.MINUTE]
            val mTimePicker = TimePickerDialog(
                requireActivity(), R.style.Calender_dialog_theme,
                { _, selectedHour, selectedMinute ->
                    val finalHour = when(selectedHour.toString()){"0","00"->"24" else-> selectedHour}
                    val finalMinute = when(selectedMinute.toString().length) {1-> "0${selectedMinute}" else-> selectedMinute.toString()}
                    time = "$finalHour:$finalMinute:00"
                    binding.timeTv.text = time
                }, hour, minute, true
            ) //Yes 24 hour time
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        }

        binding.saveBtn.setOnClickListener {
            when {
                outlet == null -> { showErrorToast(requireContext(),"Please select Outlet") }
                date.isEmpty() -> { showErrorToast(requireContext(),"Please select date") }
                time.isEmpty() -> { showErrorToast(requireContext(),"Please select time") }
                else -> {
                    try {
                        dashboardViewModel.insertNewSchedule(
                            Schedule(
                                System.currentTimeMillis(),
                                outlet!!.outlet_id,
                                "9.072264",
                                "7.491302",
                                outlet!!.location_name,
                                outlet!!.street_no + ", " + outlet?.street_name,
                                outlet!!.outlet_name,
                                date,
                                time,
                                outlet!!.country_id,
                                outlet!!.state_id,
                                outlet!!.region_id,
                                outlet!!.location_id,
                                is_local = 1
                            )
                        )
                    } catch (e: Exception) {/**/}
                }
            }

        }


        // create schedule response
        dashboardViewModel.insertSchedule.observe(requireActivity()) {
            if (it != null) {
                if (it.toInt() != -1) {
                    dashboardViewModel.getDashboardData()
                } else {
                    showErrorToast(requireContext(),"Failed to create Schedule")
                }
            }
        }

        dashboardViewModel.dashboardData.observe(requireActivity()) {
            if (it != null) {
                if (isToday(date)){
                    val dashboardItem = it
                    dashboardItem.pending_merch = (dashboardItem.pending_merch?:0)+1
                    dashboardItem.merchandise_visit = (dashboardItem.merchandise_visit?:0)+1
                    dashboardViewModel.updateDashboardData(dashboardItem)
                }
                showSuccessToast(requireContext(),"Schedule created successfully")
                findNavController().navigate(R.id.dashboardFragment)
            }
        }

        // handle error
        dashboardViewModel.errorMessage.observe(requireActivity()) {
            showErrorToast(requireContext(),it.toString())
        }


        return binding.root
    }

    private fun isToday(visitDate: String?): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val calendar = Calendar.getInstance()
            val currentDate = sdf.format(calendar.time)
            visitDate.equals(currentDate)
        } catch (e: Exception){
            false
        }
    }
}
package com.gdm.alphageek.view.ui.module.store_detailing

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.data.dates.DatesModel
import com.gdm.alphageek.databinding.FragmentStoreDetailingDashboardBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.isGONE
import com.gdm.alphageek.utils.isVISIBLE
import com.gdm.alphageek.view.adapter.CustomDateAdapter
import com.gdm.alphageek.view.adapter.DetailScheduleAdapter
import com.gdm.alphageek.viewmodels.DashboardViewModel
import com.gdm.alphageek.viewmodels.DetailScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class StoreDetailingDashboardFragment : Fragment() {
    private lateinit var binding: FragmentStoreDetailingDashboardBinding
    private val detailScheduleViewModel: DetailScheduleViewModel by viewModels()
    private val dashboardViewModel:DashboardViewModel by viewModels()
    private var customDateAdapter: CustomDateAdapter? = null
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoreDetailingDashboardBinding.inflate(layoutInflater)

        val calendar = Calendar.getInstance()
        val currentTime: DateFormat = SimpleDateFormat("HH:mm:ss")
        Utils.startTime =  currentTime.format(calendar.time)

        setupDates()

        // dashboard data
        dashboardViewModel.detailDashboardData.observe(viewLifecycleOwner){
            if (it != null){
                if ((customDateAdapter?.getRowIndex()?:0) == 0) {
                    binding.visitOpenDetailsTv.text = "Total : ${it.open_details?:"0"}"
                    binding.visitCloseDetailsTv.text = "Total : ${it.close_details ?: "0"}"
                    binding.visitCountTv.text = "Total : ${it.total_added ?: 0}"
                    binding.repPeople.text = "Total : ${it.reps_details ?: "0"}"
                } else {
                    binding.visitOpenDetailsTv.text = "Total : 0"
                    binding.visitCloseDetailsTv.text = "Total : 0"
                    binding.visitCountTv.text = "Total : 0"
                    binding.repPeople.text = "Total : 0"
                }
            }
        }

        // get  current date schedule data
        detailScheduleViewModel.getScheduleList(simpleDateFormat.format(calendar.time))
        detailScheduleViewModel.detailScheduleList.observe(viewLifecycleOwner){
            if (it != null){
                dashboardViewModel.getDetailDashboardData()
                binding.totalVisit.text = "You Have ${it.size} Store Visit Today"
                // set up the information
                if (it.isNotEmpty()){
                    binding.emptyViewLayout.isGONE()
                    binding.recyclerview.isVISIBLE()
                    binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                    binding.recyclerview.adapter = DetailScheduleAdapter(it,requireContext())
                }else{
                    binding.emptyViewLayout.isVISIBLE()
                    binding.recyclerview.isGONE()
                }
            }
        }

        //setup calender
        val myCalendar: Calendar = Calendar.getInstance()
        val dateSetListener: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "yyyy-MM-dd"
                val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)
            }

        binding.calenderBtn.setOnClickListener{
            DatePickerDialog(
                requireActivity(),
                R.style.Calender_dialog_theme,
                dateSetListener,
                myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]
            ).show()
        }
        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    private fun setupDates() {
        val dateFormat: DateFormat = SimpleDateFormat("dd")
        val dayFormat: DateFormat = SimpleDateFormat("EEE")
        val calendar = Calendar.getInstance()
        val datesModelList: MutableList<DatesModel> = ArrayList()
        for (i in 0..14) {
            datesModelList.add(DatesModel(dayFormat.format(calendar.time),dateFormat.format(calendar.time),simpleDateFormat.format(calendar.time)))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        customDateAdapter = CustomDateAdapter(datesModelList,requireActivity(),detailScheduleViewModel)
        binding.dateRecyclerview.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.HORIZONTAL,false)
        binding.dateRecyclerview.adapter = customDateAdapter
    }

}
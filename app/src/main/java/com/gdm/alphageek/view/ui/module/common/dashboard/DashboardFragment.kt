package com.gdm.alphageek.view.ui.module.common.dashboard

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
import com.gdm.alphageek.databinding.FragmentDashboardBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.isGONE
import com.gdm.alphageek.utils.isVISIBLE
import com.gdm.alphageek.view.adapter.CustomDateAdapter
import com.gdm.alphageek.view.adapter.ScheduleAdapter
import com.gdm.alphageek.viewmodels.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private val dashboardViewModel:DashboardViewModel by viewModels()
    private var customDateAdapter: CustomDateAdapter? = null
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(layoutInflater)
        Utils.planogramImgShare = null
        Utils.imageShare = null

        // set Start Time
        val calendar = Calendar.getInstance()
        val currentTime: DateFormat = SimpleDateFormat("HH:mm:ss")
        Utils.startTime =  currentTime.format(calendar.time)

        setupDates()

        // dashboard data
        dashboardViewModel.dashboardData.observe(viewLifecycleOwner){
            if (it != null){
                if ((customDateAdapter?.getRowIndex()?:0) == 0) {
                    binding.actualVisitTv.text = "Actual    : ${it.visited_merch?:0}"
                    binding.pendingVisitTv.text = "Pending : ${it.pending_merch?:0}"
                } else {
                    binding.actualVisitTv.text = "Actual    : 0"
                }
                binding.totalLogin.text = "Total : ${it.login_count?:0}"
                binding.totalProducts.text = "Total : ${(it.products?:0)}"
                binding.posmProducts.text = "Total : ${(it.posm_product?:0)}"
                binding.totalOutlet.text = "Total : ${(it.outlets?:0)}"
            }
        }

        // get  current date schedule data
        dashboardViewModel.getScheduleList(simpleDateFormat.format(calendar.time))
        dashboardViewModel.scheduleList.observe(viewLifecycleOwner) {
            if (it != null){
                dashboardViewModel.getDashboardData()
                binding.visitPlannedTv.text = "Planned : ${it.size?:0}"
                when{(customDateAdapter?.getRowIndex()?:0) != 0-> binding.pendingVisitTv.text = "Pending : ${it.size?:0}"}
                binding.totalVisit.text = "You Have ${it.size} Store Visit Today"
                    if (it.isNotEmpty()){
                        binding.emptyViewLayout.isGONE()
                        binding.recyclerview.isVISIBLE()
                        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                        binding.recyclerview.adapter = ScheduleAdapter(it,Utils.visitTypes(Utils.currentPage),requireContext())
                    }else{
                        binding.emptyViewLayout.isVISIBLE()
                        binding.recyclerview.isGONE()
                    }
            }
        }

        //setup calender
        val myCalendar: Calendar = Calendar.getInstance()
        val dateSetListener: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "yyyy-MM-dd"
                val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)
                dashboardViewModel.getScheduleList(sdf.format(myCalendar.time))
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
        customDateAdapter = CustomDateAdapter(datesModelList,requireContext(),dashboardViewModel)
        binding.dateRecyclerview.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.HORIZONTAL,false)
        binding.dateRecyclerview.adapter = customDateAdapter
    }
}
package com.gdm.alphageek.view.ui.module.common.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageek.databinding.FragmentScheduleTodayBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.isGONE
import com.gdm.alphageek.utils.isVISIBLE
import com.gdm.alphageek.view.adapter.ScheduleAdapter
import com.gdm.alphageek.viewmodels.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ScheduleTodayFragment : Fragment() {
    private lateinit var binding: FragmentScheduleTodayBinding
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleTodayBinding.inflate(layoutInflater)


        // get  current date schedule data
        val currentDate: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        dashboardViewModel.getScheduleList(currentDate.format(calendar.time))

        // observe schedule data
        dashboardViewModel.scheduleList.observe(requireActivity()){
            if (it != null){
                // set up the information
                if (it.isNotEmpty()){
                    binding.emptyViewLayout.isGONE()
                    binding.recyclerview.isVISIBLE()
                    binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                    binding.recyclerview.adapter = ScheduleAdapter(it, Utils.visitTypes(Utils.currentPage),requireContext())
                }else{
                    binding.emptyViewLayout.isVISIBLE()
                    binding.recyclerview.isGONE()
                }


            }
        }

        return binding.root
    }

}
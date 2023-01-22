package com.gdm.alphageek.view.ui.module.outlet_recruitment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.down_sync.RoutePlan
import com.gdm.alphageek.databinding.FragmentRoutePlanBinding
import com.gdm.alphageek.utils.isGONE
import com.gdm.alphageek.utils.isVISIBLE
import com.gdm.alphageek.view.adapter.LocationAdapter
import com.gdm.alphageek.viewmodels.DashboardViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoutePlanFragment : Fragment() {
    private lateinit var binding: FragmentRoutePlanBinding
    private val routeList = arrayListOf<RoutePlan>()
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoutePlanBinding.inflate(layoutInflater)

        // get route plans
        dashboardViewModel.getRoutePlanList()
        dashboardViewModel.routePlan.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                routeList.clear()
                routeList.addAll(it)
                Handler(Looper.getMainLooper()).postDelayed({
                    it.forEach { it1 ->
                        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(it1.day_of_week))
                    }
                    binding.tabLayout.getTabAt(0)?.select()
                },200)
            }
        }

        // observe schedule data
        dashboardViewModel.routeDetailList.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isNotEmpty()) {
                    Log.wtf("LOCATIONS",Gson().toJson(it))
                    binding.emptyViewLayout.isGONE()
                    binding.recyclerview.isVISIBLE()
                    binding.recyclerview.layoutManager = GridLayoutManager(requireActivity(), 2)
                    binding.recyclerview.adapter = LocationAdapter(it)
                    val bundle = Bundle()
                    bundle.putString("location_list",Gson().toJson(it))
                    childFragmentManager.beginTransaction().add(R.id.frameLayout,MapsFragment::class.java,bundle).commit()
                } else {
                    binding.emptyViewLayout.isVISIBLE()
                    binding.recyclerview.isGONE()
                }
            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {/**/}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                dashboardViewModel.getRoutePlanDetailList(routeList[tab?.position?:0].id)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {/**/}
        })

        return binding.root
    }
}
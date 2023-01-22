package com.gdm.alphageek.view.ui.module.planogram

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.databinding.FragmentPlanogramCheckBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.isVISIBLE
import com.gdm.alphageek.view.adapter.PlanogramQuestionsAdapter
import com.gdm.alphageek.viewmodels.MerchandisingViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class PlanogramCheckFragment : Fragment() {
    private lateinit var binding:FragmentPlanogramCheckBinding
    private val merchandisingViewModel: MerchandisingViewModel by viewModels()
    private var planogramQuestionsAdapter: PlanogramQuestionsAdapter? =  null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlanogramCheckBinding.inflate(layoutInflater)

        val currentTime: DateFormat = SimpleDateFormat("HH:mm:ss")
        val calendar = Calendar.getInstance()
        Utils.startTime =  currentTime.format(calendar.time)


        binding.continueBtn.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("questions_data", Gson().toJson(planogramQuestionsAdapter?.selectedItem()))
            findNavController().navigate(R.id.planogramImageFragment,bundle)
        }

        // get value from db
        merchandisingViewModel.getPlanogramQuestions()
        // observe schedule data
        merchandisingViewModel.planogramQuestions.observe(requireActivity()) {
            if (it != null) {
                // set up the information
                binding.recyclerview.isVISIBLE()
                binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                planogramQuestionsAdapter = PlanogramQuestionsAdapter(ArrayList(it), requireActivity(),false)
                binding.recyclerview.adapter = planogramQuestionsAdapter
            }
        }


        return binding.root
    }

}
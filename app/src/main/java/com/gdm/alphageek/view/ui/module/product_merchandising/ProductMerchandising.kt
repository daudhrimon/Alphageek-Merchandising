package com.gdm.alphageek.view.ui.module.product_merchandising

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.databinding.FragmentProductMerchandisingBinding
import com.gdm.alphageek.utils.isVISIBLE
import com.gdm.alphageek.view.adapter.PlanogramQuestionsAdapter
import com.gdm.alphageek.viewmodels.MerchandisingViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductMerchandising : Fragment() {
    private lateinit var binding: FragmentProductMerchandisingBinding
    private val merchandisingViewModel: MerchandisingViewModel by viewModels()
    private var planogramQuestionsAdapter: PlanogramQuestionsAdapter? =  null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductMerchandisingBinding.inflate(layoutInflater)

        binding.continueBtn.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("product_list",arguments?.getString("product_list").toString())
            bundle.putString("questions_data",Gson().toJson(planogramQuestionsAdapter?.selectedItem()))
            findNavController().navigate(R.id.productMerchandisingImage,bundle)
        }

        // get value from db
        merchandisingViewModel.getPlanogramQuestions()
        // observe schedule data
        merchandisingViewModel.planogramQuestions.observe(requireActivity()) {
            if (it != null) {
                // set up the information
                binding.recyclerview.isVISIBLE()
                binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                planogramQuestionsAdapter = PlanogramQuestionsAdapter(ArrayList(it),requireActivity(),false)
                binding.recyclerview.adapter = planogramQuestionsAdapter
            }
        }


        return binding.root
    }


}
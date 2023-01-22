package com.gdm.alphageek.view.ui.module.common.outlet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.databinding.FragmentOutletListBinding
import com.gdm.alphageek.utils.isGONE
import com.gdm.alphageek.utils.isVISIBLE
import com.gdm.alphageek.view.adapter.OutletListAdapter
import com.gdm.alphageek.viewmodels.OutletViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OutletListFragment : Fragment() {
    private lateinit var binding: FragmentOutletListBinding
    private val outletViewModel:OutletViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOutletListBinding.inflate(layoutInflater)


        // get value from db
        outletViewModel.getAllOutlet()
        // observe schedule data
        outletViewModel.outletList.observe(viewLifecycleOwner) {
            if (it != null){
                if (it.isNotEmpty()){
                    binding.emptyViewLayout.isGONE()
                    binding.recyclerview.isVISIBLE()
                    binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                    binding.recyclerview.adapter = OutletListAdapter(it,requireActivity())
                }else{
                    binding.emptyViewLayout.isVISIBLE()
                    binding.recyclerview.isGONE()
                }
            }
        }

        binding.createOutlet.setOnClickListener{
            findNavController().navigate(R.id.createOutletFragment)
        }

        return binding.root
    }
}
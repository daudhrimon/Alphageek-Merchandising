package com.gdm.alphageek.view.ui.module.store_detailing

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.databinding.FragmentStoreDetailNotesBinding
import com.gdm.alphageek.view.adapter.DetailNotesAdapter

class StoreDetailNotesFragment : Fragment() {
    private lateinit var binding:FragmentStoreDetailNotesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoreDetailNotesBinding.inflate(layoutInflater)

        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())

        val list = ArrayList<String>()
        list.add("")
        list.add("")
        list.add("")
        list.add("")
        list.add("")
        list.add("")
        list.add("")
        list.add("")
        binding.recyclerview.adapter = DetailNotesAdapter(list)

        binding.createNotes.setOnClickListener{
            findNavController().navigate(R.id.createStoreDetailNotesFragment)
        }

        return binding.root
    }

}
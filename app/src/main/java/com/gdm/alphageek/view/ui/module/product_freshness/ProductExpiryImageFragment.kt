package com.gdm.alphageek.view.ui.module.product_freshness

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.down_sync.Outlet
import com.gdm.alphageek.data.local.image_model.ImageModel
import com.gdm.alphageek.databinding.FragmentProductExpiryImageBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.showErrorToast
import com.gdm.alphageek.view.adapter.ImageViewAdapter
import com.gdm.alphageek.viewmodels.OutletViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ProductExpiryImageFragment : Fragment() {
    private lateinit var binding:FragmentProductExpiryImageBinding
    private val outletViewModel : OutletViewModel by viewModels()
    private var outletData : Outlet? = null
    private var imageList: ArrayList<Uri> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProductExpiryImageBinding.inflate(layoutInflater)


        // get current outlet info
        outletViewModel.getOutletById(Utils.currentSchedule.outlet_id)
        outletViewModel.outlet.observe(viewLifecycleOwner) {
            if (it != null) {
                outletData = it
            }
        }


        binding.addImage.setOnClickListener {
            ImagePicker.with(this)
                .crop()                                    //Crop image(Optional), Check Customization for more option
                .compress(1024)                    //Final image size will be less than 1 MB(Optional)
                .cameraOnly()
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }


        binding.continueBtn.setOnClickListener {
            val images = ArrayList<ImageModel>()
            if (imageList.isEmpty()) {
                showErrorToast(requireContext(),"Please upload image first")
            } else {
                for (items in imageList) {
                    images.add(
                        ImageModel(
                            Utils.convertImageToBase64(
                                requireActivity().contentResolver,
                                items
                            )
                        )
                    )
                }

                val bundle = Bundle()
                bundle.putString("product_list", arguments?.getString("product_list").toString())
                bundle.putString("image_list", Gson().toJson(images))
                findNavController().navigate(R.id.notesFragment,bundle)
            }

        }


        return binding.root
    }


    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    if (imageList.size == 3) {
                        showErrorToast(requireContext(),"You can't add more than 3 pictures")
                    } else {
                        imageList.add(fileUri)
                        setupRecyclerview()
                    }
                //binding.addImage.setImageResource(R.drawable.ic_camera_shape)
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(
                        requireActivity(),
                        ImagePicker.getError(data),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    Toast.makeText(requireActivity(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun setupRecyclerview() {
        when(imageList.size) {
            1 -> binding.recyclerView.layoutManager = GridLayoutManager(context,1)
            2 -> binding.recyclerView.layoutManager = GridLayoutManager(context,2)
            3 -> binding.recyclerView.layoutManager = GridLayoutManager(context,3)
        }
        Utils.imageShare = imageList
        binding.recyclerView.adapter = ImageViewAdapter(imageList)
    }

}
package com.gdm.alphageek.view.ui.module.competing_tracking

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.competition_tracking.PromoDescriptionData
import com.gdm.alphageek.data.local.image_model.ImageModel
import com.gdm.alphageek.databinding.FragmentProductCompetingImageBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.showErrorToast
import com.gdm.alphageek.view.adapter.ImageViewAdapter
import com.gdm.alphageek.view.adapter.PromoDescriptionAdapter
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson

class ProductCompetingImageFragment : Fragment() {
    private lateinit var binding: FragmentProductCompetingImageBinding
    private var imageList: ArrayList<Uri> = ArrayList()
    private var promoDescription: ArrayList<PromoDescriptionData> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductCompetingImageBinding.inflate(layoutInflater)


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
            } else if (promoDescription.isEmpty()){
                showErrorToast(requireContext(),"Please add promo description")
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
                bundle.putString("promo_description_list", Gson().toJson(promoDescription))
                findNavController().navigate(R.id.notesFragment,bundle)
            }
        }

        binding.addBtn.setOnClickListener{
            val data = binding.promoDescription.text.toString()
            if (data.isEmpty()){
                binding.promoDescription.error = "promo description"
            }else{
                promoDescription.add(PromoDescriptionData(data))
                binding.promoRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                binding.promoRecyclerview.adapter = PromoDescriptionAdapter(promoDescription,false)
                binding.promoDescription.setText("")
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

                    if (imageList.size == 4) {
                        showErrorToast(requireContext(),"You can't add more than 4 pictures")
                    } else {
                        imageList.add(fileUri)
                        setupRecyclerview()
                    }
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
            4 -> binding.recyclerView.layoutManager = GridLayoutManager(context,2)
        }
        binding.recyclerView.adapter = ImageViewAdapter(imageList)
        Utils.imageShare = imageList
    }
}
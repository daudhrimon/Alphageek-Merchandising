package com.gdm.alphageek.view.ui.module.planogram

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.planogram_image.PlanogramImage
import com.gdm.alphageek.data.local.planogram_image.PlanogramTempImage
import com.gdm.alphageek.databinding.FragmentPlanogramImageBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.showErrorToast
import com.gdm.alphageek.view.adapter.PlanogramImageAdapter
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson

class PlanogramImageFragment : Fragment() {
    private lateinit var binding:FragmentPlanogramImageBinding
    private var firstImageUri:Uri? = null
    private var imageList = ArrayList<PlanogramTempImage>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlanogramImageBinding.inflate(layoutInflater)


        binding.continueBtn.setOnClickListener{
            findNavController().navigate(R.id.notesFragment)
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
                    startForFirstImageResult.launch(intent)
                }
        }

        binding.continueBtn.setOnClickListener {
            val images = ArrayList<PlanogramImage>()
            if (imageList.isEmpty()) {
                showErrorToast(requireContext(),"Please upload image first")
            } else {
                for (items in imageList) {
                    images.add(
                        PlanogramImage(
                            Utils.convertImageToBase64(
                                requireActivity().contentResolver,
                                items.beforeImage
                            ),
                            Utils.convertImageToBase64(
                                requireActivity().contentResolver,
                                items.afterImage
                            )
                        )
                    )
                }

                val bundle = Bundle()
                bundle.putString("questions_data", arguments?.getString("questions_data").toString())
                bundle.putString("image_list", Gson().toJson(images))
                findNavController().navigate(R.id.notesFragment,bundle)
            }
        }

        return binding.root
    }


    private val startForFirstImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    firstImageUri = data?.data!!
                    // call again for second image
                    ImagePicker.with(this)
                        .crop()
                        .compress(1024)
                        .cameraOnly()
                        .maxResultSize(
                            1080,
                            1080
                        )
                        .createIntent { intent ->
                            startForSecondImageResult.launch(intent)
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

    private val startForSecondImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val secondUri = data?.data!!

                    imageList.add(PlanogramTempImage(firstImageUri!!,secondUri))
                    setupRecyclerview()
                    Utils.planogramImgShare = imageList
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
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = PlanogramImageAdapter(imageList)
    }
}
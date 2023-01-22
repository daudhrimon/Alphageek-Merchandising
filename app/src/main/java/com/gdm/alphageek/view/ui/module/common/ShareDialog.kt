package com.gdm.alphageek.view.ui.module.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.competition_tracking.PromoDescriptionData
import com.gdm.alphageek.data.local.down_sync.PlanogramQuestions
import com.gdm.alphageek.data.local.out_of_stock.OutOfStockData
import com.gdm.alphageek.data.local.posm_products.PosmDeploymentData
import com.gdm.alphageek.data.local.posm_products.PosmTrackingData
import com.gdm.alphageek.data.local.price_check.PriceCheckData
import com.gdm.alphageek.data.local.product_availability.ProductAvailableData
import com.gdm.alphageek.data.local.product_order.ProductOrderData
import com.gdm.alphageek.databinding.DialogShareBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.isVISIBLE
import com.gdm.alphageek.view.adapter.*
import com.gkemon.XMLtoPDF.PdfGenerator
import com.gkemon.XMLtoPDF.PdfGeneratorListener
import com.gkemon.XMLtoPDF.model.FailureResponse
import com.gkemon.XMLtoPDF.model.SuccessResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ShareDialog(context: Context,
                  private val module: String?,
                  private val moduleInfo: String?,
                  private val descriptionList: String?,
                  private val note: String?,
                  private val productList: String?,
                  private val questionData: String?,
                  private val status: String) : Dialog(context) {

    private lateinit var binding: DialogShareBinding
    private var shareMenu: PopupMenu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogShareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)
        val width = context.resources.displayMetrics.widthPixels
        window?.setLayout((8 * width) / 9, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.module.text = module ?: ""
        binding.moduleInfo.text = moduleInfo ?: ""

        when(status) {
            "OUTLET" -> {
                setImagesAdapter()
            }
            "MERCHANDISING" -> {
                setProductAvailabilityAdepter()
                setQuestionsAdapter()
                setImagesAdapter()
            }
            "ORDERING" -> {
                if (productList != null && productList.isNotEmpty()) {
                    val productsList = Gson().fromJson<ArrayList<ProductOrderData>?>(productList,object : TypeToken<ArrayList<ProductOrderData>?>(){}.type)
                    if (productsList != null && productsList.isNotEmpty()) {
                        binding.productLayout.isVISIBLE()
                        binding.productRecycler.adapter = ProductOrderAdapter(productsList,true)
                    }
                }
            }
            "POSM" -> {
                if (productList != null && productList.isNotEmpty()) {
                    val trackingList = Gson().fromJson<ArrayList<PosmTrackingData>?>(productList,object : TypeToken<ArrayList<PosmTrackingData>?>(){}.type)
                    if (trackingList != null && trackingList.isNotEmpty()) {
                        binding.productsTv.text = "POSM Tracking Products"
                        binding.productLayout.isVISIBLE()
                        binding.productRecycler.adapter = PosmTrackingAdapter(trackingList,true)
                    }
                }
                if (questionData != null && questionData.isNotEmpty()) {
                    val deployList = Gson().fromJson<ArrayList<PosmDeploymentData>?>(questionData,object : TypeToken<ArrayList<PosmDeploymentData>?>(){}.type)
                    if (deployList != null && deployList.isNotEmpty()) {
                        binding.questionTv.text = "POSM Deployment Products"
                        binding.questionLayout.isVISIBLE()
                        binding.questionRecycler.adapter = PosmDeploymentAdapter(deployList,true)
                    }
                }
            }
            "COMPETITION" -> {
                setProductAvailabilityAdepter()

                if (descriptionList != null && descriptionList.isNotEmpty()) {
                    val promoDescription = Gson().fromJson<ArrayList<PromoDescriptionData>?>(descriptionList,object : TypeToken<ArrayList<PromoDescriptionData>?>(){}.type)
                    if (promoDescription != null && promoDescription.isNotEmpty()) {
                        binding.questionTv.text = "Promo Description"
                        binding.questionLayout.isVISIBLE()
                        binding.questionRecycler.adapter = PromoDescriptionAdapter(promoDescription,true)
                    }
                }
                setImagesAdapter()
            }
            "FRESHNESS" -> {
                if (productList != null && productList.isNotEmpty()) {
                    val productsList = Gson().fromJson<ArrayList<ProductAvailableData>?>(productList,object : TypeToken<ArrayList<ProductAvailableData>?>(){}.type)
                    if (productsList != null && productsList.isNotEmpty()) {
                        binding.productLayout.isVISIBLE()
                        binding.productRecycler.adapter = ProductExpiryAdapter(productsList,"Expired",true)
                    }
                }
                setImagesAdapter()
            }
            "OUT_OF_STOCK" -> {
                if (productList != null && productList.isNotEmpty()) {
                    val productsList = Gson().fromJson<ArrayList<OutOfStockData>?>(productList,object : TypeToken<ArrayList<OutOfStockData>?>(){}.type)
                    if (productsList != null && productsList.isNotEmpty()) {
                        binding.productLayout.isVISIBLE()
                        binding.productRecycler.adapter = OutOfStockAdapter(productsList,true)
                    }
                }
            }
            "PLANOGRAM" -> {
                setQuestionsAdapter()
                Utils.planogramImgShare?.let { if (it.isNotEmpty()) {
                        Utils.imageShare = null
                        binding.imageLayout.isVISIBLE()
                        binding.imageRecycler.layoutManager = LinearLayoutManager(context)
                        binding.imageRecycler.adapter = PlanogramImageAdapter(Utils.planogramImgShare!!)
                } }
            }
            "PRICING" -> {
                if (productList != null && productList.isNotEmpty()) {
                    val productsList = Gson().fromJson<ArrayList<PriceCheckData>?>(productList,object : TypeToken<ArrayList<PriceCheckData>?>(){}.type)
                    if (productsList != null && productsList.isNotEmpty()) {
                        binding.productLayout.isVISIBLE()
                        binding.productRecycler.adapter = PriceCheckAdapter(productsList,true)
                    }
                }
                setImagesAdapter()
            }
            "DETAILING" -> {
                setImagesAdapter()
            }
        }

        if (note != null && note.isNotEmpty()) {
            binding.noteLayout.isVISIBLE()
            binding.note.text = note
        }

        shareMenu = PopupMenu(context,binding.shareBtn)
        shareMenu?.menuInflater?.inflate(R.menu.share_menu,shareMenu?.menu)
        shareMenu?.setForceShowIcon(true)
        shareMenu?.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem: MenuItem? ->
            when(menuItem?.itemId) {
                R.id.share -> {
                    PdfGenerator.getBuilder()
                        .setContext(context)
                        .fromViewSource()
                        .fromView(binding.content)
                        .setFileName(module)
                        .setFolderNameOrPath("com.alphageek.share")
                        .actionAfterPDFGeneration(PdfGenerator.ActionAfterPDFGeneration.SHARE)
                        .build( object : PdfGeneratorListener() {
                            override fun onFailure(failureResponse: FailureResponse) { super.onFailure(failureResponse) }
                            override fun showLog(log: String) { super.showLog(log) }
                            override fun onStartPDFGeneration() {/**/}
                            override fun onFinishPDFGeneration() {/**/}
                            override fun onSuccess(response: SuccessResponse) { super.onSuccess(response) }
                        })
                }
                R.id.save -> {
                    PdfGenerator.getBuilder()
                        .setContext(context)
                        .fromViewSource()
                        .fromView(binding.content)
                        .setFileName(module)
                        .setFolderNameOrPath("com.alphageek.share")
                        .actionAfterPDFGeneration(PdfGenerator.ActionAfterPDFGeneration.OPEN)
                        .build( object : PdfGeneratorListener() {
                            override fun onFailure(failureResponse: FailureResponse) { super.onFailure(failureResponse) }
                            override fun showLog(log: String) { super.showLog(log) }
                            override fun onStartPDFGeneration() {/**/}
                            override fun onFinishPDFGeneration() {/**/}
                            override fun onSuccess(response: SuccessResponse) { super.onSuccess(response) }
                        })
                }
            }
            return@OnMenuItemClickListener true
        })

        binding.shareBtn.setOnClickListener {
           shareMenu?.show()
        }
    }

    private fun setProductAvailabilityAdepter() {
        if (productList != null && productList.isNotEmpty()) {
            val productsList = Gson().fromJson<ArrayList<ProductAvailableData>?>(productList,object : TypeToken<ArrayList<ProductAvailableData>?>(){}.type)
            if (productsList != null && productsList.isNotEmpty()) {
                binding.productLayout.isVISIBLE()
                binding.productRecycler.adapter = ProductAvailabilityAdapter(productsList, context,"Available",true)
            }
        }
    }

    private fun setQuestionsAdapter() {
        if (questionData != null && questionData.isNotEmpty()) {
            val questionsData = Gson().fromJson<ArrayList<PlanogramQuestions>?>(questionData,object : TypeToken<ArrayList<PlanogramQuestions>?>(){}.type)
            if (questionsData != null && questionsData.isNotEmpty()) {
                binding.questionLayout.isVISIBLE()
                binding.questionRecycler.adapter = PlanogramQuestionsAdapter(questionsData,context,true)
            }
        }
    }

    private fun setImagesAdapter() {
        if (!Utils.imageShare.isNullOrEmpty()){
            when(Utils.imageShare!!.size) {
                1 -> binding.imageRecycler.layoutManager = GridLayoutManager(context,1)
                2 -> binding.imageRecycler.layoutManager = GridLayoutManager(context,2)
                3 -> binding.imageRecycler.layoutManager = GridLayoutManager(context,3)
                4 -> binding.imageRecycler.layoutManager = GridLayoutManager(context,2)
            }
            binding.imageLayout.isVISIBLE()
            binding.imageRecycler.adapter = ImageViewAdapter(Utils.imageShare!!)
        }
    }
}
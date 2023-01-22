package com.gdm.alphageek.view.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.down_sync.Schedule
import com.gdm.alphageek.databinding.DialogOutletDetailsBinding
import com.gdm.alphageek.databinding.ScheduleItemsBinding
import com.gdm.alphageek.utils.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ScheduleAdapter(
    private val dataList: List<Schedule>,
    private val visitType: String,
    private val context: Context
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ScheduleItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: ScheduleItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n")
        fun bind(data: Schedule) {
            if (data.getPropertyValue<Int>(visitType) == 1){
                binding.visitScheduleLayout.isGONE()
                binding.detailsLayout.isVISIBLE()
            }else{
                binding.detailsLayout.isGONE()
                binding.visitScheduleLayout.isVISIBLE()
            }

            binding.visitScheduleLayout.setOnClickListener {
                val scheduledate = LocalDate.parse(data.schedule_date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                val date = Date()
                val currentDateee = LocalDate.parse(dateFormat.format(date), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val cmp = scheduledate.compareTo(currentDateee)
                if (cmp==0) {
                Utils.currentSchedule = data
                when (Utils.currentPage) {
                    Constants.NAVIGATION_OUTLET -> {
                        Navigation.createNavigateOnClickListener(R.id.outletRecruitmentVisit)
                            .onClick(binding.visitScheduleLayout)
                    }
                    Constants.NAVIGATION_MERCHANDISING -> {
                        Navigation.createNavigateOnClickListener(R.id.productAvailability)
                            .onClick(binding.visitScheduleLayout)
                    }
                    Constants.NAVIGATION_POSM -> {
                        Navigation.createNavigateOnClickListener(R.id.posmTrackingFragment)
                            .onClick(binding.visitScheduleLayout)
                    }
                    Constants.NAVIGATION_COMPETING_TRACKING -> {
                        Navigation.createNavigateOnClickListener(R.id.competingTrackingFragment)
                            .onClick(binding.visitScheduleLayout)
                    }
                    Constants.NAVIGATION_PRODUCT_FRESHNESS -> {
                        Navigation.createNavigateOnClickListener(R.id.productExpiryFragment)
                            .onClick(binding.visitScheduleLayout)
                    }
                    Constants.NAVIGATION_OUT_OF_STOCK -> {
                        Navigation.createNavigateOnClickListener(R.id.outOfStockTrackingFragment)
                            .onClick(binding.visitScheduleLayout)
                    }
                    Constants.NAVIGATION_PLANOGRAM -> {
                        Navigation.createNavigateOnClickListener(R.id.planogramCheckFragment)
                            .onClick(binding.visitScheduleLayout)
                    }
                    Constants.NAVIGATION_PRICE_CHECK -> {
                        Navigation.createNavigateOnClickListener(R.id.priceCheckFragment)
                            .onClick(binding.visitScheduleLayout)
                    }
                    Constants.NAVIGATION_PRODUCT_ORDER -> {
                        Navigation.createNavigateOnClickListener(R.id.productOrderingFragment)
                            .onClick(binding.visitScheduleLayout)
                    } }
                } else {
                    showErrorToast(context,"Future date schedule visit not allowed")
                }
            }

            // set up information
            binding.outletName.text = data.outlet_name
            binding.address.text = data.outlet_address
            binding.date.text = "Date : ${data.schedule_date}"
            binding.time.text = "Time : ${data.schedule_time}"
            binding.locationName.text = data.location_name


            binding.detailsLayout.setOnClickListener{
                showProductSelectDialog(data)
            }
        }
    }

    private fun showProductSelectDialog(data: Schedule) {
        val builder = AlertDialog.Builder(context,R.style.Calender_dialog_theme)
        val customView = DialogOutletDetailsBinding.bind(LayoutInflater.from(context).inflate(R.layout.dialog_outlet_details,null))
        builder.setView(customView.root)
        val alertDialog: AlertDialog = builder.create()
        customView.tvoutletName.text = data.outlet_name
        customView.outletPhone.text = data.schedule_date
        customView.outletaddress.text = data.location_name
        customView.saveBtn.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    inline fun <reified T : Any> Any.getPropertyValue(propertyName: String): T? {
        val getterName = "get" + propertyName.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        return try {
            javaClass.getMethod(getterName).invoke(this) as? T
        } catch (e: NoSuchMethodException) {
            null
        }
    }
}
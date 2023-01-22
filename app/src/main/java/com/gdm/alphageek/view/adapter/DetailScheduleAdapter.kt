package com.gdm.alphageek.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation.createNavigateOnClickListener
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.down_sync.DetailingSchedule
import com.gdm.alphageek.data.local.store_detailing.DetailingPeople
import com.gdm.alphageek.data.local.store_detailing.DetailingTopic
import com.gdm.alphageek.databinding.DetailingScheduleItemsBinding
import com.gdm.alphageek.utils.Utils
import com.gdm.alphageek.utils.showErrorToast
import com.google.gson.Gson
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class DetailScheduleAdapter(
    private val dataList: List<DetailingSchedule>,
    private val context: Context
) : RecyclerView.Adapter<DetailScheduleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DetailingScheduleItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: DetailingScheduleItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n")
        fun bind(data: DetailingSchedule) {
            // set up information
            binding.outletName.text = data.outlet_name
            binding.address.text = data.outlet_address

            // showing list
            // out of stock list
            val peopleList = Gson().fromJson(data.reps, Array<DetailingPeople>::class.java).asList()
            val topicList = Gson().fromJson(data.topics, Array<DetailingTopic>::class.java).asList()
            var people = ""
            var topics = ""
            for (items in peopleList) {
                people += items.name + ","
            }

            for (items in topicList) {
                topics += items.topic_name + ","
            }

            binding.topices.text = "Talk About : ${topics.removeSuffix(",")}"
            binding.people.text = "People : ${people.removeSuffix(",")}"

            // check status
            when (data.visit_status) {
                0 -> {
                    binding.status.text = "Visit"
                }
                1 -> {
                    binding.status.text = "InProgress"
                }
                else -> {
                    binding.status.text = "Completed"
                    binding.visitScheduleLayout.backgroundTintList = ContextCompat.getColorStateList(context, R.color.green)
                }
            }

            binding.visitScheduleLayout.setOnClickListener {
                val scheduledate = LocalDate.parse(data.schedule_date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                val date = Date()
                val currentDateee = LocalDate.parse(dateFormat.format(date), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val cmp = scheduledate.compareTo(currentDateee)

                if (cmp==0) {
                    if (data.visit_status != 2) {
                        val bundle = Bundle()
                        Utils.currentDetailingSchedule = data
                        Log.i("outlet_Id", data.outlet_id.toString())
                        if (data.visit_status == 1) {
                            bundle.putInt("insert_type", 1)
                        } else {
                            bundle.putInt("insert_type", 0)
                        }
                        bundle.putLong("schedule_id", data.schedule_id)

                        createNavigateOnClickListener(R.id.createStoreDetailNotesFragment, bundle).onClick(binding.visitScheduleLayout)
                    }
                } else {
                    showErrorToast(context,"Future date schedule visit not allowed")
                }
            }
        }
    }
}
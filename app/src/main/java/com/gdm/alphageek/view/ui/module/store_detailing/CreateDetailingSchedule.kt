package com.gdm.alphageek.view.ui.module.store_detailing

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.down_sync.*
import com.gdm.alphageek.data.local.store_detailing.DetailingPeople
import com.gdm.alphageek.data.local.store_detailing.DetailingTopic
import com.gdm.alphageek.databinding.FragmentCreateDetailingScheduleBinding
import com.gdm.alphageek.utils.showErrorToast
import com.gdm.alphageek.utils.showSuccessToast
import com.gdm.alphageek.view.adapter.DetailingTopicesAdapter
import com.gdm.alphageek.view.adapter.RepAdapter
import com.gdm.alphageek.viewmodels.DetailScheduleViewModel
import com.gdm.alphageek.viewmodels.OutletViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class CreateDetailingSchedule : Fragment() {
    private lateinit var binding:FragmentCreateDetailingScheduleBinding
    private val outletViewModel: OutletViewModel by viewModels()
    private val detailScheduleViewModel:DetailScheduleViewModel by viewModels()
    private var topicListApi: ArrayList<String> = ArrayList()
    private var topicListApiraw: ArrayList<StoreScheduleTopics> = ArrayList()
    private var outletList: ArrayList<Outlet> = ArrayList()
    private var repList: ArrayList<DetailingPeople> = ArrayList()
    private var topicList: ArrayList<DetailingTopic> = ArrayList()
    private var outlet: Outlet? = null
    private var date = ""
    private var time = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateDetailingScheduleBinding.inflate(layoutInflater)

        // get all outlet
        outletViewModel.getAllOutlet()

        outletViewModel.getTopicLists()

        outletViewModel.topicList.observe(viewLifecycleOwner){
            if (it != null) {
                topicListApi = ArrayList()
                topicListApiraw = ArrayList()
                topicListApiraw.clear()
                topicListApi.clear()
                binding.topicSpinner.adapter = null
                topicListApi.add("Select")
                topicListApiraw.addAll(it)
                for (item in topicListApiraw) {
                    topicListApi.add(item.topic_name)
                }
                binding.topicSpinner.adapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_dropdown_item,
                    topicListApi
                )
            }
        }
        outletViewModel.outletList.observe(viewLifecycleOwner){
            if (it != null) {
                outletList.clear()
                outletList.add(
                    Outlet(
                        -1,
                        -1,
                        "",
                        "",
                        "",
                        "",
                        "Select Outlet",
                        "",
                        -1,
                        -1,
                        -1,
                        -1,
                        "",
                        -1,
                        "",
                        "",
                        "",
                        "",
                        "",
                        0,
                        1,
                        0,
                        "",
                        null
                    )
                )
                outletList.addAll(it)
                binding.outletSpinner.adapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_dropdown_item,
                    outletList
                )
            }
        }

        // get selected outlet
        binding.topicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (i!=0 && selectedItem != null) {
                    topicList.add(DetailingTopic(selectedItem))
                    binding.topicRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                    binding.topicRecyclerview.adapter = DetailingTopicesAdapter(topicList)
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        binding.outletSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (selectedItem != "Select Outlet" && selectedItem != null) {
                    outlet = outletList[binding.outletSpinner.selectedItemPosition]
                    Log.i("reeggionid",outlet!!.region_id.toString())
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }


        //setup calender
        val myCalendar: Calendar = Calendar.getInstance()
        val dateSetListener: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { date_picker, year, monthOfYear, dayOfMonth ->
            date_picker.minDate = System.currentTimeMillis() - 1000
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val myFormat = "yyyy-MM-dd"
            val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)
            date = sdf.format(myCalendar.time)
            binding.dateTv.text = date
        }

        binding.scheduleDateLayout.setOnClickListener{
            DatePickerDialog(
                requireActivity(),
                R.style.Calender_dialog_theme,
                dateSetListener,
                myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

        binding.scheduleTimeLayout.setOnClickListener{
            val hour = myCalendar[Calendar.HOUR_OF_DAY]
            val minute = myCalendar[Calendar.MINUTE]
            val mTimePicker = TimePickerDialog(
                requireActivity(), R.style.Calender_dialog_theme,
                { _, selectedHour, selectedMinute ->
                    val finalHour = when(selectedHour.toString()){"0","00"->"24" else-> selectedHour}
                    val finalMinute = when(selectedMinute.toString().length) {1-> "0${selectedMinute}" else-> selectedMinute.toString()}
                    time = "$finalHour:$finalMinute:00"
                    binding.timeTv.text = time
                }, hour, minute, true
            ) //Yes 24 hour time
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        }

        binding.saveBtn.setOnClickListener{
            when {
                outlet == null -> {
                    showErrorToast(requireContext(),"Please select Outlet")
                }
                date.isEmpty() -> {
                    showErrorToast(requireContext(),"Please select date")
                }
                time.isEmpty() -> {
                    showErrorToast(requireContext(),"Please select time")
                }
                repList.isEmpty() -> {
                    showErrorToast(requireContext(),"Please add representative name")
                }
                topicList.isEmpty() -> {
                    showErrorToast(requireContext(),"Please select detail topic")
                }
                else -> {
                    detailScheduleViewModel.insertDetailingSchedule(
                        DetailingSchedule(
                            System.currentTimeMillis(),
                            outlet!!.outlet_id,
/*                            "9.072264",
                            "7.491302",*/
                            outlet!!.location_name,
                            outlet?.street_no+", "+outlet?.street_name,
                            outlet!!.outlet_name,
                            date,
                            time,
                            Gson().toJson(repList),
                            Gson().toJson(topicList),
                            outlet!!.country_id,
                            outlet!!.state_id,
                            outlet!!.region_id,
                            outlet!!.location_id,
                            is_local = 1,
                            0))
                }
            }
        }


        // create schedule response
        detailScheduleViewModel.insertSchedule.observe(viewLifecycleOwner){
            if (it != null) {
                if (it.toInt() != -1){
                    showSuccessToast(requireContext(),"Schedule created successfully")
                    findNavController().navigate(R.id.storeDetailingDashboardFragment)
                } else {
                    showErrorToast(requireContext(),"Failed to create Schedule")
                }
            }
        }

        // handle error
        detailScheduleViewModel.errorMessage.observe(viewLifecycleOwner) {
            showErrorToast(requireContext(),it.toString())
        }

        binding.repAddBtn.setOnClickListener{
            val data = binding.repEditText.text.toString()
            if (data.isEmpty()){
                binding.repEditText.requestFocus()
                binding.repEditText.error = "Rep"
            }else{
                repList.add(DetailingPeople(data))
                binding.repRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                binding.repRecyclerview.adapter = RepAdapter(repList)

                binding.repEditText.setText("")

            }
        }


        binding.topicsAddBtn.setOnClickListener{
            val data = binding.topicEditText.text.toString()
            if (data.isEmpty()){
                binding.topicEditText.requestFocus()
                binding.topicEditText.error = "Topics"
            }else{
                topicList.add(DetailingTopic(data))
                binding.topicRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                binding.topicRecyclerview.adapter = DetailingTopicesAdapter(topicList)
                binding.topicEditText.setText("")
            }
        }


        return binding.root
    }
}
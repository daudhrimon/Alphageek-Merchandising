package com.gdm.alphageek.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.data.local.down_sync.Brand
import com.gdm.alphageek.data.local.down_sync.PlanogramQuestions
import com.gdm.alphageek.databinding.PlanogramItemsBinding
import kotlin.Int

class PlanogramQuestionsAdapter(
    private val dataList: ArrayList<PlanogramQuestions>,
    private val context: Context,
    private val share: Boolean
) : RecyclerView.Adapter<PlanogramQuestionsAdapter.ViewHolder>() {

    private var brandList = ArrayList<Brand>().apply {
        add(Brand(0, "No"))
        add(Brand(1, "Yes"))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PlanogramItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: PlanogramItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(data: PlanogramQuestions) {
            binding.name.text = "${adapterPosition + 1} ${data.question_name}"
            binding.brandSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, brandList)

            binding.brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                    val res = brandList[binding.brandSpinner.selectedItemPosition].id
                    data.answer_type = res
                } override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }

            if (share) {
                binding.brandSpinner.isEnabled = false
                binding.brandSpinner.setSelection(data.answer_type)
            }
        }
    }

    fun selectedItem() = dataList
}
package com.gdm.alphageek.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdm.alphageek.data.local.down_sync.*
import com.gdm.alphageek.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class DetailScheduleViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val detailScheduleList = MutableLiveData<List<DetailingSchedule>>()
    val insertSchedule = MutableLiveData<Long>()
    val errorMessage = MutableLiveData<String>()
    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { errorMessage.postValue("Something went wrong !") }
    }


    fun getScheduleList(date:String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getDetailingScheduleList(date)
            withContext(Dispatchers.Main) {
                detailScheduleList.postValue(data)
            }
        }
    }

    fun insertDetailingSchedule(detailingSchedule: DetailingSchedule) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.insertDetailingSchedule(detailingSchedule)
            withContext(Dispatchers.Main) {
                insertSchedule.postValue(data)
            }
        }
    }

    fun updateDetailSchedule(schedule: DetailingSchedule) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.updateDetailingSchedule(schedule)
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}
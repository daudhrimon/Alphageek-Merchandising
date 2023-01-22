package com.gdm.alphageek.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdm.alphageek.data.local.visit.ScheduleVisit
import com.gdm.alphageek.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class VisitViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val insertVisitResponse   = MutableLiveData<Long>()
    val visitDataResponse   = MutableLiveData<ScheduleVisit>()
    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { errorMessage.postValue("Something went wrong !") }
    }

    fun insertVisitData(scheduleVisit: ScheduleVisit) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.insertVisitData(scheduleVisit)
            withContext(Dispatchers.Main) {
                insertVisitResponse.postValue(data)
            }
        }
    }

    fun getVisitData(scheduleID:Long) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getVisitData(scheduleID)
            withContext(Dispatchers.Main) {
                visitDataResponse.postValue(data)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}
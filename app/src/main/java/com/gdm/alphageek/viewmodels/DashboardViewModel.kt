package com.gdm.alphageek.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdm.alphageek.data.local.down_sync.*
import com.gdm.alphageek.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val dashboardData = MutableLiveData<Dashboard>()
    val detailDashboardData = MutableLiveData<DetailingDashboard>()
    val scheduleList = MutableLiveData<List<Schedule>>()
    val insertSchedule = MutableLiveData<Long>()
    val briefList = MutableLiveData<List<Brief>>()
    val routePlan = MutableLiveData<List<RoutePlan>>()
    val locationList = MutableLiveData<List<LocationDownSync>>()
    val routeDetailList = MutableLiveData<List<RoutePlanDetails>>()
    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { errorMessage.postValue("Something went wrong !") }
    }

    fun insertNewSchedule(schedule: Schedule) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.insertSchedule(schedule)
            withContext(Dispatchers.Main) {
                insertSchedule.postValue(data)
            }
        }
    }

    fun updateScheduleData(schedule: Schedule) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.updateSchedule(schedule)
        }
    }
    fun updateDashboardData(dashboard: Dashboard) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.updateDashboard(dashboard)
        }
    }

    fun updateDetailingDashboard(dashboard: DetailingDashboard) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.updateDetailingDashBoardDataList(dashboard)
        }
    }

    fun getDashboardData() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getDashboardData()
            withContext(Dispatchers.Main) {
                dashboardData.postValue(data)
            }
        }
    }

    fun getDetailDashboardData() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getDetailDashboardData()
            withContext(Dispatchers.Main) {
                detailDashboardData.postValue(data)
            }
        }
    }

    fun getScheduleList(date:String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getScheduleList(date)
            withContext(Dispatchers.Main) {
                scheduleList.postValue(data)
            }
        }
    }


    fun getBriefList() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getBriefList()
            withContext(Dispatchers.Main) {
                briefList.postValue(data)
            }
        }
    }

    fun getRoutePlanList() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getRoutePlan()
            withContext(Dispatchers.Main) {
                routePlan.postValue(data)
            }
        }
    }

    fun getAllLocationList() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getAllLocation()
            withContext(Dispatchers.Main) {
                locationList.postValue(data)
            }
        }
    }

    fun getRoutePlanDetailList(dayID:Int) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getRoutePlanDetails(dayID)
            withContext(Dispatchers.Main) {
                routeDetailList.postValue(data)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}
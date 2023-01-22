package com.gdm.alphageek.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdm.alphageek.data.local.down_sync.*
import com.gdm.alphageek.data.local.store_detailing.StoreDetailingData
import com.gdm.alphageek.data.local.visit.ScheduleVisit
import com.gdm.alphageek.data.remote.BaseResponse
import com.gdm.alphageek.repository.AppRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import okhttp3.RequestBody
import retrofit2.http.Body
import javax.inject.Inject

@HiltViewModel
class DataSyncViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val downSyncResponse = MutableLiveData<Boolean>()
    val upSyncResponse = MutableLiveData<Boolean>()
    val localOutletList = MutableLiveData<List<Outlet>>()
    val updatableOutletList = MutableLiveData<List<Outlet>>()
    val offlineScheduleList = MutableLiveData<List<Schedule>>()
    val offlineDetailSchedules = MutableLiveData<List<DetailingSchedule>>()
    val scheduleVisitList = MutableLiveData<List<ScheduleVisit>>()
    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { errorMessage.postValue("Something went wrong !") }
    }

    private val syncExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { onError(it) }
    }

    fun userLocation(gio_lat: String, gio_long: String, address: String) {
        job = CoroutineScope(Dispatchers.IO).launch {
            repository.userLocation(gio_lat, gio_long, address)
        }
    }

    fun getAllOfflineOutlet() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getAllOfflineOutlet()
            withContext(Dispatchers.Main) {
                localOutletList.postValue(data)
            }
        }
    }

    fun getAllUpdatableOutlet() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getAllUpdatableOutlet()
            withContext(Dispatchers.Main) {
                updatableOutletList.postValue(data)
            }
        }
    }

    fun getAllOfflineScheduleList() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getAllOfflineScheduleList()
            withContext(Dispatchers.Main) {
                offlineScheduleList.postValue(data)
            }
        }
    }

    fun getAllOfflineDetailSchedules() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getAllOfflineDetailScheduleList()
            withContext(Dispatchers.Main) {
                offlineDetailSchedules.postValue(data)
            }
        }
    }

    fun getScheduleVisitData() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getScheduleVisitData()
            withContext(Dispatchers.Main) {
                scheduleVisitList.postValue(data)
            }
        }
    }

    fun upSync(@Body body: RequestBody) {
        job = CoroutineScope(Dispatchers.IO + syncExceptionHandler).launch {
            val response = repository.upSyncData(body)
            withContext(Dispatchers.Main) {
                when { response.isSuccessful && response.body() != null -> {
                        when { response.body()!!.success -> deleteInfoLocalDb()
                            else -> response.body()?.message?.let { errorMessage.postValue(it) }}
                } else -> response.errorBody()?.let{onError(it.toString())} }
            }
        }
    }

    fun downSync() {
        job = CoroutineScope(Dispatchers.IO + syncExceptionHandler).launch {
            val response = repository.getDownSyncInfo()
            withContext(Dispatchers.Main) {
                when { response.isSuccessful && response.body() != null -> {
                        when { response.body()!!.success -> saveInfoLocalDb(response.body()!!.data)
                            else -> response.body()?.message?.let { errorMessage.postValue(it) }}
                } else -> response.errorBody()?.let{onError(it.toString())} }
            }
        }
    }

    private fun saveInfoLocalDb(downSyncData: DownSyncData) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val routePlan = ArrayList<RoutePlan>(ArrayList(emptyList()))
            val routePlanDetails = ArrayList<RoutePlanDetails>(ArrayList(emptyList()))
            downSyncData.route_plane?.forEach {
                it.details?.forEach { det->
                    val routePlanDetail = RoutePlanDetails(
                        0,
                        it.id,
                        det.location_id,
                        det.location_name,
                        det.gio_lat,
                        det.gio_long
                    )
                    routePlanDetails.add(routePlanDetail)
                }
                routePlan.add(RoutePlan(it.id, it.day_of_week))
            }.also {
                repository.insertRoutePlan(routePlan)
                repository.insertRoutePlanDetails(routePlanDetails)
            }
            val detailingSchedule = ArrayList<DetailingSchedule>(ArrayList(emptyList()))
            val detailingVisitData = ArrayList<StoreDetailingVisited>(ArrayList(emptyList()))
            downSyncData.store_schedule_list?.forEach {
                val detailSchedule = DetailingSchedule(
                    schedule_id = it.schedule_id,
                    outlet_id = it.outlet_id.toLong(),
                    location_name = it.location_name!!,
                    outlet_address = it.outlet_address!!,
                    outlet_name = it.outlet_name!!,
                    schedule_date = it.schedule_date!!,
                    schedule_time = it.schedule_time!!,
                    reps = it.reps!!,
                    topics = it.topics!!,
                    country_id = it.country_id!!,
                    state_id = it.state_id!!,
                    region_id = it.region_id!!,
                    location_id = it.location_id!!,
                    is_local = it.is_local,
                    visit_status = it.visit_status,
                )
                val detailingVisit = StoreDetailingVisited(
                    id = it.schedule_id,
                    schedule_id = it.schedule_id,
                    country_id = it.country_id,
                    state_id = it.state_id,
                    region_id = it.region_id,
                    location_id = it.location_id,
                    visit_date = it.schedule_date,
                    visit_time = it.schedule_time,
                    outlet_type_id = it.type_id,
                    outlet_channel_id = it.channel_id,
                    status = it.visit_status,
                    note = when{it.visited_data.isNullOrEmpty()->"" else->it.visited_data[0].note},
                    visited_image = when{it.visited_data.isNullOrEmpty()->"" else->it.visited_data[0].visited_image},
                    outlet_id = it.outlet_id
                )
                repository.insertVisitData(ScheduleVisit(
                    id = it.schedule_id,
                    schedule_id = it.schedule_id,
                    outlet_id = it.outlet_id,
                    outlet_type_id = it.type_id,
                    outlet_channel_id = it.channel_id,
                    visit_date = it.schedule_date,
                    visit_time = it.schedule_time,
                    country_id = it.country_id,
                    state_id = it.state_id,
                    region_id = it.region_id,
                    location_id = it.location_id,
                    visit_type = 24,
                    image_list = "",
                    notes = when{it.visited_data.isNullOrEmpty()->"" else->it.visited_data[0].note},
                    store_detailing_visit = Gson().toJson(StoreDetailingData(
                        id = it.schedule_id,
                        schedule_id = it.schedule_id,
                        status = it.visit_status.toString(),
                        note = when{it.visited_data.isNullOrEmpty()->"" else->it.visited_data[0].note},
                        outlet_name = "",
                        outlet_id = it.outlet_id,
                        outlet_address = "",
                        is_local = 1,
                        is_insert = 1
                    ))
                ))
                detailingSchedule.add(detailSchedule)
                detailingVisitData.add(detailingVisit)
            }.also {
                repository.insertDetailingSchedule(detailingSchedule)
                repository.insertDetailingScheduleVisited(detailingVisitData)
            }
            downSyncData.store_schedule_topics?.let{repository.insertStoreSchedulingTopicList(it)}
            downSyncData.brand_list?.let{repository.insertBrandList(it)}
            downSyncData.brifs?.let{repository.insertBriefList(it)}
            downSyncData.dashboard?.let{repository.insertDashBoardData(it)}
            downSyncData.store_schedule_dashboard?.let{repository.insertDetailingDashBoardDataList(it)}
            downSyncData.outlets?.let{repository.insertOutletList(it)}
            downSyncData.outlet_channels?.let{repository.insertOutletChannelList(it)}
            downSyncData.outlet_types?.let{repository.insertOutletType(it)}
            downSyncData.products?.let{repository.insertProductList(it)}
            downSyncData.location?.let{repository.insertLocation(it)}
            downSyncData.schedules?.let{repository.insertSchedule(it)}
            downSyncData.posms_product_list?.let{repository.insertPosmProduct(it)}
            downSyncData.planogram_question?.let{repository.insertPlanogramQuestion(it)}
            downSyncData.questions?.let{repository.insertQuestions(it)}
            withContext(Dispatchers.Main) {
                downSyncResponse.postValue(true)
            }
        }
    }

    fun deleteInfoLocalDb() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.deleteVisitData()
            repository.deleteTopicList()
            repository.deleteBrandList()
            repository.deleteBriefList()
            repository.deleteDashBoardData()
            repository.deleteDetailingDashBoardData()
            repository.deleteOutletList()
            repository.deleteOutletChannelList()
            repository.deleteOutletType()
            repository.deleteProductList()
            repository.deleteRoutePlan()
            repository.deleteLocation()
            repository.deleteSchedule()
            repository.deleteDetailingSchedule()
            repository.deletePosmProduct()
            repository.deletePlanogramQuestion()
            repository.deleteRoutePlanDetail()
            repository.deleteQuestions()
            withContext(Dispatchers.Main) {
                upSyncResponse.postValue(true)
            }
        }
    }


    private fun onError(response: String) {
        try {
            val error = Gson().fromJson(response, BaseResponse::class.java)
            errorMessage.postValue(error.message)
        }catch (e:Exception){
            errorMessage.postValue("Your internet connection may be unstable !")
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}
package com.gdm.alphageek.repository
import com.gdm.alphageek.api.ApiService
import com.gdm.alphageek.data.local.down_sync.*
import com.gdm.alphageek.data.local.visit.ScheduleVisit
import com.gdm.alphageek.database.AppDao
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import javax.inject.Inject

class AppRepository @Inject constructor(private val appDao: AppDao,private val apiService: ApiService) {

    suspend fun userLocation(gio_lat: String, gio_long: String,address:String) = apiService.liveTrac(gio_lat, gio_long,address,1)
    suspend fun userLogin(email: String, password: String,deviceID:String,loginAddress: String) = apiService.login(email, password,deviceID,loginAddress,1)
    suspend fun signUp(name: String,email: String, password: String,deviceID:String,ip_address:String) = apiService.signUP(name,email, password,deviceID,ip_address)
    suspend fun userLogout(ip_address: String,loginAddress: String) = apiService.logout(ip_address,loginAddress)
    suspend fun getCountryList() = apiService.getCountryList()
    suspend fun getStateList(countryID:String) = apiService.getStateList(countryID)
    suspend fun getLGAList(stateID:String) = apiService.getLGAList(stateID)
    suspend fun getBankList() = apiService.getBankList()
    suspend fun getEducationList() = apiService.getEducationList()
    suspend fun getDocumentTypeList() = apiService.getDocumentTypeList()
    suspend fun getProfileInfo() = apiService.getProfileInfo()
    suspend fun getDownSyncInfo() = apiService.downSync()
    suspend fun updateProfile(
        firstname: RequestBody,lastname: RequestBody,
        middle_name: RequestBody,gender: RequestBody,
        phone: RequestBody,address: RequestBody,
        country_id: RequestBody,state_id: RequestBody,
        lga: RequestBody,nin: RequestBody,bvn: RequestBody,
        lasra: RequestBody,education: RequestBody,bank_id: RequestBody,
        account_name: RequestBody,account_number: RequestBody,
        guarantor_name: RequestBody,guarantor_email: RequestBody,
        guarantor_phone: RequestBody,guarantor_id_type: RequestBody,
        guarantor_Document: MultipartBody.Part?,userImage: MultipartBody.Part?
    ) = apiService.updateProfile(
        firstname,lastname,middle_name,gender,
        phone,address,country_id,state_id,lga,nin,bvn,lasra,education,
        bank_id,account_name,account_number,guarantor_name,guarantor_email,
        guarantor_phone,guarantor_id_type,guarantor_Document,userImage
    )

    // up sync
    suspend fun upSyncData(@Body body: RequestBody) = apiService.upSyncData(body)


    // local db operations for down sync
    suspend fun insertBrandList(brandList: List<Brand>) = appDao.insertBrandList(brandList)
    suspend fun insertStoreSchedulingTopicList(topic: List<StoreScheduleTopics>) = appDao.insertStoreSchedulingTopicist(topic)
    suspend fun insertBriefList(data: List<Brief>) = appDao.insertBriefList(data)
    suspend fun insertDashBoardData(dashboard: Dashboard) = appDao.insertDashBoardDataList(dashboard)
    fun updateDetailingDashBoardDataList(schedule: DetailingDashboard) = appDao.updateDetailingDashboard(schedule)

    suspend fun insertDetailingDashBoardDataList(detailingDashboard: DetailingDashboard) = appDao.insertDetailingDashBoardDataList(detailingDashboard)
    suspend fun insertOutletList(data: List<Outlet>) = appDao.insertOutletList(data)
    suspend fun insertOutletChannelList(data: List<OutletChannel>) = appDao.insertOutletChannelList(data)
    suspend fun insertOutletType(data: List<OutletType>) = appDao.insertOutletType(data)
    suspend fun insertProductList(data: List<Product>) = appDao.insertProductList(data)
    suspend fun insertRoutePlan(data: List<RoutePlan>) = appDao.insertRoutePlan(data)
    suspend fun insertRoutePlanDetails(data: List<RoutePlanDetails>) = appDao.insertRoutePlanDetails(data)
    suspend fun insertLocation(data: List<LocationDownSync>) = appDao.insertLocation(data)
    suspend fun insertSchedule(data: List<Schedule>) = appDao.insertSchedule(data)
    suspend fun insertDetailingSchedule(detailingSchedule: List<DetailingSchedule>) = appDao.insertDetailingSchedule(detailingSchedule)
    suspend fun insertDetailingScheduleVisited(detailingSchedule: List<StoreDetailingVisited>) = appDao.insertDetailingSchedulevisited(detailingSchedule)
    suspend fun insertPosmProduct(data: List<Posm>) = appDao.insertPosmProduct(data)
    suspend fun insertPlanogramQuestion(data: List<PlanogramQuestions>) = appDao.insertPlanogramQuestion(data)
    suspend fun insertQuestions(data: List<Questions>?) = appDao.insertQuestions(data)


    // local db operations for data deletion
    suspend fun deleteTopicList()               = appDao.deleteTopicList()
    suspend fun deleteBrandList()               = appDao.deleteBrandList()
    suspend fun deleteBriefList()               = appDao.deleteBriefList()
    suspend fun deleteDashBoardData()           = appDao.deleteDashBoardDataList()
    suspend fun deleteDetailingDashBoardData()  = appDao.deleteDetailingDashBoardData()
    suspend fun deleteOutletList()              = appDao.deleteOutletList()
    suspend fun deleteOutletChannelList()       = appDao.deleteOutletChannelList()
    suspend fun deleteOutletType()              = appDao.deleteOutletType()
    suspend fun deleteProductList()             = appDao.deleteProductList()
    suspend fun deleteRoutePlan()               = appDao.deleteRoutePlan()
    suspend fun deleteLocation()                = appDao.deleteLocation()
    suspend fun deleteSchedule()                = appDao.deleteSchedule()
    suspend fun deleteDetailingSchedule()       = appDao.deleteDetailingSchedule()
    suspend fun deletePosmProduct()             = appDao.deletePosmProduct()
    suspend fun deletePlanogramQuestion()       = appDao.deletePlanogramQuestion()
    suspend fun deleteVisitData()               = appDao.deleteVisitData()
    suspend fun deleteRoutePlanDetail()         = appDao.deleteRoutePlanData()
    suspend fun deleteQuestions()               = appDao.deleteQuestions()

    // fetch from local database
    fun updateDashboard(schedule: Dashboard) = appDao.updateDashBoardData(schedule)


    fun getDashboardData() = appDao.getDashBoardInfo()
    fun getDetailDashboardData() = appDao.getDetailDashBoardInfo()
    fun getBriefList() = appDao.getBriefList()
    fun getRoutePlan() = appDao.getRoutePlan()
    fun getAllLocation() = appDao.getAllLocation()
//    fun getLocationByRoute(dayID:String) = appDao.getLocationByRoute(dayID)
    fun getRoutePlanDetails(dayID:Int) = appDao.getallRouteDetail(dayID)
    fun getAllOfflineScheduleList() = appDao.getAllOfflineScheduleList()
    fun getAllOfflineDetailScheduleList() = appDao.getAllOfflineDetailScheduleList()
    fun getScheduleVisitData() = appDao.getScheduleVisitData()


    // outlet info
    fun getAllOutlet()                     = appDao.getAllOutLet()
    fun getAllTopics()                     = appDao.getAllTopics()
    fun getAllOfflineOutlet()              = appDao.getAllOfflineOutlet()
    fun getAllUpdatableOutlet()            = appDao.getAllUpdatableOutlet()
    fun getOutletById(outletID:Long)       = appDao.getOutletById(outletID)
    fun insertOutlet(outlet: Outlet)       = appDao.insertOutlet(outlet)
    fun updateOutlet(outlet: Outlet)       = appDao.updateOutlet(outlet)
    fun getOutletTypes()                   = appDao.getOutLetType()
    fun getOutLetChannel()                 = appDao.getOutLetChannel()

    // schedule
    fun insertSchedule(schedule: Schedule) = appDao.insertSchedule(schedule)
    fun updateSchedule(schedule: Schedule) = appDao.updateSchedule(schedule)
    fun getScheduleList(date:String) = appDao.getScheduleList(date)
    fun checkSchedule(scheduleID:Long,visitType:Int):Boolean = appDao.checkSchedule(scheduleID,visitType)

    // detailing schedule
    fun insertDetailingSchedule(detailingSchedule: DetailingSchedule) = appDao.insertDetailingSchedule(detailingSchedule)
    fun getDetailingScheduleList(date:String) = appDao.getDetailingScheduleList(date)
    fun updateDetailingSchedule(detailingSchedule: DetailingSchedule) = appDao.updateDetailingSchedule(detailingSchedule)

    // visit
    fun insertVisitData(scheduleVisit: ScheduleVisit) = appDao.insertScheduleVisit(scheduleVisit)
    fun getVisitData(scheduleID: Long) = appDao.getVisitData(scheduleID)
    fun updateScheduleVisit(scheduleVisit: ScheduleVisit) = appDao.updateScheduleVisit(scheduleVisit)

    // product and brand
    fun getProductByBrand(brandID:Int) = appDao.getProductByBrand(brandID)
    fun getProductPosmByBrand(brandID:Int) = appDao.getProductPosmByBrand(brandID)
    fun getAllBrand() = appDao.getAllBrand()

    // merchandising
    fun getPlanogramQuestions() = appDao.getPlanogramQuestions()
    fun getQuestions() = appDao.getQuestions()
}

package com.gdm.alphageek.database

import androidx.room.*
import com.gdm.alphageek.data.local.down_sync.*
import com.gdm.alphageek.data.local.visit.ScheduleVisit

@Dao
interface AppDao {

    // down sync operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrandList(brandList: List<Brand>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoreSchedulingTopicist(topicList: List<StoreScheduleTopics>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBriefList(data: List<Brief>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDashBoardDataList(dashboard: Dashboard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetailingDashBoardDataList(detailingDashboard: DetailingDashboard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutletList(data: List<Outlet>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutletChannelList(data: List<OutletChannel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutletType(data: List<OutletType>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductList(data: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePlan(data: List<RoutePlan>)


    @Insert
    suspend fun insertRoutePlanDetails(data : List<RoutePlanDetails>)


    @Insert
    suspend fun insertLocation(data: List<LocationDownSync>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(data: List<Schedule>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetailingSchedule(detailingSchedule: List<DetailingSchedule>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetailingSchedulevisited(detailingSchedule: List<StoreDetailingVisited>)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosmProduct(data: List<Posm>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanogramQuestion(data: List<PlanogramQuestions>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(data: List<Questions>?)

    // delete sync operations
    @Query("DELETE FROM brand")
    suspend fun deleteBrandList()

    @Query("DELETE FROM store_schedule_topics")
    suspend fun deleteTopicList()

    @Query("DELETE FROM brief")
    suspend fun deleteBriefList()

    @Query("DELETE FROM dashboard")
    suspend fun deleteDashBoardDataList()

    @Query("DELETE FROM detailing_dashboard")
    suspend fun deleteDetailingDashBoardData()

    @Query("DELETE FROM outlet")
    suspend fun deleteOutletList()

    @Query("DELETE FROM outlet_channel")
    suspend fun deleteOutletChannelList()

    @Query("DELETE FROM outlet_type")
    suspend fun deleteOutletType()

    @Query("DELETE FROM product")
    suspend fun deleteProductList()

    @Query("DELETE FROM route_plan")
    suspend fun deleteRoutePlan()

    @Query("DELETE FROM location")
    suspend fun deleteLocation()

    @Query("DELETE FROM schedule")
    suspend fun deleteSchedule()

    @Query("DELETE FROM detailed_schedule")
    suspend fun deleteDetailingSchedule()

    @Query("DELETE FROM posm")
    suspend fun deletePosmProduct()

    @Query("DELETE FROM planogram_questions")
    suspend fun deletePlanogramQuestion()

    @Query("DELETE FROM schedule_visit")
    suspend fun deleteVisitData()

    @Query("DELETE FROM route_plan_details")
    suspend fun deleteRoutePlanData()

    @Query("DELETE FROM questions")
    suspend fun deleteQuestions()

    // dashboard information
    @Query("select * from Dashboard")
    fun getDashBoardInfo(): Dashboard

    @Query("select * from detailing_dashboard")
    fun getDetailDashBoardInfo(): DetailingDashboard

    @Query("select * from schedule where schedule_date=:date")
    fun getScheduleList(date:String): List<Schedule>

    @Query("select * from detailed_schedule where schedule_date=:date")
    fun getDetailingScheduleList(date:String): List<DetailingSchedule>

    @Query("select * from brief")
    fun getBriefList(): List<Brief>

    @Query("select * from route_plan")
    fun getRoutePlan(): List<RoutePlan>

    @Query("select * from location")
    fun getAllLocation(): List<LocationDownSync>

    @Query("select * from route_plan_details where id = :id")
    fun getRouteDetails(id :String): List<RoutePlanDetails>



    // schedule
    @Query("select * from schedule where is_local = 1")
    fun getAllOfflineScheduleList(): List<Schedule>

    @Query("select * from detailed_schedule where is_local = 1")
    fun getAllOfflineDetailScheduleList(): List<DetailingSchedule>

    @Query("select * from schedule_visit where visit_type != 24")
    fun getScheduleVisitData(): List<ScheduleVisit> 
    
    @Query("select * from route_plan_details where rootId = :rootIId")
    fun getallRouteDetail(rootIId : Int): List<RoutePlanDetails>


    @Query("select * from schedule_visit where schedule_id = :scheduleID")
    fun getVisitData(scheduleID: Long): ScheduleVisit

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSchedule(schedule: Schedule):Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDetailingSchedule(detailingSchedule: DetailingSchedule):Long

    // dashboard information


    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateDashBoardData(dashboard: Dashboard):Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateDetailingDashboard(dashboard: DetailingDashboard):Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSchedule(schedule: Schedule):Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateDetailingSchedule(detailingSchedule: DetailingSchedule):Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertScheduleVisit(scheduleVisit: ScheduleVisit):Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateScheduleVisit(scheduleVisit: ScheduleVisit):Int

    @Query("select * from schedule_visit where schedule_id = :scheduleID and visit_type = :visitType")

    fun checkSchedule(scheduleID:Long,visitType:Int): Boolean


    // outlet
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOutlet(outlet: Outlet):Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateOutlet(outlet: Outlet):Int

    @Query("select * from outlet")
    fun getAllOutLet(): List<Outlet>

    @Query("select * from store_schedule_topics")
    fun getAllTopics(): List<StoreScheduleTopics>

    @Query("select * from outlet where is_local = 1")
    fun getAllOfflineOutlet(): List<Outlet>

    @Query("select * from outlet where is_up = 1")
    fun getAllUpdatableOutlet(): List<Outlet>

    @Query("select * from outlet where outlet_id = :outletID")
    fun getOutletById(outletID:Long): Outlet

    @Query("select * from outlet_type")
    fun getOutLetType(): List<OutletType>

    @Query("select * from outlet_channel")
    fun getOutLetChannel(): List<OutletChannel>

    // product and brand
    @Query("select * from brand")
    fun getAllBrand():List<Brand>

    @Query("select * from product where brand_id = :brandID")
    fun getProductByBrand(brandID:Int): List<Product>

    @Query("select * from posm where brand_id = :brandID")
    fun getProductPosmByBrand(brandID:Int): List<Posm>

    // merchandising
    @Query("select * from planogram_questions")
    fun getPlanogramQuestions(): List<PlanogramQuestions>

    @Query("select * from questions")
    fun getQuestions(): List<Questions>
}
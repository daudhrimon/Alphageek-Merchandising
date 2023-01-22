package com.gdm.alphageek.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gdm.alphageek.data.local.down_sync.*
import com.gdm.alphageek.data.local.store_detailing.StoreDetailingData
import com.gdm.alphageek.data.local.visit.ScheduleVisit

@Database(
    entities = [
        Brand::class,
        Brief::class,
        Dashboard::class,
        Outlet::class,
        OutletChannel::class,
        OutletType::class,
        Product::class,
        RoutePlan::class,
        LocationDownSync::class,
        Schedule::class,
        Posm::class,
        ScheduleVisit::class,
        PlanogramQuestions::class,
        DetailingSchedule::class,
        StoreDetailingData::class,
        DetailingDashboard::class,
        StoreDetailingVisited::class,
        StoreScheduleTopics::class,
        RoutePlanDetails::class,
        Questions::class
    ],
    version = 33,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getAppDao(): AppDao
}

package com.gdm.alphageek.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule")
data class Schedule(
    @PrimaryKey
    val schedule_id: Long,
    val outlet_id: Long,
    val gio_lat: String?,
    val gio_long: String?,
    val location_name: String?,
    val outlet_address: String?,
    val outlet_name: String?,
    val schedule_date: String?,
    val schedule_time: String?,
    val country_id: Int?,
    val state_id: Int?,
    val region_id: Int?,
    val location_id: Int?,
    val is_local:Int = 0,
    var outlet_visit: Int = 0,
    var merchandising_visit: Int = 0,
    var posm_visit:Int = 0,
    var competition_visit:Int = 0,
    var freshness_visit:Int = 0,
    var oos_visit:Int = 0,
    var planogram_visit:Int = 0,
    var pricing_visit:Int = 0,
    var ordering_visit:Int = 0
)

package com.gdm.alphageek.data.local.visit

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_visit")
data class ScheduleVisit(
    @PrimaryKey(autoGenerate = true)
    var id:Long,
    var schedule_id: Long,
    var outlet_id: Long?,
    var outlet_type_id: Int?,
    var outlet_channel_id: Int?,
    var visit_date: String?,
    var visit_time: String?,
    var country_id: Int? = null,
    var state_id: Int? = null,
    var region_id: Int? = null,
    var location_id: Int? = null,
    var visit_type: Int,
    var image_list: String? = null,
    var notes: String? = null,
    var planogram_list: String? = null,
    var available_list: String? = null,
    var posm_tracking_list: String? = null,
    var posm_deploy_list: String? = null,
    var promo_description_list: String? = null,
    var store_detailing_visit: String? = null,
    var stat_time: String? = null,
    var end_time: String? = null,
    var gio_lat: String? = null,
    var gio_long: String? = null,
    var is_exception: String? = null,
    var visit_distance: String? = null,
    var isInternetAvailable: Boolean? = false
)

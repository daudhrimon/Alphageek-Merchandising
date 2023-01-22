package com.gdm.alphageek.data.local.down_sync

class DetailingScheduleResponse(
    val schedule_id: Long,
    val outlet_id: Long,
    val location_name: String?,
    val outlet_address: String?,
    val outlet_name: String?,
    val schedule_date: String?,
    val schedule_time: String?,
    val reps: String?,
    val topics: String?,
    val country_id: Int?,
    val type_id: Int?,
    val channel_id: Int?,
    val state_id: Int?,
    val region_id: Int?,
    val location_id: Int?,
    var is_local: Int = 0,
    var visit_status: Int = 0,
    var visited_data: List<StoreDetailingVisitedResponse>
)
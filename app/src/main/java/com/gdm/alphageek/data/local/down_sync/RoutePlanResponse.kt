package com.gdm.alphageek.data.local.down_sync

class RoutePlanResponse (
    val id: Int,
    val day_of_week: String,
    var details : List<RoutePlanDetailsResponse>?
)
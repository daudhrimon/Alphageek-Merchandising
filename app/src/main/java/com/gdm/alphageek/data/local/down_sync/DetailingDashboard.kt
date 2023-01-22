package com.gdm.alphageek.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detailing_dashboard")
data class DetailingDashboard(
    @PrimaryKey
    var id: Int = 0,
    var open_details: Int?,
    var close_details: Int?,
    var reps_details: Int?,
    var total_added: Int?,
)

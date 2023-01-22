package com.gdm.alphageek.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detailed_schedule_visited")
class StoreDetailingVisited (
    @PrimaryKey
    var id:Long,
    var outlet_id: Long?,
    var schedule_id: Long?,
    var country_id: Int?,
    var state_id: Int?,
    var region_id: Int?,
    var location_id: Int?,
    var visit_date: String?,
    var visit_time: String?,
    var outlet_type_id: Int?,
    var outlet_channel_id: Int?,
    var status: Int?,
    var note: String?,
    var visited_image: String?

        )

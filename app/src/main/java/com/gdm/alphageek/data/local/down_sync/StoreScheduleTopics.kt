package com.gdm.alphageek.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "store_schedule_topics")
data class StoreScheduleTopics(
    @PrimaryKey(autoGenerate = true)
    var id:Long,
    val topic_name: String
)

package com.gdm.alphageek.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brief")
data class Brief(
    @PrimaryKey
    val id: Long,
    val title: String,
    val description: String,
    val file: String?,
    val viewed: Int?,
)
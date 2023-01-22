package com.gdm.alphageek.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dashboard")
data class Dashboard(
    @PrimaryKey
    var id:Int = 0,
    var outlets: Int?,
    val products: Int?,
    val posm_product: Int?,
    var merchandise_visit: Int?, // Visit Planed
    var visited_merch: Int?, // Actual Visited
    var pending_merch: Int?, // Pending Visit
    val login_count: Long?, // login count
)
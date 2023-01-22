package com.gdm.alphageek.data.local.down_sync

data class DownSyncResponse(
    val code: Int,
    val `data`: DownSyncData,
    val success: Boolean,
    val message: String
    )
package com.gdm.alphageek.data.remote.profile

data class ProfileResponse(
    val code: Int,
    val data: ProfileData,
    val message: String,
    val success: Boolean
)
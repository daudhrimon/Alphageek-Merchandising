package com.gdm.alphageek.data.remote

data class BaseResponse(
    val code: Int,
    val message: String,
    val success: Boolean
)

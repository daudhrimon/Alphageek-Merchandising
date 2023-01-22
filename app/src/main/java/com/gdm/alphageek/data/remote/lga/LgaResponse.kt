package com.gdm.alphageek.data.remote.lga

data class LgaResponse(
    val code: Int,
    val `data`: List<LgaData>,
    val message: String,
    val success: Boolean
)
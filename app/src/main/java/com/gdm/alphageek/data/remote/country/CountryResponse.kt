package com.gdm.alphageek.data.remote.country

data class CountryResponse(
    val code: Int,
    val `data`: List<CountryData>,
    val message: String,
    val success: Boolean
)
package com.gdm.alphageek.data.remote.state

data class StateResponse(
    val code: Int,
    val `data`: List<StateData>,
    val message: String,
    val success: Boolean
)
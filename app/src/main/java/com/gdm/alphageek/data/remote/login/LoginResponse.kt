package com.gdm.alphageek.data.remote.login

data class LoginResponse(
    val code: Int,
    val `data`: LoginData?,
    val message: String,
    val success: Boolean
)
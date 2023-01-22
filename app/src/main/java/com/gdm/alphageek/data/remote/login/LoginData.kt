package com.gdm.alphageek.data.remote.login

data class LoginData(
    val access_token: String,
    val token_type: String,
    val user_status: Int
)
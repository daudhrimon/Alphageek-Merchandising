package com.gdm.alphageek.data.remote.state

data class StateData(
    val country_id: Int,
    val id: Int,
    val state_name: String
){
    override fun toString(): String {
        return state_name
    }
}
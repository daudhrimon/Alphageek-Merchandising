package com.gdm.alphageek.data.remote.education

data class EducationData(
    val id: Int,
    val title: String
){
    override fun toString(): String {
        return title
    }
}
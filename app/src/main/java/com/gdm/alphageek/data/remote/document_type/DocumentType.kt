package com.gdm.alphageek.data.remote.document_type

data class DocumentType(
    val id: Int,
    val name: String
){
    override fun toString(): String {
        return name
    }
}
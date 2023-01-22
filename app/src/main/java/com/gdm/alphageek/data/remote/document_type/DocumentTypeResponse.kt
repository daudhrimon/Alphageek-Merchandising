package com.gdm.alphageek.data.remote.document_type

data class DocumentTypeResponse(
    val code: Int,
    val `data`: List<DocumentType>,
    val message: String,
    val success: Boolean
)
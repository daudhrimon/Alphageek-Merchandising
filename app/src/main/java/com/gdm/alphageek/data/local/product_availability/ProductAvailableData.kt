package com.gdm.alphageek.data.local.product_availability

data class ProductAvailableData(
    val product_id: Int?,
    val category_id: Int?,
    val brand_id: Int?,
    val client_id: Int?,
    val product_name: String?,
    val product_image: String?,
    val availability_qty: Int?,
    val facing_qty: Int?,
)

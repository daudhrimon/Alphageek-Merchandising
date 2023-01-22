package com.gdm.alphageek.data.local.product_order

data class ProductOrderData(
    val product_id: Int,
    val product_category_id: Int,
    val category_id: Int,
    val brand_id: Int,
    val client_id: Int,
    val product_name: String,
    val product_image: String?,
    var unit_price: Double,
    val unit_per_case: Int,
    var order_case_qty: Int,
    var order_unit_qty: Int,
    )

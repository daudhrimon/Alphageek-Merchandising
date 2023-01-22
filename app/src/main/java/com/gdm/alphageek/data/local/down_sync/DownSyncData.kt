package com.gdm.alphageek.data.local.down_sync

data class DownSyncData(
    val brand_list: List<Brand>?,
    val brifs: List<Brief>?,
    val dashboard: Dashboard?,
    val store_schedule_dashboard: DetailingDashboard?,
    val outlet_channels: List<OutletChannel>?,
    val outlet_types: List<OutletType>?,
    val outlets: List<Outlet>?,
    val products: List<Product>?,
    val route_plane: List<RoutePlanResponse>?,
    val location: List<LocationDownSync>?,
    val schedules: List<Schedule>?,
    val store_schedule_list: List<DetailingScheduleResponse>?,
    val posms_product_list: List<Posm>?,
    val planogram_question: List<PlanogramQuestions>?,
    val store_schedule_topics: List<StoreScheduleTopics>?,
    val questions: List<Questions>?
    )
package com.gdm.alphageek.view.ui.welcome_screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.gdm.alphageek.MainActivity
import com.gdm.alphageek.R
import com.gdm.alphageek.data.local.competition_tracking.PromoDescriptionData
import com.gdm.alphageek.data.local.down_sync.DetailingSchedule
import com.gdm.alphageek.data.local.down_sync.Outlet
import com.gdm.alphageek.data.local.down_sync.PlanogramQuestions
import com.gdm.alphageek.data.local.down_sync.Schedule
import com.gdm.alphageek.data.local.image_model.ImageModel
import com.gdm.alphageek.data.local.out_of_stock.OutOfStockData
import com.gdm.alphageek.data.local.planogram_image.PlanogramImage
import com.gdm.alphageek.data.local.posm_products.PosmDeploymentData
import com.gdm.alphageek.data.local.posm_products.PosmTrackingData
import com.gdm.alphageek.data.local.price_check.PriceCheckData
import com.gdm.alphageek.data.local.product_availability.ProductAvailableData
import com.gdm.alphageek.data.local.product_order.ProductOrderData
import com.gdm.alphageek.data.local.store_detailing.StoreDetailingData
import com.gdm.alphageek.data.local.visit.ScheduleVisit
import com.gdm.alphageek.data.remote.LocationAddress
import com.gdm.alphageek.data.remote.profile.ProfileData
import com.gdm.alphageek.databinding.ActivityWelcomeBinding
import com.gdm.alphageek.databinding.DialogUpsyncBinding
import com.gdm.alphageek.utils.*
import com.gdm.alphageek.view.ui.auth.LoginActivity
import com.gdm.alphageek.viewmodels.DataSyncViewModel
import com.gdm.alphageek.viewmodels.ProfileViewModel
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType
import okhttp3.RequestBody

@AndroidEntryPoint
class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    // start location
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    // end location
    private val dataSyncViewModel: DataSyncViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private var detailScheduleList = ArrayList<DetailingSchedule>()
    private val upSyncForLogout = MutableLiveData<Boolean>()
    private var updatableOutletList = ArrayList<Outlet>()
    private var visitList = ArrayList<ScheduleVisit>()
    private var scheduleList = ArrayList<Schedule>()
    private var outletList = ArrayList<Outlet>()
    private lateinit var syncDialog: AlertDialog
    private var dialog: Dialog? = null
    private var locationAddress = ""
    private var doLogout = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        SharedPref.init(this)
        initializeSyncDialog()
        setupProfileInfo()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1);
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1);
            }
        }

        binding.outletLayout.setOnClickListener {
            navigateToMainPage(Constants.NAVIGATION_OUTLET)
        }
        binding.merchandisingLayout.setOnClickListener {
            navigateToMainPage(Constants.NAVIGATION_MERCHANDISING)
        }
        binding.storeDetailingLayout.setOnClickListener {
            navigateToMainPage(Constants.NAVIGATION_DETAILING)
        }
        binding.posmLayout.setOnClickListener {
            navigateToMainPage(Constants.NAVIGATION_POSM)
        }
        binding.competingTrackingLayout.setOnClickListener {
            navigateToMainPage(Constants.NAVIGATION_COMPETING_TRACKING)
        }
        binding.productFreshnessLayout.setOnClickListener {
            navigateToMainPage(Constants.NAVIGATION_PRODUCT_FRESHNESS)
        }
        binding.planogramLayout.setOnClickListener {
            navigateToMainPage(Constants.NAVIGATION_PLANOGRAM)
        }
        binding.outOfStockLayout.setOnClickListener {
            navigateToMainPage(Constants.NAVIGATION_OUT_OF_STOCK)
        }
        binding.priceCheckLayout.setOnClickListener {
            navigateToMainPage(Constants.NAVIGATION_PRICE_CHECK)
        }
        binding.productOrderLayout.setOnClickListener {
            navigateToMainPage(Constants.NAVIGATION_PRODUCT_ORDER)
        }
        binding.profileCard.setOnClickListener {
            ProfileDialog(this, upSyncForLogout).show()
        }

        // get user profile
        if (Utils.checkForInternet(this)) {
            syncDialog.show()
            profileViewModel.getProfileInfo()
        } else {
            setupProfileInfo()
        }

        // observe profile response
        profileViewModel.profileResponse.observe(this) {
            try {
                if (it != null) {
                    if (it.success) {
                        // starting for the up sync process
                        dataSyncViewModel.getAllOfflineScheduleList()
                        SharedPref.write("PROFILE", Gson().toJson(it.data))
                        SharedPref.write("USER_ID", it.data.details?.user_id.toString())
                        setupProfileInfo()
                    }
                }
            } catch (e: Exception) {
                setupProfileInfo()
                if (syncDialog.isShowing) { syncDialog.dismiss() }
                if (Utils.checkForInternet(this)) {
                    SharedPref.write("PROFILE", "")
                    SharedPref.write("USER_ID", "")
                    showErrorToast(this,"Please Login Again")
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }

        // data upload sync
        dataSyncViewModel.upSyncResponse.observe(this) {
            if (it != null && it) {
                if (doLogout) {
                    doLogout = false
                    syncDialog.dismiss()
                    if (Utils.checkForInternet(this)) {
                        ProgressLoader.init(this)
                        showSuccessToast(this,"UpSync Successful, Please wait...logout process is running")
                        ProgressLoader.show()
                        val address = locationAddress.replace("null","")
                        profileViewModel.userLogout(Utils.getWifiIPAddress(this),when{address.isNotEmpty()->address else->"Unknown"})
                    } else {
                        showInfoToast(this,"UpSync Successful, No Internet Connection...logout process skipped")
                    }
                } else {
                    dataSyncViewModel.downSync()
                }
            }
        }

        // data down sync
        dataSyncViewModel.downSyncResponse.observe(this) {
            if (it != null && it) {
                if (syncDialog.isShowing) {
                    syncDialog.dismiss()
                    showSuccessToast(this,"Database updated Successfully")
                }
            }
        }

        // observe profile related error
        profileViewModel.errorMessage.observe(this) {
            if (it != null) {
                showErrorToast(this,it.toString())
                if (syncDialog.isShowing) { syncDialog.dismiss() }
                doLogout = false
            }
        }

        // handle sync error
        dataSyncViewModel.errorMessage.observe(this) {
            doLogout = false
            if (it != null) {
                showErrorToast(this,it.toString())
                if (syncDialog.isShowing) { syncDialog.dismiss() }
            }
        }

        // sync button click
        binding.syncBtn.setOnClickListener {
            doLogout = false
            if (Utils.checkForInternet(this)) {
                syncDialog.show()
                profileViewModel.getProfileInfo()
            } else {
                showInfoToast(this,"No Internet Connection")
            }
        }

        // observe upSyncForLogout
        upSyncForLogout.observe(this) {
            if (Utils.checkForInternet(this)) {
                doLogout = true
                profileViewModel.getProfileInfo()
            } else {
                showInfoToast(this,"No Internet Connection")
            }
        }

        // observe logout response
        profileViewModel.logoutResponse.observe(this) {
            ProgressLoader.dismiss()
            if ((it?.message?:"").isNotEmpty()) {
                if (it.message == "Successfully logged out") {
                    showSuccessToast(this,it.message)
                    SharedPref.write("PROFILE", "")
                    SharedPref.write("USER_ID", "")
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    showErrorToast(this,it.message)
                }
            }
        }

        // observe schedule data
        dataSyncViewModel.offlineScheduleList.observe(this) {
            if (it != null) {
                // set up the information
                scheduleList.clear()
                scheduleList.addAll(it)
                // get all outlet
                dataSyncViewModel.getAllOfflineOutlet()
            }
        }

        // observe outlet list data
        dataSyncViewModel.localOutletList.observe(this) {
            if (it != null) {
                // set up the information
                outletList.clear()
                outletList.addAll(it)
                // get all updatable outlets
                dataSyncViewModel.getAllUpdatableOutlet()
            }
        }

        // observe updatable outletList
        dataSyncViewModel.updatableOutletList.observe(this) {
            if (it != null) {
                updatableOutletList.clear()
                updatableOutletList.addAll(it)
                // get all detail schedule
                dataSyncViewModel.getAllOfflineDetailSchedules()
            }
        }

        // observe detail schedule data
        dataSyncViewModel.offlineDetailSchedules.observe(this) {
            if (it != null) {
                // set up the information
                detailScheduleList.clear()
                detailScheduleList.addAll(it)
                // get all outlet
                dataSyncViewModel.getScheduleVisitData()
            }
        }

        // observe schedule visit data
        dataSyncViewModel.scheduleVisitList.observe(this) {
            if (it != null) {
                // set up the information
                visitList.clear()
                visitList.addAll(it)
                processAllData()
            }
        }

        // location enabler alert dialog
        dialog = AlertDialog.Builder(this,R.style.Calender_dialog_theme)
            .setTitle("Enable GPS/Location")
            .setMessage("Go to settings & enable GPS/Location access")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent) }
            .setCancelable(false).create()

        // register Gps BrodCast Receiver
        registerReceiver(GpsBrodCastReceiver(dialog), IntentFilter(LocationManager.MODE_CHANGED_ACTION))

        // get Locations
        getLocationUpdates()
    }

    // start receiving location update when activity  visible/foreground
    override fun onResume() {
        super.onResume()
        if (Utils.checkForInternet(this)){
            showGpsEnablerDialogs()
        } else {
            Utils.gio_lat = null
            Utils.gio_long = null
        }
        if (Utils.haveToSync) {
            Utils.haveToSync = false
            doLogout = false
            syncDialog.show()
            profileViewModel.getProfileInfo()
        }
    }

    // stop receiving location update when activity not visible/foreground
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        Utils.planogramImgShare = null
        Utils.imageShare = null
    }


    private fun getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 20000
            fastestInterval = 20000
            smallestDisplacement = 1f
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) { p0 ?: return
                if (p0.locations.isNotEmpty()) {
                    // use your location object
                    // get latitude , longitude and other info from this
                    Utils.gio_lat = (p0?.lastLocation?.latitude?:0.0).toString()
                    Utils.gio_long = (p0?.lastLocation?.longitude?:0.0).toString()
                    val locationAddress = LocationAddress()
                    locationAddress.getAddressFromLocation(p0.lastLocation!!.latitude, p0.lastLocation!!.longitude,this@WelcomeActivity,GeoCodeHandler())
                }
            }
        }
    }
    @SuppressLint("HandlerLeak")
    internal inner class GeoCodeHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            locationAddress = when (message.what) {
                1 -> { val bundle = message.data
                    bundle.getString("address")?:"Unknown"
                } else -> "Unknown"
            }
            try {
                val address = locationAddress.replace("null","")
                dataSyncViewModel.userLocation(Utils.gio_lat ?: "0.0", Utils.gio_long ?: "0.0",when{address.isNotEmpty()->address else->"Unknown"})
            } catch (_ :Exception){}
        }
    }

    //start location updates
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    // stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    private fun showGpsEnablerDialogs() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (isGpsEnabled || isNetworkEnabled) {
            dialog?.dismiss()
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            startLocationUpdates()
        } else {
            dialog?.show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        showGpsEnablerDialogs()
                    }
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
                return
            }
        }
    }


    private fun initializeSyncDialog() {
        val builder = AlertDialog.Builder(this,R.style.Calender_dialog_theme)
        val customView = DialogUpsyncBinding.inflate(layoutInflater)
        builder.setView(customView.root)
        syncDialog = builder.create()
        syncDialog.setCancelable(false)
        syncDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setupProfileInfo() {
        val profileInfo = Gson().fromJson(SharedPref.getUserInfo()?:"", ProfileData::class.java)
        profileInfo?.details?.image?.let {
            when{it.isNotEmpty()-> Picasso.get().load(it).error(R.drawable.ic_user).into(binding.profileImg)}
        }
        profileInfo?.reg_info?.name?.let {it.replace("  "," ").also { name->
            binding.welcomeMsg.text = "Hello, $name"
        }}
    }


    private fun navigateToMainPage(currentPage: String) {
        Utils.currentPage = currentPage
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun processAllData() {
        if (outletList.isEmpty() && scheduleList.isEmpty() && visitList.isEmpty() && detailScheduleList.isEmpty() && updatableOutletList.isEmpty()) {
            dataSyncViewModel.deleteInfoLocalDb()
        } else {
            val gson = Gson()
            val outletElement = gson.toJsonTree(outletList, object : TypeToken<List<Outlet?>?>() {}.type)
            val updateOutletElement = gson.toJsonTree(updatableOutletList, object : TypeToken<List<Outlet?>?>() {}.type)
            val scheduleElement = gson.toJsonTree(scheduleList, object : TypeToken<List<Schedule?>?>() {}.type)
            val detailScheduleElement = gson.toJsonTree(detailScheduleList, object : TypeToken<List<DetailingSchedule?>?>() {}.type)

            val jsonObject = JsonObject()
            val jsonArray = JsonArray()

            val outletArray = outletElement.asJsonArray
            val updateOutletArray = updateOutletElement.asJsonArray
            val scheduleArray = scheduleElement.asJsonArray
            val detailScheduleArray = detailScheduleElement.asJsonArray

            jsonObject.add("create_outlet", outletArray)
            jsonObject.add("update_outlet", updateOutletArray)
            jsonObject.add("create_schedule", scheduleArray)
            jsonObject.add("create_store_schedule", detailScheduleArray)
            jsonObject.add("visit_data", jsonArray)

            visitList.forEach {
                val visitObject = JsonObject()
                val mainObject = JsonObject()
                visitObject.addProperty("user_id", SharedPref.getUserID())
                visitObject.addProperty("schedule_id", it.schedule_id)
                visitObject.addProperty("outlet_id", it.outlet_id)
                visitObject.addProperty("country_id", it.country_id)
                visitObject.addProperty("state_id", it.state_id)
                visitObject.addProperty("region_id", it.region_id)
                visitObject.addProperty("location_id", it.location_id)
                visitObject.addProperty("visit_date", it.visit_date)
                visitObject.addProperty("visit_time", it.visit_time)
                visitObject.addProperty("outlet_type_id", it.outlet_type_id)
                visitObject.addProperty("outlet_channel_id", it.outlet_channel_id)

                visitObject.addProperty("stat_time", it.stat_time)
                visitObject.addProperty("end_time", it.end_time)
                visitObject.addProperty("gio_lat", it.gio_lat)
                visitObject.addProperty("gio_long", it.gio_long)
                visitObject.addProperty("is_exception", it.is_exception)
                visitObject.addProperty("visit_distance", it.visit_distance)
                visitObject.addProperty("isInternetAvailable", it.isInternetAvailable)

                when (it.visit_type) {
                    1 -> {
                        val imageList = Gson().fromJson(it.image_list, Array<ImageModel>::class.java).asList()
                        val imageListType = Gson().toJsonTree(imageList, object : TypeToken<List<ImageModel>?>() {}.type)

                        visitObject.add("visited_images", imageListType.asJsonArray)
                        visitObject.addProperty("note", it.notes)

                        mainObject.add("outlet_visit", visitObject)

                        val json = gson.toJsonTree(mainObject, object : TypeToken<JsonObject>() {}.type)
                        jsonArray.add(json)
                        jsonObject.add("visit_data", jsonArray)
                    }
                    2 -> {
                        // image list to json array
                        val imageList = Gson().fromJson(it.image_list!!.replace("\n", ""), Array<ImageModel>::class.java).asList()
                        val imageListType = Gson().toJsonTree(imageList, object : TypeToken<List<ImageModel>?>() {}.type)

                        // planogram questions
                        val questionList = Gson().fromJson(it.planogram_list, Array<PlanogramQuestions>::class.java).asList()
                        val questionListType = Gson().toJsonTree(questionList, object : TypeToken<List<PlanogramQuestions>?>() {}.type)

                        // available products
                        val productList = Gson().fromJson(it.available_list, Array<ProductAvailableData>::class.java).asList()
                        val productListType = Gson().toJsonTree(productList, object : TypeToken<List<ProductAvailableData>?>() {}.type)

                        visitObject.add("visited_images", imageListType.asJsonArray)
                        visitObject.add("visit_planograms", questionListType.asJsonArray)
                        visitObject.add("visit_product_availabilities", productListType.asJsonArray)

                        mainObject.add("merchandis_visit", visitObject)
                        val json = gson.toJsonTree(mainObject, object : TypeToken<JsonObject>() {}.type)
                        jsonArray.add(json)
                        jsonObject.add("visit_data", jsonArray)

                    }
                    3 -> {
                        val detailingData = Gson().fromJson(it.store_detailing_visit, StoreDetailingData::class.java)
                        try {
                            visitObject.addProperty("visited_images", it.image_list!!.replace("\n",""))
                            visitObject.addProperty("notes", detailingData.note)
                            visitObject.addProperty("status", detailingData.status)
                            visitObject.addProperty("is_insert", detailingData.is_insert)
                            mainObject.add("store_schedule_details", visitObject)
                            val json = gson.toJsonTree(mainObject, object : TypeToken<JsonObject>() {}.type)
                            jsonArray.add(json)
                            jsonObject.add("visit_data", jsonArray)
                        } catch (e :Exception){/**/}
                    }
                    4 -> {
                        // posm tracking list
                        val trackingProductList = Gson().fromJson(it.posm_tracking_list, Array<PosmTrackingData>::class.java).asList()
                        val trackingProductListType = Gson().toJsonTree(trackingProductList, object : TypeToken<List<PosmTrackingData>?>() {}.type)

                        // posm deploy list
                        val deployProductList = Gson().fromJson(it.posm_deploy_list, Array<PosmDeploymentData>::class.java).asList()
                        val deployProductListType = Gson().toJsonTree(deployProductList, object : TypeToken<List<PosmDeploymentData>?>() {}.type)

                        visitObject.add("posm_tracking_list", trackingProductListType.asJsonArray)
                        visitObject.add("posm_deploy_list", deployProductListType.asJsonArray)

                        mainObject.add("posm_visit", visitObject)

                        val json = gson.toJsonTree(mainObject, object : TypeToken<JsonObject>() {}.type)
                        jsonArray.add(json)
                        jsonObject.add("visit_data", jsonArray)
                    }
                    5 -> {
                        // image list to json array
                        val imageList = Gson().fromJson(it.image_list!!.replace("\n", ""), Array<ImageModel>::class.java).asList()
                        val imageListType = Gson().toJsonTree(imageList, object : TypeToken<List<ImageModel>?>() {}.type)

                        // available list
                        val productList = Gson().fromJson(it.available_list, Array<ProductAvailableData>::class.java).asList()
                        val productListType = Gson().toJsonTree(productList, object : TypeToken<List<ProductAvailableData>?>() {}.type)

                        // promo description
                        val promoDescriptionList = Gson().fromJson(it.promo_description_list, Array<PromoDescriptionData>::class.java).asList()
                        val promoDescriptionListType = Gson().toJsonTree(promoDescriptionList, object : TypeToken<List<PromoDescriptionData>?>() {}.type)

                        visitObject.add("available_list", productListType.asJsonArray)
                        visitObject.add("visited_images", imageListType.asJsonArray)
                        visitObject.add("promo_description", promoDescriptionListType.asJsonArray)
                        visitObject.addProperty("note", it.notes)

                        mainObject.add("competition_visit", visitObject)

                        val json = gson.toJsonTree(mainObject, object : TypeToken<JsonObject>() {}.type)
                        jsonArray.add(json)
                        jsonObject.add("visit_data", jsonArray)
                    }
                    6 -> {
                        // image list to json array
                        val imageList = Gson().fromJson(it.image_list!!.replace("\n", ""), Array<ImageModel>::class.java).asList()
                        val imageListType = Gson().toJsonTree(imageList, object : TypeToken<List<ImageModel>?>() {}.type)

                        // available list
                        val productList = Gson().fromJson(it.available_list, Array<ProductAvailableData>::class.java).asList()
                        val productListType = Gson().toJsonTree(productList, object : TypeToken<List<ProductAvailableData>?>() {}.type)

                        visitObject.add("expiry_product_list", productListType.asJsonArray)
                        visitObject.add("visited_images", imageListType.asJsonArray)
                        visitObject.addProperty("note", it.notes)

                        mainObject.add("freshness_visit", visitObject)

                        val json = gson.toJsonTree(mainObject, object : TypeToken<JsonObject>() {}.type)
                        jsonArray.add(json)
                        jsonObject.add("visit_data", jsonArray)
                    }
                    7 -> {
                        // out of stock list
                        val productList = Gson().fromJson(it.available_list, Array<OutOfStockData>::class.java).asList()
                        val productListType = Gson().toJsonTree(productList, object : TypeToken<List<OutOfStockData>?>() {}.type)

                        visitObject.add("out_of_stock", productListType.asJsonArray)
                        visitObject.addProperty("note", it.notes)

                        mainObject.add("oos_visit", visitObject)

                        val json = gson.toJsonTree(mainObject, object : TypeToken<JsonObject>() {}.type)
                        jsonArray.add(json)
                        jsonObject.add("visit_data", jsonArray)
                    }
                    8 -> {
                        // image list to json array
                        val imageList = Gson().fromJson(it.image_list!!.replace("\n", ""), Array<PlanogramImage>::class.java).asList()
                        val imageListType = Gson().toJsonTree(imageList, object : TypeToken<List<PlanogramImage>?>() {}.type)
                        // planogram questions
                        val questionList = Gson().fromJson(it.planogram_list, Array<PlanogramQuestions>::class.java).asList()
                        val questionListType = Gson().toJsonTree(questionList, object : TypeToken<List<PlanogramQuestions>?>() {}.type)

                        visitObject.add("visit_planograms", questionListType.asJsonArray)
                        visitObject.add("visited_images", imageListType.asJsonArray)
                        visitObject.addProperty("note", it.notes)

                        mainObject.add("planogram_visit", visitObject)

                        val json = gson.toJsonTree(mainObject, object : TypeToken<JsonObject>() {}.type)
                        jsonArray.add(json)
                        jsonObject.add("visit_data", jsonArray)
                    }
                    9 -> {
                        // image list to json array
                        val imageList = Gson().fromJson(it.image_list!!.replace("\n", ""), Array<ImageModel>::class.java).asList()
                        val imageListType = Gson().toJsonTree(imageList, object : TypeToken<List<ImageModel>?>() {}.type)

                        // planogram questions
                        // out of stock list
                        val productList = Gson().fromJson(it.available_list, Array<PriceCheckData>::class.java).asList()
                        val productListType = Gson().toJsonTree(productList, object : TypeToken<List<PriceCheckData>?>() {}.type)

                        visitObject.add("pricing_check_list", productListType.asJsonArray)
                        visitObject.add("visited_images", imageListType.asJsonArray)

                        mainObject.add("pricing_visit", visitObject)

                        val json = gson.toJsonTree(mainObject, object : TypeToken<JsonObject>() {}.type)
                        jsonArray.add(json)
                        jsonObject.add("visit_data", jsonArray)


                    }
                    10 -> {
                        // product order list
                        val productList = Gson().fromJson(it.available_list, Array<ProductOrderData>::class.java).asList()
                        val productListType = Gson().toJsonTree(productList, object : TypeToken<List<ProductOrderData>?>() {}.type)

                        visitObject.add("order_list", productListType.asJsonArray)
                        visitObject.addProperty("note", it.notes)

                        mainObject.add("ordering_visit", visitObject)

                        val json = gson.toJsonTree(mainObject, object : TypeToken<JsonObject>() {}.type)
                        jsonArray.add(json)
                        jsonObject.add("visit_data", jsonArray)
                    }
                }
            }.also {
                val bodyRequest: RequestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString())
                dataSyncViewModel.upSync(bodyRequest)
                Log.wtf("up_sync", jsonObject.toString())
            }
        }
    }
}
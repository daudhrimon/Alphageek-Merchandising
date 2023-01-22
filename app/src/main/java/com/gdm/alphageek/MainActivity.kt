package com.gdm.alphageek

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.navigation.Navigation
import com.gdm.alphageek.data.remote.LocationAddress
import com.gdm.alphageek.databinding.ActivityMainBinding
import com.gdm.alphageek.utils.*
import com.gdm.alphageek.viewmodels.DataSyncViewModel
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    //start location
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    // end location
    private lateinit var currentFragment: String
    private val dataSyncViewModel: DataSyncViewModel by viewModels()
    private val navController by lazy { Navigation.findNavController(this, R.id.navHostFragment) }
    lateinit var titleName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // nav menu click event listener
        titleName                  = binding.toolbar.titleName
        val hView                  = binding.navView.getHeaderView(0)
        val dashboardLayout        = hView.findViewById<LinearLayout>(R.id.dashboardLayout)
        val routePlanLayout        = hView.findViewById<LinearLayout>(R.id.routePlanLayout)
        val outletLayout           = hView.findViewById<LinearLayout>(R.id.outletLayout)
        val visitScheduleLayout    = hView.findViewById<LinearLayout>(R.id.visitScheduleLayout)
        val detailingLayout        = hView.findViewById<LinearLayout>(R.id.detailingLayout)
        val inboxLayout            = hView.findViewById<LinearLayout>(R.id.inboxLayout)
        val helpLayout             = hView.findViewById<LinearLayout>(R.id.helpLayout)
        val syncLayout             = hView.findViewById<LinearLayout>(R.id.syncLayout)

        // hide and show according to module
        when (Utils.currentPage) {
            Constants.NAVIGATION_OUTLET -> {
                routePlanLayout.isVISIBLE()
                visitScheduleLayout.isVISIBLE()
                outletLayout.isVISIBLE()
            }
            //Constants.NAVIGATION_MERCHANDISING -> {/**/}
            Constants.NAVIGATION_DETAILING -> {
                navController.navigate(R.id.storeDetailingDashboardFragment)
                detailingLayout.isVISIBLE()
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentFragment = destination.label.toString()
            titleName.text = currentFragment
            when (currentFragment) { "Inbox" -> inboxLayout.isGONE() else -> inboxLayout.isVISIBLE() }
            when (currentFragment) { "Help & Support" -> helpLayout.isGONE() else -> helpLayout.isVISIBLE() }
        }

        binding.toolbar.menuBtn.setOnClickListener {
            binding.drawerLayout.open()
        }
        // event click listener
        dashboardLayout.setOnClickListener{
            if(currentFragment == "Dashboard") {
                finish()
            } else {
                binding.drawerLayout.close()
                navController.navigate(R.id.dashboardFragment)
            }
        }
        outletLayout.setOnClickListener {
            binding.drawerLayout.close()
            if (currentFragment != "Outlets"){
                navController.navigate(R.id.outletListFragment)
            }
        }
        inboxLayout.setOnClickListener {
            binding.drawerLayout.close()
            if (currentFragment != "Inbox"){
                navController.navigate(R.id.inboxFragment)
            }
        }
        visitScheduleLayout.setOnClickListener {
            binding.drawerLayout.close()
            if (currentFragment != "Visit Schedule"){
                navController.navigate(R.id.scheduleFragment)
            }
        }
        routePlanLayout.setOnClickListener {
            binding.drawerLayout.close()
            if (currentFragment != "Route Plan"){
                navController.navigate(R.id.routePlanFragment)
            }
        }
        detailingLayout.setOnClickListener {
            binding.drawerLayout.close()
            if (currentFragment != "Store Detailed Schedules"){
                navController.navigate(R.id.detailingScheduleListFragment)
            }
        }
        helpLayout.setOnClickListener {
            binding.drawerLayout.close()
            if (currentFragment != "Help & Support"){
                navController.navigate(R.id.helpAndSupportFragment)
            }
        }
        syncLayout.setOnClickListener {
            if (Utils.checkForInternet(this)){
                Utils.haveToSync = true
                finish()
            } else {
                showInfoToast(this,"No internet connection")
            }
        }

        getLocationUpdates()
    }

    // start receiving location update when activity visible/foreground
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
            binding.drawerLayout.close()
        } else {
            when (currentFragment) {
                Constants.NAVIGATION_DASHBOARD -> finish()
                Constants.NAVIGATION_OUTLET_LIST -> navController.navigate(R.id.dashboardFragment)
                else -> {
                    super.onBackPressed()
                }
            }
        }
    }

    private fun getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 10000
            smallestDisplacement = 1f
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                if (p0.locations.isNotEmpty()) {
                    // use your location object
                    // get latitude , longitude and other info from this
                    Utils.gio_lat = (p0?.lastLocation?.latitude?:0.0).toString()
                    Utils.gio_long = (p0?.lastLocation?.longitude?:0.0).toString()
                    val locationAddress = LocationAddress()
                    locationAddress.getAddressFromLocation(p0.lastLocation!!.latitude, p0.lastLocation!!.longitude, this@MainActivity, GeoCodeHandler())
                }
            }
        }
    }
    @SuppressLint("HandlerLeak")
    internal inner class GeoCodeHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            val locationAddress: String = when (message.what) {
                1 -> {
                    val bundle = message.data
                    bundle.getString("address")?:"Unknown"
                } else -> "Unknown"
            }
            try {
                val address = locationAddress.replace("null","")
                dataSyncViewModel.userLocation(Utils.gio_lat ?: "0.0", Utils.gio_long ?: "0.0",when{address.isNotEmpty()->address else->"Unknown"})
            } catch (_ :Exception){}
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }
}
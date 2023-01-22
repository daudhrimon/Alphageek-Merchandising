package com.gdm.alphageek.application

import android.Manifest
import android.R
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gdm.alphageek.data.AppLocationService
import com.gdm.alphageek.data.remote.LocationAddress
import com.google.android.gms.location.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import dagger.hilt.android.HiltAndroidApp
import java.lang.Thread.sleep


@HiltAndroidApp
class BaseApplication :Application(), LocationListener {
    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 30
        fastestInterval = 10
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        maxWaitTime = 60
    }
    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                Toast.makeText(
                    applicationContext,
                    "Got Location: " + location.toString(),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
    }
    private  val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private  val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66
    var currentLocaiton: Location? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    val REQUEST_CODE = 101
    //
    lateinit var appLocationService: AppLocationService
    lateinit var location: Location
    var  longitudeStr:String=""
    var  latitudeStr :String=""

    override fun onCreate() {
        super.onCreate()
//        fetchLastLocation()
     //   appLocationService = AppLocationService(applicationContext)
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(applicationContext)

//
//        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
//            == PackageManager.PERMISSION_GRANTED
//        ) {
//
//            fusedLocationProvider?.requestLocationUpdates(
//                locationRequest,
//                locationCallback,
//                Looper.getMainLooper()
//            )
//        }
//        checkLocationPermission()

    }
    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    applicationContext as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(applicationContext)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        //Prompt the user once explanation has been shown
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            } else {
                // No explanation needed, we can request the permission.
                requestLocationPermission()
            }
        } else {
//            checkBackgroundLocation()
        }
    }
    private fun checkBackgroundLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBackgroundLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            applicationContext as Activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }
    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                applicationContext as Activity,
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                applicationContext as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }
//    internal inner class GeoCodeHandler : Handler() {
//        override fun handleMessage(message: Message) {
//            val locationAddress: String
//            locationAddress = when (message.what) {
//                1 -> {
//                    val bundle = message.data
//                    bundle.getString("address")!!
//                }
//                else -> null.toString()
//            }
//            Toast.makeText(applicationContext,locationAddress,Toast.LENGTH_LONG).show()
//
////            binding. tvAddress.text = locationAddress
//        }
//    }

    //

    private fun fetchLastLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext);
        fusedLocationProviderClient!!.removeLocationUpdates(object : LocationCallback() {})
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                applicationContext as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
            return
        }
        val task: Task<Location> = fusedLocationProviderClient!!.lastLocation
        task.addOnSuccessListener(OnSuccessListener<Location?> { location ->
            if (location != null) {
                currentLocaiton = location
                Log.i("location",currentLocaiton!!.longitude.toString())

                sleep(4000)

                fetchLastLocation()
            }
        })
    }

    override fun onLocationChanged(p0: Location) {
        Log.i("lllog",p0.latitude.toString())
        //Toast.makeText(applicationContext,p0.latitude.toString(),Toast.LENGTH_LONG).show()
    }
}
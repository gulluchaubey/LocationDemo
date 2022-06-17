package com.android.locationdemo

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.locationdemo.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions.check
import java.security.Permissions
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    companion object{
       var lat:String=""
       var long:String=""
    }

    private lateinit var locationPermissionStatusListener: PermissionStatusListener
    private lateinit var networkConnectivity: NetworkConnectivity
    private var alertDialog: AlertDialog? = null

    private var client: FusedLocationProviderClient? = null
    private lateinit var lbm:LocalBroadcastManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lbm = LocalBroadcastManager.getInstance(this)

        getLocationPermission()
    }

    private fun getLocationPermission() {
        networkConnectivity = NetworkConnectivity(application)
        networkConnectivity.observe(this, { isAvailable ->
            when (isAvailable) {
                false -> {
                    showToast("No internet.")
                }
            }
        })

        locationPermissionStatusListener =
            PermissionStatusListener(application, Manifest.permission.ACCESS_COARSE_LOCATION)

        locationPermissionStatusListener.observe(this, { permission ->
            when (permission) {
                is PermissionStatus.Granted -> {
                    initStartService()
                }
                is PermissionStatus.Denied -> {
                    showLocationPermissionsNotEnabledDialog()
                }
                is PermissionStatus.Blocked -> {
                    showLocationPermissionsNotEnabledDialog()
                }
            }
        })
    }
    private fun initStartService() {
        if (!isMyServiceRunning(LocationService::class.java, this)) {
            startForegroundService(Intent(this, LocationService::class.java))
        }
    }

    private fun showLocationPermissionsNotEnabledDialog() {

        alertDialog = AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("Allow otherwise location cannot be fetched.")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                //Using 3rd party lib *Permissions* for handling response

                com.nabinbhandari.android.permissions.Permissions.check(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    null, permissionHandler)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                showLocationPermissionsNotEnabledDialog()
            }
            .setCancelable(false) //to disable outside click for cancel
            .create()

        alertDialog?.show()
    }
    private val permissionHandler = object : PermissionHandler() {
        override fun onGranted() {
            initStartService()
            showToast("Location permission granted.")
        }

        override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
            showLocationPermissionsNotEnabledDialog()
        }

        override fun onJustBlocked(context: Context?,
            justBlockedList: ArrayList<String>?,
            deniedPermissions: ArrayList<String>?) {
            showLocationPermissionsNotEnabledDialog()
        }
    }




    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent != null) {
                when (intent.action) {
                    "location" -> {
                        val lat = intent.getDoubleExtra("lat", 0.0)
                        val long = intent.getDoubleExtra("long", 0.0)
                        showToast("Latitude->$lat + Longitude->$long")
                    }
                }
            }
        }
    }
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }
    override fun onResume() {
        super.onResume()
        lbm.registerReceiver(receiver, IntentFilter("location"))
    }



}
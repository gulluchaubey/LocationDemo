package com.android.locationdemo

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.locationdemo.utils.NetworkConnectivity
import com.android.locationdemo.utils.PermissionStatus
import com.android.locationdemo.utils.PermissionStatusListener
import com.android.locationdemo.utils.showToast
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions.check
import java.security.Permissions
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var locationPermissionStatusListener: PermissionStatusListener
    private lateinit var networkConnectivity: NetworkConnectivity
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    private fun showLocationPermissionsNotEnabledDialog() {
//        if (alertDialog?.isShowing == true) {
//            return // null or already being shown
//        }

        alertDialog = AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("Allow otherwise location cannot be fetched.")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                //Using 3rd party lib *Permissions* for handling response

                com.nabinbhandari.android.permissions.Permissions.check(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    null,
                    permissionHandler
                )
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
            showToast("Location permission granted.")
        }

        override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
            showLocationPermissionsNotEnabledDialog()
        }

        override fun onJustBlocked(
            context: Context?,
            justBlockedList: ArrayList<String>?,
            deniedPermissions: ArrayList<String>?
        ) {
            showLocationPermissionsNotEnabledDialog()
        }
    }


}
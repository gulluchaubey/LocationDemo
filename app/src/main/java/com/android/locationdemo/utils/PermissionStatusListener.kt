package com.android.locationdemo.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData

class PermissionStatusListener(
    private val context: Context,
    private val permissionToListen: String
) : LiveData<PermissionStatus>() {

    override fun onActive() = handlePermissionCheck()

    private fun handlePermissionCheck() {
        val isPermissionGranted = ActivityCompat.checkSelfPermission(
            context,
            permissionToListen
        ) == PackageManager.PERMISSION_GRANTED

        if (isPermissionGranted)
            postValue(PermissionStatus.Granted())
        else
            postValue(PermissionStatus.Denied())
    }
}

sealed class PermissionStatus {
    data class Granted(val message: String = "Permission Granted") : PermissionStatus()
    data class Denied(val message: String = "Permission Denied") : PermissionStatus()
    data class Blocked(val message: String = "Permission Blocked") : PermissionStatus()
}
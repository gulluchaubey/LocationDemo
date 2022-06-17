package com.android.locationdemo.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.util.Log
import android.widget.Toast

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun isMyServiceRunning(serviceClass: Class<*>, mActivity: Activity): Boolean {
    val manager: ActivityManager =
        mActivity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            Log.i("Service status", "Running")
            return true
        }
    }
    Log.i("Service status", "Not running")
    return false
}
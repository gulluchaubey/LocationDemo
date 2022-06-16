package com.android.locationdemo

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.locationdemo.utils.Constants
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient

class LocationService : Service() {
    private val TAG = "LocationService"
    private var client: FusedLocationProviderClient? = null
    private var request: LocationRequest? = null

    companion object {
        var location = MutableLiveData<Location>()
        var last_location = MutableLiveData<Location>()
//        var isServiceRunning = MutableLiveData<Boolean>()
    }

    override fun onCreate() {
        super.onCreate()
        client = LocationServices.getFusedLocationProviderClient(this)
        request = LocationRequest.create()


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            createNotificationChanel()
        startLocationUpdates()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {
        val NOTIFICATION_CHANNEL_ID = Constants.CHANNEL_ID
        val channelName = Constants.CHANNEL_NAME
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(channel)

        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setSmallIcon(R.mipmap.ic_launcher)
//            .setSmallIcon(applicationInfo.icon)
            .build()
        startForeground(2, notification)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if(locationResult!=null){
                val mLocation = locationResult.lastLocation

            }
        }
       /* override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            if (locationResult != null) {
                val mLocation = locationResult.lastLocation
                location.value = mLocation
                Log.d(TAG, "location update ${mLocation.latitude},${mLocation.longitude}")
                sendBroadcastForLocation(mLocation)
                        Log.d(TAG, "onLocationResult: $it")

            }
        }*/
    }

    private fun sendBroadcastForLocation(mLocation: Location) {
        val intent = Intent("location")
        intent.putExtra("lat", mLocation.latitude)
        intent.putExtra("long", mLocation.longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


    private fun startLocationUpdates() {
        request?.interval = 10000
//        request?.fastestInterval = 1000
//        request?.smallestDisplacement = 1f
        request?.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client?.requestLocationUpdates(request, locationCallback, null)
        }
    }

    private fun stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        client?.lastLocation?.addOnSuccessListener {
            last_location.value = it
        }?.addOnFailureListener {
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
        }
        client?.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


}
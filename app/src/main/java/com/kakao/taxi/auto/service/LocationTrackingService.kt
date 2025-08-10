package com.kakao.taxi.auto.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.kakao.taxi.auto.R

class LocationTrackingService : Service() {

    companion object {
        private const val TAG = "LocationTrackingService"
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "location_channel"
        private const val LOCATION_UPDATE_INTERVAL = 30000L // 30 seconds
        private const val LOCATION_FASTEST_INTERVAL = 15000L // 15 seconds
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isTracking = false
    private var currentLocation: Location? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "LocationTrackingService created")
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationTracking()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    currentLocation = location
                    Log.d(TAG, "Location updated: ${location.latitude}, ${location.longitude}")
                    
                    // Here you can broadcast the location or save it for automation purposes
                    broadcastLocationUpdate(location)
                }
            }
        }
    }

    private fun startLocationTracking() {
        if (isTracking) return
        
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Location permissions not granted")
            stopSelf()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
            setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL * 2)
        }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        isTracking = true
        
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        Log.d(TAG, "Location tracking started")
    }

    private fun stopLocationTracking() {
        if (!isTracking) return
        
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isTracking = false
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.d(TAG, "Location tracking stopped")
    }

    private fun broadcastLocationUpdate(location: Location) {
        val intent = Intent("com.kakao.taxi.auto.LOCATION_UPDATE").apply {
            putExtra("latitude", location.latitude)
            putExtra("longitude", location.longitude)
            putExtra("accuracy", location.accuracy)
            putExtra("timestamp", location.time)
        }
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "위치 추적",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "백그라운드에서 위치를 추적합니다"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("위치 추적 중")
            .setContentText("카카오택시 자동화를 위해 위치를 추적하고 있습니다")
            .setSmallIcon(R.drawable.ic_location)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    fun getCurrentLocation(): Location? = currentLocation

    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
        Log.d(TAG, "LocationTrackingService destroyed")
    }
}
package com.kakao.taxi.auto.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kakao.taxi.auto.MainActivity
import com.kakao.taxi.auto.R

class AutomationService : Service() {

    companion object {
        private const val TAG = "AutomationService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "automation_channel"
        
        const val ACTION_START = "START_AUTOMATION"
        const val ACTION_STOP = "STOP_AUTOMATION"
    }

    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AutomationService created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startAutomation()
            }
            ACTION_STOP -> {
                stopAutomation()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startAutomation() {
        if (isRunning) return
        
        Log.d(TAG, "Starting automation service")
        isRunning = true
        
        val notification = createNotification(
            getString(R.string.notification_service_running),
            "자동화 서비스가 백그라운드에서 실행 중입니다."
        )
        
        startForeground(NOTIFICATION_ID, notification)
        
        // Start your automation logic here
        // This could include monitoring for KaKao Taxi app state,
        // coordinating with accessibility service, etc.
    }

    private fun stopAutomation() {
        if (!isRunning) return
        
        Log.d(TAG, "Stopping automation service")
        isRunning = false
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AutomationService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stop, "중지", stopPendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Log.d(TAG, "AutomationService destroyed")
    }
}
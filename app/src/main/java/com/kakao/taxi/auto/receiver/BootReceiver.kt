package com.kakao.taxi.auto.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Boot completed, intent action: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                // Check if auto-start is enabled in preferences
                val sharedPrefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                val autoStartEnabled = sharedPrefs.getBoolean("auto_start_on_boot", false)
                
                if (autoStartEnabled) {
                    Log.d(TAG, "Auto-start enabled, starting main activity")
                    
                    // Start the main activity
                    val mainIntent = Intent(context, com.kakao.taxi.auto.MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("started_from_boot", true)
                    }
                    
                    context.startActivity(mainIntent)
                } else {
                    Log.d(TAG, "Auto-start disabled, not starting application")
                }
            }
        }
    }
}
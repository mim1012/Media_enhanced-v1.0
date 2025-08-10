package com.kakao.taxi.auto.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenStateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScreenStateReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Screen state changed: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                Log.d(TAG, "Screen turned on")
                handleScreenOn(context)
            }
            Intent.ACTION_SCREEN_OFF -> {
                Log.d(TAG, "Screen turned off")
                handleScreenOff(context)
            }
            Intent.ACTION_USER_PRESENT -> {
                Log.d(TAG, "User present (unlocked)")
                handleUserPresent(context)
            }
        }
    }

    private fun handleScreenOn(context: Context) {
        // Broadcast screen on event for other components
        val intent = Intent("com.kakao.taxi.auto.SCREEN_ON")
        context.sendBroadcast(intent)
        
        // You can implement logic here to handle when screen turns on
        // For example, check if KaKao Taxi app should be launched
    }

    private fun handleScreenOff(context: Context) {
        // Broadcast screen off event for other components
        val intent = Intent("com.kakao.taxi.auto.SCREEN_OFF")
        context.sendBroadcast(intent)
        
        // You can implement logic here to handle when screen turns off
        // For example, pause certain automation activities to save battery
    }

    private fun handleUserPresent(context: Context) {
        // Broadcast user present event for other components
        val intent = Intent("com.kakao.taxi.auto.USER_PRESENT")
        context.sendBroadcast(intent)
        
        // You can implement logic here to handle when user unlocks the device
        // For example, resume automation activities
        
        val sharedPrefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val autoResumeEnabled = sharedPrefs.getBoolean("auto_resume_on_unlock", false)
        
        if (autoResumeEnabled) {
            Log.d(TAG, "Auto-resume enabled, checking automation status")
            // Add logic to resume automation if needed
        }
    }
}
package com.kakao.taxi.auto.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class KakaoTaxiAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "KakaoTaxiAccessibility"
        private const val KAKAO_TAXI_PACKAGE = "com.kakaocorp.kakaotaxi"
        
        // UI element identifiers for KaKao Taxi app
        private const val ACCEPT_BUTTON_TEXT = "승인"
        private const val CALL_REQUEST_TEXT = "호출"
        private const val DRIVER_ACCEPT_TEXT = "기사"
    }

    private var isServiceEnabled = false
    private var autoAcceptEnabled = true

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceEnabled = true
        Log.d(TAG, "Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isServiceEnabled || event == null) return
        
        val packageName = event.packageName?.toString()
        if (packageName != KAKAO_TAXI_PACKAGE) return

        Log.d(TAG, "Accessibility Event: ${event.eventType}, Package: $packageName")

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(event)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                handleViewClicked(event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handleWindowContentChanged(event)
            }
        }
    }

    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return
        
        // Check for call request dialog
        if (isCallRequestDialog(rootNode)) {
            handleCallRequest(rootNode)
        }
        
        rootNode.recycle()
    }

    private fun handleViewClicked(event: AccessibilityEvent) {
        Log.d(TAG, "View clicked: ${event.contentDescription}")
    }

    private fun handleWindowContentChanged(event: AccessibilityEvent) {
        if (!autoAcceptEnabled) return
        
        val rootNode = rootInActiveWindow ?: return
        
        // Look for accept buttons or call request notifications
        val acceptButtons = findNodesByText(rootNode, ACCEPT_BUTTON_TEXT)
        for (button in acceptButtons) {
            if (button.isClickable) {
                Log.d(TAG, "Found accept button, attempting to click")
                performClick(button)
            }
        }
        
        rootNode.recycle()
    }

    private fun isCallRequestDialog(rootNode: AccessibilityNodeInfo): Boolean {
        val callTexts = findNodesByText(rootNode, CALL_REQUEST_TEXT)
        val driverTexts = findNodesByText(rootNode, DRIVER_ACCEPT_TEXT)
        
        return callTexts.isNotEmpty() && driverTexts.isNotEmpty()
    }

    private fun handleCallRequest(rootNode: AccessibilityNodeInfo) {
        if (!autoAcceptEnabled) {
            Log.d(TAG, "Auto accept is disabled")
            return
        }
        
        Log.d(TAG, "Handling call request")
        
        // Look for accept button
        val acceptButtons = findNodesByText(rootNode, ACCEPT_BUTTON_TEXT)
        
        for (button in acceptButtons) {
            if (button.isClickable && button.isEnabled) {
                Log.d(TAG, "Clicking accept button")
                performClick(button)
                return
            }
        }
        
        // If no accept button found, try to find clickable elements
        val clickableNodes = findClickableNodes(rootNode)
        for (node in clickableNodes) {
            val text = node.text?.toString() ?: node.contentDescription?.toString() ?: ""
            if (text.contains(ACCEPT_BUTTON_TEXT) || text.contains("승인") || text.contains("수락")) {
                Log.d(TAG, "Clicking potential accept button: $text")
                performClick(node)
                return
            }
        }
    }

    private fun findNodesByText(rootNode: AccessibilityNodeInfo, text: String): List<AccessibilityNodeInfo> {
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        
        fun searchRecursively(node: AccessibilityNodeInfo) {
            val nodeText = node.text?.toString() ?: ""
            val nodeContentDescription = node.contentDescription?.toString() ?: ""
            
            if (nodeText.contains(text, ignoreCase = true) || 
                nodeContentDescription.contains(text, ignoreCase = true)) {
                nodes.add(AccessibilityNodeInfo.obtain(node))
            }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    searchRecursively(child)
                    child.recycle()
                }
            }
        }
        
        searchRecursively(rootNode)
        return nodes
    }

    private fun findClickableNodes(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val clickableNodes = mutableListOf<AccessibilityNodeInfo>()
        
        fun searchRecursively(node: AccessibilityNodeInfo) {
            if (node.isClickable) {
                clickableNodes.add(AccessibilityNodeInfo.obtain(node))
            }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    searchRecursively(child)
                    child.recycle()
                }
            }
        }
        
        searchRecursively(rootNode)
        return clickableNodes
    }

    private fun performClick(node: AccessibilityNodeInfo) {
        try {
            // First try the standard click action
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.d(TAG, "Successfully clicked using ACTION_CLICK")
                return
            }
            
            // If standard click fails, try gesture-based click
            val bounds = android.graphics.Rect()
            node.getBoundsInScreen(bounds)
            
            if (!bounds.isEmpty) {
                val centerX = bounds.centerX().toFloat()
                val centerY = bounds.centerY().toFloat()
                
                performGestureClick(centerX, centerY)
                Log.d(TAG, "Performed gesture click at ($centerX, $centerY)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing click", e)
        }
    }

    private fun performGestureClick(x: Float, y: Float) {
        val path = Path()
        path.moveTo(x, y)
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Gesture click completed")
            }
            
            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Gesture click cancelled")
            }
        }, null)
    }

    fun setAutoAcceptEnabled(enabled: Boolean) {
        autoAcceptEnabled = enabled
        Log.d(TAG, "Auto accept enabled: $enabled")
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
        isServiceEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceEnabled = false
        Log.d(TAG, "Accessibility Service Destroyed")
    }
}
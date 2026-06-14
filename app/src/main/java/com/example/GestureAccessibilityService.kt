package com.example

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GestureAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var screenHeight = 0
    private var screenWidth = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("GestureAccessibility", "Service Connected")
        val metrics = resources.displayMetrics
        screenHeight = metrics.heightPixels
        screenWidth = metrics.widthPixels

        serviceScope.launch {
            GestureController.gestureFlow.collectLatest { action ->
                performAction(action)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    private fun performAction(action: GestureAction) {
        when (action) {
            GestureAction.SCROLL_DOWN -> swipeRelative(0.5f, 0.8f, 0.5f, 0.2f, 300)
            GestureAction.SCROLL_UP -> swipeRelative(0.5f, 0.2f, 0.5f, 0.8f, 300)
            GestureAction.LIKE -> doubleTapRelative(0.5f, 0.5f)
            GestureAction.LIKE_AND_REACTION -> {
                // Example: Double tap then open comment/reaction somehow (long comment swipe maybe?)
                doubleTapRelative(0.5f, 0.5f)
                // Need to customize depending on the app
            }
            GestureAction.PAUSE_RESUME -> singleTapRelative(0.5f, 0.5f)
            GestureAction.EMERGENCY_STOP -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
    }

    private fun swipeRelative(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long) {
        val path = Path()
        path.moveTo(screenWidth * startX, screenHeight * startY)
        path.lineTo(screenWidth * endX, screenHeight * endY)
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun singleTapRelative(x: Float, y: Float) {
        val path = Path()
        path.moveTo(screenWidth * x, screenHeight * y)
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 100))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun doubleTapRelative(x: Float, y: Float) {
        val path = Path()
        path.moveTo(screenWidth * x, screenHeight * y)
        val gestureBuilder = GestureDescription.Builder()
        val stroke1 = GestureDescription.StrokeDescription(path, 0, 100)
        val stroke2 = GestureDescription.StrokeDescription(path, 150, 100)
        gestureBuilder.addStroke(stroke1)
        gestureBuilder.addStroke(stroke2)
        dispatchGesture(gestureBuilder.build(), null, null)
    }
}

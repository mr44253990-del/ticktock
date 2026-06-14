package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.GestureAction
import com.example.GestureController
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class GestureCameraService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null
    
    // Abstracted AI model mock variables
    private var isAnalyzing = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())

        // 4. Sensor Integration: Proximity Sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Initialize Camera and AI Engine mock
        startSimulatedAIEngine()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        isAnalyzing = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "gesture_service_channel",
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = getString(R.string.notification_channel_desc)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "gesture_service_channel")
            .setContentTitle("Gesture Control Active")
            .setContentText("Listening for hand gestures...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
            val distance = event.values[0]
            if (distance < 3.0f) {
                // Hand close to screen or covered
                Log.d("GestureCameraService", "Proximity triggered - Pause/Resume")
                GestureController.emitGesture(GestureAction.PAUSE_RESUME)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startSimulatedAIEngine() {
        isAnalyzing = true
        // 6. AI Gesture Engine (Mock) 
        // In a real scenario, this is where we bind CameraX to a LifecycleOwner
        // and feed frames to MediaPipe Tasks Vision or ML Kit models.
        serviceScope.launch {
            while (isAnalyzing) {
                delay(10000) // Emit a random gesture every 10 seconds for demo purposes
                // Only trigger randomly so it's not a complete mess while prototyping
                if (Random.nextFloat() > 0.8f) {
                    val gesture = arrayOf(
                        GestureAction.SCROLL_DOWN, 
                        GestureAction.SCROLL_UP, 
                        GestureAction.LIKE
                    ).random()
                    GestureController.emitGesture(gesture)
                }
            }
        }
    }
}

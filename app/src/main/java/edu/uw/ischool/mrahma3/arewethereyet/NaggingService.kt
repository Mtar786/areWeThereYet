package edu.uw.ischool.mrahma3.arewethereyet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class NaggingService : Service() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private var isNaggingStarted = false
    private var executor: ScheduledExecutorService? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isNaggingStarted) {
            startNagging()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopNagging()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startNagging() {
        val interval = 5L // Default interval (in minutes)
        executor = Executors.newSingleThreadScheduledExecutor()
        executor?.scheduleAtFixedRate({
            // Send the message here
            Log.d("NaggingService", "Sending message...")
            showToast("Are we there yet?")
        }, 0, interval, TimeUnit.MINUTES)
        isNaggingStarted = true

        // Start service in foreground to ensure it continues running
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun stopNagging() {
        executor?.shutdown()
        stopForeground(true)
        isNaggingStarted = false
    }

    private fun createNotification(): Notification {
        val channelId = "NaggingServiceChannel"
        val channelName = "Nagging Service Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        notificationChannel = NotificationChannel(channelId, channelName, importance)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Nagging Service")
            .setContentText("Service is running...")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setAutoCancel(true)

        return notificationBuilder.build()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val NOTIFICATION_ID = 101
    }
}



package com.aml_sakr.fitlife.feature.session.ui.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.aml_sakr.fitlife.feature.session.ui.R

/**
 * Foreground service for playing audio alerts during a workout session.
 * AC 9, 10 compliance:
 * - Plays warning alert even when screen is locked.
 * - Tied to the active session lifecycle.
 */
class SessionAudioService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): SessionAudioService = this@SessionAudioService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START_SESSION -> startForeground(NOTIFICATION_ID, createNotification())
            ACTION_PLAY_FATIGUE_ALERT -> playFatigueAlert()
            ACTION_STOP_SESSION -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun playFatigueAlert() {
        try {
            if (mediaPlayer?.isPlaying == true) return

            mediaPlayer?.release()
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            
            if (notificationUri != null) {
                val player = MediaPlayer.create(this, notificationUri)
                if (player != null) {
                    mediaPlayer = player
                    player.apply {
                        setOnCompletionListener { it.release() }
                        start()
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback if no notification sound is available
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.session_notification_title))
            .setContentText(getString(R.string.session_notification_body))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workout Session",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "session_audio_channel"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_START_SESSION = "com.aml_sakr.fitlife.ACTION_START_SESSION"
        const val ACTION_STOP_SESSION = "com.aml_sakr.fitlife.ACTION_STOP_SESSION"
        const val ACTION_PLAY_FATIGUE_ALERT = "com.aml_sakr.fitlife.ACTION_PLAY_FATIGUE_ALERT"

        fun start(context: Context) {
            val intent = Intent(context, SessionAudioService::class.java).apply {
                action = ACTION_START_SESSION
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun playAlert(context: Context) {
            val intent = Intent(context, SessionAudioService::class.java).apply {
                action = ACTION_PLAY_FATIGUE_ALERT
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, SessionAudioService::class.java).apply {
                action = ACTION_STOP_SESSION
            }
            context.startService(intent)
        }
    }
}

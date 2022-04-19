package com.untitledkingdom.ueberapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.tomcz.ellipse.common.onProcessor
import com.untitledkingdom.ueberapp.MainActivity
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import timber.log.Timber
import javax.inject.Inject

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class BackgroundService @Inject constructor() : Service() {
    private var isFirstRun = true
    private var isSendingBroadcast = true

    companion object {
        private const val CHANNEL_ID = "BackgroundService"
        private const val CHANNEL_NAME = "BackgroundContainer Reading"
        private const val ONGOING_NOTIFICATION_ID = 123
        const val ACTION_SHOW_MAIN_FRAGMENT = "ACTION_SHOW_MAIN_FRAGMENT"
        const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE "
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE "
        const val INTENT_MESSAGE_FROM_SERVICE = "INTENT_MESSAGE_FROM_SERVICE"
        var isPause = false
    }

    @Inject
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var dataStorage: DataStorage

    @Inject
    lateinit var repository: MainRepository

    @Inject
    lateinit var timeManager: TimeManager

    @Inject
    lateinit var backgroundContainer: BackgroundContainer

    override fun onCreate() {
        Timber.d("Service created")
        super.onCreate()
        scope.onProcessor(
            processor = backgroundContainer::processor,
            onEffect = ::trigger,
        )
    }

    private fun trigger(effect: BackgroundEffect) {
        when (effect) {
            BackgroundEffect.SendBroadcastToActivity -> sendBroadcastToActivity()
            BackgroundEffect.StartForegroundService -> startForegroundService()
            BackgroundEffect.Stop -> stop()
        }
    }

    private fun sendBroadcastToActivity() {
        if (isSendingBroadcast) {
            val intent = Intent(INTENT_MESSAGE_FROM_SERVICE)
            isSendingBroadcast = false
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    resumeService()
                    if (isFirstRun) {
                        isFirstRun = false
                        backgroundContainer.processor.sendEvent(BackgroundEvent.StartReading)
                    } else {
                        Timber.d("Resuming service")
                    }
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopping service")
                    stop()
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_MIN
        )
        channel.enableLights(false)
        channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        notificationManager.createNotificationChannel(channel)
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_phone_bluetooth_speaker_24)
            .setContentTitle("Reading in background...")
            .setContentIntent(getMainActivityPendingIntent())
            .build()
        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun getMainActivityPendingIntent(): PendingIntent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).also {
                    it.action = ACTION_SHOW_MAIN_FRAGMENT
                },
                FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            return PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).also {
                    it.action = ACTION_SHOW_MAIN_FRAGMENT
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun pauseService() {
        isPause = true
    }

    private fun resumeService() {
        isPause = false
    }
}

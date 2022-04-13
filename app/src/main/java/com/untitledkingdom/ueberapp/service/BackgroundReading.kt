package com.untitledkingdom.ueberapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.juul.kable.ConnectionLostException
import com.untitledkingdom.ueberapp.MainActivity
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.DeviceConst
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class BackgroundReading @Inject constructor() : Service() {
    private var isFirstRun = true
    private var isPause = false

    companion object {
        private const val CHANNEL_ID = "BackgroundReading"
        private const val CHANNEL_NAME = "Background Reading"
        const val ACTION_SHOW_MAIN_FRAGMENT = "ACTION_SHOW_MAIN_FRAGMENT"
        const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE "
        const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE "
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE "
    }

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var dataStorage: DataStorage

    @Inject
    lateinit var repository: MainRepository

    override fun onCreate() {
        Timber.d("Service created")
        super.onCreate()
    }

    private suspend fun startObservingData(device: Device) {
        try {
            Timber.d("Starting collecting data from service")
            device.observationOnDataCharacteristic().collect { reading ->
                Timber.d("Reading in service $reading")
                repository.saveData(
                    deviceReading = reading,
                    serviceUUID = DeviceConst.SERVICE_DATA_SERVICE,
                )
            }
        } catch (e: ConnectionLostException) {
            Timber.d("Service cannot connect to device!")
        } catch (e: Exception) {
            Timber.d("Service error! $e")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    resumeService()
                    if (isFirstRun) {
                        isFirstRun = false
                        handleService()
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

    private fun handleService() {
        scope.launch {
            try {
                val device = Device(dataStorage)
                startObservingData(device)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    startForegroundService()
                }
            } catch (e: ConnectionLostException) {
                Timber.d("Exception during creating device $e")
                stop()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        createNotificationChannel(notificationManager)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_phone_bluetooth_speaker_24)
            .setContentTitle("Reading in background...")
            .setContentIntent(getMainActivityPendingIntent())
        startForeground(134, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_MAIN_FRAGMENT
        },
        FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )

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

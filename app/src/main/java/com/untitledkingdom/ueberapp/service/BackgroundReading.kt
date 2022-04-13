package com.untitledkingdom.ueberapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.untitledkingdom.ueberapp.MainActivity
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConstants
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.DeviceConst
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class BackgroundReading @Inject constructor() : Service() {
    private var isFirstRun = true

    companion object {
        private const val CHANNEL_ID = "BackgroundReading"
        const val ACTION_SHOW_MAIN_FRAGMENT = "ACTION_SHOW_MAIN_FRAGMENT"
        const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE "
        const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE "
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE "
    }

    private var scope: CoroutineScope? = null
    private fun getScope() = scope
        ?: CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)

    @Inject
    lateinit var dataStorage: DataStorage

    @Inject
    lateinit var repository: MainRepository

    override fun onCreate() {
        Timber.d("Service created")
        super.onCreate()
    }

    private suspend fun startObservingData(device: Device) {
        Timber.d("Starting collecting data from service")
        device.observationOnDataCharacteristic().collect { reading ->
            Timber.d("Reading in service $reading")
            repository.saveData(
                deviceReading = reading,
                serviceUUID = DeviceConst.SERVICE_DATA_SERVICE,
            )
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID, "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager!!.createNotificationChannel(serviceChannel)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        isFirstRun = false
                        getScope().launch {
                            if (dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS) == "") {
                                stopForeground(true)
                                stopSelf()
                                getScope().cancel()
                            }
                            val device = Device(dataStorage = dataStorage)
                            startObservingData(device)
                            startForegroundService()
                        }
                    } else {
                        Timber.d("Resuming service")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    onDestroy()
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startForegroundService() {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Foreground service")
            .setColor(0x000000)
            .setContentText("Device is reading data....")
            .setSmallIcon(R.drawable.ic_baseline_phone_bluetooth_speaker_24)
            .setContentIntent(getMainActivityPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
        startForeground(1, notification)
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

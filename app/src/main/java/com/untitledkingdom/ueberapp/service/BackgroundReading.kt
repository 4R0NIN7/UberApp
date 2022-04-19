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
import com.juul.kable.ConnectionLostException
import com.untitledkingdom.ueberapp.MainActivity
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConstants
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.DeviceConst
import com.untitledkingdom.ueberapp.devices.DeviceDataStatus
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.functions.checkIfDateIsTheSame
import com.untitledkingdom.ueberapp.utils.functions.toDateString
import com.untitledkingdom.ueberapp.utils.functions.toUByteArray
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class BackgroundReading @Inject constructor() : Service() {
    private var isFirstRun = true
    private var isSendingBroadcast = true

    companion object {
        private const val CHANNEL_ID = "BackgroundReading"
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

    override fun onCreate() {
        Timber.d("Service created")
        super.onCreate()
    }

    private suspend fun startObservingData(device: Device) {
        try {
            Timber.d("Starting collecting data from service")
            startForegroundService()
            device.observationOnDataCharacteristic().collect { reading ->
                sendBroadcastToActivity()
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
            if (dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS) == "") {
                stop()
            }
            try {
                val device = Device(dataStorage)
                writeDateToDevice(
                    service = DeviceConst.SERVICE_TIME_SETTINGS,
                    characteristic = DeviceConst.TIME_CHARACTERISTIC,
                    device = device
                )
                startObservingData(device)
            } catch (e: ConnectionLostException) {
                Timber.d("Exception during creating device $e")
                stop()
            }
        }
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

    private suspend fun writeDateToDevice(
        service: String,
        characteristic: String,
        device: Device
    ) {
        try {
            val status = device.readDate(
                fromCharacteristic = characteristic,
                fromService = service
            )
            when (status) {
                is DeviceDataStatus.SuccessDate -> checkDate(
                    status.date,
                    service,
                    characteristic,
                    device
                )
                DeviceDataStatus.Error -> throw Exception()
                else -> {}
            }
        } catch (e: Exception) {
            Timber.d("Unable to write deviceReading $e")
        }
    }

    private suspend fun checkDate(
        bytes: List<Byte>,
        service: String,
        characteristic: String,
        device: Device
    ) {
        val dateFromDevice = toDateString(bytes.toByteArray())
        val currentDate = timeManager.provideCurrentLocalDateTime()
        val checkIfTheSame = checkIfDateIsTheSame(
            date = currentDate,
            dateFromDevice = dateFromDevice
        )
        if (!checkIfTheSame) {
            Timber.d("writeDateToDevice Saving date")
            device.write(currentDate.toUByteArray(), service, characteristic)
        }
    }
}

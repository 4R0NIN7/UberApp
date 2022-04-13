package com.untitledkingdom.ueberapp.workManager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.DeviceConst
import com.untitledkingdom.ueberapp.devices.DeviceStatus
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.functions.checkIfDateIsTheSame
import com.untitledkingdom.ueberapp.utils.functions.toDateString
import com.untitledkingdom.ueberapp.utils.functions.toUByteArray
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
@HiltWorker
class ReadingWorker @AssistedInject constructor(
    @Assisted dataStorage: DataStorage,
    private val timeManager: TimeManager,
    private val repository: MainRepository,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val device = Device(dataStorage)
    override suspend fun doWork(): Result {
        return try {
            Timber.d("WorkManager doWork")
            handleDate()
            handleReadings()
            makeStatusNotification(message = "WorkManager succeed", context = applicationContext)
            Result.success()
        } catch (e: Exception) {
            Timber.d(e)
            Result.failure()
        }
    }

    private suspend fun handleDate() {
        try {
            val status = device.readDate(
                fromCharacteristic = DeviceConst.TIME_CHARACTERISTIC,
                fromService = DeviceConst.SERVICE_TIME_SETTINGS
            )
            when (status) {
                is DeviceStatus.SuccessDate -> checkDate(
                    status.date, DeviceConst.SERVICE_TIME_SETTINGS, DeviceConst.TIME_CHARACTERISTIC
                )
                DeviceStatus.Error -> throw Exception()
                else -> {}
            }
        } catch (e: Exception) {
            Timber.d("Unable to handleDate workManager $e")
            throw e
        }
    }

    private suspend fun checkDate(bytes: List<Byte>, service: String, characteristic: String) {
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

    private suspend fun handleReadings() {
        try {
            val status = device.read(
                fromService = DeviceConst.SERVICE_DATA_SERVICE,
                fromCharacteristic = DeviceConst.READINGS_CHARACTERISTIC
            )
            when (status) {
                is DeviceStatus.SuccessDeviceReading -> {
                    repository.saveData(
                        DeviceConst.SERVICE_DATA_SERVICE,
                        deviceReading = status.reading
                    )
                }
                DeviceStatus.Error -> throw Exception()
                else -> {}
            }
        } catch (e: Exception) {
            Timber.d("Unable to handle readings workManager $e")
            throw e
        }
    }

    fun makeStatusNotification(message: String, context: Context) {
        val name = WorkManagerConst.NOTIFICATION_CHANNEL_NAME
        val description = WorkManagerConst.NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(WorkManagerConst.CHANNEL_ID, name, importance)
        channel.description = description
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
        val builder = NotificationCompat.Builder(context, WorkManagerConst.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(WorkManagerConst.NOTIFICATION_TITLE)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))
        NotificationManagerCompat.from(context)
            .notify(WorkManagerConst.NOTIFICATION_ID, builder.build())
    }
}

object WorkManagerConst {
    const val WORK_TAG = "READING_FROM_DEVICE"
    const val NOTIFICATION_CHANNEL_NAME = "Work Manager Channel"
    const val NOTIFICATION_CHANNEL_DESCRIPTION = "Work Manager Description"
    const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
    const val NOTIFICATION_TITLE = "Work manager response"
    const val NOTIFICATION_ID = 1
}

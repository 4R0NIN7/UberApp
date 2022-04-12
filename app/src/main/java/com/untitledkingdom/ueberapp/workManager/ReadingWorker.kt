package com.untitledkingdom.ueberapp.workManager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
}

object WorkManagerConst {
    const val WORK_TAG = "READING_FROM_DEVICE"
}

package com.untitledkingdom.ueberapp.workManager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.feature.data.BleData
import com.untitledkingdom.ueberapp.feature.data.BleDevice
import com.untitledkingdom.ueberapp.feature.data.BleDeviceStatus
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@HiltWorker
class ReadingWorker @Inject constructor(
    private val bleDevice: BleDevice,
    private val database: Database,
    private val timeManager: TimeManager,
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("doWork")
            when (val status = bleDevice.readOnce().first()) {
                is BleDeviceStatus.Success -> {
                    Timber.d("do Work Worker Success")
                    val bleData = BleData(
                        data = status.data,
                        localDateTime = timeManager.provideCurrentLocalDateTime(),
                        serviceUUID = bleDevice.serviceUUID,
                        characteristicUUID = bleDevice.characteristicUUID
                    )
                    Timber.d("doWork saved to DataBase $bleData")
                    database.getDao().saveData(bleData)
                    Result.success()
                }
                else -> {
                    Timber.d("doWork error during reading")
                    throw IllegalAccessException()
                }
            }
        } catch (e: IllegalAccessException) {
            Result.retry()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

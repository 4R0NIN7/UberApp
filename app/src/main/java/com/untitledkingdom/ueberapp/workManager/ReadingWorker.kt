package com.untitledkingdom.ueberapp.workManager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class ReadingWorker @AssistedInject constructor(
    private val repository: MainRepository,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("doWork")
//            repository.readOnceFromDevice(
//                service = DeviceConst.SERVICE_DATA_SERVICE,
//                characteristic = DeviceConst.READINGS_CHARACTERISTIC
//            )
            Result.success()
        } catch (e: Exception) {
            Timber.d(e)
            Result.failure()
        }
    }
}

package com.untitledkingdom.ueberapp.workmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.untitledkingdom.ueberapp.MainActivity
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.data.DeviceConst
import com.untitledkingdom.ueberapp.service.ReadingRepository
import com.untitledkingdom.ueberapp.service.ReadingService
import com.untitledkingdom.ueberapp.utils.ContainerDependencies
import com.untitledkingdom.ueberapp.utils.DaggerReadingWorkerComponent
import com.untitledkingdom.ueberapp.utils.ScopeProviderEntryPoint
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalUnsignedTypes
class ReadingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "ReadingWorker"
        private const val CHANNEL_NAME = "ReadingWorker Reading"
        private const val ONGOING_NOTIFICATION_ID = 321
        const val WORK_NAME = "ReadingWorkerName"
    }

    private fun getScope(): CoroutineScope {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ScopeProviderEntryPoint::class.java
        )
        return hiltEntryPoint.scope()
    }

    init {
        Timber.d("ReadingWorker init")
        DaggerReadingWorkerComponent.builder()
            .scope(getScope())
            .dependencies(
                EntryPointAccessors.fromApplication(
                    applicationContext,
                    ContainerDependencies::class.java
                )
            )
            .build()
            .inject(this)
    }

    @Inject
    lateinit var device: Device

    @Inject
    lateinit var repository: ReadingRepository

    private val serviceUUID = DeviceConst.SERVICE_DATA_SERVICE
    override suspend fun doWork(): Result {
        device.observationOnDataCharacteristic()
            .onStart {
                repository.start(serviceUUID)
                setForeground(createForegroundInfo())
            }
            .onCompletion {
                repository.stop()
                getScope().coroutineContext.cancelChildren()
                stop()
            }
            .takeWhile {
                Timber.d("IsWorking")
                !isStopped
            }
            .collect { reading ->
                repository.saveData(
                    reading = reading,
                    serviceUUID = serviceUUID
                )
            }
        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.enableLights(false)
        channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        notificationManager.createNotificationChannel(channel)
        val notification = Notification.Builder(applicationContext, CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_phone_bluetooth_speaker_24)
            .setShowWhen(true)
            .setContentTitle("Reading from ÜberDevice")
            .setContentText("Click to go to details")
            .setContentIntent(getMainActivityPendingIntent())
            .build()
        return ForegroundInfo(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun getMainActivityPendingIntent(): PendingIntent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getActivity(
                applicationContext,
                0,
                Intent(applicationContext, MainActivity::class.java).also {
                    it.action = ReadingService.ACTION_SHOW_MAIN_FRAGMENT
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            return PendingIntent.getActivity(
                applicationContext,
                0,
                Intent(applicationContext, MainActivity::class.java).also {
                    it.action = ReadingService.ACTION_SHOW_MAIN_FRAGMENT
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

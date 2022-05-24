package com.untitledkingdom.ueberapp.background.workmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.tomcz.ellipse.common.onProcessor
import com.untitledkingdom.ueberapp.MainActivity
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.background.ReadingContainer
import com.untitledkingdom.ueberapp.background.service.ReadingService
import com.untitledkingdom.ueberapp.background.state.ReadingEffect
import com.untitledkingdom.ueberapp.background.state.ReadingEvent
import com.untitledkingdom.ueberapp.devices.data.Reading
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
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
        const val ACTION_SHOW_MAIN_FRAGMENT = "ACTION_SHOW_MAIN_FRAGMENT"
        const val INTENT_MESSAGE_FROM_WORKER = "INTENT_MESSAGE_FROM_SERVICE"
    }

    private var reading: Reading? = null
    private var isSendingBroadcast = true
    private var isFirstTime = true
    private fun getScope(): CoroutineScope {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ScopeProviderEntryPoint::class.java
        )
        return hiltEntryPoint.scope()
    }

    @Inject
    lateinit var readingContainer: ReadingContainer

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
        getScope().onProcessor(
            processor = readingContainer::processor,
            onEffect = ::trigger,
        )
    }

    private fun trigger(effect: ReadingEffect) {
        when (effect) {
            is ReadingEffect.StartNotifying -> startNotifying(effect.reading)
            ReadingEffect.SendBroadcastToActivity -> sendBroadcastToActivity()
            ReadingEffect.Stop -> stopWorkManager()
        }
    }

    private fun sendBroadcastToActivity() {
        if (isSendingBroadcast) {
            val intent = Intent(ReadingService.INTENT_MESSAGE_FROM_SERVICE)
            isSendingBroadcast = false
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }

    private fun stopWorkManager() {
        Timber.d("Stopping scope isActive ${getScope().coroutineContext.isActive}")
        getScope().coroutineContext.cancelChildren()
        cancelNotification()
        onStopped()
    }

    private fun startNotifying(readingFromProcessor: Reading) {
        reading = readingFromProcessor
    }

    /*
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
         */

    override suspend fun doWork(): Result {
        flow<Unit> {
            while (!isStopped) {
                setForeground(createForegroundInfo())
            }
        }.onStart {
            readingContainer.processor.sendEvent(ReadingEvent.StartReading)
        }.onCompletion {
            readingContainer.processor.sendEvent(ReadingEvent.StopReading)
        }.collect()
        return Result.success()
    }

    private fun cancelNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        Timber.d("Canceling notification")
        notificationManager.cancel(ONGOING_NOTIFICATION_ID)
    }

    private fun createForegroundInfo(): ForegroundInfo {
        var importance: Int = NotificationManager.IMPORTANCE_LOW
        if (isFirstTime) {
            importance = NotificationManager.IMPORTANCE_HIGH
            isFirstTime = false
        }
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            importance
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
            .setContentText(
                if (reading != null) {
                    "Temperature is ${reading!!.temperature}, Humidity is ${reading!!.humidity}"
                } else {
                    "Click to go to details"
                }
            )
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

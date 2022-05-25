package com.untitledkingdom.ueberapp.background.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.tomcz.ellipse.common.onProcessor
import com.untitledkingdom.ueberapp.MainActivity
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.WorkerReceiveCancel
import com.untitledkingdom.ueberapp.background.ReadingContainer
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
        private const val READING_CHANNEL_ID = "ReadingWorker"
        private const val READING_CHANNEL_NAME = "ReadingWorker Reading"
        private const val READING_ONGOING_NOTIFICATION_ID = 321
        private const val BATTERY_CHANNEL_ID = "BatteryWorker"
        private const val BATTERY_CHANNEL_NAME = "BatteryWorker"
        private const val BATTERY_ONGOING_NOTIFICATION_ID = 123
        const val WORK_NAME = "ReadingWorkerName"
        const val ACTION_SHOW_MAIN_FRAGMENT = "ACTION_SHOW_MAIN_FRAGMENT"
        const val INTENT_MESSAGE_FROM_WORKER_GO_TO_MAIN = "INTENT_MESSAGE_FROM_WORKER"
        const val INTENT_MESSAGE_FROM_WORKER_CANCEL = "INTENT_MESSAGE_FROM_WORKER"
        const val ACTION_CANCEL_WORK = "CANCEL WORK"
    }

    private var reading: Reading? = null
    private var batteryLevel: Int = -1
    private var isShowingBatteryNotification = false
    private var isShowingNotificationAlready = false
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
            ReadingEffect.Stop -> stopWorkManager()
            is ReadingEffect.NotifyBatterLow -> warnBatteryLow(effect.batteryLevel)
        }
    }

    private fun warnBatteryLow(battery: Int) {
        batteryLevel = battery
        isShowingBatteryNotification = batteryLevel in 0..20
    }

    private fun stopWorkManager() {
        Timber.d("Stopping scope isActive ${getScope().coroutineContext.isActive}")
        getScope().coroutineContext.cancelChildren()
        cancelAllNotification()
        onStopped()
    }

    private fun startNotifying(readingFromProcessor: Reading) {
        reading = readingFromProcessor
    }

    override suspend fun doWork(): Result {
        flow<Unit> {
            while (!isStopped) {
                setForeground(createReadingNotification())
                if (isShowingBatteryNotification && !isShowingNotificationAlready) {
                    setForeground(createBatteryNotification())
                    isShowingNotificationAlready = true
                }
                if (!isShowingBatteryNotification && isShowingNotificationAlready) {
                    cancelBatteryNotification()
                }
            }
        }.onStart {
            createNotificationChannels()
            readingContainer.processor.sendEvent(
                ReadingEvent.StartBattery,
                ReadingEvent.StartReading
            )
        }.onCompletion {
            readingContainer.processor.sendEvent(ReadingEvent.StopReading)
        }.collect()
        return Result.success()
    }

    private fun cancelAllNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        notificationManager.cancelAll()
    }

    private fun cancelBatteryNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        isShowingNotificationAlready = false
        notificationManager.cancel(BATTERY_ONGOING_NOTIFICATION_ID)
    }

    private fun createBatteryNotification(): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, BATTERY_CHANNEL_ID)
            .setAutoCancel(true)
            .setOngoing(false)
            .setSmallIcon(R.drawable.ic_baseline_battery_0_bar_24)
            .setShowWhen(true)
            .setContentTitle("Battery low on ÜberDevice!")
            .build()
        return ForegroundInfo(BATTERY_ONGOING_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannels() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        val readingChannel = NotificationChannel(
            READING_CHANNEL_ID,
            READING_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        val batteryChannel = NotificationChannel(
            BATTERY_CHANNEL_ID,
            BATTERY_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        batteryChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        readingChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        readingChannel.enableLights(false)
        batteryChannel.enableLights(true)
        notificationManager.createNotificationChannel(readingChannel)
        notificationManager.createNotificationChannel(batteryChannel)
    }

    private fun createReadingNotification(): ForegroundInfo {
        val cancelAction =
            NotificationCompat.Action.Builder(
                R.drawable.ic_baseline_delete_forever_24,
                "Cancel",
                cancelWorkIntent()
            ).build()
        val goToMainAction =
            NotificationCompat.Action.Builder(
                R.drawable.ic_baseline_logout_24,
                "Go to details",
                getMainActivityPendingIntent()
            ).build()
        val notification = NotificationCompat.Builder(applicationContext, READING_CHANNEL_ID)
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
            .addAction(cancelAction)
            .addAction(goToMainAction)
            .build()
        return ForegroundInfo(READING_ONGOING_NOTIFICATION_ID, notification)
    }

    private fun cancelWorkIntent(): PendingIntent {
        val intent = Intent(this.applicationContext, WorkerReceiveCancel::class.java).also {
            it.action = ACTION_CANCEL_WORK
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                this.applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                this.applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    private fun getMainActivityPendingIntent(): PendingIntent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getActivity(
                applicationContext,
                0,
                Intent(applicationContext, MainActivity::class.java).also {
                    it.action = ACTION_SHOW_MAIN_FRAGMENT
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            return PendingIntent.getActivity(
                applicationContext,
                0,
                Intent(applicationContext, MainActivity::class.java).also {
                    it.action = ACTION_SHOW_MAIN_FRAGMENT
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

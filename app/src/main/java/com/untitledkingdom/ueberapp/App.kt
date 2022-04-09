package com.untitledkingdom.ueberapp

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import com.untitledkingdom.ueberapp.workManager.ReadingWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {
    @Inject
    lateinit var myWorkerFactory: MyWorkerFactory
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(myWorkerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

class MyWorkerFactory @Inject constructor(
    private val repository: MainRepository
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ReadingWorker =
        ReadingWorker(
            repository = repository,
            context = appContext,
            workerParameters
        )
}

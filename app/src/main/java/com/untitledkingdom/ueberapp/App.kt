package com.untitledkingdom.ueberapp

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import com.untitledkingdom.ueberapp.utils.Modules
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.workManager.ReadingWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber
import javax.inject.Inject

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
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

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class MyWorkerFactory @Inject constructor(
    private val repository: MainRepository,
    private val timeManager: TimeManager,
    private val dataStorage: DataStorage,
    @Modules.IoDispatcher private val dispatcher: CoroutineDispatcher
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ReadingWorker =
        ReadingWorker(
            repository = repository,
            timeManager = timeManager,
            dataStorage = dataStorage,
            context = appContext,
            params = workerParameters,
            dispatcher = dispatcher
        )
}

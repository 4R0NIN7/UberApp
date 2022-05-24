package com.untitledkingdom.ueberapp.background

import com.untitledkingdom.ueberapp.api.ApiService
import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.database.data.BleDataEntity
import com.untitledkingdom.ueberapp.devices.data.Reading
import com.untitledkingdom.ueberapp.utils.AppModules
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
class ReadingRepositoryImpl @Inject constructor(
    private val database: Database,
    private val apiService: ApiService,
    private val timeManager: TimeManager,
    @AppModules.ReadingScope private val scope: CoroutineScope
) : ReadingRepository {
    private var isStarted: Boolean = false
    private var isSendingData = false
    private suspend fun countData(serviceUUID: String) {
        database.getDao().countNotSynchronized(serviceUUID).collect { count ->
            if (count != null) {
                val countCondition = count == 20 || count > 22
                if (countCondition && !isSendingData) {
                    isSendingData = true
                    val data = database
                        .getDao()
                        .getDataNotSynchronized(serviceUUID)
                    sendData(data)
                }
            }
        }
    }

    private suspend fun sendData(data: List<BleDataEntity>) {
        Timber.d(
            "Size of data ${data.size}" +
                "\nFirst id is ${data.first().id}" +
                "\nLast id is ${data.last().id}"
        )
        try {
            val response = apiService.sendDataToService(bleDatumEntities = data)
            if (response.isSuccessful) {
                database.getDao().saveAllData(
                    dataList = data.map {
                        BleDataEntity(
                            id = it.id,
                            temperature = it.temperature,
                            humidity = it.humidity,
                            dateTime = it.dateTime,
                            serviceUUID = it.serviceUUID,
                            isSynchronized = true
                        )
                    }
                )
            } else {
                Timber.d("Unable to send data!")
            }
        } catch (e: Exception) {
            Timber.d("Exception is sending data! $e")
        } finally {
            isSendingData = false
        }
    }

    override suspend fun saveData(serviceUUID: String, reading: Reading) {
        val now = timeManager.provideCurrentLocalDateTime()
        val bleDataEntity = BleDataEntity(
            temperature = reading.temperature,
            humidity = reading.humidity,
            dateTime = now,
            serviceUUID = serviceUUID,
        )
        database.getDao().saveData(data = bleDataEntity)
    }

    override fun start(serviceUUID: String) {
        if (!isStarted) {
            if (scope.isActive) {
                scope.launch {
                    countData(serviceUUID)
                }
            }
        }
    }

    private suspend fun generateData(serviceUUID: String) {
        for (i in 0..100) {
            database.getDao().saveData(
                BleDataEntity(
                    temperature = Random.nextFloat(),
                    humidity = Random.nextInt(),
                    dateTime = timeManager.provideCurrentLocalDateTime().minusDays(10),
                    serviceUUID = serviceUUID,
                )
            )
        }
    }

    override fun stop() {
        isStarted = false
    }
}

package com.untitledkingdom.ueberapp.service

import com.untitledkingdom.ueberapp.api.ApiService
import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.database.data.BleDataEntity
import com.untitledkingdom.ueberapp.devices.data.Reading
import com.untitledkingdom.ueberapp.utils.AppModules
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.functions.childScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

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

    private suspend fun countData(serviceUUID: String) {
        database.getDao().countNotSynchronized(serviceUUID).collect { count ->
            if (count != null) {
                if (count == 20 || count > 30) {
                    val data = database
                        .getDao()
                        .getDataNotSynchronized(serviceUUID)
                    sendData(data)
                }
            }
        }
    }

    private fun sendData(data: List<BleDataEntity>) = scope.childScope().launch {
        Timber.d("Size of data ${data.size}\nFirst id is ${data.first().id}\nLast id is ${data.last().id}")
        Timber.d("Sending data...")
        try {
            val response = apiService.sendDataToService(bleDatumEntities = data)
            if (response.isSuccessful) {
                Timber.d("Data sent!")
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
                throw Exception()
            }
        } catch (e: Exception) {
            Timber.d("Unable to send data! $e")
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
            Timber.d("IsStarted already true")
            if (scope.isActive) {
                Timber.d("Scope active")
                scope.launch {
                    countData(serviceUUID)
                }
            }
        }
    }

    override fun stop() {
        isStarted = false
    }
}

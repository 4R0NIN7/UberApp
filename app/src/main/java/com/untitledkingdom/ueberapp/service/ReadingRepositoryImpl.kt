package com.untitledkingdom.ueberapp.service

import com.untitledkingdom.ueberapp.api.ApiService
import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.database.data.BleDataEntity
import com.untitledkingdom.ueberapp.devices.data.Reading
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class ReadingRepositoryImpl @Inject constructor(
    private val database: Database,
    private val apiService: ApiService,
    private val timeManager: TimeManager
) : ReadingRepository {
    private val incrementer = AtomicInteger(1)
    private val numberOfTries = (incrementer.get().toFloat() * 19f).toInt()
    private var lastIdSent: Int = 0
    private var isFirstTime = true

    private fun setLastId(newId: Int) {
        lastIdSent = newId
    }

    override fun start() {
        isFirstTime = true
    }

    private suspend fun sendData(data: List<BleDataEntity>) {
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
            Timber.d("Unable to send data!")
            incrementer.incrementAndGet()
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
        sendDataToServer(serviceUUID)
    }

    private suspend fun sendDataToServer(serviceUUID: String) {
        if (isFirstTime) {
            isFirstTime = false
            val dataThatWasNotSynchronized = database
                .getDao()
                .getDataNotSynchronized(serviceUUID)
            sendData(dataThatWasNotSynchronized)
            setLastId(
                database
                    .getDao()
                    .getAllData()
                    .last { it.serviceUUID == serviceUUID }.id
            )
        } else {
            val data = database
                .getDao()
                .getAllData()
                .filter { it.serviceUUID == serviceUUID }
            if (lastIdSent + numberOfTries == data.last().id) {
                sendData(
                    data.filter {
                        it.id in lastIdSent..data.last().id
                    }
                )
            }
        }
    }
}

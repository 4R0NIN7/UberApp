package com.untitledkingdom.ueberapp.feature.main

import com.juul.kable.peripheral
import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConstants
import com.untitledkingdom.ueberapp.devices.ScanParameters
import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.functions.generateRandomString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile
import timber.log.Timber
import java.time.LocalDateTime
import java.time.Month
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val database: Database,
    private val timeManager: TimeManager,
    private val scope: CoroutineScope,
    private val dataStorage: DataStorage
) : MainRepository {

    private var isReading = true
    private val delay: Long = 5000
    private suspend fun saveToDataBase(
        value: String,
        characteristicUUID: String,
        serviceUUID: String
    ) {
        val now = timeManager.provideCurrentLocalDateTime()
        val bleData = BleData(
            data = value,
            localDateTime = now,
            serviceUUID = serviceUUID,
            characteristicUUID = characteristicUUID
        )
        database.getDao().saveData(data = bleData)
        Timber.d("Saved to dataBase")
    }

    private suspend fun getDataFromDataBase(
        serviceUUID: String,
        characteristicUUID: String
    ): List<BleData> = database
        .getDao()
        .getAllData()
        .filter { it.serviceUUID == serviceUUID && it.characteristicUUID == characteristicUUID }

    override suspend fun wipeData() {
        database.getDao().wipeData()
    }

    private suspend fun prepareMockData() {
        for (i in 1..30) {
            val bleData = BleData(
                data = generateRandomString(),
                localDateTime = LocalDateTime.of(
                    2022, Month.MARCH, i, i, i, i
                ),
                serviceUUID = "00001813-0000-1000-8000-00805f9b34fb",
                characteristicUUID = "00002a31-0000-1000-8000-00805f9b34fb"
            )
            database.getDao().saveData(data = bleData)
        }
    }

    override fun sendData() {
        Timber.d("sendData - There's 20 records!")
    }

    override fun startReadingDataFromDevice(): Flow<RepositoryStatus> = flow {
        Timber.d("Starting reading in Repository")
        val peripheral =
            scope.peripheral(dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS))
        val device = ScanParameters(device = peripheral)
        try {
            while (true) {
                device.write(generateRandomString())
                delay(delay)
                saveToDataBase(
                    value = device.read().first(),
                    characteristicUUID = device.characteristicUUID,
                    serviceUUID = device.serviceUUID
                )
                val dataFromDataBase = getDataFromDataBase(
                    serviceUUID = device.serviceUUID,
                    characteristicUUID = device.characteristicUUID
                )
                if (dataFromDataBase.size % 20 == 0) {
                    sendData()
                }
                emit(RepositoryStatus.Success(dataFromDataBase))
            }
        } catch (e: Exception) {
            throw e
        }
    }.onStart {
        isReading = true
    }.takeWhile {
        isReading
    }.onCompletion {
        isReading = true
    }.catch { cause ->
        Timber.d(cause)
    }

    override fun stopReadingDataFromDevice() {
        Timber.d("Stopping reading in Repository")
        isReading = false
    }

    override suspend fun readOnceFromDevice() {
        val peripheral =
            scope.peripheral(dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS))
        val device = ScanParameters(device = peripheral)
        try {
            device.write(generateRandomString())
            delay(delay)
            saveToDataBase(device.read().first(), device.characteristicUUID, device.serviceUUID)
            if (getDataFromDataBase(device.characteristicUUID, device.serviceUUID).size % 20 == 0) {
                sendData()
            }
        } catch (e: Exception) {
            throw e
        }
    }
}

package com.untitledkingdom.ueberapp.feature.data

import androidx.work.PeriodicWorkRequest
import com.juul.kable.DiscoveredService
import com.juul.kable.Peripheral
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import com.untitledkingdom.ueberapp.utils.functions.generateRandomString
import com.untitledkingdom.ueberapp.workManager.ReadingWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BleDevice @Inject constructor(
    val device: Peripheral,
    val services: List<DiscoveredService>,
) {
    val serviceUUID = "00001813-0000-1000-8000-00805f9b34fb"
    val characteristicUUID = "00002a31-0000-1000-8000-00805f9b34fb"
    val periodicWorkRequest = PeriodicWorkRequest
        .Builder(ReadingWorker::class.java, 15, TimeUnit.MINUTES)
        .build()
    private val service: DiscoveredService = services.first {
        it.serviceUuid == UUID.fromString(serviceUUID)
    }
    private var isReading = true

    fun readFromDeviceInLoop(): Flow<BleDeviceStatus> = flow {
        while (true) {
            write(generateRandomString())
            delay(1000)
            read()
                .catch { cause -> emit(BleDeviceStatus.Error(cause.message ?: "Error")) }
                .collect {
                    emit(BleDeviceStatus.Success(it))
                }
        }
    }.takeWhile {
        isReading
    }.onStart {
        device.connect()
    }.onCompletion {
        device.disconnect()
        isReading = true
    }

    fun endReading() {
        isReading = false
    }

    fun readOnce(): Flow<BleDeviceStatus> = flow {
        write(generateRandomString())
        delay(1000)
        read()
            .catch { cause -> emit(BleDeviceStatus.Error(cause.message ?: "Error")) }
            .collect {
                emit(BleDeviceStatus.Success(it))
            }
    }.onStart {
        device.connect()
    }.onCompletion {
        device.disconnect()
    }

    private fun read(): Flow<String> = flow {
        try {
            device.services?.first {
                it.serviceUuid == service.serviceUuid
            }?.characteristics?.forEach {
                if (it.characteristicUuid == UUID.fromString(characteristicUUID)) {
                    val data = device.read(
                        characteristicOf(
                            service = service.serviceUuid.toString(),
                            characteristic = characteristicUUID
                        )
                    )
                    emit(String(data))
                }
            }
        } catch (e: Exception) {
            Timber.d("Exception in writeToDevice! + $e")
            throw e
        }
    }

    private suspend fun write(value: String) {
        try {
            device.services?.first {
                it.serviceUuid == service.serviceUuid
            }?.characteristics?.forEach {
                if (it.characteristicUuid == UUID.fromString(characteristicUUID)) {
                    device.write(
                        characteristicOf(
                            service = service.serviceUuid.toString(),
                            characteristic = characteristicUUID
                        ),
                        value.toByteArray(),
                        writeType = WriteType.WithResponse
                    )
                }
            }
        } catch (e: Exception) {
            Timber.d("Exception in writeToDevice! + $e")
        }
    }

    fun printService() {
        println("Services are")
        services.forEach {
            println(it.serviceUuid)
        }
    }
}

sealed class BleDeviceStatus {
    data class Success(val data: String) : BleDeviceStatus()
    data class Error(val message: String) : BleDeviceStatus()
}

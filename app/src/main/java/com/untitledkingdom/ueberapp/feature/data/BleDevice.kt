package com.untitledkingdom.ueberapp.feature.data

import com.juul.kable.Advertisement
import com.juul.kable.DiscoveredService
import com.juul.kable.Peripheral
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import com.untitledkingdom.ueberapp.utils.generateRandomString
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import java.util.*

class BleDevice constructor(
    val device: Peripheral,
    val advertisement: Advertisement,
    val services: List<DiscoveredService>
) {
    private val service: DiscoveredService = services.first {
        it.serviceUuid == UUID.fromString("00001813-0000-1000-8000-00805f9b34fb")
    }
    private val characteristic = "00002a31-0000-1000-8000-00805f9b34fb"

    fun readFromDevice(): Flow<BleDeviceStatus> = flow {
        try {
            device.services?.first {
                it.serviceUuid == service.serviceUuid
            }?.characteristics?.forEach {
                if (it.characteristicUuid == UUID.fromString(characteristic)) {
                    val data = device.read(
                        characteristicOf(
                            service = service.serviceUuid.toString(),
                            characteristic = characteristic
                        )
                    )
                    emit(BleDeviceStatus.Success(data = String(data)))
                }
            }
        } catch (e: Exception) {
            Timber.d("Exception in writeToDevice! + $e")
            emit(BleDeviceStatus.Error(message = e.message ?: "Unknown error!"))
        }
    }.onStart {
        device.connect()
    }.onCompletion {
        device.disconnect()
    }

    fun readFromDeviceInLoop() = flow {
        while (true) {
            try {
                device.services?.first {
                    it.serviceUuid == service.serviceUuid
                }?.characteristics?.forEach {
                    if (it.characteristicUuid == UUID.fromString(characteristic)) {
                        device.write(
                            characteristicOf(
                                service = service.serviceUuid.toString(),
                                characteristic = characteristic
                            ),
                            generateRandomString().toByteArray(),
                            writeType = WriteType.WithResponse
                        )
                        delay(2000)
                        val data = device.read(
                            characteristicOf(
                                service = service.serviceUuid.toString(),
                                characteristic = characteristic
                            )
                        )
                        emit(BleDeviceStatus.Success(data = String(data)))
                    }
                }
            } catch (e: Exception) {
                Timber.d("Exception in writeToDevice! + $e")
                emit(BleDeviceStatus.Error(message = e.message ?: "Unknown error!"))
            }
        }
    }.onStart {
        device.connect()
    }.onCompletion {
        device.disconnect()
    }

    suspend fun writeToDevice(value: String) {
        try {
            device.connect()
            device.services?.first {
                it.serviceUuid == service.serviceUuid
            }?.characteristics?.forEach {
                if (it.characteristicUuid == UUID.fromString(characteristic)) {
                    device.write(
                        characteristicOf(
                            service = service.serviceUuid.toString(),
                            characteristic = characteristic
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
}

sealed class BleDeviceStatus {
    data class Success(val data: String) : BleDeviceStatus()
    data class Error(val message: String) : BleDeviceStatus()
}

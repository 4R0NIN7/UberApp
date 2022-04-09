package com.untitledkingdom.ueberapp.devices

import com.juul.kable.Peripheral
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ScanParameters @Inject constructor(private val device: Peripheral) {
    val serviceUUID = "00001813-0000-1000-8000-00805f9b34fb"
    val characteristicUUID = "00002a31-0000-1000-8000-00805f9b34fb"
    fun read(): Flow<String> = flow {
        try {
            device.services?.first {
                it.serviceUuid == UUID.fromString(serviceUUID)
            }?.characteristics?.forEach {
                if (it.characteristicUuid == UUID.fromString(characteristicUUID)) {
                    val data = device.read(
                        characteristicOf(
                            service = serviceUUID,
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
    }.onStart { device.connect() }.onCompletion { device.disconnect() }

    suspend fun write(value: String) {
        try {
            device.connect()
            device.services?.first {
                it.serviceUuid == UUID.fromString(serviceUUID)
            }?.characteristics?.forEach {
                if (it.characteristicUuid == UUID.fromString(characteristicUUID)) {
                    device.write(
                        characteristicOf(
                            service = serviceUUID,
                            characteristic = characteristicUUID
                        ),
                        value.toByteArray(),
                        writeType = WriteType.WithResponse
                    )
                }
            }
        } catch (e: Exception) {
            Timber.d("Exception in writeToDevice! + $e")
            throw e
        } finally {
            device.disconnect()
        }
    }
}

sealed class BleDeviceStatus {
    data class Success(val data: String) : BleDeviceStatus()
    data class Error(val message: String) : BleDeviceStatus()
}

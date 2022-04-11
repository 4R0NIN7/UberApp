package com.untitledkingdom.ueberapp.devices

import android.content.res.Resources
import com.juul.kable.Peripheral
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ScanParameters @Inject constructor(private val device: Peripheral) {
    val serviceUUID = "00001813-0000-1000-8000-00805f9b34fb"
    suspend fun read(fromCharacteristic: String): String {
        try {
            device.connect()
            device.services?.first {
                it.serviceUuid == UUID.fromString(serviceUUID)
            }?.characteristics?.forEach { discoveredCharacteristic ->
                if (discoveredCharacteristic.characteristicUuid == UUID.fromString(
                        fromCharacteristic
                    )
                ) {
                    val data = device.read(
                        characteristicOf(
                            service = serviceUUID,
                            characteristic = fromCharacteristic
                        )
                    )
                    return String(data)
                }
            }
            throw Resources.NotFoundException()
        } catch (e: Resources.NotFoundException) {
            Timber.d("deviceRead There are no characteristics!")
            return ""
        } catch (e: Exception) {
            Timber.d("Exception in writeToDevice! + $e")
            throw e
        } finally {
            device.disconnect()
        }
    }

    suspend fun write(value: String, toCharacteristic: String) {
        try {
            device.connect()
            device.services?.first {
                it.serviceUuid == UUID.fromString(serviceUUID)
            }?.characteristics?.forEach {
                if (it.characteristicUuid == UUID.fromString(toCharacteristic)) {
                    device.write(
                        characteristicOf(
                            service = serviceUUID,
                            characteristic = toCharacteristic
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

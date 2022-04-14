package com.untitledkingdom.ueberapp.devices

import ReadingsOuterClass
import com.juul.kable.ConnectionLostException
import com.juul.kable.Peripheral
import com.juul.kable.State
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import com.juul.kable.peripheral
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConstants
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.utils.functions.backoff
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class Device @Inject constructor(
    private val dataStorage: DataStorage
) {
    private var scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var device: Peripheral? = null
    private val atomicInteger: AtomicInteger = AtomicInteger()
    private suspend fun getDevice(): Peripheral {
        return device
            ?: scope.peripheral(
                dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS)
            ).also {
                device = it
                scope.enableAutoReconnect()
            }
    }

    private suspend fun CoroutineScope.enableAutoReconnect() {
        getDevice().state
            .filter { it is State.Disconnected }
            .onEach {
                reconnect()
            }
            .catch { cause ->
                Timber.d("Exception in autoReconnect $cause")
            }
            .launchIn(this)
    }

    private suspend fun reconnect() {
        try {
            Timber.d("Attempt number ${atomicInteger.get()}")
            val reconnectTime =
                backoff(
                    base = 100,
                    multiplier = 2f,
                    retry = atomicInteger.getAndIncrement()
                )
            delay(reconnectTime)
            getDevice().connect()
        } catch (e: Exception) {
            Timber.d("Exception in connect after delay $e")
            reconnect()
        }
    }

    fun CoroutineScope.connect() {
        atomicInteger.incrementAndGet()
        launch {
            try {
                getDevice().connect()
                atomicInteger.set(0)
            } catch (e: ConnectionLostException) {
                Timber.d("Connection lost")
            }
        }
    }

    private fun CoroutineScope.disconnect() {
        launch {
            try {
                getDevice().disconnect()
            } catch (e: ConnectionLostException) {
                Timber.d("Connection lost")
            }
        }
    }

    suspend fun read(fromService: String, fromCharacteristic: String): DeviceDataStatus {
        try {
            getDevice().services?.first {
                it.serviceUuid == UUID.fromString(fromService)
            }?.characteristics?.forEach { discoveredCharacteristic ->
                if (discoveredCharacteristic.characteristicUuid == UUID.fromString(
                        fromCharacteristic
                    )
                ) {
                    var readings: ReadingsOuterClass.Readings?
                    withContext(Dispatchers.IO) {
                        val dataBytes = getDevice().read(
                            characteristicOf(
                                service = fromService,
                                characteristic = fromCharacteristic
                            )
                        )
                        readings = ReadingsOuterClass.Readings.parseFrom(dataBytes)
                    }
                    if (readings != null) {
                        return DeviceDataStatus.SuccessDeviceDataReading(
                            DeviceReading(readings!!.temperature, readings!!.hummidity)
                        )
                    }
                }
            }
            return DeviceDataStatus.Error
        } catch (e: Exception) {
            Timber.d("Exception in read! + $e")
            throw e
        }
    }

    suspend fun readDate(fromService: String, fromCharacteristic: String): DeviceDataStatus {
        try {
            var date: ByteArray = byteArrayOf()
            getDevice().services?.first {
                it.serviceUuid == UUID.fromString(fromService)
            }?.characteristics?.forEach { discoveredCharacteristic ->
                if (discoveredCharacteristic.characteristicUuid == UUID.fromString(
                        fromCharacteristic
                    )
                ) {
                    date = getDevice().read(
                        characteristicOf(
                            service = fromService,
                            characteristic = fromCharacteristic
                        )
                    )
                }
            }
            return DeviceDataStatus.SuccessDate(date = date.toList())
        } catch (e: Exception) {
            Timber.d("Exception in read! + $e")
            throw e
        }
    }

    suspend fun write(value: ByteArray, toService: String, toCharacteristic: String) {
        try {
            getDevice().services?.first {
                it.serviceUuid == UUID.fromString(toService)
            }?.characteristics?.forEach { it ->
                if (it.characteristicUuid == UUID.fromString(toCharacteristic)) {
                    getDevice().write(
                        characteristicOf(
                            service = toService,
                            characteristic = toCharacteristic
                        ),
                        value,
                        writeType = WriteType.WithResponse
                    )
                }
            }
        } catch (e: Exception) {
            Timber.d("Exception in writeToDevice! + $e")
        }
    }

    fun observationOnDataCharacteristic(): Flow<DeviceReading> = flow {
        try {
            getDevice().observe(
                characteristicOf(
                    service = DeviceConst.SERVICE_DATA_SERVICE,
                    characteristic = DeviceConst.READINGS_CHARACTERISTIC
                )
            ).collect { data ->
                Timber.d("emitGetDevices")
                val reading = ReadingsOuterClass.Readings.parseFrom(data)
                emit(DeviceReading(reading.temperature, reading.hummidity))
            }
        } catch (e: Exception) {
            Timber.d("Device disconnected!")
        }
    }

    fun disconnectFromDevice() {
        scope.cancel("Device disconnected!")
    }
}

sealed class DeviceDataStatus {
    data class SuccessDeviceDataReading(val reading: DeviceReading) : DeviceDataStatus()
    data class SuccessDate(val date: List<Byte>) : DeviceDataStatus()
    object Error : DeviceDataStatus()
}

sealed class DeviceStatus {
    object Connecting : DeviceStatus()
    object Connected : DeviceStatus()
    object Disconnecting : DeviceStatus()
    object Disconnected : DeviceStatus()
}

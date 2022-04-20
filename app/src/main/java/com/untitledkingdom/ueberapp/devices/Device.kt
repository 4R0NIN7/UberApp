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
import com.untitledkingdom.ueberapp.devices.data.DeviceConst
import com.untitledkingdom.ueberapp.devices.data.DeviceDataStatus
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.utils.Modules
import com.untitledkingdom.ueberapp.utils.functions.delayValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@ExperimentalUnsignedTypes
@FlowPreview
@ExperimentalCoroutinesApi
class Device @Inject constructor(
    private val dataStorage: DataStorage,
    @Modules.IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    private var device: Peripheral? = null
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
    private suspend fun getPeripheral(): Peripheral {
        val macAddress = dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS)
        if (macAddress == "") {
            cancel()
        }
        return device
            ?: scope.peripheral(macAddress)
                .also {
                    device = it
                    scope.enableAutoReconnect()
                }
    }

    private val attempts: AtomicInteger = AtomicInteger()
    private suspend fun CoroutineScope.enableAutoReconnect() {
        getPeripheral().state
            .filter { it is State.Disconnected }
            .onEach {
                Timber.d("Reconnect in autoReconnect")
                reconnect()
            }
            .catch { cause ->
                Timber.d("Exception in autoReconnect $cause")
            }
            .launchIn(this)
    }

    private suspend fun reconnect() {
        try {
            Timber.d("Attempt number ${attempts.get()}")
            val reconnectTime =
                delayValue(
                    base = 100,
                    multiplier = 2f,
                    retry = attempts.getAndIncrement()
                )
            delay(reconnectTime)
            getPeripheral().connect()
            attempts.set(0)
        } catch (e: ConnectionLostException) {
            Timber.d("Exception in connect after delay $e")
            reconnect()
        } catch (e: Exception) {
        }
    }

    fun cancel() {
        scope.cancel()
    }

    suspend fun read(fromService: String, fromCharacteristic: String): DeviceDataStatus {
        try {
            getPeripheral().services?.first {
                it.serviceUuid == UUID.fromString(fromService)
            }?.characteristics?.forEach { discoveredCharacteristic ->
                if (discoveredCharacteristic.characteristicUuid == UUID.fromString(
                        fromCharacteristic
                    )
                ) {
                    var readings: ReadingsOuterClass.Readings?
                    withContext(dispatcher) {
                        val dataBytes = getPeripheral().read(
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
            getPeripheral().connect()
            var date: ByteArray = byteArrayOf()
            getPeripheral().services?.first {
                it.serviceUuid == UUID.fromString(fromService)
            }?.characteristics?.forEach { discoveredCharacteristic ->
                if (discoveredCharacteristic.characteristicUuid == UUID.fromString(
                        fromCharacteristic
                    )
                ) {
                    date = getPeripheral().read(
                        characteristicOf(
                            service = fromService,
                            characteristic = fromCharacteristic
                        )
                    )
                }
            }
            return DeviceDataStatus.SuccessRetrievingDate(date = date.toList())
        } catch (e: ConnectionLostException) {
            Timber.d("Exception in read! + $e")
            throw e
        } finally {
            getPeripheral().disconnect()
        }
    }

    suspend fun write(value: ByteArray, toService: String, toCharacteristic: String) {
        try {
            getPeripheral().connect()
            getPeripheral().services?.first {
                it.serviceUuid == UUID.fromString(toService)
            }?.characteristics?.forEach { it ->
                if (it.characteristicUuid == UUID.fromString(toCharacteristic)) {
                    getPeripheral().write(
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
            throw e
        } finally {
            getPeripheral().disconnect()
        }
    }

    suspend fun observationOnDataCharacteristic(): Flow<DeviceReading> =
        getPeripheral().observe(
            characteristicOf(
                service = DeviceConst.SERVICE_DATA_SERVICE,
                characteristic = DeviceConst.READINGS_CHARACTERISTIC
            )
        ).map { data ->
            withContext(dispatcher) {
                @Suppress("BlockingMethodInNonBlockingContext")
                val reading = ReadingsOuterClass.Readings.parseFrom(data)
                DeviceReading(reading.temperature, reading.hummidity)
            }
        }.catch { cause ->
            when (cause) {
                is ConnectionLostException -> {
                    Timber.d("Device disconnected!")
                    throw cause
                }
                else -> {
                    Timber.d("Device disconnected error! +$cause")
                    throw cause
                }
            }
        }
}

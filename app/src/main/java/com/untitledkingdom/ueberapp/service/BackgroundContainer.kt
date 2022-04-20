package com.untitledkingdom.ueberapp.service

import com.juul.kable.ConnectionLostException
import com.juul.kable.Peripheral
import com.juul.kable.State
import com.juul.kable.peripheral
import com.tomcz.ellipse.EffectsCollector
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.processor
import com.tomcz.ellipse.common.toNoAction
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConstants
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.DeviceConst
import com.untitledkingdom.ueberapp.devices.DeviceDataStatus
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import com.untitledkingdom.ueberapp.service.state.BackgroundEffect
import com.untitledkingdom.ueberapp.service.state.BackgroundEvent
import com.untitledkingdom.ueberapp.service.state.BackgroundState
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.functions.checkIfDateIsTheSame
import com.untitledkingdom.ueberapp.utils.functions.delayValue
import com.untitledkingdom.ueberapp.utils.functions.toDateString
import com.untitledkingdom.ueberapp.utils.functions.toUByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

typealias BackgroundProcessor = Processor<BackgroundEvent, BackgroundState, BackgroundEffect>

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
class BackgroundContainer @Inject constructor(
    private val repository: MainRepository,
    private val dataStorage: DataStorage,
    private val timeManager: TimeManager
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var device: Device? = null
    private var peripheral: Peripheral? = null
    private suspend fun getPeripheral(): Peripheral {
        return peripheral
            ?: scope.peripheral(dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS))
                .also {
                    peripheral = it
                    scope.enableAutoReconnect()
                }
    }

    private suspend fun getDevice(): Device {
        return device ?: Device(getPeripheral())
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
            processor.sendEvent(BackgroundEvent.StopReading)
        }
    }

    val processor: BackgroundProcessor = scope.processor(
        initialState = BackgroundState(),
        onEvent = { event ->
            when (event) {
                BackgroundEvent.StartReading -> handleService(effects).toNoAction()
                BackgroundEvent.StopReading -> stopReading(effects).toNoAction()
            }
        }
    )

    fun cancel() {
        scope.cancel()
    }

    private fun stopReading(effects: EffectsCollector<BackgroundEffect>) {
        effects.send(BackgroundEffect.Stop)
    }

    private suspend fun handleService(effects: EffectsCollector<BackgroundEffect>) {
        try {
            writeDateToDevice(
                service = DeviceConst.SERVICE_TIME_SETTINGS,
                characteristic = DeviceConst.TIME_CHARACTERISTIC,
            )
            startObservingData(effects = effects)
        } catch (e: ConnectionLostException) {
            Timber.d("Exception during creating device $e")
            stopReading(effects = effects)
        }
    }

    private suspend fun startObservingData(
        effects: EffectsCollector<BackgroundEffect>,
    ) {
        try {
            Timber.d("Starting collecting data from service")
            effects.send(BackgroundEffect.StartForegroundService)
            getDevice().observationOnDataCharacteristic().collect { reading ->
                effects.send(BackgroundEffect.SendBroadcastToActivity)
                Timber.d("Reading in service $reading")
                repository.saveData(
                    deviceReading = reading,
                    serviceUUID = DeviceConst.SERVICE_DATA_SERVICE,
                )
            }
        } catch (e: ConnectionLostException) {
            Timber.d("Service cannot connect to device!")
            reconnect()
        } catch (e: Exception) {
            Timber.d("Service error! $e")
        }
    }

    private suspend fun writeDateToDevice(
        service: String,
        characteristic: String
    ) {
        try {
            val status = getDevice().readDate(
                fromCharacteristic = characteristic,
                fromService = service
            )
            when (status) {
                is DeviceDataStatus.SuccessDate -> checkDate(
                    status.date,
                    service,
                    characteristic,
                )
                DeviceDataStatus.Error -> throw Exception()
                else -> {}
            }
        } catch (e: Exception) {
            Timber.d("Unable to write deviceReading $e")
            throw e
        }
    }

    private suspend fun checkDate(
        bytes: List<Byte>,
        service: String,
        characteristic: String,
    ) {
        val dateFromDevice = toDateString(bytes.toByteArray())
        val currentDate = timeManager.provideCurrentLocalDateTime()
        val checkIfTheSame = checkIfDateIsTheSame(
            date = currentDate,
            dateFromDevice = dateFromDevice
        )
        if (!checkIfTheSame) {
            Timber.d("writeDateToDevice Saving date")
            getDevice().write(currentDate.toUByteArray(), service, characteristic)
        }
    }
}

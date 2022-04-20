package com.untitledkingdom.ueberapp.service

import com.juul.kable.ConnectionLostException
import com.tomcz.ellipse.EffectsCollector
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.processor
import com.tomcz.ellipse.common.toNoAction
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.data.DeviceConst
import com.untitledkingdom.ueberapp.devices.data.DeviceDataStatus
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import com.untitledkingdom.ueberapp.service.state.BackgroundEffect
import com.untitledkingdom.ueberapp.service.state.BackgroundEvent
import com.untitledkingdom.ueberapp.service.state.BackgroundState
import com.untitledkingdom.ueberapp.utils.Modules
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.functions.UtilFunctions
import com.untitledkingdom.ueberapp.utils.functions.toUByteArray
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber
import javax.inject.Inject

typealias BackgroundProcessor = Processor<BackgroundEvent, BackgroundState, BackgroundEffect>

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
class BackgroundContainer @Inject constructor(
    private val repository: MainRepository,
    private val timeManager: TimeManager,
    private val device: Device,
    @Modules.IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
    val processor: BackgroundProcessor = scope.processor(
        initialState = BackgroundState(),
        onEvent = { event ->
            when (event) {
                BackgroundEvent.StartReading -> startReading(effects).toNoAction()
                BackgroundEvent.StopReading -> stopReading(effects).toNoAction()
            }
        }
    )

    fun cancel() {
        device.cancel()
        scope.cancel()
    }

    private fun stopReading(effects: EffectsCollector<BackgroundEffect>) {
        effects.send(BackgroundEffect.Stop)
    }

    private suspend fun startReading(effects: EffectsCollector<BackgroundEffect>) {
        try {
            writeDateToDevice(
                service = DeviceConst.SERVICE_TIME_SETTINGS,
                characteristic = DeviceConst.TIME_CHARACTERISTIC,
                effects = effects
            )
            startObservingData(effects = effects)
        } catch (e: ConnectionLostException) {
            Timber.d("ConnectionLostException during handle $e")
            stopReading(effects = effects)
        } catch (e: Exception) {
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
            device.observationOnDataCharacteristic().collect { reading ->
                repository.saveData(
                    deviceReading = reading,
                    serviceUUID = DeviceConst.SERVICE_DATA_SERVICE,
                )
                effects.send(BackgroundEffect.SendBroadcastToActivity)
            }
        } catch (e: ConnectionLostException) {
            Timber.d("Service cannot connect to device!")
            startReading(effects)
        } catch (e: Exception) {
            Timber.d("startObservingData exception! $e")
        }
    }

    private suspend fun writeDateToDevice(
        service: String,
        characteristic: String,
        effects: EffectsCollector<BackgroundEffect>
    ) {
        try {
            val status = device.readDate(
                fromCharacteristic = characteristic,
                fromService = service
            )
            when (status) {
                is DeviceDataStatus.SuccessRetrievingDate -> validateDate(
                    status.date,
                    service,
                    characteristic,
                )
                DeviceDataStatus.Error -> throw Exception()
                else -> {}
            }
        } catch (e: ConnectionLostException) {
            Timber.d("Unable to write deviceReading $e")
            startReading(effects)
        } catch (e: Exception) {
            stopReading(effects)
        }
    }

    private suspend fun validateDate(
        bytes: List<Byte>,
        service: String,
        characteristic: String,
    ) {
        val dateFromDevice = UtilFunctions.toDateString(bytes.toByteArray())
        val currentDate = timeManager.provideCurrentLocalDateTime()
        val checkIfDateAreTheSame = UtilFunctions.checkIfDateIsTheSame(
            date = currentDate,
            dateFromDevice = dateFromDevice
        )
        if (!checkIfDateAreTheSame) {
            device.write(currentDate.toUByteArray(), service, characteristic)
        }
    }
}

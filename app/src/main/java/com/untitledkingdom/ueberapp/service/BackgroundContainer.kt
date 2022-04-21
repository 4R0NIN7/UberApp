package com.untitledkingdom.ueberapp.service

import com.juul.kable.ConnectionLostException
import com.tomcz.ellipse.EffectsCollector
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.processor
import com.tomcz.ellipse.common.toNoAction
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.data.DeviceConst
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import com.untitledkingdom.ueberapp.service.state.BackgroundEffect
import com.untitledkingdom.ueberapp.service.state.BackgroundEvent
import com.untitledkingdom.ueberapp.service.state.BackgroundState
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
    scope: CoroutineScope
) {
    val processor: BackgroundProcessor = scope.processor(
        initialState = BackgroundState(),
        onEvent = { event ->
            when (event) {
                BackgroundEvent.StartReading -> startReading(effects).toNoAction()
                BackgroundEvent.StopReading -> stopReading(effects).toNoAction()
            }
        }
    )

    private fun stopReading(effects: EffectsCollector<BackgroundEffect>) {
        effects.send(BackgroundEffect.Stop)
    }

    private suspend fun startReading(effects: EffectsCollector<BackgroundEffect>) {
        try {
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
}

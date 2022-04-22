package com.untitledkingdom.ueberapp.service

import com.juul.kable.ConnectionLostException
import com.tomcz.ellipse.EffectsCollector
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.processor
import com.tomcz.ellipse.common.toNoAction
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.data.DeviceConst
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import com.untitledkingdom.ueberapp.service.state.ReadingEffect
import com.untitledkingdom.ueberapp.service.state.ReadingEvent
import com.untitledkingdom.ueberapp.service.state.ReadingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber
import javax.inject.Inject

typealias BackgroundProcessor = Processor<ReadingEvent, ReadingState, ReadingEffect>

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
class ReadingContainer @Inject constructor(
    private val repository: MainRepository,
    private val device: Device,
    scope: CoroutineScope
) {
    val processor: BackgroundProcessor = scope.processor(
        initialState = ReadingState(),
        onEvent = { event ->
            when (event) {
                ReadingEvent.StartReading -> startReading(effects).toNoAction()
                ReadingEvent.StopReading -> stopReading(effects).toNoAction()
            }
        }
    )

    private fun stopReading(effects: EffectsCollector<ReadingEffect>) {
        effects.send(ReadingEffect.Stop)
    }

    private suspend fun startReading(effects: EffectsCollector<ReadingEffect>) {
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
        effects: EffectsCollector<ReadingEffect>,
    ) {
        try {
            Timber.d("Starting collecting data from service")
            effects.send(ReadingEffect.StartForegroundService)
            device.observationOnDataCharacteristic().collect { reading ->
                repository.saveData(
                    deviceReading = reading,
                    serviceUUID = DeviceConst.SERVICE_DATA_SERVICE,
                )
                effects.send(ReadingEffect.SendBroadcastToActivity)
            }
        } catch (e: ConnectionLostException) {
            Timber.d("Service cannot connect to device!")
            startReading(effects)
        } catch (e: Exception) {
            Timber.d("startObservingData exception! $e")
        }
    }
}

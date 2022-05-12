package com.untitledkingdom.ueberapp.service

import com.juul.kable.ConnectionLostException
import com.tomcz.ellipse.EffectsCollector
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.processor
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.data.DeviceConst
import com.untitledkingdom.ueberapp.service.state.ReadingEffect
import com.untitledkingdom.ueberapp.service.state.ReadingEvent
import com.untitledkingdom.ueberapp.utils.AppModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import javax.inject.Inject

typealias BackgroundProcessor = Processor<ReadingEvent, Unit, ReadingEffect>

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
class ReadingContainer @Inject constructor(
    private val repository: ReadingRepository,
    private val device: Device,
    @AppModules.ReadingScope private val scope: CoroutineScope,
) {
    val processor: BackgroundProcessor = scope.processor(
        onEvent = { event ->
            when (event) {
                ReadingEvent.StartReading -> startReading(effects)
                ReadingEvent.StopReading -> stopReading(effects)
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
            effects.send(ReadingEffect.SendBroadcastToActivity)
            Timber.d("Starting collecting data from service")
            repository.start()
            device.observationOnDataCharacteristic().collect { reading ->
                repository.saveData(
                    reading = reading,
                    serviceUUID = DeviceConst.SERVICE_DATA_SERVICE,
                )
                effects.send(ReadingEffect.StartNotifying(reading))
            }
        } catch (e: ConnectionLostException) {
            Timber.d("Service cannot connect to device!")
        } catch (e: Exception) {
            Timber.d("startObservingData exception! $e")
        }
    }
}

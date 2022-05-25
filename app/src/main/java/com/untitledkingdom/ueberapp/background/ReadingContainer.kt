package com.untitledkingdom.ueberapp.background

import com.juul.kable.ConnectionLostException
import com.tomcz.ellipse.EffectsCollector
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.processor
import com.untitledkingdom.ueberapp.background.state.ReadingEffect
import com.untitledkingdom.ueberapp.background.state.ReadingEvent
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.data.DeviceConst
import com.untitledkingdom.ueberapp.utils.AppModules
import com.untitledkingdom.ueberapp.utils.functions.childScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias BackgroundProcessor = Processor<ReadingEvent, Unit, ReadingEffect>

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
class ReadingContainer @Inject constructor(
    private val repository: ReadingRepository,
    private val dataStorage: DataStorage,
    private val device: Device,
    @AppModules.ReadingScope private val scope: CoroutineScope,
) {
    val processor: BackgroundProcessor = scope.processor(
        onEvent = { event ->
            when (event) {
                ReadingEvent.StartReading -> startReading(effects)
                ReadingEvent.StartBattery -> startObservingBattery(effects)
                ReadingEvent.StopReading -> stopReading(effects)
            }
        }
    )

    private fun stopReading(effects: EffectsCollector<ReadingEffect>) {
        repository.stop()
        effects.send(ReadingEffect.Stop)
    }

    private fun startObservingBattery(effects: EffectsCollector<ReadingEffect>) =
        scope.childScope().launch {
            Timber.d("startObservingBattery")
            startObservingBatteryLevel(effects)
        }

    private suspend fun startObservingBatteryLevel(effects: EffectsCollector<ReadingEffect>) {
        try {
            device.observationOnBatteryLevelCharacteristic().collect { batterLevelDouble ->
                effects.send(ReadingEffect.NotifyBatterLow(batterLevelDouble.toInt()))
            }
        } catch (e: ConnectionLostException) {
            Timber.d("Service cannot connect to device!")
        } catch (e: Exception) {
            Timber.d("startObservingData exception! $e")
            stopReading(effects)
        }
    }

    private suspend fun startReading(effects: EffectsCollector<ReadingEffect>) {
        try {
            if (dataStorage.observeMacAddress().first() == "") {
                stopReading(effects)
            } else {
                startObservingData(effects = effects, DeviceConst.SERVICE_DATA_SERVICE)
            }
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
        serviceUUID: String
    ) {
        try {
            Timber.d("Starting collecting data from service")
            repository.start(serviceUUID)
            device.observationOnDataCharacteristic().collect { reading ->
                repository.saveData(
                    reading = reading,
                    serviceUUID = serviceUUID
                )
                effects.send(ReadingEffect.StartNotifying(reading))
            }
        } catch (e: ConnectionLostException) {
            Timber.d("Service cannot connect to device!")
        } catch (e: Exception) {
            Timber.d("startObservingData exception! $e")
            stopReading(effects)
        }
    }
}

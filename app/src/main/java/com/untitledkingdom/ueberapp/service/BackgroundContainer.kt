package com.untitledkingdom.ueberapp.service

import com.juul.kable.ConnectionLostException
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
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.functions.checkIfDateIsTheSame
import com.untitledkingdom.ueberapp.utils.functions.toDateString
import com.untitledkingdom.ueberapp.utils.functions.toUByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias BackgroundProcessor = Processor<BackgroundEvent, BackgroundState, BackgroundEffect>

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
class BackgroundContainer @Inject constructor(
    private val scope: CoroutineScope,
    private val repository: MainRepository,
    private val dataStorage: DataStorage,
    private val timeManager: TimeManager
) {

    val processor: BackgroundProcessor = scope.processor(
        initialState = BackgroundState(),
        onEvent = { event ->
            when (event) {
                BackgroundEvent.StartReading -> handleService(effects).toNoAction()
            }
        }
    )

    private fun handleService(effects: EffectsCollector<BackgroundEffect>) {
        scope.launch {
            if (dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS) == "") {
                effects.send(BackgroundEffect.Stop)
            }
            try {
                val device = Device(dataStorage)
                writeDateToDevice(
                    service = DeviceConst.SERVICE_TIME_SETTINGS,
                    characteristic = DeviceConst.TIME_CHARACTERISTIC,
                    device = device
                )
                startObservingData(device = device, effects = effects)
            } catch (e: ConnectionLostException) {
                Timber.d("Exception during creating device $e")
                effects.send(BackgroundEffect.Stop)
            }
        }
    }

    private suspend fun startObservingData(
        device: Device,
        effects: EffectsCollector<BackgroundEffect>
    ) {
        try {
            Timber.d("Starting collecting data from service")
            effects.send(BackgroundEffect.StartForegroundService)
            device.observationOnDataCharacteristic().collect { reading ->
                effects.send(BackgroundEffect.SendBroadcastToActivity)
                Timber.d("Reading in service $reading")
                repository.saveData(
                    deviceReading = reading,
                    serviceUUID = DeviceConst.SERVICE_DATA_SERVICE,
                )
            }
        } catch (e: ConnectionLostException) {
            Timber.d("Service cannot connect to device!")
        } catch (e: Exception) {
            Timber.d("Service error! $e")
        }
    }

    private suspend fun writeDateToDevice(
        service: String,
        characteristic: String,
        device: Device
    ) {
        try {
            val status = device.readDate(
                fromCharacteristic = characteristic,
                fromService = service
            )
            when (status) {
                is DeviceDataStatus.SuccessDate -> checkDate(
                    status.date,
                    service,
                    characteristic,
                    device
                )
                DeviceDataStatus.Error -> throw Exception()
                else -> {}
            }
        } catch (e: Exception) {
            Timber.d("Unable to write deviceReading $e")
        }
    }

    private suspend fun checkDate(
        bytes: List<Byte>,
        service: String,
        characteristic: String,
        device: Device
    ) {
        val dateFromDevice = toDateString(bytes.toByteArray())
        val currentDate = timeManager.provideCurrentLocalDateTime()
        val checkIfTheSame = checkIfDateIsTheSame(
            date = currentDate,
            dateFromDevice = dateFromDevice
        )
        if (!checkIfTheSame) {
            Timber.d("writeDateToDevice Saving date")
            device.write(currentDate.toUByteArray(), service, characteristic)
        }
    }
}

sealed interface BackgroundEvent {
    object StartReading : BackgroundEvent
}

sealed interface BackgroundEffect {
    object SendBroadcastToActivity : BackgroundEffect
    object StartForegroundService : BackgroundEffect
    object Stop : BackgroundEffect
}

data class BackgroundState(val any: Any? = null)

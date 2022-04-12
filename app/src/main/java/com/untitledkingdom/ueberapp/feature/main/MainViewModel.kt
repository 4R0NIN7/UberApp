package com.untitledkingdom.ueberapp.feature.main

import ReadingsOuterClass
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.Advertisement
import com.juul.kable.characteristicOf
import com.juul.kable.peripheral
import com.tomcz.ellipse.EffectsCollector
import com.tomcz.ellipse.PartialState
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.NoAction
import com.tomcz.ellipse.common.processor
import com.tomcz.ellipse.common.toNoAction
import com.untitledkingdom.ueberapp.ble.KableService
import com.untitledkingdom.ueberapp.ble.data.ScanStatus
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConstants
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.DeviceConst
import com.untitledkingdom.ueberapp.devices.DeviceStatus
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.feature.main.state.MainEffect
import com.untitledkingdom.ueberapp.feature.main.state.MainEvent
import com.untitledkingdom.ueberapp.feature.main.state.MainPartialState
import com.untitledkingdom.ueberapp.feature.main.state.MainState
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.functions.toDateString
import com.untitledkingdom.ueberapp.utils.functions.toUByteArray
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

typealias MainProcessor = Processor<MainEvent, MainState, MainEffect>

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalUnsignedTypes
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val dataStorage: DataStorage,
    private val kableService: KableService,
    private val timeManager: TimeManager
) : ViewModel() {
    private val device: Device = Device(dataStorage = dataStorage)
    val processor: MainProcessor = processor(
        initialState = MainState(),
        prepare = {
            flowOf(
                MainPartialState.SetMacAddress(
                    dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS)
                )
            )
        },
        onEvent = { event ->
            when (event) {
                MainEvent.SetCurrentDateToDevice -> writeDateToDevice(
                    service = DeviceConst.SERVICE_TIME_SETTINGS,
                    characteristic = DeviceConst.TIME_CHARACTERISTIC
                ).toNoAction()
                MainEvent.ReadCharacteristic -> device.observationOnDataCharacteristic().toNoAction()
                MainEvent.RefreshDeviceData -> refreshDeviceData(
                    macAddress = dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS),
                    effects = effects
                )
                MainEvent.StopScanning -> kableService.stopScan().toNoAction()
                is MainEvent.TabChanged -> flowOf(MainPartialState.TabChanged(event.newTabIndex))
                is MainEvent.EndConnectingToDevice -> flow {
                    kableService.stopScan()
                    device.disconnectFromDevice()
                    effects.send(MainEffect.GoToWelcome)
                }
                MainEvent.WipeData -> repository.wipeData().toNoAction()
                is MainEvent.SetSelectedDate -> flowOf(MainPartialState.SetSelectedDate(event.date))
                MainEvent.GoToDetails -> effects.send(MainEffect.OpenDetailsForDay).toNoAction()
                MainEvent.CloseDetails -> effects.send(MainEffect.GoBack).toNoAction()
            }
        }
    )

    private fun refreshDeviceData(
        macAddress: String,
        effects: EffectsCollector<MainEffect>
    ): Flow<PartialState<MainState>> =
        kableService.refreshDeviceData(macAddress = macAddress)
            .map { status ->
                when (status) {
                    is ScanStatus.Failed -> effects.send(MainEffect.ShowError(status.message as String))
                        .let { NoAction() }
                    is ScanStatus.Found -> setAdvertisementPartial(status.advertisement)
                    ScanStatus.Scanning -> setIsScanningPartial(true)
                    ScanStatus.Stopped -> setIsScanningPartial(false)
                }
            }

    private fun setAdvertisementPartial(advertisement: Advertisement): MainPartialState {
        return MainPartialState.SetAdvertisement(advertisement)
    }

    private fun setIsScanningPartial(isScanning: Boolean): MainPartialState {
        return MainPartialState.SetIsScanning(isScanning)
    }

    private fun startReadingDataFromDevice(): Flow<MainPartialState> = flow {
        val peripheral =
            viewModelScope.peripheral(dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS))
        peripheral.connect()
        peripheral
            .observe(
                characteristic = characteristicOf(
                    service = DeviceConst.SERVICE_DATA_SERVICE,
                    characteristic = DeviceConst.READINGS_CHARACTERISTIC
                )
            )
            .collect { data ->
                withContext(Dispatchers.IO) {
                    val reading = ReadingsOuterClass.Readings.parseFrom(data)
                    Timber.d("Reading is temperature = ${reading.temperature}, humidity = ${reading.hummidity}")
                    repository.saveData(
                        deviceReading = DeviceReading(
                            reading.temperature,
                            reading.hummidity
                        ),
                        serviceUUID = DeviceConst.SERVICE_DATA_SERVICE,
                    )
                }
                val dataBaseData = repository.getData(DeviceConst.SERVICE_DATA_SERVICE)
                Timber.d("DataBase last value ${dataBaseData.last()}")
                emitAll(flowOf(MainPartialState.SetValues(dataBaseData)))
            }
        peripheral.disconnect()
    }

    private suspend fun writeDateToDevice(
        service: String,
        characteristic: String,
    ) {
        try {
            val status = device.readDate(
                fromCharacteristic = characteristic,
                fromService = service
            )
            when (status) {
                is DeviceStatus.SuccessDate -> checkDate(status.date, service, characteristic)
                DeviceStatus.Error -> throw Exception()
                else -> {}
            }
        } catch (e: Exception) {
            Timber.d("Unable to write deviceReading $e")
        }
    }

    private suspend fun checkDate(bytes: List<Byte>, service: String, characteristic: String) {
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

    private fun checkIfDateIsTheSame(dateFromDevice: String, date: LocalDateTime): Boolean {
        val dateFromLocalDateTime = "${date.dayOfMonth}${date.monthValue}${date.year}"
        Timber.d("DateFromDevice $dateFromDevice, dateLocal $dateFromLocalDateTime")
        return dateFromDevice == dateFromLocalDateTime
    }
}

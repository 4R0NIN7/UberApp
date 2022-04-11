package com.untitledkingdom.ueberapp.feature.main

import androidx.lifecycle.ViewModel
import com.juul.kable.Advertisement
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
import com.untitledkingdom.ueberapp.devices.DeviceConst
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import com.untitledkingdom.ueberapp.feature.main.state.MainEffect
import com.untitledkingdom.ueberapp.feature.main.state.MainEvent
import com.untitledkingdom.ueberapp.feature.main.state.MainPartialState
import com.untitledkingdom.ueberapp.feature.main.state.MainState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

typealias MainProcessor = Processor<MainEvent, MainState, MainEffect>

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val dataStorage: DataStorage,
    private val kableService: KableService
) : ViewModel() {

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
                MainEvent.SetCurrentDateToDevice -> repository.writeDateToDevice(
                    service = DeviceConst.SERVICE_TIME_SETTINGS,
                    characteristic = DeviceConst.TIME_CHARACTERISTIC
                ).toNoAction()
                MainEvent.ReadCharacteristic -> readDataInLoop(effects)
                MainEvent.RefreshDeviceData -> refreshDeviceData(
                    macAddress = dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS),
                    effects = effects
                ).toNoAction()
                MainEvent.StartScanning -> readDataInLoop(effects)
                MainEvent.StopReadingCharacteristic -> repository.stopReadingDataFromDevice()
                    .toNoAction()
                MainEvent.StopScanning -> kableService.stopScan().toNoAction()
                is MainEvent.TabChanged -> flowOf(MainPartialState.TabChanged(event.newTabIndex))
                is MainEvent.EndConnectingToDevice -> flow {
                    kableService.stopScan()
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

    private fun readDataInLoop(
        effects: EffectsCollector<MainEffect>
    ): Flow<PartialState<MainState>> =
        repository.startReadingDataFromDevice(
            serviceUUID = DeviceConst.SERVICE_DATA_SERVICE,
            characteristic = DeviceConst.READINGS_CHARACTERISTIC
        ).map { status ->
            when (status) {
                is RepositoryStatus.SuccessBleData -> {
                    MainPartialState.SetValues(status.data)
                }
                RepositoryStatus.Error -> effects.send(MainEffect.ShowError("Unable to read data"))
                    .let { NoAction() }
                is RepositoryStatus.Loading -> MainPartialState.SetValues(status.data)
                is RepositoryStatus.SuccessString -> {
                    Timber.d("Data from device observation is ${status.data}")
                        .let { NoAction() }
                }
                else -> {
                    NoAction()
                }
            }
        }

    private fun setIsScanningPartial(isScanning: Boolean): MainPartialState {
        return MainPartialState.SetIsScanning(isScanning)
    }
}

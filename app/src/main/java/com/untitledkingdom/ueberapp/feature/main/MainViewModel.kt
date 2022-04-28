package com.untitledkingdom.ueberapp.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.Advertisement
import com.tomcz.ellipse.EffectsCollector
import com.tomcz.ellipse.PartialState
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.NoAction
import com.tomcz.ellipse.common.processor
import com.tomcz.ellipse.common.toNoAction
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConst
import com.untitledkingdom.ueberapp.devices.data.DeviceConst
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import com.untitledkingdom.ueberapp.feature.main.state.MainEffect
import com.untitledkingdom.ueberapp.feature.main.state.MainEvent
import com.untitledkingdom.ueberapp.feature.main.state.MainPartialState
import com.untitledkingdom.ueberapp.feature.main.state.MainState
import com.untitledkingdom.ueberapp.scanner.ScanService
import com.untitledkingdom.ueberapp.scanner.data.ScanStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

typealias MainProcessor = Processor<MainEvent, MainState, MainEffect>

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalUnsignedTypes
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val dataStorage: DataStorage,
    private val scanService: ScanService,
) : ViewModel() {
    val processor: MainProcessor = processor(
        initialState = MainState(),
        prepare = {
            merge(
                refreshDeviceInfo(effects),
                collectDataFromDataBase(effects),
                prepareFirstId(),
                prepareLastId()
            )
        },
        onEvent = { event ->
            when (event) {
                is MainEvent.TabChanged -> flowOf(MainPartialState.TabChanged(event.newTabIndex))
                is MainEvent.EndConnectingToDevice -> disconnect(effects).toNoAction()
                is MainEvent.SetSelectedDate -> flowOf(MainPartialState.SetSelectedDate(event.date))
                MainEvent.StartScanning -> refreshDeviceInfo(
                    effects = effects
                )
            }
        }
    )

    private suspend fun disconnect(effects: EffectsCollector<MainEffect>) {
        scanService.stopScan()
        dataStorage.saveToStorage(DataStorageConst.MAC_ADDRESS, "")
        effects.send(MainEffect.GoToWelcome)
    }

    private fun collectDataFromDataBase(effects: EffectsCollector<MainEffect>): Flow<PartialState<MainState>> =
        repository.getDataFromDataBase(serviceUUID = DeviceConst.SERVICE_DATA_SERVICE)
            .map { status ->
                when (status) {
                    RepositoryStatus.Error ->
                        effects
                            .send(MainEffect.ShowError("Error during collecting data from DB"))
                            .let { NoAction() }
                    is RepositoryStatus.SuccessBleData -> {
                        MainPartialState.SetValues(status.data)
                    }
                }
            }

    private fun prepareLastId(): Flow<PartialState<MainState>> =
        repository.lastIdSent.flatMapLatest {
            flowOf(MainPartialState.SetLastIdSend(lastIdSend = it))
        }

    private fun prepareFirstId(): Flow<PartialState<MainState>> =
        repository.firstIdSent.flatMapLatest {
            flowOf(MainPartialState.SetFirstIdSend(firstIdSend = it))
        }

    private suspend fun refreshDeviceInfo(
        effects: EffectsCollector<MainEffect>
    ): Flow<PartialState<MainState>> =
        scanService.refreshDeviceInfo(macAddress = dataStorage.getFromStorage(DataStorageConst.MAC_ADDRESS))
            .map { status ->
                when (status) {
                    is ScanStatus.Failed -> effects.send(MainEffect.ShowError(status.message as String))
                        .let { NoAction() }
                    is ScanStatus.Found -> setAdvertisementPartial(
                        status.advertisement
                    )
                    ScanStatus.Scanning -> setIsScanningPartial(true)
                    ScanStatus.Stopped -> setIsScanningPartial(false)
                }
            }

    private fun setAdvertisementPartial(
        advertisement: Advertisement
    ): MainPartialState =
        MainPartialState.SetAdvertisement(advertisement)

    private fun setIsScanningPartial(isScanning: Boolean): MainPartialState =
        MainPartialState.SetIsScanning(isScanning)

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}

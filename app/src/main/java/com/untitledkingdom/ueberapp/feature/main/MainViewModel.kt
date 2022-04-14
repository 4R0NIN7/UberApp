package com.untitledkingdom.ueberapp.feature.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.juul.kable.Advertisement
import com.tomcz.ellipse.EffectsCollector
import com.tomcz.ellipse.PartialState
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.NoAction
import com.tomcz.ellipse.common.processor
import com.untitledkingdom.ueberapp.ble.KableService
import com.untitledkingdom.ueberapp.ble.data.ScanStatus
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConstants
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.DeviceConst
import com.untitledkingdom.ueberapp.devices.DeviceDataStatus
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import com.untitledkingdom.ueberapp.feature.main.state.MainEffect
import com.untitledkingdom.ueberapp.feature.main.state.MainEvent
import com.untitledkingdom.ueberapp.feature.main.state.MainPartialState
import com.untitledkingdom.ueberapp.feature.main.state.MainState
import com.untitledkingdom.ueberapp.workManager.ReadingWorker
import com.untitledkingdom.ueberapp.workManager.WorkManagerConst
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import timber.log.Timber
import java.util.concurrent.TimeUnit
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
    private val context: Application
) : ViewModel() {
    private val device: Device = Device(dataStorage = dataStorage)
    val processor: MainProcessor = processor(
        initialState = MainState(),
        prepare = {
            merge(
                refreshDeviceData(effects),
                startCollectingData(effects),
            )
        },
        onEvent = { event ->
            when (event) {
                is MainEvent.TabChanged -> flowOf(MainPartialState.TabChanged(event.newTabIndex))
                is MainEvent.EndConnectingToDevice -> flow {
                    kableService.stopScan()
                    repository.clear()
                    device.disconnectFromDevice()
                    effects.send(MainEffect.GoToWelcome)
                }
                is MainEvent.SetSelectedDate -> flowOf(MainPartialState.SetSelectedDate(event.date))
                MainEvent.StartScanning -> refreshDeviceData(
                    effects = effects
                )
            }
        }
    )

    private fun readOnce(effects: EffectsCollector<MainEffect>): Flow<MainPartialState> = flow {
        val data = device.read(
            DeviceConst.SERVICE_DATA_SERVICE,
            fromCharacteristic = DeviceConst.READINGS_CHARACTERISTIC
        )
        when (data) {
            DeviceDataStatus.Error -> effects.send(MainEffect.ShowError("Error"))
            is DeviceDataStatus.SuccessDeviceDataReading -> {
                repository.saveData(
                    deviceReading = data.reading,
                    serviceUUID = DeviceConst.SERVICE_DATA_SERVICE,
                )
                emit(
                    MainPartialState.SetValues(
                        repository.getData(serviceUUID = DeviceConst.SERVICE_DATA_SERVICE), false
                    )
                )
            }
            else -> {}
        }
    }

    private fun startCollectingData(effects: EffectsCollector<MainEffect>): Flow<PartialState<MainState>> =
        repository.getDataFromDataBaseAsFlow(serviceUUID = DeviceConst.SERVICE_DATA_SERVICE)
            .map { status ->
                when (status) {
                    RepositoryStatus.Error ->
                        effects
                            .send(MainEffect.ShowError("Error during collecting data from DB"))
                            .let { NoAction() }
                    is RepositoryStatus.SuccessBleData -> {
                        MainPartialState.SetValues(status.data, isPreparing = false)
                    }
                }
            }

    private fun startObservingData(): Flow<MainPartialState> = flow {
        device.observationOnDataCharacteristic().collect { reading ->
            repository.saveData(
                deviceReading = reading,
                serviceUUID = DeviceConst.SERVICE_DATA_SERVICE,
            )
        }
    }

    private fun setWorkManager() {
        Timber.d("Set workManager")
        val periodicWorkRequest = PeriodicWorkRequest
            .Builder(ReadingWorker::class.java, 15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WorkManagerConst.WORK_TAG, ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest
        )
    }

    private suspend fun refreshDeviceData(
        effects: EffectsCollector<MainEffect>
    ): Flow<PartialState<MainState>> =
        kableService.refreshDeviceData(macAddress = dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS))
            .map { status ->
                when (status) {
                    is ScanStatus.Failed -> effects.send(MainEffect.ShowError(status.message as String))
                        .let { NoAction() }
                    is ScanStatus.Found -> setAdvertisementPartial(
                        status.advertisement,
                        isPreparing = false
                    )
                    ScanStatus.Scanning -> setIsScanningPartial(true)
                    ScanStatus.Stopped -> setIsScanningPartial(false)
                }
            }

    private fun setAdvertisementPartial(
        advertisement: Advertisement,
        isPreparing: Boolean
    ): MainPartialState {
        return MainPartialState.SetAdvertisement(advertisement, isPreparing)
    }

    private fun setIsScanningPartial(isScanning: Boolean): MainPartialState {
        return MainPartialState.SetIsScanning(isScanning)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel("onCleared")
    }
}

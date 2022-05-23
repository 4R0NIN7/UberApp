package com.untitledkingdom.ueberapp.feature.welcome

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
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEffect
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEvent
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomePartialState
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeState
import com.untitledkingdom.ueberapp.scanner.ScanService
import com.untitledkingdom.ueberapp.scanner.data.ScanStatus
import com.untitledkingdom.ueberapp.utils.functions.childScope
import com.untitledkingdom.ueberapp.utils.interval.FlowInterval
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import timber.log.Timber
import javax.inject.Inject

typealias WelcomeProcessor = Processor<WelcomeEvent, WelcomeState, WelcomeEffect>

@ExperimentalCoroutinesApi
@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val scanService: ScanService,
    private val dataStorage: DataStorage,
    private val flowInterval: FlowInterval
) : ViewModel() {
    private val scope = viewModelScope.childScope()

    val processor: WelcomeProcessor = processor(
        initialState = WelcomeState(),
        prepare = {
            merge(
                startScanning(effects),
                refreshAdvertisements()
            )
        },
        onEvent = { event ->
            when (event) {
                WelcomeEvent.StartScanning ->
                    startScanning(
                        effects
                    )
                WelcomeEvent.StopScanning -> {
                    scanService.stopScan().toNoAction()
                }
                is WelcomeEvent.SetScanningTo -> flowOf(
                    WelcomePartialState.SetIsScanning(isScanning = event.scanningTo)
                )
                is WelcomeEvent.StartConnectingToDevice -> {
                    connectToDeviceAndGoToMain(
                        effects = effects,
                        advertisement = event.advertisement
                    )
                }
                WelcomeEvent.RemoveScannedDevices -> flowOf(WelcomePartialState.RemoveAdvertisements)
                is WelcomeEvent.SetIsClickable -> flowOf(setIsClickablePartial(event.isClickable))
            }
        }
    )

    private fun refreshAdvertisements(): Flow<PartialState<WelcomeState>> =
        flowInterval.start().map {
            WelcomePartialState.RemoveAdvertisements
        }

    private fun connectToDeviceAndGoToMain(
        effects: EffectsCollector<WelcomeEffect>,
        advertisement: Advertisement
    ): Flow<WelcomePartialState> = flow {
        emit(setIsClickablePartial(true))
        emit(setIsConnectingPartial(true))
        connectToDevice(advertisement, effects)
        emit(setIsConnectingPartial(false))
    }

    private fun startScanning(
        effects: EffectsCollector<WelcomeEffect>,
    ): Flow<PartialState<WelcomeState>> = scanService
        .scan()
        .map { status ->
            when (status) {
                ScanStatus.Scanning -> setIsScanningPartial(true)
                is ScanStatus.Found -> setAdvertisements(
                    advertisement = status.advertisement
                )
                is ScanStatus.ConnectToPreviouslyConnectedDevice -> {
                    connectToDevice(status.advertisement, effects).let { NoAction() }
                }
                is ScanStatus.Failed -> effects.send(WelcomeEffect.ShowError(status.message as String))
                    .let { NoAction() }
                ScanStatus.Stopped -> setIsScanningPartial(false)
                ScanStatus.Omit -> {
                    NoAction()
                }
            }
        }

    private suspend fun connectToDevice(
        advertisement: Advertisement,
        effects: EffectsCollector<WelcomeEffect>
    ) {
        try {
            val peripheral = scanService.returnPeripheral(
                advertisement = advertisement,
                scope = scope
            )
            peripheral.connect()
            dataStorage.saveToStorage(DataStorageConst.MAC_ADDRESS, advertisement.address)
            effects.send(WelcomeEffect.GoToMain)
        } catch (e: Exception) {
            Timber.d("Exception in connect to device! + $e")
            effects.send(WelcomeEffect.ShowError("${e.message}"))
        }
    }

    private fun setIsScanningPartial(isScanning: Boolean): WelcomePartialState {
        return WelcomePartialState.SetIsScanning(isScanning)
    }

    private fun setIsClickablePartial(isClickable: Boolean): WelcomePartialState {
        return WelcomePartialState.SetIsClickable(isClickable)
    }

    private fun setIsConnectingPartial(isConnecting: Boolean): WelcomePartialState {
        return WelcomePartialState.SetIsConnecting(isConnecting)
    }

    private fun setAdvertisements(
        advertisement: Advertisement,
    ): WelcomePartialState {
        return WelcomePartialState.SetAdvertisements(
            checkIfAdvertisementAlreadyExists(advertisement)
        )
    }

    private fun checkIfAdvertisementAlreadyExists(
        newAdvertisement: Advertisement
    ): List<Advertisement> {
        val advertisements = processor.state.value.advertisements.toMutableList()
        val advertisementsMapRssi = processor.state.value.advertisementsRssiMap.toMutableMap()
        val indexQuery = advertisements.indexOfFirst { it.address == newAdvertisement.address }
        return if (indexQuery != -1) {
            val oldAdvertisement = advertisements[indexQuery]
            advertisements -= oldAdvertisement
            advertisements += newAdvertisement
            val list =
                advertisementsMapRssi[newAdvertisement]?.toMutableList()
                    ?.plus(newAdvertisement.rssi)
            if (list != null) {
                advertisementsMapRssi[newAdvertisement] = list
            }
            advertisements.toList()
        } else {
            advertisements += newAdvertisement
            advertisements.toList()
        }
    }
}

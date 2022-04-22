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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

typealias WelcomeProcessor = Processor<WelcomeEvent, WelcomeState, WelcomeEffect>

@ExperimentalCoroutinesApi
@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val scanService: ScanService,
    private val dataStorage: DataStorage
) : ViewModel() {
    private val scope = viewModelScope.childScope()
    val processor: WelcomeProcessor = processor(
        initialState = WelcomeState(),
        prepare = {
            startScanning(effects)
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
                is WelcomeEvent.SetIsClickable -> flowOf(WelcomePartialState.SetIsClickable(event.isClickable))
                WelcomeEvent.StartService -> startService(effects).toNoAction()
            }
        }
    )

    private suspend fun startService(effects: EffectsCollector<WelcomeEffect>) {
        if (dataStorage.getFromStorage(DataStorageConst.MAC_ADDRESS) != "") {
            effects.send(WelcomeEffect.StartService)
        }
    }

    private fun connectToDeviceAndGoToMain(
        effects: EffectsCollector<WelcomeEffect>,
        advertisement: Advertisement
    ): Flow<WelcomePartialState> = flow {
        emit(WelcomePartialState.SetIsClickable(true))
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

    private fun startScanning(
        effects: EffectsCollector<WelcomeEffect>,
    ): Flow<PartialState<WelcomeState>> = scanService.scan().map { status ->
        when (status) {
            ScanStatus.Scanning -> {
                setIsClickablePartial(true)
                setIsScanningPartial(true)
            }
            is ScanStatus.Found -> setAdvertisements(
                advertisement = status.advertisement
            )
            is ScanStatus.Failed -> effects.send(WelcomeEffect.ShowError(status.message as String))
                .let { NoAction() }
            ScanStatus.Stopped -> setIsScanningPartial(false)
        }
    }

    private fun setIsScanningPartial(isScanning: Boolean): WelcomePartialState {
        return WelcomePartialState.SetIsScanning(isScanning)
    }

    private fun setIsClickablePartial(isClickable: Boolean): WelcomePartialState {
        return WelcomePartialState.SetIsClickable(isClickable)
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
        val indexQuery = advertisements.indexOfFirst { it.address == newAdvertisement.address }
        return if (indexQuery != -1) {
            val oldAdvertisement = advertisements[indexQuery]
            advertisements -= oldAdvertisement
            advertisements += newAdvertisement
            advertisements.toList()
        } else {
            advertisements += newAdvertisement
            advertisements.toList()
        }
    }
}

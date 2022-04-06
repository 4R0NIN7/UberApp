package com.untitledkingdom.ueberapp.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.tomcz.ellipse.EffectsCollector
import com.tomcz.ellipse.PartialState
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.NoAction
import com.tomcz.ellipse.common.processor
import com.tomcz.ellipse.common.toNoAction
import com.untitledkingdom.ueberapp.ble.KableService
import com.untitledkingdom.ueberapp.ble.data.ScanStatus
import com.untitledkingdom.ueberapp.feature.state.MyEffect
import com.untitledkingdom.ueberapp.feature.state.MyEvent
import com.untitledkingdom.ueberapp.feature.state.MyPartialState
import com.untitledkingdom.ueberapp.feature.state.MyState
import com.untitledkingdom.ueberapp.utils.childScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.*
import java.util.concurrent.CancellationException
import javax.inject.Inject

typealias MyProcessor = Processor<MyEvent, MyState, MyEffect>

@HiltViewModel
class MyViewModel @Inject constructor(
    private val kableService: KableService
) : ViewModel() {

    private val scope = viewModelScope.childScope()
    val processor: MyProcessor = processor(
        initialState = MyState(),
        onEvent = { event ->
            when (event) {
                MyEvent.StartScanning ->
                    startScanning(
                        effects
                    )
                MyEvent.StopScanning -> {
                    kableService.stopScan().toNoAction()
                }
                is MyEvent.SetScanningTo -> flowOf(
                    MyPartialState.SetIsScanning(isScanning = event.scanningTo)
                )
                is MyEvent.StartConnectingToDevice -> connectToDevice(
                    effects = effects,
                    advertisement = event.advertisement
                ).toNoAction()
                MyEvent.RemoveScannedDevices -> flowOf(MyPartialState.RemoveAdvertisements)
                is MyEvent.TabChanged -> flowOf(MyPartialState.TabChanged(event.newTabIndex))
                is MyEvent.EndConnectingToDevice -> disconnectFromDevice(device = event.device).toNoAction()
                is MyEvent.ShowCharacteristics -> showCharacteristics(
                    event.service,
                    state.value.peripheral
                ).toNoAction()
            }
        }
    )

    private suspend fun showCharacteristics(service: UUID, device: Peripheral?) {
        if (device == null) {
            return
        }
        device.connect()
        val characteristics = device.services?.first {
            it.serviceUuid == service
        }?.characteristics
        Timber.d("Service $service")
        characteristics?.forEach { characteristic ->
            Timber.d("Characteristic ${characteristic.characteristicUuid}")
            val data = device.read(characteristic)
            Timber.d("Data is $data")
        }
    }

    private fun connectToDevice(
        effects: EffectsCollector<MyEffect>,
        advertisement: Advertisement
    ): Flow<MyPartialState> = flow {
        try {
            val device = kableService.returnPeripheral(scope = scope, advertisement = advertisement)
            emit(MyPartialState.SetConnectedToAdvertisement(advertisement = advertisement))
            emit(MyPartialState.SetConnectedToPeripheral(peripheral = device))
            device.connect()
            if (device.services != null) {
                emit(MyPartialState.SetServicesFromPeripheral(device.services!!))
            }
            effects.send(MyEffect.ConnectToDevice(device = device))
            device.disconnect()
        } catch (ie: IllegalStateException) {
            Timber.d("IllegalStateException + ${ie.message}")
            effects.send(MyEffect.ShowError("$ie"))
        } catch (ce: CancellationException) {
            Timber.d("Canceled")
            effects.send(MyEffect.ShowError("$ce"))
        }
    }

    private suspend fun disconnectFromDevice(device: Peripheral) {
        device.disconnect()
    }

    private fun startScanning(
        effects: EffectsCollector<MyEffect>,
    ): Flow<PartialState<MyState>> = kableService.scan()
        .map { status ->
            when (status) {
                ScanStatus.Scanning -> setIsScanningPartial(true)
                is ScanStatus.Found -> setAdvertisements(
                    advertisement = status.advertisement
                )
                is ScanStatus.Failed -> effects.send(MyEffect.ShowError(status.message as String))
                    .let { NoAction() }
                ScanStatus.Stopped -> setIsScanningPartial(false)
            }
        }

    private fun setIsScanningPartial(isScanning: Boolean): MyPartialState {
        return MyPartialState.SetIsScanning(isScanning)
    }

    private fun setAdvertisements(
        advertisement: Advertisement,
    ): MyPartialState {
        Timber.d("Advertisements in viewModel $advertisement")
        return MyPartialState.SetAdvertisements(
            checkIfAdvertisementAlreadyExists(advertisement)
        )
    }

    private fun checkIfAdvertisementAlreadyExists(
        newAdvertisement: Advertisement
    ): List<Advertisement> {
        val advertisements = processor.state.value.advertisements.toMutableList()
        val indexQuery = advertisements.indexOfFirst { it.address == newAdvertisement.address }
        return if (indexQuery != -1) {
            Timber.d("Old is existing!")
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

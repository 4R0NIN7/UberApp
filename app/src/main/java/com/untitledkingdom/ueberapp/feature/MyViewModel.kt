package com.untitledkingdom.ueberapp.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.Advertisement
import com.tomcz.ellipse.EffectsCollector
import com.tomcz.ellipse.PartialState
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.NoAction
import com.tomcz.ellipse.common.processor
import com.tomcz.ellipse.common.toNoAction
import com.untitledkingdom.ueberapp.ble.KableService
import com.untitledkingdom.ueberapp.ble.data.ScanStatus
import com.untitledkingdom.ueberapp.feature.data.BleDevice
import com.untitledkingdom.ueberapp.feature.data.BleDeviceStatus
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
import javax.inject.Inject
import kotlin.random.Random

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
                is MyEvent.StartConnectingToDevice -> connectToDeviceAndGoToMain(
                    effects = effects,
                    advertisement = event.advertisement
                )
                MyEvent.RemoveScannedDevices -> flowOf(MyPartialState.RemoveAdvertisements)
                is MyEvent.TabChanged -> flowOf(MyPartialState.TabChanged(event.newTabIndex))
                is MyEvent.EndConnectingToDevice -> flow {
                    emit(MyPartialState.SetConnectedToBleDevice(bleDevice = null))
                    effects.send(MyEffect.GoToWelcome)
                }
                MyEvent.ReadCharacteristic -> read(state.value.device, effects)
                MyEvent.WriteCharacteristic -> write(
                    device = state.value.device,
                    value = generateRandomString()
                ).toNoAction()
                is MyEvent.SetIsClickable -> flowOf(MyPartialState.SetIsClickable(event.isClickable))
                is MyEvent.AddValue -> flowOf(MyPartialState.AddValue(event.value))
                MyEvent.ReadDataInLoop -> readDataInLoop(state.value.device, effects)
            }
        }
    )

    private fun readDataInLoop(
        device: BleDevice?,
        effects: EffectsCollector<MyEffect>
    ): Flow<PartialState<MyState>> =
        device!!.readFromDeviceInLoop().map { status ->
            when (status) {
                is BleDeviceStatus.Success -> {
                    Timber.d("Data is ${status.data}")
                    Timber.d("Values are ${processor.state.value.readValues}")
                    MyPartialState.AddValue(status.data)
                }
                is BleDeviceStatus.Error -> effects.send(MyEffect.ShowError(status.message))
                    .let { NoAction() }
            }
        }

    private fun generateRandomString(): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..15)
            .map { i -> Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    private suspend fun write(
        device: BleDevice?,
        value: String,
    ) {
        device!!.writeToDevice(value)
    }

    private fun read(
        device: BleDevice?,
        effects: EffectsCollector<MyEffect>
    ): Flow<PartialState<MyState>> = device!!.readFromDevice().map { status ->
        when (status) {
            is BleDeviceStatus.Success -> {
                Timber.d("Data is ${status.data}")
                Timber.d("Values are ${processor.state.value.readValues}")
                MyPartialState.AddValue(status.data)
            }
            is BleDeviceStatus.Error -> effects.send(MyEffect.ShowError(status.message))
                .let { NoAction() }
        }
    }

    private fun connectToDeviceAndGoToMain(
        effects: EffectsCollector<MyEffect>,
        advertisement: Advertisement
    ): Flow<MyPartialState> = flow {
        emit(MyPartialState.SetIsClickable(true))
        try {
            val peripheral = kableService.returnPeripheral(
                advertisement = advertisement,
                scope = scope
            )
            peripheral.connect()
            val services = peripheral.services ?: listOf()
            val device = BleDevice(
                device = peripheral,
                advertisement = advertisement,
                services = services
            )
            emit(MyPartialState.SetConnectedToBleDevice(bleDevice = device))
            effects.send(MyEffect.GoToMain)
        } catch (e: Exception) {
            Timber.d("Exception in connect to device! + ${e.message}")
            emit(MyPartialState.SetConnectedToBleDevice(bleDevice = null))
            effects.send(MyEffect.ShowError("${e.message}"))
        }
    }

    private fun startScanning(
        effects: EffectsCollector<MyEffect>,
    ): Flow<PartialState<MyState>> = kableService.scan().map { status ->
        when (status) {
            ScanStatus.Scanning -> {
                setIsClickablePartial(true)
                setIsScanningPartial(true)
            }
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

    private fun setIsClickablePartial(isClickable: Boolean): MyPartialState {
        return MyPartialState.SetIsClickable(isClickable)
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

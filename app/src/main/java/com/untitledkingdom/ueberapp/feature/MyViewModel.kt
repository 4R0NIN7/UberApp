package com.untitledkingdom.ueberapp.feature

import android.bluetooth.le.ScanResult
import androidx.lifecycle.ViewModel
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.processor
import com.tomcz.ellipse.common.toNoAction
import com.untitledkingdom.ueberapp.feature.state.MyEffect
import com.untitledkingdom.ueberapp.feature.state.MyEvent
import com.untitledkingdom.ueberapp.feature.state.MyState
import com.untitledkingdom.ueberapp.feature.state.WelcomePartialState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import javax.inject.Inject

typealias MyProcessor = Processor<MyEvent, MyState, MyEffect>

@HiltViewModel
class MyViewModel @Inject constructor() : ViewModel() {
    val processor: MyProcessor = processor(
        initialState = MyState(),
        onEvent = { event ->
            when (event) {
                MyEvent.StartScanning ->
                    effects.send(MyEffect.ScanDevices).toNoAction()
                MyEvent.StopScanning ->
                    effects.send(MyEffect.StopScanDevices).toNoAction()
                is MyEvent.SetScanningTo -> flowOf(
                    WelcomePartialState.SetScanningId(scanningTo = event.scanningTo)
                )
                is MyEvent.AddScannedDevice -> {
                    addScanResult(event.scanResult)
                }
                is MyEvent.StartConnectingToDevice -> {
                    if (state.value.isScanning) {
                        effects.send(MyEffect.StopScanDevices)
                    }
                    Timber.d("Device from scanResult ${event.scanResult.device}")
                    effects.send(
                        MyEffect.ConnectToDevice(
                            scanResult = event.scanResult
                        )
                    ).toNoAction()
                }
                MyEvent.RemoveScannedDevices -> flowOf(WelcomePartialState.RemoveScannedDevices)
                is MyEvent.SetConnectedToDeviceGatt -> flowOf(
                    WelcomePartialState.SetConnectedToBluetoothGatt(
                        event.bluetoothGatt
                    )
                )
                is MyEvent.SetConnectedTo -> {
                    if (event.address != "") {
                        val scanResult =
                            state.value.scanResults.first { it.device.address == event.address }
                        flowOf(WelcomePartialState.SetConnectedToScanResult(scanResult))
                    } else {
                        flowOf(WelcomePartialState.SetConnectedToScanResult(scanResult = null))
                    }
                }
                is MyEvent.EndConnectingToDevice -> effects.send(
                    MyEffect.DisconnectFromDevice(
                        event.gatt
                    )
                ).toNoAction()
            }
        }
    )

    private fun addScanResult(result: ScanResult): Flow<WelcomePartialState> = flow {
        Timber.d("Result device ${result.device}")
        val scanResults = processor.state.value.scanResults
        val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
        if (indexQuery != -1) {
            val oldScanResult = scanResults[indexQuery]
            Timber.d("Result already exists in a list $oldScanResult")
            Timber.d("Old ScanResult device ${oldScanResult.device}")
            emit(WelcomePartialState.RemoveScanResult(oldScanResult))
            emit(WelcomePartialState.AddScanResult(result))
        } else {
            emit(WelcomePartialState.AddScanResult(scanResult = result))
        }
    }
}

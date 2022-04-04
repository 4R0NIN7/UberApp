package com.untitledkingdom.ueberapp.feature.welcome

import android.bluetooth.le.ScanResult
import androidx.lifecycle.ViewModel
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.processor
import com.tomcz.ellipse.common.toNoAction
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEffect
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEvent
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomePartialState
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import javax.inject.Inject

typealias WelcomeProcessor = Processor<WelcomeEvent, WelcomeState, WelcomeEffect>

@HiltViewModel
class WelcomeViewModel @Inject constructor() : ViewModel() {
    val processor: WelcomeProcessor = processor(
        initialState = WelcomeState(),
        onEvent = { event ->
            when (event) {
                WelcomeEvent.StartScanning ->
                    effects.send(WelcomeEffect.ScanDevices).toNoAction()
                WelcomeEvent.StopScanning ->
                    effects.send(WelcomeEffect.StopScanDevices).toNoAction()
                is WelcomeEvent.SetScanningTo -> flowOf(
                    WelcomePartialState.SetScanningId(scanningTo = event.scanningTo)
                )
                is WelcomeEvent.AddScannedDevice -> {
                    addScanResult(event.scanResult)
                }
                is WelcomeEvent.StartConnectingToDevice -> {
                    if (state.value.isScanning) {
                        effects.send(WelcomeEffect.StopScanDevices)
                    }
                    effects.send(
                        WelcomeEffect.ConnectToDevice(
                            scanResult = event.scanResult
                        )
                    ).toNoAction()
                }
                WelcomeEvent.RemoveScannedDevices -> flowOf(WelcomePartialState.RemoveScannedDevices)
                is WelcomeEvent.SetConnectedToDeviceGatt -> flowOf(
                    WelcomePartialState.SetConnectedToBluetoothGatt(
                        event.bluetoothGatt
                    )
                )
                is WelcomeEvent.SetConnectedTo -> {
                    if (event.address != "") {
                        val scanResult =
                            state.value.scanResults.first { it.device.address == event.address }
                        flowOf(WelcomePartialState.SetConnectedToScanResult(scanResult))
                    } else {
                        flowOf(WelcomePartialState.SetConnectedToScanResult(scanResult = null))
                    }
                }
                is WelcomeEvent.EndConnectingToDevice -> effects.send(
                    WelcomeEffect.DisconnectFromDevice(
                        event.gatt
                    )
                ).toNoAction()
            }
        }
    )

    private fun addScanResult(result: ScanResult): Flow<WelcomePartialState> = flow {
        val scanResults = processor.state.value.scanResults
        val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
        if (indexQuery != -1) {
            val oldScanResult = scanResults[indexQuery]
            Timber.d("Result already exists in a list $oldScanResult")
            emit(WelcomePartialState.RemoveScanResult(oldScanResult))
            emit(WelcomePartialState.AddScanResult(result))
        } else {
            emit(WelcomePartialState.AddScanResult(scanResult = result))
        }
    }
}

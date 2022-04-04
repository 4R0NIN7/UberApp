package com.untitledkingdom.ueberapp.feature.welcome

import androidx.lifecycle.ViewModel
import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.processor
import com.tomcz.ellipse.common.toNoAction
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEffect
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEvent
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomePartialState
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flowOf
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
                is WelcomeEvent.AddScannedDevice -> flowOf(
                    WelcomePartialState.AddScanResult(
                        event.scanResult
                    )
                )
                is WelcomeEvent.StartConnectingToDevice -> effects.send(
                    WelcomeEffect.ConnectToDevice(
                        selectedDevice = event.selectedDevice
                    )
                ).toNoAction()
            }
        }
    )
}

package com.untitledkingdom.ueberapp.feature.welcome

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.tomcz.ellipse.common.collectAsState
import com.tomcz.ellipse.common.previewProcessor
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEvent
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeState
import com.untitledkingdom.ueberapp.ui.Colors
import com.untitledkingdom.ueberapp.ui.Paddings.padding12
import com.untitledkingdom.ueberapp.ui.Paddings.padding16
import com.untitledkingdom.ueberapp.ui.Paddings.padding8
import com.untitledkingdom.ueberapp.ui.Shapes.shape8
import com.untitledkingdom.ueberapp.ui.Typography
import com.untitledkingdom.ueberapp.ui.fontSize14
import com.untitledkingdom.ueberapp.ui.fontSize18

@Composable
fun WelcomeScreen(processor: WelcomeProcessor) {
    Column(
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Colors.AppBackground)
            .padding(horizontal = padding12)
    ) {
        AppInfo(processor = processor)
        ConnectedDevice(processor = processor)
        Devices(processor = processor)
    }
}

@Composable
fun ConnectedDevice(processor: WelcomeProcessor) {
    val selectedGatt by processor.collectAsState { it.deviceToConnectBluetoothGatt }
    val selectedDevice by processor.collectAsState { it.selectedDevice }
    if (selectedGatt != null && selectedDevice != null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Selected device",
                style = Typography.body1,
                fontWeight = FontWeight.Normal,
                color = Colors.FilterBlue,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().background(color = Colors.LightPurple)
            ) {
                DeviceItem(scanResult = selectedDevice!!, processor = processor)
            }
        }
    }
}

@Composable
fun AppInfo(processor: WelcomeProcessor) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = padding8)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = Typography.h2,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.welcome_creator),
            style = Typography.h6,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic
        )
        Text(
            text = stringResource(R.string.welcome_information_about_what_is_next_to_do),
            style = Typography.body1,
            fontWeight = FontWeight.Normal
        )
        val isScanning by processor.collectAsState { it.isScanning }
        if (!isScanning) {
            Button(
                onClick = {
                    processor.sendEvent(WelcomeEvent.RemoveScannedDevices)
                    processor.sendEvent(WelcomeEvent.StartScanning)
                }
            ) {
                Text(text = "Start Scan")
            }
        } else {
            Button(onClick = { processor.sendEvent(WelcomeEvent.StopScanning) }) {
                Text(text = "Stop Scan")
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun Devices(processor: WelcomeProcessor) {
    val scanResults by processor.collectAsState { it.scanResults }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = padding16)
            .padding(top = padding16),
        verticalArrangement = Arrangement.spacedBy(padding8),
        contentPadding = PaddingValues(bottom = padding16)
    ) {
        items(items = scanResults.sortedBy { it.device.name }) {
            DeviceItem(
                scanResult = it,
                processor = processor
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceItem(scanResult: ScanResult, processor: WelcomeProcessor) {
    val scannedDevice = scanResult.device
    val selectedDevice by processor.collectAsState { it.deviceToConnectBluetoothGatt }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier
                .clickable {
                    if (selectedDevice != null) {
                        processor.sendEvent(WelcomeEvent.EndConnectingToDevice(selectedDevice!!))
                    } else {
                        processor.sendEvent(WelcomeEvent.StartConnectingToDevice(scanResult = scanResult))
                    }
                }
                .fillMaxWidth(),
            shape = shape8,
            border = null,
            backgroundColor = Colors.AppBackground
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Device Name: ${scannedDevice.name}",
                    style = Typography.body1,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize18,
                    color = Colors.RoomName2Color
                )
                Text(
                    text = "Device Address: ${scannedDevice.address}",
                    style = Typography.body1,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize14,
                    color = Colors.RoomName7Color
                )
                Text(
                    text = "Transmit power: ${scanResult.rssi}",
                    style = Typography.body1,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize14,
                    color = Colors.RoomName5Color
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun Preview() {
    WelcomeScreen(processor = previewProcessor(WelcomeState()))
}

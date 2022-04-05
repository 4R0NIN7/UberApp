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
import com.untitledkingdom.ueberapp.feature.MyProcessor
import com.untitledkingdom.ueberapp.feature.state.MyEvent
import com.untitledkingdom.ueberapp.feature.state.MyState
import com.untitledkingdom.ueberapp.feature.welcome.data.ScannedDevice
import com.untitledkingdom.ueberapp.ui.Colors
import com.untitledkingdom.ueberapp.ui.Paddings.padding12
import com.untitledkingdom.ueberapp.ui.Paddings.padding16
import com.untitledkingdom.ueberapp.ui.Paddings.padding8
import com.untitledkingdom.ueberapp.ui.Shapes.shape8
import com.untitledkingdom.ueberapp.ui.Typography
import com.untitledkingdom.ueberapp.ui.fontSize14
import com.untitledkingdom.ueberapp.ui.fontSize18
import com.untitledkingdom.ueberapp.utils.toScannedDevice

@Composable
fun WelcomeScreen(processor: MyProcessor) {
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
fun ConnectedDevice(processor: MyProcessor) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Colors.LightPurple)
            ) {
                DeviceItem(
                    scanResult = selectedDevice!!,
                    processor = processor,
                    scannedDevice = selectedDevice!!.toScannedDevice()
                )
            }
        }
    }
}

@Composable
fun AppInfo(processor: MyProcessor) {
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
                    processor.sendEvent(MyEvent.RemoveScannedDevices)
                    processor.sendEvent(MyEvent.StartScanning)
                }
            ) {
                Text(text = "Start Scan")
            }
        } else {
            Button(onClick = { processor.sendEvent(MyEvent.StopScanning) }) {
                Text(text = "Stop Scan")
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun Devices(processor: MyProcessor) {
    val scanResults by processor.collectAsState { it.scanResults }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = padding16)
            .padding(top = padding16),
        verticalArrangement = Arrangement.spacedBy(padding8),
        contentPadding = PaddingValues(bottom = padding16)
    ) {
        items(items = scanResults.sortedByDescending { it.device.name }) {
            DeviceItem(
                scannedDevice = it.toScannedDevice(),
                processor = processor,
                scanResult = it
            )
        }
    }
}

@Composable
fun DeviceItem(
    scannedDevice: ScannedDevice,
    processor: MyProcessor,
    scanResult: ScanResult
) {
    val selectedDevice by processor.collectAsState { it.deviceToConnectBluetoothGatt }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier
                .clickable {
                    if (selectedDevice != null) {
                        processor.sendEvent(MyEvent.EndConnectingToDevice(selectedDevice!!))
                    } else {
                        processor.sendEvent(MyEvent.StartConnectingToDevice(scanResult = scanResult))
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
                    text = "Transmit power: ${scannedDevice.rssi}",
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
    WelcomeScreen(processor = previewProcessor(MyState()))
}

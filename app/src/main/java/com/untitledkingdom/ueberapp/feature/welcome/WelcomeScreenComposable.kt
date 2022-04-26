package com.untitledkingdom.ueberapp.feature.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
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
import com.untitledkingdom.ueberapp.ui.common.DeviceItem
import com.untitledkingdom.ueberapp.ui.values.AppBackground
import com.untitledkingdom.ueberapp.ui.values.Black
import com.untitledkingdom.ueberapp.ui.values.Typography
import com.untitledkingdom.ueberapp.ui.values.White
import com.untitledkingdom.ueberapp.ui.values.padding12
import com.untitledkingdom.ueberapp.ui.values.padding16
import com.untitledkingdom.ueberapp.ui.values.padding8
import com.untitledkingdom.ueberapp.utils.functions.toScannedDevice

@Composable
fun WelcomeScreen(processor: WelcomeProcessor) {
    Column(
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppBackground)
            .padding(horizontal = padding12)
    ) {
        AppInfo(processor = processor)
        Devices(processor = processor)
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
                    processor.sendEvent(
                        WelcomeEvent.RemoveScannedDevices,
                        WelcomeEvent.StartScanning
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Black,
                    contentColor = White
                ),
            ) {
                Text(text = "Start Scan")
            }
        } else {
            Button(
                onClick = {
                    processor.sendEvent(WelcomeEvent.StopScanning)
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Black,
                    contentColor = White
                ),
            ) {
                Text(text = "Stop Scan")
            }
        }
    }
}

@Composable
fun Devices(processor: WelcomeProcessor) {
    val advertisements by processor.collectAsState { it.advertisements }
    val isScanning by processor.collectAsState { it.isScanning }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = padding16)
            .padding(top = padding16),
        verticalArrangement = Arrangement.spacedBy(padding8),
        contentPadding = PaddingValues(bottom = padding16)
    ) {
        items(items = advertisements.sortedBy { it.name }) {
            DeviceItem(
                scannedDevice = it.toScannedDevice(),
                action = {
                    if (isScanning) processor.sendEvent(WelcomeEvent.StopScanning)
                    processor.sendEvent(WelcomeEvent.StartConnectingToDevice(it))
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun Preview() {
    WelcomeScreen(processor = previewProcessor(WelcomeState()))
}

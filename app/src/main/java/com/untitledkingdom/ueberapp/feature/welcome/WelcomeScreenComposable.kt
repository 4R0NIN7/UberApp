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
import com.untitledkingdom.ueberapp.ui.common.DeviceItem
import com.untitledkingdom.ueberapp.ui.values.AppBackground
import com.untitledkingdom.ueberapp.ui.values.Typography
import com.untitledkingdom.ueberapp.ui.values.padding12
import com.untitledkingdom.ueberapp.ui.values.padding16
import com.untitledkingdom.ueberapp.ui.values.padding8
import com.untitledkingdom.ueberapp.utils.functions.toScannedDevice

@Composable
fun WelcomeScreen(processor: MyProcessor) {
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
                    processor.sendEvent(MyEvent.RemoveScannedDevices, MyEvent.StartScanning)
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

@Composable
fun Devices(processor: MyProcessor) {
    val advertisements by processor.collectAsState { it.advertisements }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = padding16)
            .padding(top = padding16),
        verticalArrangement = Arrangement.spacedBy(padding8),
        contentPadding = PaddingValues(bottom = padding16)
    ) {
        items(items = advertisements.sortedByDescending { it.name }) {
            DeviceItem(
                scannedDevice = it.toScannedDevice(),
                processor = processor,
                advertisement = it,
                canDisconnect = false
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun Preview() {
    WelcomeScreen(processor = previewProcessor(MyState()))
}

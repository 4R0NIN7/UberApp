package com.untitledkingdom.ueberapp.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.juul.kable.Advertisement
import com.tomcz.ellipse.common.collectAsState
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.feature.MyProcessor
import com.untitledkingdom.ueberapp.feature.state.MyEvent
import com.untitledkingdom.ueberapp.feature.welcome.data.ScannedDevice
import com.untitledkingdom.ueberapp.ui.values.AppBackground
import com.untitledkingdom.ueberapp.ui.values.Black
import com.untitledkingdom.ueberapp.ui.values.Blue
import com.untitledkingdom.ueberapp.ui.values.Gray
import com.untitledkingdom.ueberapp.ui.values.Typography
import com.untitledkingdom.ueberapp.ui.values.fontSize18
import com.untitledkingdom.ueberapp.ui.values.padding8
import com.untitledkingdom.ueberapp.ui.values.shape8

@Composable
fun DeviceItem(
    scannedDevice: ScannedDevice,
    processor: MyProcessor,
    advertisement: Advertisement,
    canDisconnect: Boolean
) {
    val device by processor.collectAsState { it.device }
    val isClickable by processor.collectAsState { it.isClickable }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier
                .clickable {
                    if (isClickable) {
                        if (canDisconnect) {
                            processor.sendEvent(MyEvent.EndConnectingToDevice(device!!))
                            processor.sendEvent(MyEvent.SetIsClickable(false))
                        } else {
                            processor.sendEvent(MyEvent.StartConnectingToDevice(advertisement = advertisement))
                            processor.sendEvent(MyEvent.SetIsClickable(false))
                        }
                    }
                }
                .fillMaxWidth(),
            shape = shape8,
            border = null,
            backgroundColor = AppBackground
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                RowText(key = "Device name", value = scannedDevice.name, colorValue = Gray)
                RowText(key = "Device address", value = scannedDevice.address, colorValue = Gray)
                RowText(
                    key = "Signal strength", value = scannedDevice.rssi.toString(),
                    colorValue = colorRssi(
                        rssi = scannedDevice.rssi
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(padding8)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Signal strength",
                        style = Typography.body1,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize18,
                        color = Black
                    )
                    Image(
                        signalStrengthRssi(rssi = scannedDevice.rssi),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(color = Black)
                    )
                }
            }
        }
    }
}

@Composable
private fun signalStrengthRssi(rssi: Int): Painter {
    return when (rssi) {
        in -40..0 -> painterResource(id = R.drawable.ic_baseline_signal_3)
        in -60..-40 -> painterResource(id = R.drawable.ic_baseline_signal_2)
        else -> painterResource(id = R.drawable.ic_baseline_signal_1)
    }
}

@Composable
private fun colorRssi(rssi: Int): Color {
    return when (rssi) {
        in -40..0 -> Blue
        in -60..-40 -> Black
        else -> Gray
    }
}

@Composable
private fun RowText(
    key: String,
    value: String,
    colorValue: Color,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier
        .padding(padding8)
        .fillMaxWidth()
) {
    Text(
        text = key,
        style = Typography.body1,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize18,
        color = Black
    )
    Text(
        text = value,
        style = Typography.body1,
        fontWeight = FontWeight.Normal,
        fontSize = fontSize18,
        color = colorValue
    )
}

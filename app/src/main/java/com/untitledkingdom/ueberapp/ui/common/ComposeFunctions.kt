package com.untitledkingdom.ueberapp.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.feature.welcome.data.ScannedDevice
import com.untitledkingdom.ueberapp.ui.values.AppBackground
import com.untitledkingdom.ueberapp.ui.values.Black
import com.untitledkingdom.ueberapp.ui.values.Gray
import com.untitledkingdom.ueberapp.ui.values.Purple200
import com.untitledkingdom.ueberapp.ui.values.PurpleRed
import com.untitledkingdom.ueberapp.ui.values.RoomName3Color
import com.untitledkingdom.ueberapp.ui.values.SplashPurple
import com.untitledkingdom.ueberapp.ui.values.Typography
import com.untitledkingdom.ueberapp.ui.values.White
import com.untitledkingdom.ueberapp.ui.values.fontSize18
import com.untitledkingdom.ueberapp.ui.values.padding8
import com.untitledkingdom.ueberapp.ui.values.shape8
import com.untitledkingdom.ueberapp.ui.values.signalAlmostDisconnected
import com.untitledkingdom.ueberapp.ui.values.signalBad
import com.untitledkingdom.ueberapp.ui.values.signalFull
import com.untitledkingdom.ueberapp.ui.values.signalGood
import com.untitledkingdom.ueberapp.utils.date.DateFormatter

@Composable
fun DeviceItem(
    scannedDevice: ScannedDevice,
    action: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier
                .clickable {
                    action()
                }
                .fillMaxWidth(),
            shape = shape8,
            border = null,
            backgroundColor = AppBackground
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                RowText(key = "Device name", value = scannedDevice.name, colorValue = Purple200)
                RowText(
                    key = "Device address",
                    value = scannedDevice.address,
                    colorValue = RoomName3Color
                )
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
        in -30..-0 -> signalFull
        in -40..-30 -> signalGood
        in -60..-40 -> signalBad
        else -> signalAlmostDisconnected
    }
}

@Composable
internal fun RowText(
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

@Composable
fun ReadingItem(
    bleData: BleData
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = shape8,
            border = null,
            backgroundColor = AppBackground
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                RowText(
                    key = "DeviceReading at",
                    value = bleData.localDateTime.format(DateFormatter.dateDDMMMMYYYYHHMMSS),
                    colorValue = Black
                )
                RowText(
                    key = "Temperature ",
                    value = bleData.deviceReading.temperature.toString(),
                    colorValue = Gray
                )
                RowText(
                    key = "Humidity ",
                    value = bleData.deviceReading.humidity.toString(),
                    colorValue = Gray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(padding8)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Synchronized ",
                        style = Typography.body1,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize18,
                        color = Black
                    )
                    if (bleData.isSynchronized) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_beenhere_24),
                            contentDescription = null,
                            tint = signalFull
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_do_not_disturb_alt_24),
                            contentDescription = null,
                            tint = PurpleRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Toolbar(
    title: String,
    action: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .wrapContentWidth(
                            Alignment.Start
                        )
                        .padding(bottom = 3.dp),
                    color = Black
                )
            }
        },
        backgroundColor = White,
        navigationIcon = {
            IconButton(onClick = { action() }) {
                Image(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(id = R.drawable.ic_baseline_close_24),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(SplashPurple)
                )
            }
        }
    )
}

@Composable
fun LoadingDialog() {
    Dialog(onDismissRequest = { }) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
        ) {
            CircularProgressIndicator(
                color = SplashPurple
            )
        }
    }
}

fun isSynchronized(
    firstIdSend: Int,
    lastIdSend: Int,
    id: Int
): Boolean = id in firstIdSend..lastIdSend

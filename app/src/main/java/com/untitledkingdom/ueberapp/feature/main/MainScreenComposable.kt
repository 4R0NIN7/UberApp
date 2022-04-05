package com.untitledkingdom.ueberapp.feature.main

import android.bluetooth.BluetoothGattService
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.tomcz.ellipse.common.collectAsState
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.feature.MyProcessor
import com.untitledkingdom.ueberapp.feature.state.MyEvent
import com.untitledkingdom.ueberapp.feature.welcome.DeviceItem
import com.untitledkingdom.ueberapp.ui.Colors
import com.untitledkingdom.ueberapp.ui.Paddings
import com.untitledkingdom.ueberapp.ui.Paddings.padding12
import com.untitledkingdom.ueberapp.ui.Paddings.padding16
import com.untitledkingdom.ueberapp.ui.Paddings.padding2
import com.untitledkingdom.ueberapp.ui.Paddings.padding24
import com.untitledkingdom.ueberapp.ui.Shapes.shape8
import com.untitledkingdom.ueberapp.ui.Typography
import com.untitledkingdom.ueberapp.ui.fontSize18
import com.untitledkingdom.ueberapp.utils.toScannedDevice

@ExperimentalPagerApi
@Composable
fun MainScreenCompose(processor: MyProcessor) {
    Scaffold(
        backgroundColor = Colors.AppBackground,
        topBar = {
            Tabs(processor = processor)
        },
        content = {
        }
    )
}

@ExperimentalPagerApi
@Composable
fun Tabs(processor: MyProcessor) {
    val tabIndex by processor.collectAsState { it.tabIndex }
    val tabs = listOf(
        stringResource(R.string.main_main_screen),
        stringResource(R.string.main_history),
        stringResource(R.string.main_settings)
    )
    Column(
        modifier = Modifier.background(
            Colors.AppBackground
        )
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = padding12)
                .padding(top = padding12),
            shape = shape8,
            backgroundColor = Colors.AppBackground
        ) {
            TabRow(
                selectedTabIndex = tabIndex,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[tabIndex])
                            .clip(
                                shape = RoundedCornerShape(
                                    topStart = padding24,
                                    topEnd = padding24
                                )
                            )
                            .padding(horizontal = padding16)
                            .background(Colors.White)
                            .height(padding2)
                    )
                },
                contentColor = Colors.DevicesTabsColorBlack,
                backgroundColor = Colors.DevicesTabsColorBlack
            ) {
                tabs.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                    ) {
                        Tab(
                            selectedContentColor = Colors.White,
                            unselectedContentColor = Colors.Gray,
                            modifier = Modifier.wrapContentWidth(Alignment.Start),
                            selected = tabIndex == index,
                            onClick = {
                                processor.sendEvent(MyEvent.TabChanged(index))
                            },
                            text = {
                                TabTitle(title = title)
                            },
                        )
                        if (index in 1..3) {
                            DividerGray(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                                    .wrapContentWidth(Alignment.End)
                                    .padding(vertical = padding12)
                            )
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier.background(
                Colors.AppBackground,
            )
        ) {
            when (tabIndex) {
                0 -> {
                    processor.sendEvent(MyEvent.TabChanged(0))
                    MainScreen(processor)
                }
                1 -> {
                    processor.sendEvent(MyEvent.TabChanged(1))
                    HistoryScreen(processor)
                }
                2 -> {
                    processor.sendEvent(MyEvent.TabChanged(2))
                    SettingsScreen(processor)
                }
            }
        }
    }
}

@Composable
fun MainScreen(processor: MyProcessor) {
    DeviceInfo(processor = processor)
}

@Composable
fun HistoryScreen(processor: MyProcessor) {
}

@Composable
fun SettingsScreen(processor: MyProcessor) {
}

@Composable
private fun TabTitle(title: String) {
    Text(
        text = title,
        overflow = TextOverflow.Visible,
        maxLines = 1,
        style = TextStyle(
            fontSize = 12.5.sp,
            letterSpacing = 0.sp
        ),
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun DeviceInfo(processor: MyProcessor) {
    Column(
        verticalArrangement = Arrangement.spacedBy(padding24),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding24)
            .padding(horizontal = padding12)
    ) {
        ConnectedDevice(processor = processor)
        Services(processor = processor)
    }
}

@Composable
fun Services(processor: MyProcessor) {
    val services by processor.collectAsState { it.deviceToConnectBluetoothGatt!!.services }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = padding16)
            .padding(top = padding16),
        verticalArrangement = Arrangement.spacedBy(Paddings.padding8),
        contentPadding = PaddingValues(bottom = padding16)
    ) {
        items(items = services) {
            Service(it, processor)
        }
    }
}

@Composable
fun Service(service: BluetoothGattService, processor: MyProcessor) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    processor.sendEvent(MyEvent.ShowCharacteristics(service.uuid))
                },
            shape = shape8,
            border = null,
            backgroundColor = Colors.AppBackground
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = service.uuid.toString(),
                    style = Typography.body1,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize18,
                    color = Colors.Black
                )
            }
        }
    }
}

@Composable
fun DividerGray(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = Colors.SectionDividerLight,
    )
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

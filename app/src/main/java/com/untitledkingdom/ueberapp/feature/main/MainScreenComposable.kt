package com.untitledkingdom.ueberapp.feature.main

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
import androidx.compose.material.Button
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
import com.juul.kable.DiscoveredService
import com.tomcz.ellipse.common.collectAsState
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.feature.MyProcessor
import com.untitledkingdom.ueberapp.feature.state.MyEvent
import com.untitledkingdom.ueberapp.ui.common.DeviceItem
import com.untitledkingdom.ueberapp.ui.values.AppBackground
import com.untitledkingdom.ueberapp.ui.values.Black
import com.untitledkingdom.ueberapp.ui.values.BlackSelectedDay
import com.untitledkingdom.ueberapp.ui.values.DevicesTabsColorBlack
import com.untitledkingdom.ueberapp.ui.values.Gray
import com.untitledkingdom.ueberapp.ui.values.SectionDividerLight
import com.untitledkingdom.ueberapp.ui.values.Typography
import com.untitledkingdom.ueberapp.ui.values.White
import com.untitledkingdom.ueberapp.ui.values.fontSize18
import com.untitledkingdom.ueberapp.ui.values.padding12
import com.untitledkingdom.ueberapp.ui.values.padding16
import com.untitledkingdom.ueberapp.ui.values.padding2
import com.untitledkingdom.ueberapp.ui.values.padding24
import com.untitledkingdom.ueberapp.ui.values.padding8
import com.untitledkingdom.ueberapp.ui.values.shape8
import com.untitledkingdom.ueberapp.utils.toScannedDevice

@ExperimentalPagerApi
@Composable
fun MainScreenCompose(processor: MyProcessor) {
    Scaffold(
        backgroundColor = AppBackground,
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
            AppBackground
        )
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = padding12)
                .padding(top = padding12),
            shape = shape8,
            backgroundColor = AppBackground
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
                            .background(White)
                            .height(padding2)
                    )
                },
                contentColor = DevicesTabsColorBlack,
                backgroundColor = DevicesTabsColorBlack
            ) {
                tabs.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                    ) {
                        Tab(
                            selectedContentColor = White,
                            unselectedContentColor = Gray,
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
                AppBackground,
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
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding24)
            .padding(horizontal = padding12)
    ) {
        Column {
            ConnectedDevice(processor = processor)
        }
        Values(processor = processor)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding8),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { processor.sendEvent(MyEvent.ReadCharacteristic) }) {
                Text("Start Reading")
            }
            Button(onClick = { processor.sendEvent(MyEvent.StopReadingCharacteristic) }) {
                Text("Stop Reading")
            }
        }
    }
}

@Composable
fun Values(processor: MyProcessor) {
    val readValues by processor.collectAsState { it.readValues }
    LazyColumn(
        modifier = Modifier
            .height(300.dp)
            .padding(horizontal = padding16)
            .padding(top = padding16),
        verticalArrangement = Arrangement.spacedBy(padding8),
        contentPadding = PaddingValues(bottom = padding16)
    ) {
        items(items = readValues) { value ->
            Value(
                value = value,
                indexOf = readValues.indexOf(value)
            )
        }
    }
}

@Composable
fun Value(value: String, indexOf: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Value $indexOf",
            style = Typography.h6,
            fontWeight = FontWeight.SemiBold,
            color = BlackSelectedDay,
        )
        Text(
            text = " $value",
            style = Typography.h6,
            fontWeight = FontWeight.Normal,
            color = Gray,
        )
    }
}

@Composable
fun Service(service: DiscoveredService, processor: MyProcessor) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    processor.sendEvent(MyEvent.ReadCharacteristic)
                },
            shape = shape8,
            border = null,
            backgroundColor = AppBackground
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = service.serviceUuid.toString(),
                    style = Typography.body1,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize18,
                    color = Black
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
        color = SectionDividerLight,
    )
}

@Composable
fun ConnectedDevice(processor: MyProcessor) {
    val device by processor.collectAsState { it.device }
    val selectedAdvertisement by processor.collectAsState { it.selectedAdvertisement }
    if (device != null && selectedAdvertisement != null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Selected device",
                style = Typography.h6,
                fontWeight = FontWeight.SemiBold,
                color = BlackSelectedDay,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                DeviceItem(
                    advertisement = selectedAdvertisement!!,
                    processor = processor,
                    scannedDevice = selectedAdvertisement!!.toScannedDevice(),
                    canDisconnect = true
                )
            }
        }
    }
}

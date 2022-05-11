package com.untitledkingdom.ueberapp.feature.main

import android.annotation.SuppressLint
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
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.tomcz.ellipse.common.collectAsState
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.database.data.BleDataCharacteristics
import com.untitledkingdom.ueberapp.feature.details.DetailsScreen
import com.untitledkingdom.ueberapp.feature.main.state.MainEvent
import com.untitledkingdom.ueberapp.ui.common.DeviceItem
import com.untitledkingdom.ueberapp.ui.common.LinearProgressBar
import com.untitledkingdom.ueberapp.ui.common.ReadingItem
import com.untitledkingdom.ueberapp.ui.common.RowText
import com.untitledkingdom.ueberapp.ui.values.AppBackground
import com.untitledkingdom.ueberapp.ui.values.Black
import com.untitledkingdom.ueberapp.ui.values.BlackTitle
import com.untitledkingdom.ueberapp.ui.values.Blue
import com.untitledkingdom.ueberapp.ui.values.DevicesTabsColorBlack
import com.untitledkingdom.ueberapp.ui.values.DividerLight
import com.untitledkingdom.ueberapp.ui.values.Gray
import com.untitledkingdom.ueberapp.ui.values.SplashPurple
import com.untitledkingdom.ueberapp.ui.values.Typography
import com.untitledkingdom.ueberapp.ui.values.White
import com.untitledkingdom.ueberapp.ui.values.fontSize18
import com.untitledkingdom.ueberapp.ui.values.padding12
import com.untitledkingdom.ueberapp.ui.values.padding16
import com.untitledkingdom.ueberapp.ui.values.padding2
import com.untitledkingdom.ueberapp.ui.values.padding24
import com.untitledkingdom.ueberapp.ui.values.padding72
import com.untitledkingdom.ueberapp.ui.values.padding8
import com.untitledkingdom.ueberapp.ui.values.shape8
import com.untitledkingdom.ueberapp.utils.date.DateFormatter
import com.untitledkingdom.ueberapp.utils.functions.decimalFormat
import com.untitledkingdom.ueberapp.utils.functions.toScannedDevice
import timber.log.Timber

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun MainScreenCompose(processor: MainProcessor) {
    Scaffold(
        backgroundColor = AppBackground,
        topBar = {
            val selectedDate by processor.collectAsState { it.selectedDate }
            val values by processor.collectAsState { it.values }
            if (selectedDate != "" && values.isNotEmpty()) {
                DetailsScreen(processor = processor)
            } else {
                Tabs(processor = processor)
            }
        }
    ) {}
}

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun Tabs(processor: MainProcessor) {
    val tabIndex by processor.collectAsState { it.tabIndex }
    val tabs = listOf(
        stringResource(R.string.main_main_screen),
        stringResource(R.string.main_history),
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
                                processor.sendEvent(MainEvent.TabChanged(index))
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
                    processor.sendEvent(MainEvent.TabChanged(newTabIndex = 0))
                    MainScreen(processor)
                }
                1 -> {
                    processor.sendEvent(MainEvent.TabChanged(newTabIndex = 1))
                    HistoryScreen(processor)
                }
            }
        }
    }
}

@Composable
fun MainScreen(processor: MainProcessor) {
    DeviceInfo(processor = processor)
}

@ExperimentalMaterialApi
@Composable
fun HistoryScreen(processor: MainProcessor) {
    val dayCharacteristics by processor.collectAsState {
        it.dataCharacteristics
    }
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding24)
            .padding(horizontal = padding12)
    ) {
        if (dayCharacteristics.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(padding8),
                contentPadding = PaddingValues(bottom = padding72)
            ) {
                items(items = dayCharacteristics) { day ->
                    DayDisplay(
                        processor = processor,
                        characteristics = day
                    )
                }
            }
        } else {
            Text("History is empty!")
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun DayDisplay(
    characteristics: BleDataCharacteristics,
    processor: MainProcessor
) {
    val date = characteristics.day
    val dateString = date.format(DateFormatter.dateYYYYMMDD)
    val temperatureString = "Min: ${decimalFormat.format(characteristics.minimalTemperature)}\n" +
        "Avg: ${decimalFormat.format(characteristics.averageTemperature)}\n" +
        "Max: ${decimalFormat.format(characteristics.maximalTemperature)}"
    val humidityString = "Min: ${decimalFormat.format(characteristics.minimalHumidity)}\n" +
        "Avg: ${decimalFormat.format(characteristics.averageHumidity)}\n" +
        "Max: ${decimalFormat.format(characteristics.maximalHumidity)}"
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier
                .clickable {
                    Timber.d("date in history $date")
                    processor.sendEvent(MainEvent.OpenDetails(date = dateString))
                }
                .fillMaxWidth(),
            shape = shape8,
            border = null,
            backgroundColor = AppBackground
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                RowText(key = "Date", value = dateString, colorValue = Black)
                ReadingsRow(
                    key = "Temperature",
                    value = temperatureString,
                    colorValue = SplashPurple
                )
                ReadingsRow(
                    key = "Humidity",
                    value = humidityString,
                    colorValue = Blue
                )
            }
        }
    }
}

@Composable
fun ReadingsRow(
    key: String,
    value: String,
    colorValue: Color
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
    Row(horizontalArrangement = Arrangement.Center) {
        Text(
            text = value,
            style = Typography.body1,
            fontWeight = FontWeight.Normal,
            fontSize = fontSize18,
            color = colorValue
        )
    }
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
fun DeviceInfo(processor: MainProcessor) {
    Column(
        verticalArrangement = Arrangement.spacedBy(padding24),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding24)
            .padding(horizontal = padding12)
    ) {
        ConnectedDevice(processor = processor)
        ActualReading(processor = processor)
    }
}

@Composable
fun DividerGray(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = DividerLight,
    )
}

@Composable
fun ConnectedDevice(processor: MainProcessor) = Column {
    val selectedAdvertisement by processor.collectAsState { it.advertisement }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(padding24)
    ) {
        Text(
            text = "Selected device",
            style = Typography.h6,
            fontWeight = FontWeight.SemiBold,
            color = BlackTitle,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (selectedAdvertisement != null) {
                DeviceItem(
                    scannedDevice = selectedAdvertisement!!.toScannedDevice(),
                    action = {
                        processor.sendEvent(
                            MainEvent.EndConnectingToDevice
                        )
                    }
                )
            } else {
                LinearProgressBar()
            }
        }
    }
}

@Composable
fun ActualReading(processor: MainProcessor) = Column {
    val lastData by processor.collectAsState { it.lastDeviceReading }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(padding24)
    ) {
        if (lastData != null) {
            Text(
                text = "Actual data from device",
                style = Typography.h6,
                fontWeight = FontWeight.SemiBold,
                color = BlackTitle,
            )
            ReadingItem(
                bleData = lastData!!
            )
        } else {
            LinearProgressBar()
        }
    }
}

package com.untitledkingdom.ueberapp.feature.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tomcz.ellipse.common.collectAsState
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.feature.main.DividerGray
import com.untitledkingdom.ueberapp.feature.main.MainProcessor
import com.untitledkingdom.ueberapp.feature.main.state.MainEvent
import com.untitledkingdom.ueberapp.ui.common.LineGraphWithText
import com.untitledkingdom.ueberapp.ui.common.ReadingItem
import com.untitledkingdom.ueberapp.ui.common.Toolbar
import com.untitledkingdom.ueberapp.ui.common.isSynchronized
import com.untitledkingdom.ueberapp.ui.values.AppBackground
import com.untitledkingdom.ueberapp.ui.values.padding12
import com.untitledkingdom.ueberapp.ui.values.padding16
import com.untitledkingdom.ueberapp.ui.values.padding8
import com.untitledkingdom.ueberapp.utils.date.DateFormatter

@ExperimentalMaterialApi
@Composable
fun DetailsScreen(processor: MainProcessor) {
    Scaffold(topBar = {
        Toolbar(
            title = stringResource(R.string.main_close),
            action = {
                processor.sendEvent(
                    MainEvent.SetSelectedDate("")
                )
            }
        )
    }) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppBackground)
                .padding(horizontal = padding12)
                .padding(it)
        ) {
            val listState = rememberLazyListState()
            val selectedDate by processor.collectAsState { state -> state.selectedDate }
            val readings by processor.collectAsState { state ->
                state.values.filter { bleData ->
                    bleData.localDateTime.format(DateFormatter.dateDDMMMMYYYY) == selectedDate
                }.distinct().sortedBy { data -> data.id }
            }
            Chart(listState, readings)
            DividerGray()
            Readings(processor = processor, listState, readings)
        }
    }
}

@Composable
fun Chart(listState: LazyListState, readings: List<BleData>) {
    Column {
        LineGraphWithText(
            data = readings,
            modifier = Modifier.fillMaxWidth(),
            listState = listState,
        )
    }
}

@Composable
fun Readings(
    processor: MainProcessor,
    listState: LazyListState,
    readings: List<BleData>
) {
    val firstIdSend by processor.collectAsState { it.firstIdSend }
    val lastIdSend by processor.collectAsState { it.lastIdSend }
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = padding16),
        verticalArrangement = Arrangement.spacedBy(padding8),
        contentPadding = PaddingValues(bottom = padding16),
        state = listState
    ) {
        items(items = readings) { bleData ->
            ReadingItem(
                bleData = bleData,
                isSynchronized = isSynchronized(
                    firstIdSend = firstIdSend,
                    lastIdSend = lastIdSend,
                    id = bleData.id
                )
            )
        }
    }
}

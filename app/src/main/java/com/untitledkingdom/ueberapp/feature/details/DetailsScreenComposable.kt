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
import com.untitledkingdom.ueberapp.feature.main.DividerGray
import com.untitledkingdom.ueberapp.feature.main.MainProcessor
import com.untitledkingdom.ueberapp.feature.main.state.MainEvent
import com.untitledkingdom.ueberapp.ui.common.LineGraphWithText
import com.untitledkingdom.ueberapp.ui.common.ReadingItem
import com.untitledkingdom.ueberapp.ui.common.Toolbar
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
            Chart(processor = processor, listState)
            DividerGray()
            Readings(processor = processor, listState)
        }
    }
}

@Composable
fun Chart(processor: MainProcessor, listState: LazyListState) {
    val selectedDate by processor.collectAsState { it.selectedDate }
    val readings by processor.collectAsState {
        it.values.filter { bleData ->
            bleData.localDateTime.format(DateFormatter.dateDDMMMMYYYY) == selectedDate
        }.distinct()
    }
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
) {
    val selectedDate by processor.collectAsState { it.selectedDate }
    val valuesFilteredBySelectedDate by processor.collectAsState {
        it.values.filter { bleData ->
            bleData.localDateTime.format(DateFormatter.dateDDMMMMYYYY) == selectedDate
        }.distinct()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = padding16),
        verticalArrangement = Arrangement.spacedBy(padding8),
        contentPadding = PaddingValues(bottom = padding16),
        state = listState
    ) {
        items(items = valuesFilteredBySelectedDate) { bleData ->
            ReadingItem(
                bleData = bleData,
            )
        }
    }
}

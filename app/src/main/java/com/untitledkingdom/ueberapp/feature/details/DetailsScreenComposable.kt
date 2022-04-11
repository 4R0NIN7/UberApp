package com.untitledkingdom.ueberapp.feature.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.madrapps.plot.line.DataPoint
import com.tomcz.ellipse.common.collectAsState
import com.untitledkingdom.ueberapp.R
import com.untitledkingdom.ueberapp.feature.main.DividerGray
import com.untitledkingdom.ueberapp.feature.main.MainProcessor
import com.untitledkingdom.ueberapp.feature.main.state.MainEvent
import com.untitledkingdom.ueberapp.ui.common.LineGraphWithText
import com.untitledkingdom.ueberapp.ui.common.Toolbar
import com.untitledkingdom.ueberapp.ui.common.ValueItem
import com.untitledkingdom.ueberapp.ui.values.AppBackground
import com.untitledkingdom.ueberapp.ui.values.padding12
import com.untitledkingdom.ueberapp.ui.values.padding16
import com.untitledkingdom.ueberapp.ui.values.padding8
import com.untitledkingdom.ueberapp.utils.date.DateFormatter
import timber.log.Timber

@Composable
fun DetailsScreen(processor: MainProcessor) {
    Scaffold(topBar = {
        Toolbar(
            title = stringResource(R.string.main_close),
            action = { processor.sendEvent(MainEvent.GoBack) }
        )
    }) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppBackground)
                .padding(horizontal = padding12)
        ) {
            Chart(processor = processor)
            DividerGray()
            Values(processor = processor)
        }
    }
}

@Composable
fun Chart(processor: MainProcessor) {
    val selectedDate by processor.collectAsState { it.selectedDate }
    val readings by processor.collectAsState {
        it.values.filter { bleData ->
            bleData.localDateTime.format(DateFormatter.dateDDMMMMYYYY) == selectedDate
        }
    }
    val temperaturePoints = readings.map {
        DataPoint(it.id.toFloat(), it.data.temperature.toFloat())
    }
    val humidityPoints = readings.map {
        DataPoint(it.id.toFloat(), it.data.humidity.toFloat())
    }
    Column(verticalArrangement = Arrangement.SpaceBetween) {
        LineGraphWithText(
            lines = listOf(temperaturePoints, humidityPoints),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun Values(processor: MainProcessor) {
    val selectedDate by processor.collectAsState { it.selectedDate }
    val valuesFilteredBySelectedDate by processor.collectAsState {
        it.values.filter { bleData ->
            bleData.localDateTime.format(DateFormatter.dateDDMMMMYYYY) == selectedDate
        }
    }
    Timber.d("selectedDate $selectedDate")
    LazyColumn(
        modifier = Modifier
            .height(300.dp)
            .padding(horizontal = padding16)
            .padding(top = padding16),
        verticalArrangement = Arrangement.spacedBy(padding8),
        contentPadding = PaddingValues(bottom = padding16)
    ) {
        items(items = valuesFilteredBySelectedDate) { bleData ->
            ValueItem(
                bleData = bleData
            )
        }
    }
}

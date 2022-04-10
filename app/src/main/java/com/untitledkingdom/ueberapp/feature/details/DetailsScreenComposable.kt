package com.untitledkingdom.ueberapp.feature.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tomcz.ellipse.common.collectAsState
import com.untitledkingdom.ueberapp.feature.main.DividerGray
import com.untitledkingdom.ueberapp.feature.main.MainProcessor
import com.untitledkingdom.ueberapp.ui.common.ValueItem
import com.untitledkingdom.ueberapp.ui.values.AppBackground
import com.untitledkingdom.ueberapp.ui.values.RoomName1Color
import com.untitledkingdom.ueberapp.ui.values.RoomName2Color
import com.untitledkingdom.ueberapp.ui.values.padding12
import com.untitledkingdom.ueberapp.ui.values.padding16
import com.untitledkingdom.ueberapp.ui.values.padding8
import com.untitledkingdom.ueberapp.utils.date.DateFormatter
import me.bytebeats.views.charts.line.LineChart
import me.bytebeats.views.charts.line.LineChartData
import me.bytebeats.views.charts.line.render.line.SolidLineDrawer
import me.bytebeats.views.charts.line.render.xaxis.SimpleXAxisDrawer
import me.bytebeats.views.charts.line.render.yaxis.SimpleYAxisDrawer
import timber.log.Timber

@Composable
fun DetailsScreen(processor: MainProcessor) {
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

@Composable
fun Chart(processor: MainProcessor) {
    val readings by processor.collectAsState { it.values }
    val temperaturePoints = readings.map {
        LineChartData.Point(
            value = it.data.temperature.toFloat(), label = ""
        )
    }
    val humidityPoints = readings.map {
        LineChartData.Point(
            value = it.data.humidity.toFloat(),
            label = ""
        )
    }
    Column(verticalArrangement = Arrangement.SpaceBetween) {
        Box(
            modifier = Modifier.height(150.dp).fillMaxWidth()
        ) {
            LineChart(
                lineChartData = LineChartData(points = temperaturePoints),
                lineDrawer = SolidLineDrawer(color = RoomName1Color),
                xAxisDrawer = SimpleXAxisDrawer(),
                yAxisDrawer = SimpleYAxisDrawer(),
            )
        }
        Box(
            modifier = Modifier.height(150.dp).fillMaxWidth()
        ) {
            LineChart(
                lineChartData = LineChartData(points = humidityPoints),
                lineDrawer = SolidLineDrawer(color = RoomName2Color),
                xAxisDrawer = SimpleXAxisDrawer(),
                yAxisDrawer = SimpleYAxisDrawer(),
            )
        }
    }
}

@Composable
fun Values(processor: MainProcessor) {
    val selectedDate by processor.collectAsState { it.selectedDate }
    val valuesGroupedByDate by processor.collectAsState {
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
        items(items = valuesGroupedByDate) { bleData ->
            ValueItem(
                bleData = bleData
            )
        }
    }
}

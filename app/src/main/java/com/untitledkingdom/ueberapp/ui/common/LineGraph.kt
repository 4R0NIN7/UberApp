package com.untitledkingdom.ueberapp.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.madrapps.plot.line.DataPoint
import com.madrapps.plot.line.LineGraph
import com.madrapps.plot.line.LinePlot
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.ui.values.AppBackground
import com.untitledkingdom.ueberapp.ui.values.Black
import com.untitledkingdom.ueberapp.ui.values.GrayOsVersion
import com.untitledkingdom.ueberapp.ui.values.PurpleRed
import com.untitledkingdom.ueberapp.ui.values.RoomName3Color
import com.untitledkingdom.ueberapp.ui.values.White
import com.untitledkingdom.ueberapp.ui.values.padding8
import com.untitledkingdom.ueberapp.ui.values.signalBad
import com.untitledkingdom.ueberapp.ui.values.signalFull
import com.untitledkingdom.ueberapp.ui.values.signalGood
import com.untitledkingdom.ueberapp.utils.date.DateFormatter
import com.untitledkingdom.ueberapp.utils.functions.toPx
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@Composable
internal fun LineGraphWithText(
    data: List<DeviceReading>,
    modifier: Modifier,
    listState: LazyListState
) {
    val temperature = data.map {
        DataPoint(data.indexOf(it).toFloat(), it.reading.temperature)
    }
    val humidity = data.map {
        DataPoint(data.indexOf(it).toFloat(), it.reading.humidity.toFloat())
    }
    val totalWidth = remember { mutableStateOf(0) }
    Column(
        modifier = Modifier.onGloballyPositioned {
            totalWidth.value = it.size.width
        }
    ) {
        val xOffset = remember { mutableStateOf(100f) }
        val cardWidth = remember { mutableStateOf(0) }
        val visibility = remember { mutableStateOf(false) }
        val points = remember { mutableStateOf(listOf<DataPoint>()) }
        val coroutineScope = rememberCoroutineScope()
        val density = LocalDensity.current
        if (visibility.value) {
            Surface(
                modifier = Modifier
                    .width(200.dp)
                    .onGloballyPositioned {
                        cardWidth.value = it.size.width
                    }
                    .graphicsLayer(translationX = xOffset.value),
                shape = RoundedCornerShape(padding8),
                color = White
            ) {
                Column(
                    Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    val value = points.value
                    if (value.isNotEmpty()) {
                        val datePoint =
                            try {
                                data[value[0].x.toInt()].localDateTime.format(
                                    DateFormatter.dateDDMMMMYYYYHHMMSS
                                )
                            } catch (e: IndexOutOfBoundsException) {
                                "Unknown"
                            }
                        Text(
                            modifier = Modifier.padding(vertical = 8.dp),
                            text = "Reading on $datePoint",
                            style = MaterialTheme.typography.subtitle1,
                            color = Black
                        )
                        ScoreRow("Humidity", value[1].y, PurpleRed)
                        ScoreRow("Temperature", value[0].y, signalGood)
                    }
                }
            }
        }
        MaterialTheme(colors = MaterialTheme.colors.copy(surface = AppBackground)) {
            LineGraph(
                plot = LinePlot(
                    listOf(
                        LinePlot.Line(
                            dataPoints = temperature,
                            connection = LinePlot.Connection(signalGood, 2.dp),
                            intersection = LinePlot.Intersection(color = RoomName3Color),
                            highlight = LinePlot.Highlight { center ->
                                val color = signalGood
                                drawCircle(color, 9.dp.toPx(), center, alpha = 0.3f)
                                drawCircle(color, 6.dp.toPx(), center)
                                drawCircle(Color.Black, 3.dp.toPx(), center)
                            },
                        ),
                        LinePlot.Line(
                            dataPoints = humidity,
                            connection = LinePlot.Connection(color = signalFull, 1.dp),
                            intersection = LinePlot.Intersection(color = signalBad),
                            highlight = LinePlot.Highlight { center ->
                                val color = signalGood
                                drawCircle(color, 9.dp.toPx(), center, alpha = 0.3f)
                                drawCircle(color, 6.dp.toPx(), center)
                                drawCircle(Color.White, 3.dp.toPx(), center)
                            },
                            areaUnderLine = LinePlot.AreaUnderLine(color = GrayOsVersion)
                        ),
                    ),
                    grid = LinePlot.Grid(GrayOsVersion, steps = 8),
                    xAxis = LinePlot.XAxis(unit = 2f, roundToInt = true),
                    paddingRight = padding8,
                    paddingTop = padding8,
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .padding(horizontal = padding8),
                onSelectionStart = {
                    visibility.value = true
                },
                onSelectionEnd = {
                    visibility.value = false
                },
                onSelection = { x, pts ->
                    val cWidth = cardWidth.value.toFloat()
                    var xCenter = x + padding8.toPx(density)
                    xCenter = when {
                        xCenter + cWidth / 2f > totalWidth.value -> totalWidth.value - cWidth
                        xCenter - cWidth / 2f < 0f -> 0f
                        else -> xCenter - cWidth / 2f
                    }
                    xOffset.value = xCenter
                    points.value = pts
                    coroutineScope.launch {
                        val value = points.value
                        listState.animateScrollToItem(index = value[0].x.toInt())
                    }
                }
            )
        }
    }
}

@Composable
private fun ScoreRow(title: String, value: Float, color: Color) {
    val formatted = DecimalFormat("#.#").format(value)
    Box(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(modifier = Modifier.align(Alignment.CenterStart)) {
            Image(
                painter = ColorPainter(color),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 4.dp)
                    .size(10.dp)
                    .clip(RoundedCornerShape(padding8))
            )
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                color = Color.DarkGray
            )
        }
        Text(
            modifier = Modifier
                .padding(end = 8.dp)
                .align(Alignment.CenterEnd),
            text = formatted,
            style = MaterialTheme.typography.subtitle2,
            color = Color.DarkGray
        )
    }
}

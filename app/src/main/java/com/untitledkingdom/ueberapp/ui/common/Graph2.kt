package com.untitledkingdom.ueberapp.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.madrapps.plot.line.DataPoint
import com.madrapps.plot.line.LineGraph
import com.madrapps.plot.line.LinePlot
import com.untitledkingdom.ueberapp.ui.values.AppBackground
import com.untitledkingdom.ueberapp.ui.values.Black
import com.untitledkingdom.ueberapp.ui.values.RoomName1Color
import com.untitledkingdom.ueberapp.ui.values.RoomName2Color
import com.untitledkingdom.ueberapp.ui.values.White
import com.untitledkingdom.ueberapp.ui.values.padding8
import com.untitledkingdom.ueberapp.utils.functions.toPx
import java.text.DecimalFormat

@Composable
internal fun LineGraphWithText(lines: List<List<DataPoint>>, modifier: Modifier) {
    val totalWidth = remember { mutableStateOf(0) }
    Column(
        Modifier.onGloballyPositioned {
            totalWidth.value = it.size.width
        }
    ) {
        val xOffset = remember { mutableStateOf(0f) }
        val cardWidth = remember { mutableStateOf(0) }
        val visibility = remember { mutableStateOf(false) }
        val points = remember { mutableStateOf(listOf<DataPoint>()) }
        val density = LocalDensity.current
        Box(Modifier.height(150.dp)) {
            if (visibility.value) {
                Surface(
                    modifier = Modifier
                        .width(200.dp)
                        .align(Alignment.BottomCenter)
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
                            val x = DecimalFormat("#.#").format(value[0].x)
                            Text(
                                modifier = Modifier.padding(vertical = 8.dp),
                                text = "Score at $x:00 hrs",
                                style = MaterialTheme.typography.subtitle1,
                                color = Black
                            )
                            ScoreRow("Humidity", value[1].y, RoomName2Color)
                            ScoreRow("Temperature", value[0].y, RoomName1Color)
                        }
                    }
                }
            }
        }
        val padding = 16.dp
        val temperature = lines[0]
        val humidity = lines[1]
        MaterialTheme(colors = MaterialTheme.colors.copy(surface = AppBackground)) {
            LineGraph(
                plot = LinePlot(
                    listOf(
                        LinePlot.Line(
                            humidity,
                            LinePlot.Connection(RoomName2Color, 2.dp),
                            LinePlot.Intersection(),
                            LinePlot.Highlight { center ->
                                val color = RoomName2Color
                                drawCircle(color, 9.dp.toPx(), center, alpha = 0.3f)
                                drawCircle(color, 6.dp.toPx(), center)
                                drawCircle(Color.White, 3.dp.toPx(), center)
                            },
                        ),
                        LinePlot.Line(
                            temperature,
                            LinePlot.Connection(),
                            LinePlot.Intersection(),
                            LinePlot.Highlight { center ->
                                val color = RoomName1Color
                                drawCircle(color, 9.dp.toPx(), center, alpha = 0.3f)
                                drawCircle(color, 6.dp.toPx(), center)
                                drawCircle(Color.White, 3.dp.toPx(), center)
                            },
                            LinePlot.AreaUnderLine()
                        ),
                    ),
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = padding),
                onSelectionStart = { visibility.value = true },
                onSelectionEnd = { visibility.value = false }
            ) { x, pts ->
                val cWidth = cardWidth.value.toFloat()
                var xCenter = x + padding.toPx(density)
                xCenter = when {
                    xCenter + cWidth / 2f > totalWidth.value -> totalWidth.value - cWidth
                    xCenter - cWidth / 2f < 0f -> 0f
                    else -> xCenter - cWidth / 2f
                }
                xOffset.value = xCenter
                points.value = pts
            }
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
                painter = ColorPainter(color), contentDescription = "Line color",
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

@Preview(showBackground = true)
@Composable
fun LineGraphWithTextPreview() {
    LineGraphWithText(
        listOf(DataPoints.dataPoints1, DataPoints.dataPoints2),
        modifier = Modifier.fillMaxWidth()
    )
}

object DataPoints {
    val dataPoints1 = listOf(
        DataPoint(0f, 0f),
        DataPoint(1f, 20f),
        DataPoint(2f, 50f),
        DataPoint(3f, 10f),
        DataPoint(4f, 0f),
        DataPoint(5f, -25f),
        DataPoint(6f, -75f),
        DataPoint(7f, -100f),
        DataPoint(8f, -80f),
        DataPoint(9f, -75f),
        DataPoint(10f, -55f),
        DataPoint(11f, -45f),
        DataPoint(12f, 50f),
        DataPoint(13f, 80f),
        DataPoint(14f, 70f),
        DataPoint(15f, 125f),
        DataPoint(16f, 200f),
        DataPoint(17f, 170f),
        DataPoint(18f, 135f),
        DataPoint(19f, 60f),
        DataPoint(20f, 20f),
        DataPoint(21f, 40f),
        DataPoint(22f, 75f),
        DataPoint(23f, 50f),
    )

    val dataPoints2 = listOf(
        DataPoint(0f, 0f),
        DataPoint(1f, 0f),
        DataPoint(2f, 25f),
        DataPoint(3f, 75f),
        DataPoint(4f, 100f),
        DataPoint(5f, 80f),
        DataPoint(6f, 75f),
        DataPoint(7f, 50f),
        DataPoint(8f, 80f),
        DataPoint(9f, 70f),
        DataPoint(10f, 0f),
        DataPoint(11f, 0f),
        DataPoint(12f, 45f),
        DataPoint(13f, 20f),
        DataPoint(14f, 40f),
        DataPoint(15f, 75f),
        DataPoint(16f, 50f),
        DataPoint(17f, 75f),
        DataPoint(18f, 40f),
        DataPoint(19f, 20f),
        DataPoint(20f, 0f),
        DataPoint(21f, 0f),
        DataPoint(22f, 50f),
        DataPoint(23f, 25f),
    )
}

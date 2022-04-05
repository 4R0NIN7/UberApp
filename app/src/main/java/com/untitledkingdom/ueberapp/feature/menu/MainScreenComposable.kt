package com.untitledkingdom.ueberapp.feature.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.untitledkingdom.ueberapp.ui.Colors
import com.untitledkingdom.ueberapp.ui.Paddings.padding12
import com.untitledkingdom.ueberapp.ui.Paddings.padding16
import com.untitledkingdom.ueberapp.ui.Paddings.padding2
import com.untitledkingdom.ueberapp.ui.Paddings.padding24
import com.untitledkingdom.ueberapp.ui.Shapes.shape8

@ExperimentalPagerApi
@Composable
fun MainScreenCompose(processor: MyProcessor) {
    Scaffold(
        modifier = Modifier.padding(horizontal = padding12),
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
            modifier = Modifier.padding(horizontal = padding12),
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
fun DividerGray(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = Colors.SectionDividerLight,
    )
}

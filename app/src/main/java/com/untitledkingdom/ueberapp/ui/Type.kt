package com.untitledkingdom.ueberapp.ui

import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)

val h7: TextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    letterSpacing = 0.15.sp
)

val h7Normal: TextStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    letterSpacing = 0.15.sp
)

val textStyleToolBar: TextStyle = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    letterSpacing = 0.15.sp
)

val textStyleDayOfWeek = TextStyle(
    fontWeight = FontWeight.W600,
    letterSpacing = 0.38.sp,
    color = Color.Black,
    fontSize = fontSize14
)

val textStyleEventHoursOnline = TextStyle(
    fontSize = 14.sp,
    color = Colors.MediumGray,
)

val textStyleEventTitleOnline = TextStyle(
    fontSize = 16.sp,
    color = Colors.GrayCalendarItemText
)

val textStyleEventHours = TextStyle(
    fontSize = 14.sp,
    color = Colors.GrayCalendarItemText,
)

val textStyleEventTitle = TextStyle(
    fontSize = 16.sp,
    color = Colors.Black,
)

val textStyleEventDetails = TextStyle(
    fontSize = 18.sp,
    fontWeight = FontWeight.Medium,
    color = Colors.Black
)

val textStyleEventDetailsTitle = TextStyle(
    fontSize = 24.sp,
    fontWeight = FontWeight.Medium,
    color = Colors.Black
)

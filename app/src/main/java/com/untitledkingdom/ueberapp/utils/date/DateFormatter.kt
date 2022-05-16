package com.untitledkingdom.ueberapp.utils.date

import java.time.format.DateTimeFormatter
import java.util.*

object DateFormatter {
    val dateDDMMMMYYYY: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
    val dateDDMMMMYYYYHHMMSS: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH)
    val dateHHMMSS: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)
    val dateYYYYMMDD: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
}

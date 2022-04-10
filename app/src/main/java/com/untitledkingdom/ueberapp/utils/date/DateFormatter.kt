package com.untitledkingdom.ueberapp.utils.date

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object DateFormatter {
    val dateDDMMMMYYYY: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
    private val dateHHMMSS: DateTimeFormatter =
        DateTimeFormatter.ofPattern("hh:mm:ss", Locale.ENGLISH)

    fun convertToHHMMSS(basicDate: String): String {
        val parsedDate = LocalDateTime.parse(basicDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return parsedDate.format(dateHHMMSS)
    }
}

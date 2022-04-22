package com.untitledkingdom.ueberapp.utils.functions

import okhttp3.internal.and
import java.time.LocalDateTime

object DateConverter {
    fun toDateString(byteArray: ByteArray): String {
        val day = byteArray[0].toUByte()
        val month = byteArray[1].toUByte()
        val year = uBytesToYear(byteArray[3], byteArray[2])
        return "$day$month$year"
    }

    private fun uBytesToYear(high: Byte, low: Byte): Int {
        return high and 0xff shl 8 or (low and 0xff)
    }

    fun checkIfDateIsTheSame(dateFromDevice: String, date: LocalDateTime): Boolean {
        val dateFromLocalDateTime = "${date.dayOfMonth}${date.monthValue}${date.year}"
        return dateFromDevice == dateFromLocalDateTime
    }
}

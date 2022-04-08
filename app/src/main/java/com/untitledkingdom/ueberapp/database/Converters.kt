package com.untitledkingdom.ueberapp.database

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.untitledkingdom.ueberapp.feature.data.BleData
import java.time.LocalDateTime

class Converters {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @TypeConverter
    fun toDate(dateString: String?): LocalDateTime? {
        return LocalDateTime.parse(dateString)
    }

    @TypeConverter
    fun toDateString(date: LocalDateTime?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromBleData(value: BleData): String {
        val adapter = moshi.adapter(BleData::class.java)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toBleData(value: String): BleData {
        val adapter = moshi.adapter(BleData::class.java)
        return adapter.fromJson(value)!!
    }
}

package com.untitledkingdom.ueberapp.feature.date

import com.untitledkingdom.ueberapp.util.BaseCoroutineTest
import com.untitledkingdom.ueberapp.utils.functions.UtilFunctions
import com.untitledkingdom.ueberapp.utils.functions.toUByteArray
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.LocalDateTime

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
class DateTest : BaseCoroutineTest() {
    private val utilsFunctions = UtilFunctions
    private val localDateTime: LocalDateTime = LocalDateTime.of(
        1970,
        1,
        1,
        1,
        1,
        1
    )
    private val date = "111970"

    @Test
    fun `test date feature`() {
        val localDateTimeToByteArray = localDateTime.toUByteArray()
        val dateStringFromByteArray = utilsFunctions.toDateString(byteArray = localDateTimeToByteArray)
        assertTrue(dateStringFromByteArray == date)
        assertFalse(dateStringFromByteArray == "121970")
    }
}

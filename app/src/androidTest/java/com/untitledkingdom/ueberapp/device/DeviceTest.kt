package com.untitledkingdom.ueberapp.device

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.data.DeviceConst
import com.untitledkingdom.ueberapp.devices.data.DeviceDataStatus
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.utils.functions.UtilFunctions
import com.untitledkingdom.ueberapp.utils.functions.toUByteArray
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import java.time.LocalDateTime

@ExperimentalUnsignedTypes
@FlowPreview
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class DeviceTest {

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = StandardTestDispatcher()

    private val dataStorage by lazy { mockk<DataStorage>() }
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())
    private val device =
        spyk(Device(dataStorage = dataStorage, dispatcher = dispatcher, scope = scope))

    private val byteList = listOf(1.toByte(), 2.toByte())
    private val deviceReading = mockk<DeviceReading>()

    private val localDateTime: LocalDateTime = LocalDateTime.of(
        1970,
        1,
        1,
        1,
        1,
        1
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun readDateFromDevice(): Unit = runTest {
        coEvery { device.readDate(any(), any()) } returns DeviceDataStatus.SuccessRetrievingDate(
            byteList
        )
        val deviceStatus = device.readDate(
            DeviceConst.SERVICE_TIME_SETTINGS,
            DeviceConst.TIME_CHARACTERISTIC
        )
        coVerify {
            device.readDate(
                DeviceConst.SERVICE_TIME_SETTINGS,
                DeviceConst.TIME_CHARACTERISTIC
            )
        }
        confirmVerified(device)
        assertTrue(deviceStatus == DeviceDataStatus.SuccessRetrievingDate(byteList))
    }

    @Test
    fun writeDateToDevice(): Unit = runTest {
        coEvery { device.write(any(), any(), any()) } returns Unit
        device.write(
            byteArrayOf(),
            DeviceConst.SERVICE_TIME_SETTINGS,
            DeviceConst.TIME_CHARACTERISTIC
        )
        coVerify {
            device.write(
                byteArrayOf(),
                DeviceConst.SERVICE_TIME_SETTINGS,
                DeviceConst.TIME_CHARACTERISTIC
            )
        }
        confirmVerified(device)
    }

    @Test
    fun readDateFromDeviceAndWriteDateToDevice(): Unit = runTest {
        val uByteArray = localDateTime.toUByteArray()
        val dateString = UtilFunctions.toDateString(uByteArray)
        coEvery {
            device.write(
                uByteArray, DeviceConst.SERVICE_TIME_SETTINGS,
                DeviceConst.TIME_CHARACTERISTIC
            )
        } returns Unit
        coEvery {
            device.readDate(
                DeviceConst.SERVICE_TIME_SETTINGS,
                DeviceConst.TIME_CHARACTERISTIC
            )
        } returns DeviceDataStatus.SuccessRetrievingDate(uByteArray.toList())
        device.readDate(
            DeviceConst.SERVICE_TIME_SETTINGS,
            DeviceConst.TIME_CHARACTERISTIC
        )
        device.write(
            uByteArray,
            DeviceConst.SERVICE_TIME_SETTINGS,
            DeviceConst.TIME_CHARACTERISTIC
        )
        coVerify {
            device.readDate(
                DeviceConst.SERVICE_TIME_SETTINGS,
                DeviceConst.TIME_CHARACTERISTIC
            )
            device.write(
                uByteArray,
                DeviceConst.SERVICE_TIME_SETTINGS,
                DeviceConst.TIME_CHARACTERISTIC
            )
        }
        confirmVerified(device)
        assertTrue(
            UtilFunctions.checkIfDateIsTheSame(
                dateFromDevice = dateString,
                date = localDateTime
            )
        )
    }

    @Test
    fun observeDataFromDevice(): Unit = runTest {
        coEvery { device.observationOnDataCharacteristic() } returns flowOf(deviceReading)
        val reading = device.observationOnDataCharacteristic()
        coVerify {
            device.observationOnDataCharacteristic()
        }
        val differentReading = flowOf(DeviceReading(1f, 2))
        confirmVerified(device)
        assertNotEquals(differentReading, reading)
        assertEquals(deviceReading, reading.first())
    }
}

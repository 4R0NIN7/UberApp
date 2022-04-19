package com.untitledkingdom.ueberapp.device

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConstants
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.DeviceConst
import com.untitledkingdom.ueberapp.devices.DeviceDataStatus
import com.untitledkingdom.ueberapp.utils.functions.checkIfDateIsTheSame
import com.untitledkingdom.ueberapp.utils.functions.toDateString
import com.untitledkingdom.ueberapp.utils.functions.toUByteArray
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
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

    private lateinit var dispatcherProvider: TestDispatcherProvider

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = StandardTestDispatcher()

    private val dataStorage by lazy { mockk<DataStorage>() }
    private val device = spyk(Device(dataStorage))

    private val byteList = listOf(1.toByte(), 2.toByte())
    private val macAddress = "00:11:22:33:AA:BB"

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
        dispatcherProvider = TestDispatcherProvider(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun readDateFromDevice(): Unit = runTest {
        coEvery { dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS) } returns macAddress
        coEvery { device.readDate(any(), any()) } returns DeviceDataStatus.SuccessDate(byteList)
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
        assertTrue(deviceStatus == DeviceDataStatus.SuccessDate(byteList))
    }

    @Test
    fun writeDateToDevice(): Unit = runTest {
        coEvery { dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS) } returns macAddress
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
        val dateString = toDateString(uByteArray)
        coEvery { dataStorage.getFromStorage(DataStorageConstants.MAC_ADDRESS) } returns macAddress
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
        } returns DeviceDataStatus.SuccessDate(uByteArray.toList())
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
        assertTrue(checkIfDateIsTheSame(dateFromDevice = dateString, date = localDateTime))
    }
}

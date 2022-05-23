package com.untitledkingdom.ueberapp.utils.interval

import com.untitledkingdom.ueberapp.feature.welcome.WelcomeConst
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface FlowInterval {
    fun start(delay: Long = WelcomeConst.REFRESH_ADVERTISEMENTS): Flow<Any>
}

class FlowIntervalImpl @Inject constructor() : FlowInterval {
    override fun start(delay: Long): Flow<Any> = flow {
        while (true) {
            emit(Any())
            delay(delay)
        }
    }.cancellable().buffer()
}

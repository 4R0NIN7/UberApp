package com.untitledkingdom.ueberapp.feature

interface Repository {
    suspend fun saveToDataBase(value: String)
    fun sendData()
}

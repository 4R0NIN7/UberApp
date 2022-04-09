package com.untitledkingdom.ueberapp.datastore

interface DataStorage {
    suspend fun saveToStorage(key: String, value: String)
    suspend fun getFromStorage(key: String): String
}

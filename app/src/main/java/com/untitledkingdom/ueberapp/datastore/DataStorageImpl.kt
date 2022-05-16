package com.untitledkingdom.ueberapp.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStorageImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val security: SecureData
) : DataStorage {
    override suspend fun saveToStorage(key: String, value: String) {
        val encryptedValue = security.encrypt(value)
        dataStore.edit { dataStore ->
            dataStore[stringPreferencesKey(key)] = encryptedValue ?: ""
        }
    }

    override suspend fun getFromStorage(key: String): String {
        val valueFromStorage = dataStore.data.map { dataStore ->
            dataStore[stringPreferencesKey(key)] ?: ""
        }
        val decryptedValue = security.decrypt(valueFromStorage.first())
        return decryptedValue ?: ""
    }

    override fun observeMacAddress(): Flow<String> = dataStore.data.map { dataStore ->
        val valueFromStorage = dataStore[stringPreferencesKey(DataStorageConst.MAC_ADDRESS)] ?: ""
        val decryptedValue = security.decrypt(valueFromStorage)
        decryptedValue ?: ""
    }
}

package com.saadahmedev.videoplayer.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.saadahmedev.videoplayer.BuildConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class DataStoreManager(private val context: Context) {

    private companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(BuildConfig.APPLICATION_ID)
    }

    private val gson = Gson()

    suspend fun saveStringList(key: String, list: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = gson.toJson(list)
        }
    }

    fun getStringList(key: String): List<String> {
        return runBlocking {
            val prefKey = stringPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            val jsonList = preferences[prefKey] ?: "[]"

            gson.fromJson(jsonList, Array<String>::class.java).toList()
        }
    }

    suspend fun saveLong(key: String?, value: Long) {
        if (key == null) return
        val prefKey = longPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return runBlocking {
            val prefKey = longPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            preferences[prefKey] ?: defaultValue
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
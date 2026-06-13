package com.paoloesan.lntranslator_mobile.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings_prefs",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "settings_prefs"))
    }
)

object DataStoreManager {
    @Volatile
    private var cachedPreferences: Preferences? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isCollecting = false

    private fun ensureCacheInitialized(context: Context) {
        if (cachedPreferences == null) {
            synchronized(this) {
                if (cachedPreferences == null) {
                    cachedPreferences = runBlocking {
                        context.applicationContext.dataStore.data.first()
                    }
                    if (!isCollecting) {
                        isCollecting = true
                        scope.launch {
                            context.applicationContext.dataStore.data.collect { prefs ->
                                cachedPreferences = prefs
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Sync Getters ---
    fun getString(context: Context, key: String, defaultValue: String? = null): String? {
        ensureCacheInitialized(context)
        return cachedPreferences?.get(stringPreferencesKey(key)) ?: defaultValue
    }

    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int {
        ensureCacheInitialized(context)
        return cachedPreferences?.get(intPreferencesKey(key)) ?: defaultValue
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        ensureCacheInitialized(context)
        return cachedPreferences?.get(booleanPreferencesKey(key)) ?: defaultValue
    }

    // --- Flows ---
    fun getStringFlow(context: Context, key: String, defaultValue: String? = null): Flow<String?> {
        return context.applicationContext.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey(key)] ?: defaultValue
        }
    }

    fun getIntFlow(context: Context, key: String, defaultValue: Int = 0): Flow<Int> {
        return context.applicationContext.dataStore.data.map { prefs ->
            prefs[intPreferencesKey(key)] ?: defaultValue
        }
    }

    fun getBooleanFlow(context: Context, key: String, defaultValue: Boolean = false): Flow<Boolean> {
        return context.applicationContext.dataStore.data.map { prefs ->
            prefs[booleanPreferencesKey(key)] ?: defaultValue
        }
    }

    // --- Setters (Suspend & Sync variants) ---
    suspend fun putString(context: Context, key: String, value: String?) {
        context.applicationContext.dataStore.edit { prefs ->
            val prefKey = stringPreferencesKey(key)
            if (value != null) {
                prefs[prefKey] = value
            } else {
                prefs.remove(prefKey)
            }
        }
    }

    fun putStringSync(context: Context, key: String, value: String?) {
        runBlocking {
            putString(context, key, value)
        }
    }

    suspend fun putInt(context: Context, key: String, value: Int) {
        context.applicationContext.dataStore.edit { prefs ->
            prefs[intPreferencesKey(key)] = value
        }
    }

    fun putIntSync(context: Context, key: String, value: Int) {
        runBlocking {
            putInt(context, key, value)
        }
    }

    suspend fun putBoolean(context: Context, key: String, value: Boolean) {
        context.applicationContext.dataStore.edit { prefs ->
            prefs[booleanPreferencesKey(key)] = value
        }
    }

    fun putBooleanSync(context: Context, key: String, value: Boolean) {
        runBlocking {
            putBoolean(context, key, value)
        }
    }

    suspend fun remove(context: Context, key: String) {
        context.applicationContext.dataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey(key))
            prefs.remove(intPreferencesKey(key))
            prefs.remove(booleanPreferencesKey(key))
        }
    }

    fun removeSync(context: Context, key: String) {
        runBlocking {
            remove(context, key)
        }
    }
}

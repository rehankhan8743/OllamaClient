package com.rehan.ollamaclient.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DEFAULT_SERVER_ID = longPreferencesKey("default_server_id")
        val DEFAULT_MODEL = stringPreferencesKey("default_model")
        val DEFAULT_NUM_CTX = intPreferencesKey("default_num_ctx")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "system" }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[DYNAMIC_COLOR] ?: true }
    val defaultServerId: Flow<Long> = context.dataStore.data.map { it[DEFAULT_SERVER_ID] ?: -1L }
    val defaultModel: Flow<String> = context.dataStore.data.map { it[DEFAULT_MODEL] ?: "" }
    val defaultNumCtx: Flow<Int> = context.dataStore.data.map { it[DEFAULT_NUM_CTX] ?: 4096 }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[DYNAMIC_COLOR] = enabled }
    }

    suspend fun setDefaultServerId(id: Long) {
        context.dataStore.edit { it[DEFAULT_SERVER_ID] = id }
    }

    suspend fun setDefaultModel(model: String) {
        context.dataStore.edit { it[DEFAULT_MODEL] = model }
    }

    suspend fun setDefaultNumCtx(numCtx: Int) {
        context.dataStore.edit { it[DEFAULT_NUM_CTX] = numCtx }
    }
}

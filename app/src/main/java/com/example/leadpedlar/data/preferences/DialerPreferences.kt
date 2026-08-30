package com.example.leadpedlar.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.leadpedlar.data.model.CallAppType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dialerDataStore by preferencesDataStore(name = "leadpedlar_dialer_prefs")

class DialerPreferences(private val context: Context) {

    companion object {
        val KEY_DEFAULT_APP_ID = stringPreferencesKey("default_call_app_id")
        val KEY_ALWAYS_ASK = booleanPreferencesKey("always_ask_before_calling")
        val KEY_LAST_USED_APP_ID = stringPreferencesKey("last_used_call_app_id")
    }

    val preferredAppFlow: Flow<CallAppType> = context.dialerDataStore.data.map { prefs ->
        val appId = prefs[KEY_DEFAULT_APP_ID]
        CallAppType.fromId(appId)
    }

    val alwaysAskFlow: Flow<Boolean> = context.dialerDataStore.data.map { prefs ->
        prefs[KEY_ALWAYS_ASK] ?: true
    }

    val lastUsedAppFlow: Flow<CallAppType> = context.dialerDataStore.data.map { prefs ->
        val appId = prefs[KEY_LAST_USED_APP_ID]
        CallAppType.fromId(appId)
    }

    suspend fun getPreferredApp(): CallAppType {
        val prefs = context.dialerDataStore.data.first()
        val alwaysAsk = prefs[KEY_ALWAYS_ASK] ?: true
        if (alwaysAsk) {
            return CallAppType.SYSTEM_CHOOSER
        }
        return CallAppType.fromId(prefs[KEY_DEFAULT_APP_ID])
    }

    suspend fun saveDefaultApp(appType: CallAppType, alwaysUseThisApp: Boolean) {
        context.dialerDataStore.edit { prefs ->
            prefs[KEY_LAST_USED_APP_ID] = appType.id
            if (alwaysUseThisApp && appType != CallAppType.SYSTEM_CHOOSER) {
                prefs[KEY_DEFAULT_APP_ID] = appType.id
                prefs[KEY_ALWAYS_ASK] = false
            } else {
                prefs[KEY_ALWAYS_ASK] = true
            }
        }
    }

    suspend fun resetToAlwaysAsk() {
        context.dialerDataStore.edit { prefs ->
            prefs[KEY_DEFAULT_APP_ID] = CallAppType.SYSTEM_CHOOSER.id
            prefs[KEY_ALWAYS_ASK] = true
        }
    }
}

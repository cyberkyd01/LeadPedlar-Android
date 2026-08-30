package com.example.leadpedlar.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.leadpedlar.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appDataStore by preferencesDataStore(name = "leadpedlar_app_prefs")

class AppPreferences(private val context: Context) {

    companion object {
        val KEY_SERVER_URL = stringPreferencesKey("server_url")
        val KEY_SESSION_TOKEN = stringPreferencesKey("session_token")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        const val DEFAULT_SERVER_URL = "https://www.leadpedlar.xyz"
    }

    val serverUrlFlow: Flow<String> = context.appDataStore.data.map { prefs ->
        val saved = prefs[KEY_SERVER_URL]
        if (saved.isNullOrBlank() || saved.contains("10.0.2.2")) {
            DEFAULT_SERVER_URL
        } else {
            saved
        }
    }

    val isLoggedInFlow: Flow<Boolean> = context.appDataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val userFlow: Flow<User> = context.appDataStore.data.map { prefs ->
        User(
            email = prefs[KEY_USER_EMAIL] ?: "",
            role = prefs[KEY_USER_ROLE] ?: "AGENT",
            name = prefs[KEY_USER_NAME] ?: ""
        )
    }

    val sessionTokenFlow: Flow<String?> = context.appDataStore.data.map { prefs ->
        prefs[KEY_SESSION_TOKEN]
    }

    suspend fun saveServerUrl(url: String) {
        context.appDataStore.edit { prefs ->
            prefs[KEY_SERVER_URL] = url
        }
    }

    suspend fun saveAuthSession(token: String, user: User) {
        context.appDataStore.edit { prefs ->
            prefs[KEY_SESSION_TOKEN] = token
            prefs[KEY_USER_EMAIL] = user.email
            prefs[KEY_USER_ROLE] = user.role
            prefs[KEY_USER_NAME] = user.name ?: ""
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun clearSession() {
        context.appDataStore.edit { prefs ->
            prefs.remove(KEY_SESSION_TOKEN)
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_USER_ROLE)
            prefs.remove(KEY_USER_NAME)
            prefs[KEY_IS_LOGGED_IN] = false
        }
    }
}

package com.example.artsy_mobile_app

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.json.Json
import androidx.core.content.edit

object UserSessionManager {
    private const val PREFS_NAME = "user_session"
    private const val KEY_USER = "user"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private lateinit var preferences: SharedPreferences
    private val json = Json { ignoreUnknownKeys = true }

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(user: User, accessToken: String, refreshToken: String) {
        preferences.edit {
            putString(KEY_USER, json.encodeToString(user))
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
        }
    }

    fun getUser(): User? {
        val userJson = preferences.getString(KEY_USER, null) ?: return null
        return runCatching { json.decodeFromString<User>(userJson) }.getOrNull()
    }

    fun getAccessToken(): String? = preferences.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = preferences.getString(KEY_REFRESH_TOKEN, null)

    fun clearSession() {
        preferences.edit { clear() }
    }

    fun isLoggedIn(): Boolean = getAccessToken() != null
}
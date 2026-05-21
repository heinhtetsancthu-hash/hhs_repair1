package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_URL = "api_url"
        private const val KEY_USE_MOCK_CLOUD = "use_mock_cloud"
        private const val KEY_LAST_SYNC = "last_sync_time"
        private const val KEY_OFFLINE_MODE = "offline_mode"
        private const val KEY_SELECTED_ERROR_TYPES = "error_types"
    }

    var apiUrl: String
        get() = prefs.getString(KEY_API_URL, "https://api.mobilerepair.example.com/v1") ?: "https://api.mobilerepair.example.com/v1"
        set(value) = prefs.edit().putString(KEY_API_URL, value).apply()

    var useMockCloud: Boolean
        get() = prefs.getBoolean(KEY_USE_MOCK_CLOUD, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_MOCK_CLOUD, value).apply()

    var lastSyncTime: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC, value).apply()

    var isForceOffline: Boolean
        get() = prefs.getBoolean(KEY_OFFLINE_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_OFFLINE_MODE, value).apply()

    var errorTypes: Set<String>
        get() = prefs.getStringSet(KEY_SELECTED_ERROR_TYPES, setOf("Screen Damage", "Battery Issue", "Charging Port", "Software Bug", "Water Damage", "Button Fault", "Camera Repair", "Other")) ?: setOf("Screen Damage", "Battery Issue", "Charging Port", "Software Bug", "Water Damage", "Button Fault", "Camera Repair", "Other")
        set(value) = prefs.edit().putStringSet(KEY_SELECTED_ERROR_TYPES, value).apply()

    fun clearSyncTime() {
        prefs.edit().remove(KEY_LAST_SYNC).apply()
    }
}

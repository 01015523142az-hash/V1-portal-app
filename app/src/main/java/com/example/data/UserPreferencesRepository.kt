package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Calendar
import java.util.Locale

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class QuietHoursSettings(
    val isEnabled: Boolean = false,
    val startHour: Int = 22, // 10:00 PM default
    val startMinute: Int = 0,
    val endHour: Int = 7, // 7:00 AM default
    val endMinute: Int = 0
) {
    fun isCurrentlyInQuietHours(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        if (!isEnabled) return false
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute

        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes..endMinutes
        } else {
            // Overnight interval (e.g. 22:00 -> 07:00)
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }

    fun formattedTimeRange(): String {
        val startFormatted = formatHourMinute(startHour, startMinute)
        val endFormatted = formatHourMinute(endHour, endMinute)
        return "$startFormatted – $endFormatted"
    }

    companion object {
        fun formatHourMinute(hour: Int, minute: Int): String {
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            return String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm)
        }
    }
}

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme_enabled")
        val KEY_THEME_PRESET = stringPreferencesKey("theme_preset_id")
        val KEY_FONT_PRESET = stringPreferencesKey("font_preset_id")
        val KEY_AUTO_BIOMETRIC = booleanPreferencesKey("auto_biometric_login")
        val KEY_SAVED_USER_EMAIL = stringPreferencesKey("saved_user_email")
        val KEY_LEADS_MAP_VIEW = booleanPreferencesKey("leads_map_view_enabled")
        val KEY_QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val KEY_QUIET_HOURS_START_HOUR = intPreferencesKey("quiet_hours_start_hour")
        val KEY_QUIET_HOURS_START_MINUTE = intPreferencesKey("quiet_hours_start_minute")
        val KEY_QUIET_HOURS_END_HOUR = intPreferencesKey("quiet_hours_end_hour")
        val KEY_QUIET_HOURS_END_MINUTE = intPreferencesKey("quiet_hours_end_minute")
        val KEY_LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        val KEY_SIMULATE_OFFLINE = booleanPreferencesKey("simulate_offline_mode")
    }

    val isDarkThemeFlow: Flow<Boolean> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_DARK_THEME] ?: true
        }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_DARK_THEME] = isDark
        }
    }

    val themePresetFlow: Flow<String> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_THEME_PRESET] ?: "proptech_sapphire"
        }

    suspend fun setThemePreset(presetId: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_THEME_PRESET] = presetId
        }
    }

    val fontPresetFlow: Flow<String> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_FONT_PRESET] ?: "modern_sans"
        }

    suspend fun setFontPreset(fontId: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_FONT_PRESET] = fontId
        }
    }

    val autoBiometricLoginFlow: Flow<Boolean> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_AUTO_BIOMETRIC] ?: true
        }

    suspend fun setAutoBiometricLogin(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_AUTO_BIOMETRIC] = enabled
        }
    }

    val isLeadsMapViewFlow: Flow<Boolean> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_LEADS_MAP_VIEW] ?: false
        }

    suspend fun setLeadsMapView(isMap: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_LEADS_MAP_VIEW] = isMap
        }
    }

    val quietHoursSettingsFlow: Flow<QuietHoursSettings> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            QuietHoursSettings(
                isEnabled = preferences[KEY_QUIET_HOURS_ENABLED] ?: false,
                startHour = preferences[KEY_QUIET_HOURS_START_HOUR] ?: 22,
                startMinute = preferences[KEY_QUIET_HOURS_START_MINUTE] ?: 0,
                endHour = preferences[KEY_QUIET_HOURS_END_HOUR] ?: 7,
                endMinute = preferences[KEY_QUIET_HOURS_END_MINUTE] ?: 0
            )
        }

    suspend fun setQuietHoursSettings(settings: QuietHoursSettings) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_QUIET_HOURS_ENABLED] = settings.isEnabled
            preferences[KEY_QUIET_HOURS_START_HOUR] = settings.startHour
            preferences[KEY_QUIET_HOURS_START_MINUTE] = settings.startMinute
            preferences[KEY_QUIET_HOURS_END_HOUR] = settings.endHour
            preferences[KEY_QUIET_HOURS_END_MINUTE] = settings.endMinute
        }
    }

    suspend fun getQuietHoursSettingsSync(): QuietHoursSettings {
        return try {
            quietHoursSettingsFlow.first()
        } catch (_: Exception) {
            QuietHoursSettings()
        }
    }

    val lastSyncTimestampFlow: Flow<Long> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] ?: (System.currentTimeMillis() - 4 * 60 * 1000)
        }

    suspend fun setLastSyncTimestamp(timestamp: Long) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    val isSimulatedOfflineFlow: Flow<Boolean> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_SIMULATE_OFFLINE] ?: false
        }

    suspend fun setSimulatedOffline(isOffline: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_SIMULATE_OFFLINE] = isOffline
        }
    }
}

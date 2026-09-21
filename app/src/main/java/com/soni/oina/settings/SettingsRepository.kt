package com.soni.oina.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "oina_settings")

enum class ThemeMode { DARK, LIGHT, SYSTEM }

enum class LensFacingPref { FRONT, BACK }

/**
 * Immutable snapshot of all user-configurable settings.
 */
data class OinaSettings(
    val mirrorPreview: Boolean = true,
    val saveMirroredPhoto: Boolean = true,
    val defaultCameraFront: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val keepScreenOn: Boolean = true,
    val screenBrightness: Float = 1.0f
)

/**
 * Local-only persistence for OINA. Backed entirely by DataStore Preferences
 * (a private file on-device). No network, no cloud sync, no third-party SDK.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val MIRROR_PREVIEW = booleanPreferencesKey("mirror_preview")
        val SAVE_MIRRORED = booleanPreferencesKey("save_mirrored_photo")
        val DEFAULT_CAMERA_FRONT = booleanPreferencesKey("default_camera_front")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val SCREEN_BRIGHTNESS = floatPreferencesKey("screen_brightness")
        // Reserved for a future persisted zoom default; not required by current UI.
        val ZOOM_PERCENT = intPreferencesKey("zoom_percent")
    }

    val settingsFlow: Flow<OinaSettings> = context.dataStore.data.map { prefs ->
        OinaSettings(
            mirrorPreview = prefs[Keys.MIRROR_PREVIEW] ?: true,
            saveMirroredPhoto = prefs[Keys.SAVE_MIRRORED] ?: true,
            defaultCameraFront = prefs[Keys.DEFAULT_CAMERA_FRONT] ?: true,
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.DARK,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
            screenBrightness = prefs[Keys.SCREEN_BRIGHTNESS] ?: 1.0f
        )
    }

    suspend fun setMirrorPreview(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MIRROR_PREVIEW] = enabled }
    }

    suspend fun setSaveMirroredPhoto(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SAVE_MIRRORED] = enabled }
    }

    suspend fun setDefaultCameraFront(front: Boolean) {
        context.dataStore.edit { it[Keys.DEFAULT_CAMERA_FRONT] = front }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }

    suspend fun setScreenBrightness(value: Float) {
        context.dataStore.edit { it[Keys.SCREEN_BRIGHTNESS] = value.coerceIn(0.1f, 1.0f) }
    }
}

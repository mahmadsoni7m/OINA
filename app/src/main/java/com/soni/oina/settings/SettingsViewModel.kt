package com.soni.oina.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application.applicationContext)

    val settings: StateFlow<OinaSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OinaSettings()
    )

    fun setMirrorPreview(enabled: Boolean) = viewModelScope.launch {
        repository.setMirrorPreview(enabled)
    }

    fun setSaveMirroredPhoto(enabled: Boolean) = viewModelScope.launch {
        repository.setSaveMirroredPhoto(enabled)
    }

    fun setDefaultCameraFront(front: Boolean) = viewModelScope.launch {
        repository.setDefaultCameraFront(front)
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        repository.setThemeMode(mode)
    }

    fun setKeepScreenOn(enabled: Boolean) = viewModelScope.launch {
        repository.setKeepScreenOn(enabled)
    }

    fun setScreenBrightness(value: Float) = viewModelScope.launch {
        repository.setScreenBrightness(value)
    }
}

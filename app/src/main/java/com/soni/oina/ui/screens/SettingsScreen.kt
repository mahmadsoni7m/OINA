package com.soni.oina.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.soni.oina.BuildConfig
import com.soni.oina.R
import com.soni.oina.settings.OinaSettings
import com.soni.oina.settings.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: OinaSettings,
    onBack: () -> Unit,
    onMirrorPreviewChange: (Boolean) -> Unit,
    onSaveMirroredChange: (Boolean) -> Unit,
    onDefaultCameraFrontChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onBrightnessChange: (Float) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item { SectionHeader(stringResource(R.string.section_camera)) }
            item {
                SwitchRow(
                    title = stringResource(R.string.setting_mirror_preview),
                    subtitle = stringResource(R.string.setting_mirror_preview_desc),
                    checked = settings.mirrorPreview,
                    onCheckedChange = onMirrorPreviewChange
                )
            }
            item {
                SwitchRow(
                    title = stringResource(R.string.setting_save_mirrored),
                    subtitle = stringResource(R.string.setting_save_mirrored_desc),
                    checked = settings.saveMirroredPhoto,
                    onCheckedChange = onSaveMirroredChange
                )
            }
            item {
                SwitchRow(
                    title = stringResource(R.string.setting_default_camera),
                    subtitle = if (settings.defaultCameraFront)
                        stringResource(R.string.setting_default_camera_front)
                    else stringResource(R.string.setting_default_camera_back),
                    checked = settings.defaultCameraFront,
                    onCheckedChange = onDefaultCameraFrontChange
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { SectionHeader(stringResource(R.string.section_appearance)) }
            item {
                ThemeSelector(
                    selected = settings.themeMode,
                    onSelect = onThemeModeChange
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { SectionHeader(stringResource(R.string.section_display)) }
            item {
                SwitchRow(
                    title = stringResource(R.string.setting_keep_screen_on),
                    subtitle = stringResource(R.string.setting_keep_screen_on_desc),
                    checked = settings.keepScreenOn,
                    onCheckedChange = onKeepScreenOnChange
                )
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.setting_brightness),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Slider(
                        value = settings.screenBrightness,
                        onValueChange = onBrightnessChange,
                        valueRange = 0.1f..1.0f
                    )
                }
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { SectionHeader(stringResource(R.string.section_about)) }
            item { AboutRow(stringResource(R.string.about_developer), stringResource(R.string.app_developer)) }
            item { AboutRow(stringResource(R.string.about_version), BuildConfig.VERSION_NAME) }
            item { AboutRow(stringResource(R.string.about_description), stringResource(R.string.app_description)) }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .wrapContentHeight(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun ThemeSelector(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val options = listOf(
        ThemeMode.DARK to stringResource(R.string.theme_dark),
        ThemeMode.LIGHT to stringResource(R.string.theme_light),
        ThemeMode.SYSTEM to stringResource(R.string.theme_system)
    )
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        options.forEach { (mode, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.RadioButton(
                    selected = selected == mode,
                    onClick = { onSelect(mode) },
                    colors = androidx.compose.material3.RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

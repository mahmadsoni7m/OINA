package com.soni.oina.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.soni.oina.settings.SettingsViewModel
import com.soni.oina.ui.screens.MirrorScreen
import com.soni.oina.ui.screens.SettingsScreen

private object Routes {
    const val MIRROR = "mirror"
    const val SETTINGS = "settings"
}

@Composable
fun OinaNavHost(
    settingsViewModel: SettingsViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val settings by settingsViewModel.settings.collectAsState()

    NavHost(navController = navController, startDestination = Routes.MIRROR) {
        composable(Routes.MIRROR) {
            MirrorScreen(
                settings = settings,
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                onBack = { navController.popBackStack() },
                onMirrorPreviewChange = settingsViewModel::setMirrorPreview,
                onSaveMirroredChange = settingsViewModel::setSaveMirroredPhoto,
                onDefaultCameraFrontChange = settingsViewModel::setDefaultCameraFront,
                onThemeModeChange = settingsViewModel::setThemeMode,
                onKeepScreenOnChange = settingsViewModel::setKeepScreenOn,
                onBrightnessChange = settingsViewModel::setScreenBrightness
            )
        }
    }
}

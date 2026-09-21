package com.soni.oina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soni.oina.navigation.OinaNavHost
import com.soni.oina.settings.SettingsViewModel
import com.soni.oina.ui.theme.OinaTheme

/**
 * Single-activity host. All UI lives in Compose; MainActivity itself only
 * wires up edge-to-edge display and the OINA theme.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settings by settingsViewModel.settings.collectAsState()

            OinaTheme(themeMode = settings.themeMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                ) {
                    OinaNavHost(settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}

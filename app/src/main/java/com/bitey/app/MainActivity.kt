package com.bitey.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.bitey.app.core.ui.theme.BiteyTheme
import com.bitey.app.core.ui.theme.ThemeMode
import com.bitey.app.core.ui.theme.ThemePreferences
import com.bitey.app.ui.BiteyApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var themeMode by remember {
                mutableStateOf(ThemePreferences.getThemeMode(this))
            }

            BiteyTheme(
                themeMode = themeMode,
                onThemeModeChanged = { newMode ->
                    themeMode = newMode
                    ThemePreferences.setThemeMode(this, newMode)
                }
            ) {
                BiteyApp()
            }
        }
    }
}

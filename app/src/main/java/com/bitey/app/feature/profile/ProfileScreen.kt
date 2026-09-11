package com.bitey.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.LocalOnThemeModeChanged
import com.bitey.app.core.ui.theme.LocalThemeMode
import com.bitey.app.feature.profile.component.ProfileHeader
import com.bitey.app.feature.profile.component.ProfileMetricsBar
import com.bitey.app.feature.profile.component.TasteProfileSection
import com.bitey.app.feature.profile.component.ThemeSelector

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalNeumorphicTheme.current
    val currentThemeMode = LocalThemeMode.current
    val onThemeModeChanged = LocalOnThemeModeChanged.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // Clean Editorial Profile Header
        ProfileHeader()

        // Unified Minimalist Metrics Bar
        ProfileMetricsBar(
            totalEntriesCount = uiState.totalEntriesCount,
            favoriteEntriesCount = uiState.favoriteEntriesCount,
            spotsWithLocationCount = uiState.spotsWithLocationCount
        )

        // Appearance / Theme Mode Selector
        ThemeSelector(
            currentThemeMode = currentThemeMode,
            onThemeModeChanged = onThemeModeChanged
        )

        // Taste Profile Section
        TasteProfileSection(
            favoriteTagNames = uiState.favoriteTagNames,
            topMealType = uiState.topMealType
        )

        // About Footer
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bitey • Version 1.0.0",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = theme.inkMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Minimalist culinary journaling with authentic die-cut stickers.",
                style = MaterialTheme.typography.bodySmall,
                color = theme.inkMuted,
                fontSize = 10.sp
            )
        }

        // Generous bottom spacer for floating navbar
        Spacer(modifier = Modifier.height(96.dp))
    }
}

package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.Language
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HealthRecordViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HomeViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.SettingsViewModel  // ← NEW
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.theme.BabyGrowthTheme
import org.example.project.babygrowthtrackingapplication.ui.components.BottomNavigationBar
import org.example.project.babygrowthtrackingapplication.ui.components.NavigationTab

@Composable
fun HomeScreen(
    viewModel                 : HomeViewModel,
    healthRecordViewModel     : HealthRecordViewModel,
    settingsViewModel         : SettingsViewModel,              // ← NEW
    currentLanguage           : Language = Language.ENGLISH,
    onLanguageChange          : (Language) -> Unit = {},
    selectedTab               : NavigationTab = NavigationTab.HOME,
    onTabChange               : (NavigationTab) -> Unit = {},
    onAddBaby                 : () -> Unit = {},
    onSeeProfile              : (BabyResponse) -> Unit = {},
    // ── Baby profile tab callbacks ────────────────────────────────────────────
    onEditDetails             : (BabyResponse) -> Unit = {},
    onAddMeasurement          : (BabyResponse) -> Unit = {},
    onViewGrowthChart         : (BabyResponse) -> Unit = {},
    // ── Charts tab callbacks ──────────────────────────────────────────────────
    onAddMeasurementById      : (String) -> Unit = {},
    onViewAllMeasurementsById : (String) -> Unit = {},
    onNavigateToWelcome       : () -> Unit = {},               // ← NEW
) {
    val state = viewModel.uiState

    BabyGrowthTheme(genderTheme = state.genderTheme) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedTab   = selectedTab,
                    onTabSelected = onTabChange
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (selectedTab) {

                    NavigationTab.HOME ->
                        HomeTabContent(
                            viewModel = viewModel,
                            onAddBaby = onAddBaby
                        )

                    NavigationTab.BABY ->
                        BabyProfileTabContent(
                            viewModel         = viewModel,
                            onAddBaby         = onAddBaby,
                            onSeeProfile      = onSeeProfile,
                            onEditDetails     = onEditDetails,
                            onAddMeasurement  = onAddMeasurement,
                            onViewGrowthChart = onViewGrowthChart
                        )

                    NavigationTab.CHARTS ->
                        ChartsTabContent(
                            viewModel             = viewModel,
                            onAddMeasurement      = onAddMeasurementById,
                            onViewAllMeasurements = onViewAllMeasurementsById
                        )

                    // ── Health Record — fully wired ───────────────────────────
                    NavigationTab.HEALTH_RECORD ->
                        HealthRecordTabContent(
                            viewModel = healthRecordViewModel,
                            babies    = state.babies
                        )

                    // ── Settings — fully wired ────────────────────────────────
                    NavigationTab.SETTINGS ->
                        SettingsTabContent(
                            viewModel           = settingsViewModel,         // ← NEW
                            onLanguageChange    = onLanguageChange,
                            onDarkModeChange    = { /* BabyGrowthTheme re-compose handled by prefs */ },
                            onGenderThemeChange = { /* optional: notify HomeViewModel */ },
                            onNavigateToWelcome = onNavigateToWelcome,      // ← NEW
                        )
                }
            }
        }
    }
}
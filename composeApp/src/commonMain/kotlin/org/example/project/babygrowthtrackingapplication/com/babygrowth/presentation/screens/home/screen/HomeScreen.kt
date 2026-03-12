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
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.SettingsViewModel
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme
import org.example.project.babygrowthtrackingapplication.ui.components.BottomNavigationBar
import org.example.project.babygrowthtrackingapplication.ui.components.NavigationTab

// NOTE: BabyGrowthTheme import intentionally removed.
// HomeScreen is already a descendant of the root BabyGrowthTheme in App.kt,
// which correctly holds both `genderTheme` and `isDarkMode`. Adding another
// wrapper here would break dark-mode toggling and gender-theme changes made
// in Settings.

@Composable
fun HomeScreen(
    viewModel                 : HomeViewModel,
    healthRecordViewModel     : HealthRecordViewModel,
    settingsViewModel         : SettingsViewModel,
    currentLanguage           : Language = Language.ENGLISH,
    onLanguageChange          : (Language) -> Unit = {},
    onDarkModeChange          : (Boolean) -> Unit = {},
    onGenderThemeChange       : (GenderTheme) -> Unit = {},
    selectedTab               : NavigationTab = NavigationTab.HOME,
    onTabChange               : (NavigationTab) -> Unit = {},
    onAddBaby                 : () -> Unit = {},
    onSeeProfile              : (BabyResponse) -> Unit = {},
    onEditDetails             : (BabyResponse) -> Unit = {},
    onAddMeasurement          : (BabyResponse) -> Unit = {},
    onViewGrowthChart         : (BabyResponse) -> Unit = {},
    onAddMeasurementById      : (String) -> Unit = {},
    onViewAllMeasurementsById : (String) -> Unit = {},
    onNavigateToWelcome       : () -> Unit = {},
) {
    val state = viewModel.uiState

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
                        onAddBaby         = onAddBaby,        // FIX: was missing → "+" button was a no-op
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

                NavigationTab.HEALTH_RECORD ->
                    HealthRecordTabContent(
                        viewModel = healthRecordViewModel,
                        babies    = state.babies
                    )

                NavigationTab.SETTINGS ->
                    SettingsTabContent(
                        viewModel           = settingsViewModel,
                        onLanguageChange    = onLanguageChange,
                        onDarkModeChange    = onDarkModeChange,
                        onGenderThemeChange = onGenderThemeChange,
                        onNavigateToWelcome = onNavigateToWelcome,
                    )
            }
        }
    }
}
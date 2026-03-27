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
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.FamilyHistoryViewModel
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme
import org.example.project.babygrowthtrackingapplication.ui.components.BottomNavigationBar
import org.example.project.babygrowthtrackingapplication.ui.components.NavigationTab
import org.example.project.babygrowthtrackingapplication.ui.components.SideNavigationRail
import org.example.project.babygrowthtrackingapplication.ui.components.rememberUseSideRail

@Composable
fun HomeScreen(
    viewModel                 : HomeViewModel,
    healthRecordViewModel     : HealthRecordViewModel,
    settingsViewModel         : SettingsViewModel,
    familyHistoryViewModel    : FamilyHistoryViewModel,
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
    onNavigateToFamilyHistory : (String, String) -> Unit = { _, _ -> },
    onSleepGuide              : () -> Unit = {},
    onFeedingGuide            : () -> Unit = {},
) {
    val state       = viewModel.uiState
    val useSideRail = rememberUseSideRail()

    if (useSideRail) {
        Row(modifier = Modifier.fillMaxSize()) {
            SideNavigationRail(
                selectedTab   = selectedTab,
                onTabSelected = onTabChange
            )

            HorizontalDivider(
                modifier  = Modifier
                    .fillMaxHeight()
                    .width(androidx.compose.ui.unit.Dp.Hairline),
                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                TabContent(
                    selectedTab               = selectedTab,
                    viewModel                 = viewModel,
                    healthRecordViewModel     = healthRecordViewModel,
                    settingsViewModel         = settingsViewModel,
                    familyHistoryViewModel    = familyHistoryViewModel,
                    state                     = state,
                    currentLanguage           = currentLanguage,
                    onLanguageChange          = onLanguageChange,
                    onDarkModeChange          = onDarkModeChange,
                    onGenderThemeChange       = onGenderThemeChange,
                    onAddBaby                 = onAddBaby,
                    onSeeProfile              = onSeeProfile,
                    onEditDetails             = onEditDetails,
                    onAddMeasurement          = onAddMeasurement,
                    onViewGrowthChart         = onViewGrowthChart,
                    onAddMeasurementById      = onAddMeasurementById,
                    onViewAllMeasurementsById = onViewAllMeasurementsById,
                    onNavigateToWelcome       = onNavigateToWelcome,
                    onNavigateToFamilyHistory = onNavigateToFamilyHistory,
                    onSleepGuide              = onSleepGuide,
                    onFeedingGuide            = onFeedingGuide,
                    bottomPadding             = androidx.compose.ui.unit.Dp(0f)
                )
            }
        }
    } else {
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
                TabContent(
                    selectedTab               = selectedTab,
                    viewModel                 = viewModel,
                    healthRecordViewModel     = healthRecordViewModel,
                    settingsViewModel         = settingsViewModel,
                    familyHistoryViewModel    = familyHistoryViewModel,
                    state                     = state,
                    currentLanguage           = currentLanguage,
                    onLanguageChange          = onLanguageChange,
                    onDarkModeChange          = onDarkModeChange,
                    onGenderThemeChange       = onGenderThemeChange,
                    onAddBaby                 = onAddBaby,
                    onSeeProfile              = onSeeProfile,
                    onEditDetails             = onEditDetails,
                    onAddMeasurement          = onAddMeasurement,
                    onViewGrowthChart         = onViewGrowthChart,
                    onAddMeasurementById      = onAddMeasurementById,
                    onViewAllMeasurementsById = onViewAllMeasurementsById,
                    onNavigateToWelcome       = onNavigateToWelcome,
                    onNavigateToFamilyHistory = onNavigateToFamilyHistory,
                    onSleepGuide              = onSleepGuide,
                    onFeedingGuide            = onFeedingGuide,
                    bottomPadding             = androidx.compose.ui.unit.Dp(0f)
                )
            }
        }
    }
}

@Composable
private fun TabContent(
    selectedTab               : NavigationTab,
    viewModel                 : HomeViewModel,
    healthRecordViewModel     : HealthRecordViewModel,
    settingsViewModel         : SettingsViewModel,
    familyHistoryViewModel    : FamilyHistoryViewModel,
    state                     : org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HomeUiState,
    currentLanguage           : Language,
    onLanguageChange          : (Language) -> Unit,
    onDarkModeChange          : (Boolean) -> Unit,
    onGenderThemeChange       : (GenderTheme) -> Unit,
    onAddBaby                 : () -> Unit,
    onSeeProfile              : (BabyResponse) -> Unit,
    onEditDetails             : (BabyResponse) -> Unit,
    onAddMeasurement          : (BabyResponse) -> Unit,
    onViewGrowthChart         : (BabyResponse) -> Unit,
    onAddMeasurementById      : (String) -> Unit,
    onViewAllMeasurementsById : (String) -> Unit,
    onNavigateToWelcome       : () -> Unit,
    onNavigateToFamilyHistory : (String, String) -> Unit,
    onSleepGuide              : () -> Unit,
    onFeedingGuide            : () -> Unit,
    bottomPadding             : androidx.compose.ui.unit.Dp,
) {
    when (selectedTab) {
        NavigationTab.HOME ->
            HomeTabContent(
                viewModel      = viewModel,
                onAddBaby      = onAddBaby,
                onSleepGuide   = onSleepGuide,
                onFeedingGuide = onFeedingGuide
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

        NavigationTab.HEALTH_RECORD ->
            HealthRecordTabContent(
                viewModel = healthRecordViewModel,
                babies    = state.babies
            )

        NavigationTab.SETTINGS -> {
            val selectedBaby = state.selectedBaby
            
            // Auto-load history status for the current baby when viewing settings
            LaunchedEffect(selectedBaby?.babyId) {
                selectedBaby?.let { familyHistoryViewModel.loadFamilyHistory(it.babyId) }
            }

            SettingsTabContent(
                viewModel           = settingsViewModel,
                onLanguageChange    = onLanguageChange,
                onDarkModeChange    = onDarkModeChange,
                onGenderThemeChange = onGenderThemeChange,
                onNavigateToWelcome = onNavigateToWelcome,
                onNavigateToFamilyHistory = onNavigateToFamilyHistory,
                selectedBabyId      = selectedBaby?.babyId,
                selectedBabyName    = selectedBaby?.fullName ?: "",
                familyHistoryIsSet  = familyHistoryViewModel.uiState.isSet
            )
        }
    }
}
package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.Language
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.ui.components.BottomNavigationBar
import org.example.project.babygrowthtrackingapplication.ui.components.NavigationTab
import org.example.project.babygrowthtrackingapplication.ui.components.SideNavigationRail
import org.example.project.babygrowthtrackingapplication.ui.components.rememberUseSideRail

@Composable
fun HomeScreen(
    viewModel                  : HomeViewModel,
    healthRecordViewModel      : HealthRecordViewModel,
    settingsViewModel          : SettingsViewModel,
    familyHistoryViewModel     : FamilyHistoryViewModel,
    childIllnessesViewModel    : ChildIllnessesViewModel,
    visionMotorViewModel       : VisionMotorViewModel,
    hearingSpeechViewModel     : HearingSpeechViewModel,
    guideViewModel             : GuideViewModel,
    currentLanguage            : Language = Language.ENGLISH,
    onLanguageChange           : (Language) -> Unit = {},
    onDarkModeChange           : (Boolean) -> Unit = {},
    onGenderThemeChange        : (GenderTheme) -> Unit = {},
    selectedTab                : NavigationTab = NavigationTab.HOME,
    onTabChange                : (NavigationTab) -> Unit = {},
    onAddBaby                  : () -> Unit = {},
    onSeeProfile               : (BabyResponse) -> Unit = {},
    onEditDetails              : (BabyResponse) -> Unit = {},
    onAddMeasurement           : (BabyResponse) -> Unit = {},
    onViewGrowthChart          : (BabyResponse) -> Unit = {},
    onAddMeasurementById       : (String) -> Unit = {},
    onViewAllMeasurementsById  : (String) -> Unit = {},
    onNavigateToWelcome        : () -> Unit = {},
    onNavigateToFamilyHistory  : (String, String) -> Unit = { _, _ -> },
    onNavigateToChildIllnesses : (String, String) -> Unit = { _, _ -> },
    onNavigateToVisionMotor    : (String, String) -> Unit = { _, _ -> },
    onNavigateToHearingSpeech  : (String, String) -> Unit = { _, _ -> },
    onNavigateToSleepGuide     : () -> Unit = {},
    onNavigateToFeedingGuide   : () -> Unit = {},
    onNavigateToMemory         : () -> Unit = {},   // ← ADDED
) {
    val state       = viewModel.uiState
    val useSideRail = rememberUseSideRail()
    val dimensions  = LocalDimensions.current

    if (useSideRail) {
        Row(modifier = Modifier.fillMaxSize()) {
            SideNavigationRail(
                selectedTab   = selectedTab,
                onTabSelected = onTabChange
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(dimensions.hairlineDividerThickness),
                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                TabContent(
                    selectedTab                = selectedTab,
                    viewModel                  = viewModel,
                    healthRecordViewModel      = healthRecordViewModel,
                    settingsViewModel          = settingsViewModel,
                    familyHistoryViewModel     = familyHistoryViewModel,
                    childIllnessesViewModel    = childIllnessesViewModel,
                    visionMotorViewModel       = visionMotorViewModel,
                    hearingSpeechViewModel     = hearingSpeechViewModel,
                    guideViewModel             = guideViewModel,
                    state                      = state,
                    currentLanguage            = currentLanguage,
                    onLanguageChange           = onLanguageChange,
                    onDarkModeChange           = onDarkModeChange,
                    onGenderThemeChange        = onGenderThemeChange,
                    onAddBaby                  = onAddBaby,
                    onSeeProfile               = onSeeProfile,
                    onEditDetails              = onEditDetails,
                    onAddMeasurement           = onAddMeasurement,
                    onViewGrowthChart          = onViewGrowthChart,
                    onAddMeasurementById       = onAddMeasurementById,
                    onViewAllMeasurementsById  = onViewAllMeasurementsById,
                    onNavigateToWelcome        = onNavigateToWelcome,
                    onNavigateToFamilyHistory  = onNavigateToFamilyHistory,
                    onNavigateToChildIllnesses = onNavigateToChildIllnesses,
                    onNavigateToVisionMotor    = onNavigateToVisionMotor,
                    onNavigateToHearingSpeech  = onNavigateToHearingSpeech,
                    onNavigateToSleepGuide     = onNavigateToSleepGuide,
                    onNavigateToFeedingGuide   = onNavigateToFeedingGuide,
                    onNavigateToMemory         = onNavigateToMemory,         // ← ADDED
                    bottomPadding              = 0.dp
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
                    selectedTab                = selectedTab,
                    viewModel                  = viewModel,
                    healthRecordViewModel      = healthRecordViewModel,
                    settingsViewModel          = settingsViewModel,
                    familyHistoryViewModel     = familyHistoryViewModel,
                    childIllnessesViewModel    = childIllnessesViewModel,
                    visionMotorViewModel       = visionMotorViewModel,
                    hearingSpeechViewModel     = hearingSpeechViewModel,
                    guideViewModel             = guideViewModel,
                    state                      = state,
                    currentLanguage            = currentLanguage,
                    onLanguageChange           = onLanguageChange,
                    onDarkModeChange           = onDarkModeChange,
                    onGenderThemeChange        = onGenderThemeChange,
                    onAddBaby                  = onAddBaby,
                    onSeeProfile               = onSeeProfile,
                    onEditDetails              = onEditDetails,
                    onAddMeasurement           = onAddMeasurement,
                    onViewGrowthChart          = onViewGrowthChart,
                    onAddMeasurementById       = onAddMeasurementById,
                    onViewAllMeasurementsById  = onViewAllMeasurementsById,
                    onNavigateToWelcome        = onNavigateToWelcome,
                    onNavigateToFamilyHistory  = onNavigateToFamilyHistory,
                    onNavigateToChildIllnesses = onNavigateToChildIllnesses,
                    onNavigateToVisionMotor    = onNavigateToVisionMotor,
                    onNavigateToHearingSpeech  = onNavigateToHearingSpeech,
                    onNavigateToSleepGuide     = onNavigateToSleepGuide,
                    onNavigateToFeedingGuide   = onNavigateToFeedingGuide,
                    onNavigateToMemory         = onNavigateToMemory,         // ← ADDED
                    bottomPadding              = paddingValues.calculateBottomPadding()
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TabContent
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TabContent(
    selectedTab                : NavigationTab,
    viewModel                  : HomeViewModel,
    healthRecordViewModel      : HealthRecordViewModel,
    settingsViewModel          : SettingsViewModel,
    familyHistoryViewModel     : FamilyHistoryViewModel,
    childIllnessesViewModel    : ChildIllnessesViewModel,
    visionMotorViewModel       : VisionMotorViewModel,
    hearingSpeechViewModel     : HearingSpeechViewModel,
    guideViewModel             : GuideViewModel,
    state                      : HomeUiState,
    currentLanguage            : Language,
    onLanguageChange           : (Language) -> Unit,
    onDarkModeChange           : (Boolean) -> Unit,
    onGenderThemeChange        : (GenderTheme) -> Unit,
    onAddBaby                  : () -> Unit,
    onSeeProfile               : (BabyResponse) -> Unit,
    onEditDetails              : (BabyResponse) -> Unit,
    onAddMeasurement           : (BabyResponse) -> Unit,
    onViewGrowthChart          : (BabyResponse) -> Unit,
    onAddMeasurementById       : (String) -> Unit,
    onViewAllMeasurementsById  : (String) -> Unit,
    onNavigateToWelcome        : () -> Unit,
    onNavigateToFamilyHistory  : (String, String) -> Unit,
    onNavigateToChildIllnesses : (String, String) -> Unit,
    onNavigateToVisionMotor    : (String, String) -> Unit,
    onNavigateToHearingSpeech  : (String, String) -> Unit,
    onNavigateToSleepGuide     : () -> Unit,
    onNavigateToFeedingGuide   : () -> Unit,
    onNavigateToMemory         : () -> Unit,                // ← ADDED
    bottomPadding              : androidx.compose.ui.unit.Dp,
) {
    when (selectedTab) {
        NavigationTab.HOME ->
            HomeTabContent(
                viewModel      = viewModel,
                onAddBaby      = onAddBaby,
                onSleepGuide   = onNavigateToSleepGuide,
                onFeedingGuide = onNavigateToFeedingGuide,
                onMemory       = onNavigateToMemory,        // ← ADDED
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

            LaunchedEffect(selectedBaby?.babyId) {
                selectedBaby?.let { baby ->
                    familyHistoryViewModel.loadFamilyHistory(baby.babyId)
                    childIllnessesViewModel.loadIllnesses(baby.babyId)
                    visionMotorViewModel.load(baby.babyId, baby.ageInMonths)
                    hearingSpeechViewModel.load(baby.babyId, baby.ageInMonths)
                }
            }

            SettingsTabContent(
                viewModel                  = settingsViewModel,
                onLanguageChange           = onLanguageChange,
                onDarkModeChange           = onDarkModeChange,
                onGenderThemeChange        = onGenderThemeChange,
                onNavigateToWelcome        = onNavigateToWelcome,
                onNavigateToFamilyHistory  = onNavigateToFamilyHistory,
                onNavigateToChildIllnesses = onNavigateToChildIllnesses,
                onNavigateToVisionMotor    = onNavigateToVisionMotor,
                onNavigateToHearingSpeech  = onNavigateToHearingSpeech,
                selectedBabyId             = selectedBaby?.babyId,
                selectedBabyName           = selectedBaby?.fullName ?: "",
                familyHistoryIsSet         = familyHistoryViewModel.uiState.isSet,
                childIllnessCount          = childIllnessesViewModel.uiState.illnesses.size,
                childIllnessActiveCount    = childIllnessesViewModel.uiState.activeCount,
                visionMotorCount           = visionMotorViewModel.uiState.savedRecords.size,
                hearingSpeechCount         = hearingSpeechViewModel.uiState.savedRecords.size,
            )
        }
    }
}
package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.Language
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HomeViewModel
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.theme.BabyGrowthTheme
import org.example.project.babygrowthtrackingapplication.ui.components.BottomNavigationBar
import org.example.project.babygrowthtrackingapplication.ui.components.NavigationTab

@Composable
fun HomeScreen(
    viewModel        : HomeViewModel,
    currentLanguage  : Language = Language.ENGLISH,
    onLanguageChange : (Language) -> Unit = {},
    selectedTab      : NavigationTab = NavigationTab.HOME,
    onTabChange      : (NavigationTab) -> Unit = {},
    onAddBaby        : () -> Unit = {},
    onSeeProfile     : (BabyResponse) -> Unit = {},
    // ── Baby profile tab action callbacks ─────────────────────────────────────
    onEditDetails    : (BabyResponse) -> Unit = {},
    onAddMeasurement : (BabyResponse) -> Unit = {},
    onViewGrowthChart: (BabyResponse) -> Unit = {},
    // ── Charts tab action callbacks ───────────────────────────────────────────
    onAddMeasurementById      : (String) -> Unit = {},
    onViewAllMeasurementsById : (String) -> Unit = {}
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

                    NavigationTab.HEALTH_RECORD ->
                        HealthRecordTabContent()

                    NavigationTab.BENCH ->
                        BenchTabContent()

                    NavigationTab.SETTINGS ->
                        SettingsTabContent()
                }
            }
        }
    }
}
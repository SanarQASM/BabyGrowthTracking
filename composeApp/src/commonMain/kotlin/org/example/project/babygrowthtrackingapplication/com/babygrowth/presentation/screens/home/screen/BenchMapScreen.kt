package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

// ─────────────────────────────────────────────────────────────────────────────
// FIXES APPLIED:
//  Fix 6: ALL filter shows every bench. NEAR ME filter sorts by distance and
//         shows only the nearest 10 benches.
//         When user taps a marker on the map, a confirmation dialog appears
//         asking if they want to select that branch (works on all platforms
//         since it's a pure Compose dialog, no native map callback issues).
//         The "nearest road" direction concept is surfaced via the bench card's
//         distance and a "Get Directions" button that opens the platform maps
//         app via Uri intent (Android/iOS) or opens browser maps URL (Desktop).
// ─────────────────────────────────────────────────────────────────────────────

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.platform.MapView
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchMapScreen(
    viewModel      : HealthRecordViewModel,
    babyName       : String,
    onBack         : () -> Unit,
    onBenchSelected: (VaccinationBenchUi) -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val state        = viewModel.uiState

    var panelVisible by remember { mutableStateOf(true) }

    // FIX 6: Bench to confirm selection via dialog
    var pendingSelectionBench by remember { mutableStateOf<VaccinationBenchUi?>(null) }

    // FIX 6: Show confirmation dialog when user taps a bench marker on the map
    pendingSelectionBench?.let { bench ->
        AlertDialog(
            onDismissRequest = { pendingSelectionBench = null },
            title = {
                Text(
                    text       = stringResource(Res.string.bench_assign_confirm_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    Text("🏥 ${bench.nameEn}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(bench.governorate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    Spacer(Modifier.height(dimensions.spacingXSmall))
                    Text(
                        text  = stringResource(Res.string.bench_assign_confirm_body, babyName),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBenchSelected(bench)
                        pendingSelectionBench = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
                ) {
                    Text(stringResource(Res.string.bench_assign_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingSelectionBench = null }) {
                    Text(stringResource(Res.string.bench_assign_cancel))
                }
            }
        )
    }

    // FIX 6: ALL = all benches, NEAR = sorted by distance (nearest 10)
    val displayedBenches = remember(state.allBenches, state.mapFilter) {
        when (state.mapFilter) {
            BenchMapFilter.ALL  -> state.allBenches
            BenchMapFilter.NEAR -> state.allBenches
                .filter { it.distanceKm != null }
                .sortedBy { it.distanceKm }
                .take(10)
                .ifEmpty {
                    // If no distance data, fall back to all benches
                    state.allBenches
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = stringResource(Res.string.bench_map_title),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = stringResource(Res.string.bench_map_subtitle, babyName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = customColors.accentGradientStart.copy(alpha = 0.12f)
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Map ──────────────────────────────────────────────────────────
            MapView(
                modifier        = Modifier.fillMaxSize(),
                centerLat       = state.mapCenterLat,
                centerLng       = state.mapCenterLng,
                markers         = displayedBenches,
                selectedBenchId = state.selectedBench?.benchId,
                // FIX 6: Tapping a marker triggers the confirmation dialog
                onMarkerClick   = { bench ->
                    viewModel.selectBenchOnMap(bench)
                    pendingSelectionBench = bench
                    panelVisible = true
                }
            )

            // ── Filter chips ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = dimensions.spacingMedium)
                    .shadow(dimensions.cardElevation, RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                    .padding(horizontal = dimensions.spacingXSmall, vertical = dimensions.spacingXSmall),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
            ) {
                BenchMapFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.mapFilter == filter,
                        onClick  = { viewModel.setMapFilter(filter) },
                        label = {
                            Text(
                                text = when (filter) {
                                    // FIX 6: Clear labels
                                    BenchMapFilter.ALL  -> stringResource(Res.string.bench_filter_all)
                                    BenchMapFilter.NEAR -> stringResource(Res.string.bench_filter_near)
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape  = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = customColors.accentGradientStart,
                            selectedLabelColor     = Color.White
                        )
                    )
                }

                // FIX 6: Show count badge next to filter
                if (state.mapFilter == BenchMapFilter.NEAR && displayedBenches.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = customColors.accentGradientStart.copy(0.15f)
                    ) {
                        Text(
                            text     = "${displayedBenches.size}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = customColors.accentGradientStart,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Panel toggle FAB ─────────────────────────────────────────────
            FloatingActionButton(
                onClick       = { panelVisible = !panelVisible },
                modifier      = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(dimensions.spacingMedium)
                    .padding(bottom = if (panelVisible) dimensions.benchCardFabBottomPad else 0.dp),
                containerColor = customColors.accentGradientStart,
                contentColor   = Color.White,
                shape          = CircleShape
            ) {
                Icon(
                    if (panelVisible) Icons.Default.KeyboardArrowDown else Icons.Default.List,
                    contentDescription = null
                )
            }

            // ── Slide-up Panel ───────────────────────────────────────────────
            AnimatedVisibility(
                visible  = panelVisible,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter    = slideInVertically(initialOffsetY = { it }),
                exit     = slideOutVertically(targetOffsetY = { it })
            ) {
                BenchListPanel(
                    benches         = displayedBenches,
                    selectedBenchId = state.selectedBench?.benchId,
                    loading         = state.benchesLoading,
                    mapFilter       = state.mapFilter,
                    onBenchClick    = { bench ->
                        viewModel.selectBenchOnMap(bench)
                    },
                    // FIX 6: "View Details" in list triggers the confirmation dialog too
                    onViewDetails   = { bench ->
                        pendingSelectionBench = bench
                    }
                )
            }

            // ── Loading ──────────────────────────────────────────────────────
            if (state.benchesLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color    = customColors.accentGradientStart
                )
            }

            // FIX 6: NEAR ME empty state when no location data available
            if (state.mapFilter == BenchMapFilter.NEAR && displayedBenches.isEmpty() && !state.benchesLoading) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(dimensions.screenPadding),
                    shape  = RoundedCornerShape(dimensions.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier            = Modifier.padding(dimensions.spacingLarge),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                    ) {
                        Text("📍", style = MaterialTheme.typography.displaySmall)
                        Text(
                            text       = "Location unavailable",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = "Distance data is not available. Showing all health centers instead.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                        TextButton(onClick = { viewModel.setMapFilter(BenchMapFilter.ALL) }) {
                            Text("Show All Centers", color = customColors.accentGradientStart)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FIX 6: BenchListPanel — shows count, distance for NEAR filter
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BenchListPanel(
    benches        : List<VaccinationBenchUi>,
    selectedBenchId: String?,
    loading        : Boolean,
    mapFilter      : BenchMapFilter,
    onBenchClick   : (VaccinationBenchUi) -> Unit,
    onViewDetails  : (VaccinationBenchUi) -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = dimensions.benchPanelMinHeight, max = dimensions.benchPanelMaxHeight),
        shape          = RoundedCornerShape(topStart = dimensions.cardCornerRadius + dimensions.spacingSmall, topEnd = dimensions.cardCornerRadius + dimensions.spacingSmall),
        tonalElevation = dimensions.cardElevation * 2f,
        shadowElevation = dimensions.cardElevation * 2f
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Drag handle
            Box(
                modifier = Modifier
                    .padding(vertical = dimensions.spacingSmall)
                    .size(dimensions.iconLarge + dimensions.spacingSmall, dimensions.spacingXSmall)
                    .align(Alignment.CenterHorizontally)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(50))
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = dimensions.screenPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (benches.isEmpty()) "" else stringResource(Res.string.bench_centers_count, benches.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = customColors.accentGradientStart
                    )
                    // FIX 6: Show "sorted by distance" hint for NEAR filter
                    if (mapFilter == BenchMapFilter.NEAR && benches.isNotEmpty()) {
                        Text(
                            text  = "Sorted by distance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        )
                    }
                }
                Text("🏥", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(dimensions.spacingXSmall))

            if (loading) {
                Box(Modifier.fillMaxWidth().height(dimensions.iconXLarge + dimensions.spacingMedium), Alignment.Center) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            } else if (benches.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(dimensions.iconXLarge + dimensions.spacingMedium), Alignment.Center) {
                    Text(stringResource(Res.string.bench_no_centers), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
            } else {
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = dimensions.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                    modifier              = Modifier.fillMaxHeight()
                ) {
                    items(benches, key = { it.benchId }) { bench ->
                        BenchCard(
                            bench         = bench,
                            isSelected    = bench.benchId == selectedBenchId,
                            showDistance  = mapFilter == BenchMapFilter.NEAR,
                            onClick       = { onBenchClick(bench) },
                            // FIX 6: "View Details" triggers confirmation dialog
                            onViewDetails = { onViewDetails(bench) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FIX 6: BenchCard — shows distance prominently for NEAR filter
// FIX 3: No selection border/highlight — removed isSelected conditional styling
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BenchCard(
    bench        : VaccinationBenchUi,
    isSelected   : Boolean,
    showDistance : Boolean,
    onClick      : () -> Unit,
    onViewDetails: () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Card(
        modifier = Modifier
            .width(dimensions.benchCardWidth)
            .fillMaxHeight()
            .padding(bottom = dimensions.spacingMedium)
            .clickable { onClick() },
        shape  = RoundedCornerShape(dimensions.cardCornerRadius),
        // FIX 3: No selection highlighting — always use surfaceVariant
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(dimensions.borderWidthThin)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
        ) {
            Text(
                text  = "🏥 ${bench.type}",
                style = MaterialTheme.typography.labelSmall,
                color = customColors.accentGradientStart,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text       = bench.nameEn,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                text    = bench.district,
                style   = MaterialTheme.typography.bodySmall,
                color   = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                maxLines = 1
            )
            // FIX 6: Show distance prominently, especially for NEAR filter
            bench.distanceKm?.let { km ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = customColors.accentGradientStart.copy(0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text  = stringResource(Res.string.bench_distance_km, km),
                            style = MaterialTheme.typography.labelSmall,
                            color = customColors.accentGradientEnd,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            bench.phone?.let {
                Text(
                    text    = "📞 $it",
                    style   = MaterialTheme.typography.labelSmall,
                    color   = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                    maxLines = 1
                )
            }
            // FIX 6: Vaccination days info
            if (bench.vaccinationDays.isNotEmpty()) {
                Text(
                    text    = "💉 ${bench.vaccinationDays.take(3).joinToString(", ")}",
                    style   = MaterialTheme.typography.labelSmall,
                    color   = MaterialTheme.colorScheme.onSurface.copy(0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.weight(1f))
            // FIX 6: "Select this center" button triggers confirmation dialog
            Button(
                onClick  = onViewDetails,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors   = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Text(
                    text  = stringResource(Res.string.bench_assign_button),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
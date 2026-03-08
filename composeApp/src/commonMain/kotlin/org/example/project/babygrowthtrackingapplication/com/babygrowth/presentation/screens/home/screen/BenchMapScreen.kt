package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
    viewModel: HealthRecordViewModel,
    babyName: String,
    onBack: () -> Unit,
    onBenchSelected: (VaccinationBenchUi) -> Unit
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val state = viewModel.uiState

    var panelVisible by remember { mutableStateOf(true) }

    val displayedBenches = remember(state.allBenches, state.mapFilter) {
        when (state.mapFilter) {
            BenchMapFilter.ALL  -> state.allBenches
            BenchMapFilter.NEAR -> state.allBenches
                .filter { it.distanceKm != null }
                .sortedBy { it.distanceKm }
                .take(10)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(Res.string.bench_map_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(Res.string.bench_map_subtitle, babyName),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Map ──────────────────────────────────────────────────────────
            MapView(
                modifier = Modifier.fillMaxSize(),
                centerLat = state.mapCenterLat,
                centerLng = state.mapCenterLng,
                markers = displayedBenches,
                selectedBenchId = state.selectedBench?.benchId,
                onMarkerClick = { bench ->
                    viewModel.selectBenchOnMap(bench)
                    panelVisible = true
                }
            )

            // ── Filter chips ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = dimensions.spacingMedium)
                    .shadow(4.dp, RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BenchMapFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.mapFilter == filter,
                        onClick = { viewModel.setMapFilter(filter) },
                        label = {
                            Text(
                                text = when (filter) {
                                    BenchMapFilter.ALL  -> stringResource(Res.string.bench_filter_all)
                                    BenchMapFilter.NEAR -> stringResource(Res.string.bench_filter_near)
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = customColors.accentGradientStart,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // ── Panel toggle button ───────────────────────────────────────────
            FloatingActionButton(
                onClick = { panelVisible = !panelVisible },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(dimensions.spacingMedium)
                    .padding(bottom = if (panelVisible) 260.dp else 0.dp),
                containerColor = customColors.accentGradientStart,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    if (panelVisible) Icons.Default.KeyboardArrowDown else Icons.Default.List,
                    contentDescription = null
                )
            }

            // ── Slide-up Panel ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = panelVisible,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                BenchListPanel(
                    benches = displayedBenches,
                    selectedBenchId = state.selectedBench?.benchId,
                    loading = state.benchesLoading,
                    onBenchClick = { bench ->
                        viewModel.selectBenchOnMap(bench)
                    },
                    onViewDetails = { bench ->
                        onBenchSelected(bench)
                    }
                )
            }

            // ── Loading ──────────────────────────────────────────────────────
            if (state.benchesLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = customColors.accentGradientStart
                )
            }
        }
    }
}

@Composable
private fun BenchListPanel(
    benches: List<VaccinationBenchUi>,
    selectedBenchId: String?,
    loading: Boolean,
    onBenchClick: (VaccinationBenchUi) -> Unit,
    onViewDetails: (VaccinationBenchUi) -> Unit
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = 300.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Drag handle
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(40.dp, 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(50)
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.screenPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (benches.isEmpty()) "" else
                        stringResource(Res.string.bench_centers_count, benches.size),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = customColors.accentGradientStart
                )
                Text(
                    text = "🏥",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(4.dp))

            if (loading) {
                Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            } else if (benches.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.bench_no_centers),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = dimensions.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(benches, key = { it.benchId }) { bench ->
                        BenchCard(
                            bench = bench,
                            isSelected = bench.benchId == selectedBenchId,
                            onClick = { onBenchClick(bench) },
                            onViewDetails = { onViewDetails(bench) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BenchCard(
    bench: VaccinationBenchUi,
    isSelected: Boolean,
    onClick: () -> Unit,
    onViewDetails: () -> Unit
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Card(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .padding(bottom = dimensions.spacingMedium)
            .clickable { onClick() },
        shape = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                customColors.accentGradientStart.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, customColors.accentGradientStart) else null,
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "🏥 ${bench.type}",
                style = MaterialTheme.typography.labelSmall,
                color = customColors.accentGradientStart,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = bench.nameEn,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = bench.district,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                maxLines = 1
            )
            bench.distanceKm?.let {
                Text(
                    text = "📍 %.1f km away".toInt().toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = customColors.accentGradientEnd
                )
            }
            bench.phone?.let {
                Text(
                    text = "📞 $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                    maxLines = 1
                )
            }
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = customColors.accentGradientStart
                )
            ) {
                Text(
                    text = stringResource(Res.string.bench_detail_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
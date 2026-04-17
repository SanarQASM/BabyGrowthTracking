package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

// ─────────────────────────────────────────────────────────────────────────────
// FIXES in this version:
//
//  FIX 1 — Root cause of wrong colors (ALL screenshots):
//    Old code split records by `measuredByName.isNullOrBlank()`.
//    Because the backend was setting measuredByName = parent's own name,
//    ALL records landed in teamRecords → every line got cyan/dashed team color.
//
//    New code uses `GrowthRecordResponse.addedByTeam` which checks the explicit
//    `isTeamMeasurement` boolean the backend now sends, with measuredByName as
//    a fallback. Parent records have isTeamMeasurement=false + measuredByName=null,
//    so they correctly go to parentRecords.
//
//  FIX 2 — Per-metric team colors (distinct in ALL tab):
//    TEAM_WEIGHT_COLOR = #00ACC1 (cyan)
//    TEAM_HEIGHT_COLOR = #26A69A (teal)
//    TEAM_HEAD_COLOR   = #7E57C2 (indigo)
//    Parent lines always use gender-themed accentStart/accentEnd (solid).
//
//  FIX 3 — Smooth draw-in animation via animateFloatAsState + PathMeasure.
//
//  FIX 4 — Legend exactly mirrors canvas colors in every tab and every theme.
// ─────────────────────────────────────────────────────────────────────────────

import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HomeViewModel
import org.example.project.babygrowthtrackingapplication.data.network.*
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*

// ─────────────────────────────────────────────────────────────────────────────
// WHO Standard Growth Percentile Data (0–24 months)
// ─────────────────────────────────────────────────────────────────────────────

private val whoWeight = mapOf(
    0 to Triple(2.5, 3.3, 4.3), 1 to Triple(3.4, 4.5, 5.7),
    2 to Triple(4.4, 5.6, 7.1), 3 to Triple(5.0, 6.4, 8.0),
    4 to Triple(5.6, 7.0, 8.7), 5 to Triple(6.1, 7.5, 9.3),
    6 to Triple(6.4, 7.9, 9.8), 7 to Triple(6.7, 8.3, 10.3),
    8 to Triple(7.0, 8.6, 10.7), 9 to Triple(7.2, 8.9, 11.0),
    10 to Triple(7.5, 9.2, 11.4), 11 to Triple(7.7, 9.4, 11.7),
    12 to Triple(7.8, 9.6, 12.0), 15 to Triple(8.3, 10.3, 12.8),
    18 to Triple(8.8, 10.9, 13.7), 21 to Triple(9.2, 11.5, 14.5),
    24 to Triple(9.7, 12.2, 15.3)
)

private val whoHeight = mapOf(
    0 to Triple(46.1, 49.9, 53.7), 1 to Triple(50.8, 54.7, 58.6),
    2 to Triple(54.4, 58.4, 62.4), 3 to Triple(57.3, 61.4, 65.5),
    4 to Triple(59.7, 63.9, 68.0), 5 to Triple(61.7, 65.9, 70.1),
    6 to Triple(63.3, 67.6, 71.9), 7 to Triple(64.8, 69.2, 73.5),
    8 to Triple(66.2, 70.6, 75.0), 9 to Triple(67.5, 72.0, 76.5),
    10 to Triple(68.7, 73.3, 77.9), 11 to Triple(69.9, 74.5, 79.2),
    12 to Triple(71.0, 75.7, 80.5), 15 to Triple(73.2, 79.1, 85.0),
    18 to Triple(76.9, 82.7, 88.7), 21 to Triple(80.1, 86.2, 92.3),
    24 to Triple(82.5, 88.0, 95.5)
)

private val whoHead = mapOf(
    0 to Triple(32.1, 34.5, 36.9), 1 to Triple(35.1, 37.3, 39.5),
    2 to Triple(36.9, 39.1, 41.3), 3 to Triple(38.3, 40.5, 42.7),
    4 to Triple(39.4, 41.6, 43.8), 5 to Triple(40.3, 42.6, 44.8),
    6 to Triple(41.0, 43.3, 45.6), 7 to Triple(41.7, 44.0, 46.3),
    8 to Triple(42.2, 44.5, 46.9), 9 to Triple(42.6, 45.0, 47.4),
    10 to Triple(43.0, 45.4, 47.8), 11 to Triple(43.4, 45.8, 48.2),
    12 to Triple(43.6, 46.1, 48.5), 15 to Triple(44.2, 46.6, 49.0),
    18 to Triple(44.6, 47.0, 49.4), 21 to Triple(45.0, 47.5, 49.9),
    24 to Triple(45.2, 47.8, 50.4)
)

// ─────────────────────────────────────────────────────────────────────────────
// COLOR CONSTANTS
//
// WHO reference lines — intentionally fixed, not gender-themed.
// Team colors — distinct per metric, fixed across all themes.
// Parent lines — always use the gender-themed accentStart / accentEnd.
// ─────────────────────────────────────────────────────────────────────────────

// WHO percentile bands (fixed medical reference colors)
private val WHO_97_COLOR = Color(0xFF4CAF50)  // Green  — 97th percentile
private val WHO_50_COLOR = Color(0xFFFFC107)  // Amber  — 50th percentile (median)
private val WHO_3_COLOR = Color(0xFFF44336)  // Red    — 3rd  percentile

// Head measurement for parent — fixed semantic purple (gender-neutral body metric)
private val HEAD_COLOR = Color(0xFF9C27B0)    // Purple

// Team colors — one per metric, always dashed, fixed across all gender themes
private val TEAM_WEIGHT_COLOR = Color(0xFF00ACC1)  // Cyan    — team weight
private val TEAM_HEIGHT_COLOR = Color(0xFF26A69A)  // Teal    — team height
private val TEAM_HEAD_COLOR = Color(0xFF7E57C2)  // Indigo  — team head

enum class ChartFilter { ALL, WEIGHT, HEIGHT, HEAD }

@Composable
private fun rememberMonthNames(): List<String> = listOf(
    "",
    stringResource(Res.string.month_jan), stringResource(Res.string.month_feb),
    stringResource(Res.string.month_mar), stringResource(Res.string.month_apr),
    stringResource(Res.string.month_may), stringResource(Res.string.month_jun),
    stringResource(Res.string.month_jul), stringResource(Res.string.month_aug),
    stringResource(Res.string.month_sep), stringResource(Res.string.month_oct),
    stringResource(Res.string.month_nov), stringResource(Res.string.month_dec)
)

// ─────────────────────────────────────────────────────────────────────────────
// CHARTS TAB ENTRY POINT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ChartsTabContent(
    viewModel: HomeViewModel,
    onAddMeasurement: (String) -> Unit = {},
    onViewAllMeasurements: (String) -> Unit = {}
) {
    val state = viewModel.uiState
    val customColors = MaterialTheme.customColors
    val dimensions = LocalDimensions.current
    val isLandscape = LocalIsLandscape.current

    val activeBabies = remember(state.babies) { state.babies.filter { it.isActive } }
    var selectedBabyIndex by remember { mutableStateOf(0) }
    var chartFilter by remember { mutableStateOf(ChartFilter.ALL) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val selectedBaby = activeBabies.getOrNull(selectedBabyIndex)
    val latestGrowth = selectedBaby?.let { state.latestGrowthRecords[it.babyId] }
    val allRecords = selectedBaby?.let { state.allGrowthRecords[it.babyId] } ?: emptyList()

    val accentStart = customColors.accentGradientStart
    val accentEnd = customColors.accentGradientEnd

    // FIX 3: Animation — restarts whenever data or filter changes
    var animTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(allRecords.size, chartFilter, selectedBabyIndex) {
        animTrigger = false
        animTrigger = true
    }
    val animProgress by animateFloatAsState(
        targetValue = if (animTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "chart_draw_progress"
    )

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .width(dimensions.chartLandscapePaneWidth)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(dimensions.spacingMedium),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Box(
                        modifier = Modifier.size(dimensions.iconLarge).background(
                            accentStart.copy(0.15f), RoundedCornerShape(dimensions.spacingSmall)
                        ),
                        contentAlignment = Alignment.Center
                    ) { Text("📈", style = MaterialTheme.typography.titleMedium) }
                    Text(
                        stringResource(Res.string.chart_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (activeBabies.isEmpty()) {
                    Text(
                        stringResource(Res.string.chart_no_child_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                } else {
                    Text(
                        stringResource(Res.string.chart_select_child),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Box {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                            border = BorderStroke(
                                dimensions.borderWidthThin,
                                accentStart.copy(0.4f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = accentStart),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(
                                horizontal = dimensions.spacingMedium,
                                vertical = dimensions.spacingSmall
                            )
                        ) {
                            Text(
                                selectedBaby?.fullName
                                    ?: stringResource(Res.string.chart_select_child),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                null,
                                modifier = Modifier.size(dimensions.iconSmall)
                            )
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }) {
                            activeBabies.forEachIndexed { idx, baby ->
                                DropdownMenuItem(
                                    text = { Text(baby.fullName) },
                                    onClick = { selectedBabyIndex = idx; dropdownExpanded = false })
                            }
                        }
                    }
                }

                Text(
                    stringResource(Res.string.chart_filter_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                    fontWeight = FontWeight.SemiBold
                )
                Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                    ChartFilter.entries.forEach { f ->
                        val sel = chartFilter == f
                        FilterChip(
                            selected = sel, onClick = { chartFilter = f },
                            label = {
                                Text(
                                    filterLabel(f),
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (sel) Color.White else accentStart,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentStart,
                                selectedLabelColor = Color.White,
                                containerColor = accentStart.copy(0.08f),
                                labelColor = accentStart
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (selectedBaby != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ChartLegend(
                        babyName = selectedBaby.fullName,
                        filter = chartFilter,
                        accentStart = accentStart,
                        accentEnd = accentEnd
                    )
                }
            }

            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())
                    .padding(dimensions.spacingMedium),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensions.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(dimensions.cardElevation)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(dimensions.spacingSmall)) {
                        when {
                            selectedBaby == null -> EmptyChartBox(
                                accentStart,
                                stringResource(Res.string.chart_no_baby),
                                stringResource(Res.string.chart_no_baby_desc),
                                "👶"
                            )

                            allRecords.isEmpty() -> EmptyChartBox(
                                accentStart,
                                stringResource(Res.string.chart_no_measurement),
                                stringResource(Res.string.chart_no_measurement_desc),
                                "📊"
                            )

                            else -> GrowthChartCanvas(
                                records = allRecords,
                                filter = chartFilter,
                                accentStart = accentStart,
                                accentEnd = accentEnd,
                                animProgress = animProgress,
                                landscapeHeight = dimensions.avatarLarge * 4 + dimensions.spacingXLarge
                            )
                        }
                    }
                }
                if (selectedBaby != null) {
                    LatestMeasurementCard(
                        latestGrowth = latestGrowth,
                        accentStart = accentStart,
                        accentEnd = accentEnd,
                        onViewAll = { onViewAllMeasurements(selectedBaby.babyId) },
                        onAddMeasure = { onAddMeasurement(selectedBaby.babyId) })
                }
            }
        }
    } else {
        // ── PORTRAIT ─────────────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(accentStart, accentEnd))).padding(
                    horizontal = dimensions.spacingLarge,
                    vertical = dimensions.spacingMedium - dimensions.spacingXSmall
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall + dimensions.spacingXSmall)
                ) {
                    Box(
                        modifier = Modifier.size(dimensions.iconLarge + dimensions.spacingXSmall)
                            .background(
                                Color.White.copy(0.25f),
                                RoundedCornerShape(dimensions.spacingSmall + dimensions.spacingXSmall)
                            ), contentAlignment = Alignment.Center
                    ) {
                        Text("📈", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        stringResource(Res.string.chart_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(bottom = dimensions.spacingXXLarge + dimensions.spacingXLarge)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(
                        horizontal = dimensions.screenPadding,
                        vertical = dimensions.spacingMedium
                    ),
                    shape = RoundedCornerShape(dimensions.cardCornerRadius + dimensions.spacingXSmall),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(dimensions.cardElevation)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium),
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                    ) {
                        if (activeBabies.isEmpty()) {
                            Text(
                                stringResource(Res.string.chart_no_child_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                            )
                        } else {
                            Box {
                                OutlinedButton(
                                    onClick = { dropdownExpanded = true },
                                    shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                                    border = BorderStroke(
                                        dimensions.borderWidthThin,
                                        accentStart.copy(0.4f)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentStart),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        selectedBaby?.fullName
                                            ?: stringResource(Res.string.chart_select_child),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        null,
                                        modifier = Modifier.size(dimensions.iconSmall)
                                    )
                                }
                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }) {
                                    activeBabies.forEachIndexed { idx, baby ->
                                        DropdownMenuItem(
                                            text = { Text(baby.fullName) },
                                            onClick = {
                                                selectedBabyIndex = idx; dropdownExpanded = false
                                            })
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                        ) {
                            ChartFilter.entries.forEach { f ->
                                val sel = chartFilter == f
                                FilterChip(
                                    selected = sel, onClick = { chartFilter = f },
                                    label = {
                                        Text(
                                            filterLabel(f),
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sel) Color.White else accentStart,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentStart,
                                        selectedLabelColor = Color.White,
                                        containerColor = accentStart.copy(0.08f),
                                        labelColor = accentStart
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        when {
                            selectedBaby == null -> EmptyChartBox(
                                accentStart,
                                stringResource(Res.string.chart_no_baby),
                                stringResource(Res.string.chart_no_baby_desc),
                                "👶"
                            )

                            allRecords.isEmpty() -> EmptyChartBox(
                                accentStart,
                                stringResource(Res.string.chart_no_measurement),
                                stringResource(Res.string.chart_no_measurement_desc),
                                "📊"
                            )

                            else -> GrowthChartCanvas(
                                records = allRecords,
                                filter = chartFilter,
                                accentStart = accentStart,
                                accentEnd = accentEnd,
                                animProgress = animProgress
                            )
                        }

                        if (selectedBaby != null) {
                            ChartLegend(
                                babyName = selectedBaby.fullName,
                                filter = chartFilter,
                                accentStart = accentStart,
                                accentEnd = accentEnd
                            )
                        }
                    }
                }

                if (selectedBaby != null) {
                    LatestMeasurementCard(
                        latestGrowth = latestGrowth,
                        accentStart = accentStart,
                        accentEnd = accentEnd,
                        onViewAll = { onViewAllMeasurements(selectedBaby.babyId) },
                        onAddMeasure = { onAddMeasurement(selectedBaby.babyId) })
                }
            }
        }
    }
}

@Composable
private fun filterLabel(f: ChartFilter): String = when (f) {
    ChartFilter.ALL -> stringResource(Res.string.chart_filter_all)
    ChartFilter.WEIGHT -> stringResource(Res.string.chart_filter_weight)
    ChartFilter.HEIGHT -> stringResource(Res.string.chart_filter_height)
    ChartFilter.HEAD -> stringResource(Res.string.chart_filter_head)
}

// ─────────────────────────────────────────────────────────────────────────────
// GROWTH CHART CANVAS
//
// FIX 1: Uses record.addedByTeam (which reads isTeamMeasurement first, then
//         falls back to measuredByName) instead of measuredByName.isNullOrBlank().
//         This is the core fix — parent records now correctly land in parentRecords.
//
// FIX 2: Per-metric team colors (distinct in ALL tab).
//
// FIX 3: Smooth draw-in animation via PathMeasure clipping.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GrowthChartCanvas(
    records: List<GrowthRecordResponse>,
    filter: ChartFilter,
    accentStart: Color,
    accentEnd: Color,
    animProgress: Float,
    landscapeHeight: Dp? = null
) {
    val dimensions = LocalDimensions.current
    val maxMonths = 24

    // ── FIX 1: Use addedByTeam instead of measuredByName.isNullOrBlank() ─────
    // addedByTeam = isTeamMeasurement || !measuredByName.isNullOrBlank()
    // When backend correctly sends isTeamMeasurement=false for parent records,
    // parentRecords will contain all data added by the baby's own parent.
    val parentRecords = remember(records) { records.filter { !it.addedByTeam } }
    val teamRecords = remember(records) { records.filter { it.addedByTeam } }

    val whoRef = when (filter) {
        ChartFilter.WEIGHT -> whoWeight
        ChartFilter.HEIGHT -> whoHeight
        ChartFilter.HEAD -> whoHead
        ChartFilter.ALL -> whoWeight
    }

    val allValues = buildList {
        if (filter == ChartFilter.ALL || filter == ChartFilter.WEIGHT) addAll(records.mapNotNull { it.weight?.toFloat() })
        if (filter == ChartFilter.ALL || filter == ChartFilter.HEIGHT) addAll(records.mapNotNull { it.height?.toFloat() })
        if (filter == ChartFilter.ALL || filter == ChartFilter.HEAD) addAll(records.mapNotNull { it.headCircumference?.toFloat() })
        addAll(whoRef.values.map { it.first.toFloat() })
        addAll(whoRef.values.map { it.third.toFloat() })
    }
    val yMin = (allValues.minOrNull() ?: 0f) * 0.95f
    val yMax = (allValues.maxOrNull() ?: 100f) * 1.05f

    val yLabels = (0..5).map { i -> yMin + (yMax - yMin) / 5f * (5 - i) }
    val ageMonthsLabel = stringResource(Res.string.chart_age_months_label)
    val chartHeight = landscapeHeight ?: (dimensions.avatarLarge * 4)

    Box(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
        yLabels.forEachIndexed { i, v ->
            val topFraction = i.toFloat() / 5f
            Text(
                text = v.toInt().toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.45f),
                modifier = Modifier.align(Alignment.TopStart).fillMaxWidth().padding(
                    top = (topFraction * (chartHeight.value * 0.85f)).dp,
                    end = dimensions.spacingXSmall
                ),
                textAlign = TextAlign.End
            )
        }

        Canvas(modifier = Modifier.fillMaxSize().padding(dimensions.spacingXSmall)) {
            val w = size.width;
            val h = size.height
            val cL = 44f;
            val cR = w - 8f
            val cT = 8f;
            val cB = h - 28f
            val cW = cR - cL;
            val cH = cB - cT

            fun xOf(mo: Int) = cL + (mo.toFloat() / maxMonths) * cW
            fun yOf(v: Float) = cB - ((v - yMin) / (yMax - yMin)) * cH

            // Grid lines
            val gc = Color.Gray.copy(0.1f)
            for (i in 0..5) {
                val y = cT + cH / 5f * i; drawLine(gc, Offset(cL, y), Offset(cR, y), 1f)
            }
            listOf(0, 4, 8, 12, 16, 20, 24).forEach { mo ->
                drawLine(
                    gc,
                    Offset(xOf(mo), cT),
                    Offset(xOf(mo), cB),
                    1f
                )
            }
            val ac = Color.Gray.copy(0.3f)
            drawLine(ac, Offset(cL, cT), Offset(cL, cB), 1.5f)
            drawLine(ac, Offset(cL, cB), Offset(cR, cB), 1.5f)

            val stdDash = PathEffect.dashPathEffect(floatArrayOf(7f, 4f))
            val dataDash = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
            val whoSorted = whoRef.keys.sorted()

            // WHO reference curves
            if (filter == ChartFilter.ALL) {
                val path50 = Path()
                whoSorted.forEachIndexed { i, mo ->
                    val v = whoRef[mo]!!.second.toFloat()
                    if (i == 0) path50.moveTo(xOf(mo), yOf(v)) else path50.lineTo(xOf(mo), yOf(v))
                }
                drawPath(
                    path50,
                    WHO_50_COLOR.copy(alpha = 0.20f),
                    style = Stroke(1f, pathEffect = stdDash)
                )
            } else {
                listOf(
                    WHO_97_COLOR to { t: Triple<Double, Double, Double> -> t.third.toFloat() },
                    WHO_50_COLOR to { t: Triple<Double, Double, Double> -> t.second.toFloat() },
                    WHO_3_COLOR to { t: Triple<Double, Double, Double> -> t.first.toFloat() }
                ).forEach { (col, get) ->
                    val path = Path()
                    whoSorted.forEachIndexed { i, mo ->
                        val v = get(whoRef[mo]!!)
                        if (i == 0) path.moveTo(xOf(mo), yOf(v)) else path.lineTo(xOf(mo), yOf(v))
                    }
                    drawPath(path, col, style = Stroke(1.5f, pathEffect = stdDash))
                }
            }

            // ── FIX 3: clip drawn path to animProgress fraction ───────────────
            fun animatedPath(full: Path): Path {
                if (animProgress >= 1f) return full
                val pm = PathMeasure()
                pm.setPath(full, false)
                val total = pm.length
                if (total == 0f) return full
                val clipped = Path()
                pm.getSegment(0f, total * animProgress.coerceIn(0f, 1f), clipped, true)
                return clipped
            }

            fun buildPath(
                recs: List<GrowthRecordResponse>,
                get: (GrowthRecordResponse) -> Double?
            ): Path {
                val pts = recs.filter { get(it) != null }
                    .sortedBy { it.ageInMonths }
                    .map {
                        Offset(
                            xOf(it.ageInMonths.coerceIn(0, maxMonths)),
                            yOf(get(it)!!.toFloat())
                        )
                    }
                val path = Path()
                if (pts.size >= 2) {
                    path.moveTo(pts.first().x, pts.first().y); pts.drop(1)
                        .forEach { path.lineTo(it.x, it.y) }
                }
                return path
            }

            // ── FIX 1 + 2: correct colors per source + per metric ─────────────
            fun drawLine(
                recs: List<GrowthRecordResponse>,
                get: (GrowthRecordResponse) -> Double?,
                color: Color,
                dashed: Boolean,
                stroke: Float = 2.5f
            ) {
                val full = buildPath(recs, get)
                if (!full.isEmpty) drawPath(
                    animatedPath(full),
                    color,
                    style = Stroke(stroke, pathEffect = if (dashed) dataDash else null)
                )

                val pts = recs.filter { get(it) != null }.sortedBy { it.ageInMonths }
                    .map {
                        Offset(
                            xOf(it.ageInMonths.coerceIn(0, maxMonths)),
                            yOf(get(it)!!.toFloat())
                        )
                    }
                val visible = (pts.size * animProgress.coerceIn(0f, 1f)).toInt()
                    .coerceAtLeast(if (pts.isNotEmpty()) 1 else 0)
                val dotR = if (dashed) 4f else 5f;
                val innerR = if (dashed) 2.5f else 3f
                pts.take(visible)
                    .forEach { drawCircle(color, dotR, it); drawCircle(Color.White, innerR, it) }
            }

            when (filter) {
                ChartFilter.ALL -> {
                    // Parent — solid lines, gender-themed accent colors
                    drawLine(parentRecords, { it.weight }, accentStart, dashed = false)
                    drawLine(parentRecords, { it.height }, accentEnd, dashed = false)
                    drawLine(parentRecords, { it.headCircumference }, HEAD_COLOR, dashed = false)
                    // Team — dashed lines, fixed TEAM_* colors (distinct per metric)
                    drawLine(teamRecords, { it.weight }, TEAM_WEIGHT_COLOR, dashed = true)
                    drawLine(teamRecords, { it.height }, TEAM_HEIGHT_COLOR, dashed = true)
                    drawLine(teamRecords, { it.headCircumference }, TEAM_HEAD_COLOR, dashed = true)
                }

                ChartFilter.WEIGHT -> {
                    drawLine(parentRecords, { it.weight }, accentStart, dashed = false)
                    drawLine(teamRecords, { it.weight }, TEAM_WEIGHT_COLOR, dashed = true)
                }

                ChartFilter.HEIGHT -> {
                    drawLine(parentRecords, { it.height }, accentEnd, dashed = false)
                    drawLine(teamRecords, { it.height }, TEAM_HEIGHT_COLOR, dashed = true)
                }

                ChartFilter.HEAD -> {
                    drawLine(parentRecords, { it.headCircumference }, HEAD_COLOR, dashed = false)
                    drawLine(teamRecords, { it.headCircumference }, TEAM_HEAD_COLOR, dashed = true)
                }
            }
        }

        Row(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(
                start = dimensions.iconLarge + dimensions.spacingXSmall,
                bottom = dimensions.spacingXSmall,
                end = dimensions.spacingXSmall * 2
            ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("0", "4", "8", "12", "16", "20", "24").forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.55f)
                )
            }
        }
    }

    Text(
        ageMonthsLabel,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(0.4f),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// LEGEND
// FIX 4: Exactly mirrors canvas colors in every tab and every theme.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChartLegend(
    babyName: String,
    filter: ChartFilter,
    accentStart: Color,
    accentEnd: Color
) {
    val dimensions = LocalDimensions.current
    val legendParentAdded = stringResource(Res.string.chart_legend_parent_added)
    val legendTeamAdded = stringResource(Res.string.chart_legend_team_added)
    val legendWhoRef = stringResource(Res.string.chart_legend_who_ref_weight)
    val legendWhoAvg = stringResource(Res.string.chart_legend_who_avg)
    val legendBabyMeasure = stringResource(Res.string.chart_legend_baby_measurements)
    val legendWhoStandards = stringResource(Res.string.chart_legend_who_standards)
    val weightKg = stringResource(Res.string.chart_legend_weight_kg)
    val heightCm = stringResource(Res.string.chart_legend_height_cm)
    val headCm = stringResource(Res.string.chart_legend_head_cm)
    val unitKg = stringResource(Res.string.add_baby_unit_kg)
    val unitCm = stringResource(Res.string.add_baby_unit_cm)
    val parentPrefix = stringResource(Res.string.chart_legend_parent_prefix)
    val teamPrefix = stringResource(Res.string.chart_legend_team_prefix)

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(accentStart.copy(0.04f), RoundedCornerShape(dimensions.cardCornerRadius))
            .border(
                dimensions.borderWidthThin,
                accentStart.copy(0.1f),
                RoundedCornerShape(dimensions.cardCornerRadius)
            )
            .padding(dimensions.spacingMedium),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall + dimensions.borderWidthThin)
    ) {
        Text(
            stringResource(Res.string.chart_legend_title),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        when (filter) {
            ChartFilter.ALL -> {
                // Parent Added — solid lines, gender-themed colors
                LegendSectionHeader(legendParentAdded)
                LegendRow(accentStart, weightKg, dashed = false, showDot = true)  // parent weight
                LegendRow(accentEnd, heightCm, dashed = false, showDot = true)  // parent height
                LegendRow(HEAD_COLOR, headCm, dashed = false, showDot = true)  // parent head

                Spacer(Modifier.height(dimensions.spacingXSmall))

                // Team Added — dashed, distinct TEAM_* colors
                LegendSectionHeader(legendTeamAdded)
                LegendRow(TEAM_WEIGHT_COLOR, weightKg, dashed = true, showDot = true)
                LegendRow(TEAM_HEIGHT_COLOR, heightCm, dashed = true, showDot = true)
                LegendRow(TEAM_HEAD_COLOR, headCm, dashed = true, showDot = true)

                Spacer(Modifier.height(dimensions.spacingXSmall))

                LegendSectionHeader(legendWhoRef)
                LegendRow(
                    WHO_50_COLOR.copy(alpha = 0.5f),
                    legendWhoAvg,
                    dashed = true,
                    showDot = false
                )
            }

            ChartFilter.WEIGHT, ChartFilter.HEIGHT, ChartFilter.HEAD -> {
                // Per-tab: parent color vs team color vs WHO bands
                val parentColor: Color
                val teamColor: Color
                val metricLabel: String
                val metricUnit: String
                when (filter) {
                    ChartFilter.WEIGHT -> {
                        parentColor = accentStart; teamColor = TEAM_WEIGHT_COLOR; metricLabel =
                            stringResource(Res.string.chart_filter_weight); metricUnit = unitKg
                    }

                    ChartFilter.HEIGHT -> {
                        parentColor = accentEnd; teamColor = TEAM_HEIGHT_COLOR; metricLabel =
                            stringResource(Res.string.chart_filter_height); metricUnit = unitCm
                    }

                    ChartFilter.HEAD -> {
                        parentColor = HEAD_COLOR; teamColor = TEAM_HEAD_COLOR; metricLabel =
                            stringResource(Res.string.chart_filter_head); metricUnit = unitCm
                    }

                    else -> {
                        parentColor = accentStart; teamColor = TEAM_WEIGHT_COLOR; metricLabel =
                            ""; metricUnit = unitKg
                    }
                }

                LegendSectionHeader(legendBabyMeasure)
                // Parent row — solid, gender-themed color
                LegendRow(
                    parentColor,
                    "👪  $parentPrefix — $metricLabel ($metricUnit)",
                    dashed = false,
                    showDot = true
                )
                // Team row — dashed, TEAM_* color
                LegendRow(
                    teamColor,
                    "🏥  $teamPrefix — $metricLabel ($metricUnit)",
                    dashed = true,
                    showDot = true
                )

                Spacer(Modifier.height(dimensions.spacingXSmall))

                LegendSectionHeader(legendWhoStandards)
                LegendRow(
                    WHO_97_COLOR,
                    stringResource(Res.string.chart_legend_who97),
                    dashed = true,
                    showDot = true
                )
                LegendRow(
                    WHO_50_COLOR,
                    stringResource(Res.string.chart_legend_who50),
                    dashed = true,
                    showDot = true
                )
                LegendRow(
                    WHO_3_COLOR,
                    stringResource(Res.string.chart_legend_who3),
                    dashed = true,
                    showDot = true
                )
            }
        }
    }
}

@Composable
private fun LegendSectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
        letterSpacing = 0.3.sp
    )
}

@Composable
private fun LegendRow(color: Color, label: String, dashed: Boolean, showDot: Boolean) {
    val dimensions = LocalDimensions.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        Canvas(
            Modifier.size(
                width = dimensions.spacingLarge + dimensions.spacingXSmall,
                height = dimensions.spacingSmall + dimensions.spacingXSmall
            )
        ) {
            val y = size.height / 2
            val pe = if (dashed) PathEffect.dashPathEffect(floatArrayOf(5f, 3f)) else null
            drawLine(color, Offset(0f, y), Offset(size.width, y), 2f, pathEffect = pe)
            if (!dashed) {
                drawCircle(color, 3.5f, Offset(size.width / 2, y)); drawCircle(
                    Color.White,
                    2f,
                    Offset(size.width / 2, y)
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(0.75f),
            modifier = Modifier.weight(1f)
        )
        if (showDot) Box(
            Modifier.size(dimensions.spacingSmall + dimensions.spacingXSmall)
                .background(color, CircleShape)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LATEST MEASUREMENT CARD
// FIX: badge uses addedByTeam + TEAM_WEIGHT_COLOR for team badge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LatestMeasurementCard(
    latestGrowth: GrowthRecordResponse?,
    accentStart: Color,
    accentEnd: Color,
    onViewAll: () -> Unit,
    onAddMeasure: () -> Unit
) {
    val dimensions = LocalDimensions.current
    val unitKg = stringResource(Res.string.add_baby_unit_kg)
    val unitCm = stringResource(Res.string.add_baby_unit_cm)
    val percentileSuffix = stringResource(Res.string.chart_percentile_suffix)
    val dateEmojiPrefix = stringResource(Res.string.chart_date_prefix_emoji)
    val weightIcon = stringResource(Res.string.chart_measurement_weight_icon)
    val heightIcon = stringResource(Res.string.chart_measurement_height_icon)
    val headIcon = stringResource(Res.string.chart_measurement_head_icon)
    val pctWeightIcon = stringResource(Res.string.chart_percentile_weight_icon)
    val pctHeightIcon = stringResource(Res.string.chart_percentile_height_icon)
    val monthNames = rememberMonthNames()
    val parentPrefix = stringResource(Res.string.chart_legend_parent_prefix)

    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
        shape = RoundedCornerShape(dimensions.cardCornerRadius + dimensions.spacingXSmall),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(dimensions.cardElevation)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.chart_latest_measurement),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentStart,
                    letterSpacing = dimensions.chartLetterSpacing
                )
                if (latestGrowth != null) {
                    OutlinedButton(
                        onClick = onViewAll,
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                        border = BorderStroke(dimensions.borderWidthThin, accentStart.copy(0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = accentStart),
                        contentPadding = PaddingValues(
                            horizontal = dimensions.spacingSmall + dimensions.spacingXSmall,
                            vertical = dimensions.spacingXSmall
                        )
                    ) {
                        Text(
                            stringResource(Res.string.chart_view_all),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (latestGrowth == null) {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = dimensions.spacingMedium),
                    Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📏", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(dimensions.spacingSmall))
                        Text(
                            stringResource(Res.string.chart_no_measurement),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(dimensions.spacingSmall))
                        Button(
                            onClick = onAddMeasure,
                            colors = ButtonDefaults.buttonColors(containerColor = accentStart),
                            shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                        ) {
                            Text(
                                stringResource(Res.string.chart_add_measurement),
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                    // FIX: use addedByTeam instead of measuredByName check
                    if (latestGrowth.addedByTeam) {
                        val measurer = latestGrowth.measuredByName ?: "Team"
                        Row(
                            modifier = Modifier.background(
                                TEAM_WEIGHT_COLOR.copy(0.12f),
                                RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                            ).border(
                                dimensions.borderWidthThin,
                                TEAM_WEIGHT_COLOR.copy(0.25f),
                                RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                            ).padding(
                                horizontal = dimensions.spacingXSmall * 2,
                                vertical = dimensions.spacingXSmall / 2
                            ),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏥", style = MaterialTheme.typography.labelSmall)
                            Text(
                                measurer,
                                style = MaterialTheme.typography.labelSmall,
                                color = TEAM_WEIGHT_COLOR,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.background(
                                accentStart.copy(0.10f),
                                RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                            ).border(
                                dimensions.borderWidthThin,
                                accentStart.copy(0.25f),
                                RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                            ).padding(
                                horizontal = dimensions.spacingXSmall * 2,
                                vertical = dimensions.spacingXSmall / 2
                            ),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👪", style = MaterialTheme.typography.labelSmall)
                            Text(
                                parentPrefix,
                                style = MaterialTheme.typography.labelSmall,
                                color = accentStart
                            )
                        }
                    }
                    Spacer(Modifier.height(dimensions.spacingXSmall / 2))
                    MeasureRow(
                        weightIcon,
                        latestGrowth.weight?.let { "$it $unitKg" }
                            ?: stringResource(Res.string.chart_weight_empty))
                    MeasureRow(
                        heightIcon,
                        latestGrowth.height?.let { "$it $unitCm" }
                            ?: stringResource(Res.string.chart_height_empty))
                    MeasureRow(
                        headIcon,
                        latestGrowth.headCircumference?.let { "$it $unitCm" }
                            ?: stringResource(Res.string.chart_head_empty))
                    Text(
                        "$dateEmojiPrefix  ${
                            formatChartDate(
                                latestGrowth.measurementDate,
                                monthNames
                            )
                        }",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.55f)
                    )
                    val wp = latestGrowth.weightPercentile;
                    val hp = latestGrowth.heightPercentile
                    if (wp != null || hp != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                            wp?.let {
                                Text(
                                    "$pctWeightIcon $it$percentileSuffix",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accentStart
                                )
                            }
                            hp?.let {
                                Text(
                                    "$pctHeightIcon $it$percentileSuffix",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accentEnd
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(dimensions.spacingXSmall))
                OutlinedButton(
                    onClick = onAddMeasure,
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                    border = BorderStroke(dimensions.borderWidthThin, accentStart.copy(0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentStart),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(dimensions.iconSmall))
                    Spacer(Modifier.width(dimensions.spacingXSmall))
                    Text(
                        stringResource(Res.string.chart_add_measurement),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MeasureRow(icon: String, text: String) {
    val dimensions = LocalDimensions.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
    ) {
        Text(icon, style = MaterialTheme.typography.bodySmall)
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(0.85f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MEASUREMENT HISTORY LIST + CARD
// FIX: uses addedByTeam + correct TEAM_WEIGHT_COLOR for team badge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MeasurementHistoryList(
    babyId: String,
    records: List<GrowthRecordResponse>,
    accentStart: Color,
    viewModel: HomeViewModel
) {
    val dimensions = LocalDimensions.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        items(records, key = { it.recordId }) { record ->
            MeasurementCard(
                record = record,
                accentStart = accentStart,
                onDelete = { viewModel.deleteGrowthRecord(babyId, record.recordId) })
        }
    }
}

@Composable
private fun MeasurementCard(
    record: GrowthRecordResponse,
    accentStart: Color,
    onDelete: () -> Unit
) {
    val dimensions = LocalDimensions.current
    var showDel by remember { mutableStateOf(false) }
    val unitKg = stringResource(Res.string.add_baby_unit_kg)
    val unitCm = stringResource(Res.string.add_baby_unit_cm)
    val notRecorded = stringResource(Res.string.profile_not_recorded_value)
    val monthNames = rememberMonthNames()
    val parentPrefix = stringResource(Res.string.chart_legend_parent_prefix)

    if (showDel) {
        AlertDialog(
            onDismissRequest = { showDel = false },
            title = { Text(stringResource(Res.string.chart_delete_measurement_title)) },
            text = {
                Text(
                    stringResource(
                        Res.string.chart_delete_measurement_desc,
                        formatChartDate(record.measurementDate, monthNames)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { showDel = false; onDelete() }) {
                    Text(
                        stringResource(Res.string.all_measures_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDel = false
                }) { Text(stringResource(Res.string.add_measure_cancel)) }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(dimensions.chartDividerThickness)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.Top
        ) {
            val parts = record.measurementDate.split("-")
            Box(
                modifier = Modifier.size(dimensions.avatarMedium + dimensions.spacingXSmall)
                    .background(
                        accentStart.copy(0.12f),
                        RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                    ).border(
                    dimensions.borderWidthThin,
                    accentStart.copy(0.3f),
                    RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                ), contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        parts.getOrNull(2) ?: notRecorded,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentStart
                    )
                    Text(
                        monthAbbrev(parts.getOrNull(1)?.toIntOrNull() ?: 0, monthNames),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentStart.copy(0.8f)
                    )
                }
            }
            Spacer(Modifier.width(dimensions.spacingSmall + dimensions.spacingXSmall))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall - dimensions.borderWidthThin)
            ) {
                // FIX: use addedByTeam
                if (record.addedByTeam) {
                    val measurer = record.measuredByName ?: "Team"
                    Row(
                        modifier = Modifier.background(
                            TEAM_WEIGHT_COLOR.copy(0.12f),
                            RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                        ).padding(
                            horizontal = dimensions.spacingXSmall * 2,
                            vertical = dimensions.spacingXSmall / 2
                        ),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏥", style = MaterialTheme.typography.labelSmall)
                        Text(
                            measurer,
                            style = MaterialTheme.typography.labelSmall,
                            color = TEAM_WEIGHT_COLOR,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.background(
                            accentStart.copy(0.10f),
                            RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                        ).border(
                            dimensions.borderWidthThin,
                            accentStart.copy(0.25f),
                            RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                        ).padding(
                            horizontal = dimensions.spacingXSmall * 2,
                            vertical = dimensions.spacingXSmall / 2
                        )
                    ) {
                        Text(
                            "👪 $parentPrefix",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentStart
                        )
                    }
                }
                Spacer(Modifier.height(dimensions.spacingXSmall / 2))
                Text("${stringResource(Res.string.chart_measurement_weight_icon)}  ${record.weight?.let { "$it $unitKg" } ?: notRecorded}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                Text("${stringResource(Res.string.chart_measurement_height_icon)}  ${record.height?.let { "$it $unitCm" } ?: notRecorded}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                Text("${stringResource(Res.string.chart_measurement_head_icon)}  ${record.headCircumference?.let { "$it $unitCm" } ?: notRecorded}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
            }
            IconButton(
                onClick = { showDel = true },
                modifier = Modifier.size(dimensions.iconLarge)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.all_measures_delete),
                    tint = MaterialTheme.colorScheme.error.copy(0.7f),
                    modifier = Modifier.size(dimensions.iconMedium)
                )
            }
        }
    }
}

@Composable
private fun EmptyChartBox(accentStart: Color, title: String, subtitle: String, emoji: String) {
    val dimensions = LocalDimensions.current
    Box(
        modifier = Modifier.fillMaxWidth().height(dimensions.avatarLarge * 3)
            .background(accentStart.copy(0.04f), RoundedCornerShape(dimensions.cardCornerRadius))
            .border(
                dimensions.borderWidthThin,
                accentStart.copy(0.15f),
                RoundedCornerShape(dimensions.cardCornerRadius)
            ), contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Text(emoji, style = MaterialTheme.typography.displaySmall)
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                textAlign = TextAlign.Center
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.45f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AllMeasurementsScreen(
    babyId: String,
    babyName: String,
    isFemale: Boolean,
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions = LocalDimensions.current
    val accentStart = customColors.accentGradientStart
    val accentEnd = customColors.accentGradientEnd
    val state = viewModel.uiState
    val records =
        (state.allGrowthRecords[babyId] ?: emptyList()).sortedByDescending { it.measurementDate }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(accentStart, accentEnd))).padding(
                    horizontal = dimensions.spacingSmall,
                    vertical = dimensions.spacingSmall
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null,
                            tint = Color.White
                        )
                    }
                    Text(
                        stringResource(Res.string.all_measures_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier.background(
                            Color.White.copy(0.22f),
                            RoundedCornerShape(dimensions.buttonCornerRadius)
                        ).padding(
                            horizontal = dimensions.spacingSmall + dimensions.spacingXSmall,
                            vertical = dimensions.spacingXSmall
                        )
                    ) {
                        Text(
                            "${if (isFemale) "👶" else "👦"} $babyName",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (records.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📏", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))
                    Text(
                        stringResource(Res.string.all_measures_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.55f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(
                    horizontal = dimensions.screenPadding,
                    vertical = dimensions.spacingMedium
                ),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                items(records, key = { it.recordId }) { record ->
                    MeasurementCard(
                        record = record,
                        accentStart = accentStart,
                        onDelete = { viewModel.deleteGrowthRecord(babyId, record.recordId) })
                }
            }
        }
    }
}

private fun formatChartDate(dateStr: String, monthNames: List<String>): String {
    val parts = dateStr.split("-")
    if (parts.size != 3) return dateStr
    val m = parts[1].toIntOrNull() ?: return dateStr
    return "${monthNames.getOrElse(m) { parts[1] }} ${parts[2]}, ${parts[0]}"
}

private fun monthAbbrev(m: Int, monthNames: List<String>): String = monthNames.getOrElse(m) { "--" }
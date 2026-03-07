package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HomeViewModel
import org.example.project.babygrowthtrackingapplication.data.network.*
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// WHO Standard Growth Percentile Data (0–24 months)
// Triple = (3rd percentile, 50th percentile, 97th percentile)
// ─────────────────────────────────────────────────────────────────────────────

private val whoWeight = mapOf(   // kg
    0  to Triple(2.5,  3.3,  4.3),  1  to Triple(3.4,  4.5,  5.7),
    2  to Triple(4.4,  5.6,  7.1),  3  to Triple(5.0,  6.4,  8.0),
    4  to Triple(5.6,  7.0,  8.7),  5  to Triple(6.1,  7.5,  9.3),
    6  to Triple(6.4,  7.9,  9.8),  7  to Triple(6.7,  8.3,  10.3),
    8  to Triple(7.0,  8.6,  10.7), 9  to Triple(7.2,  8.9,  11.0),
    10 to Triple(7.5,  9.2,  11.4), 11 to Triple(7.7,  9.4,  11.7),
    12 to Triple(7.8,  9.6,  12.0), 15 to Triple(8.3,  10.3, 12.8),
    18 to Triple(8.8,  10.9, 13.7), 21 to Triple(9.2,  11.5, 14.5),
    24 to Triple(9.7,  12.2, 15.3)
)

private val whoHeight = mapOf(   // cm
    0  to Triple(46.1, 49.9, 53.7),  1  to Triple(50.8, 54.7, 58.6),
    2  to Triple(54.4, 58.4, 62.4),  3  to Triple(57.3, 61.4, 65.5),
    4  to Triple(59.7, 63.9, 68.0),  5  to Triple(61.7, 65.9, 70.1),
    6  to Triple(63.3, 67.6, 71.9),  7  to Triple(64.8, 69.2, 73.5),
    8  to Triple(66.2, 70.6, 75.0),  9  to Triple(67.5, 72.0, 76.5),
    10 to Triple(68.7, 73.3, 77.9), 11 to Triple(69.9, 74.5, 79.2),
    12 to Triple(71.0, 75.7, 80.5), 15 to Triple(73.2, 79.1, 85.0),
    18 to Triple(76.9, 82.7, 88.7), 21 to Triple(80.1, 86.2, 92.3),
    24 to Triple(82.5, 88.0, 95.5)
)

private val whoHead = mapOf(   // cm
    0  to Triple(32.1, 34.5, 36.9),  1  to Triple(35.1, 37.3, 39.5),
    2  to Triple(36.9, 39.1, 41.3),  3  to Triple(38.3, 40.5, 42.7),
    4  to Triple(39.4, 41.6, 43.8),  5  to Triple(40.3, 42.6, 44.8),
    6  to Triple(41.0, 43.3, 45.6),  7  to Triple(41.7, 44.0, 46.3),
    8  to Triple(42.2, 44.5, 46.9),  9  to Triple(42.6, 45.0, 47.4),
    10 to Triple(43.0, 45.4, 47.8), 11 to Triple(43.4, 45.8, 48.2),
    12 to Triple(43.6, 46.1, 48.5), 15 to Triple(44.2, 46.6, 49.0),
    18 to Triple(44.6, 47.0, 49.4), 21 to Triple(45.0, 47.5, 49.9),
    24 to Triple(45.2, 47.8, 50.4)
)

// ─────────────────────────────────────────────────────────────────────────────
// Chart-level colour constants
// These are fixed semantic chart colors, not gender-theme colors.
// They intentionally live here as named constants rather than raw hex literals.
// WHO colors follow a universal traffic-light convention (green/amber/red).
// HEAD_COLOR and TEAM_COLOR are also fixed semantic colors for chart readability.
// ─────────────────────────────────────────────────────────────────────────────

private val WHO_97_COLOR = Color(0xFF4CAF50)   // green  – high range
private val WHO_50_COLOR = Color(0xFFFFC107)   // amber  – average
private val WHO_3_COLOR  = Color(0xFFF44336)   // red    – low range
private val HEAD_COLOR   = Color(0xFF9C27B0)   // purple – head circumference
private val TEAM_COLOR   = Color(0xFF00ACC1)   // teal   – team-added records

// ─────────────────────────────────────────────────────────────────────────────
// Chart filter enum
// ─────────────────────────────────────────────────────────────────────────────

enum class ChartFilter { ALL, WEIGHT, HEIGHT, HEAD }

// ─────────────────────────────────────────────────────────────────────────────
// CHARTS TAB ENTRY POINT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ChartsTabContent(
    viewModel            : HomeViewModel,
    onAddMeasurement     : (String) -> Unit = {},
    onViewAllMeasurements: (String) -> Unit = {}
) {
    val state        = viewModel.uiState
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    val activeBabies      = remember(state.babies) { state.babies.filter { it.isActive } }
    var selectedBabyIndex by remember { mutableStateOf(0) }
    var chartFilter       by remember { mutableStateOf(ChartFilter.ALL) }
    var dropdownExpanded  by remember { mutableStateOf(false) }

    val selectedBaby = activeBabies.getOrNull(selectedBabyIndex)
    val latestGrowth = selectedBaby?.let { state.latestGrowthRecords[it.babyId] }
    val allRecords   = selectedBaby?.let { state.allGrowthRecords[it.babyId] } ?: emptyList()

    val accentStart = customColors.accentGradientStart
    val accentEnd   = customColors.accentGradientEnd

    Column(modifier = Modifier.fillMaxSize()) {

        // ── STICKY HEADER ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(accentStart, accentEnd)))
                .padding(horizontal = dimensions.spacingLarge, vertical = dimensions.spacingMedium - dimensions.spacingXSmall)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall + dimensions.spacingXSmall)
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensions.iconLarge + dimensions.spacingXSmall)
                        .background(Color.White.copy(0.25f), RoundedCornerShape(dimensions.spacingSmall + dimensions.spacingXSmall)),
                    contentAlignment = Alignment.Center
                ) { Text("📈", style = MaterialTheme.typography.titleLarge) }
                Text(
                    stringResource(Res.string.chart_title),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
            }
        }

        // ── SCROLLABLE BODY ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = dimensions.spacingXXLarge + dimensions.spacingXLarge)
        ) {

            // ── Main card: selector + filter + chart + legend ─────────────────
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensions.screenPadding,
                        vertical   = dimensions.spacingMedium
                    ),
                shape     = RoundedCornerShape(dimensions.cardCornerRadius + dimensions.spacingXSmall),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(dimensions.cardElevation)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                ) {

                    // ── Baby selector ─────────────────────────────────────────
                    Column {
                        Text(
                            stringResource(Res.string.home_select_child),
                            style         = MaterialTheme.typography.labelMedium,
                            fontWeight    = FontWeight.Bold,
                            color         = accentStart,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))
                        Box {
                            OutlinedButton(
                                onClick  = { dropdownExpanded = !dropdownExpanded },
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                                border   = BorderStroke(1.5.dp, accentStart.copy(0.5f)),
                                colors   = ButtonDefaults.outlinedButtonColors(
                                    containerColor = accentStart.copy(0.07f),
                                    contentColor   = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    activeBabies.getOrNull(selectedBabyIndex)?.fullName
                                        ?: stringResource(Res.string.chart_no_child_hint),
                                    style      = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier   = Modifier.weight(1f)
                                )
                                Icon(
                                    if (dropdownExpanded) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint     = accentStart,
                                    modifier = Modifier.size(dimensions.iconMedium)
                                )
                            }

                            DropdownMenu(
                                expanded         = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier         = Modifier.fillMaxWidth(0.9f)
                            ) {
                                activeBabies.forEachIndexed { idx, baby ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                baby.fullName,
                                                style      = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (idx == selectedBabyIndex)
                                                    FontWeight.Bold else FontWeight.Normal,
                                                color      = if (idx == selectedBabyIndex)
                                                    accentStart
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            selectedBabyIndex = idx
                                            dropdownExpanded  = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ── Filter tabs ───────────────────────────────────────────
                    val filters = listOf(
                        ChartFilter.ALL    to stringResource(Res.string.chart_filter_all),
                        ChartFilter.WEIGHT to stringResource(Res.string.chart_filter_weight),
                        ChartFilter.HEIGHT to stringResource(Res.string.chart_filter_height),
                        ChartFilter.HEAD   to stringResource(Res.string.chart_filter_head)
                    )
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall + dimensions.spacingXSmall)
                    ) {
                        filters.forEach { (f, label) ->
                            val sel = f == chartFilter
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(dimensions.buttonCornerRadius))
                                    .background(if (sel) accentStart else accentStart.copy(0.08f))
                                    .clickable { chartFilter = f }
                                    .padding(vertical = dimensions.spacingSmall - dimensions.spacingXSmall),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    style      = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    color      = if (sel) Color.White else accentStart,
                                    maxLines   = 1,
                                    overflow   = TextOverflow.Ellipsis,
                                    textAlign  = TextAlign.Center
                                )
                            }
                        }
                    }

                    // ── Chart canvas ──────────────────────────────────────────
                    if (selectedBaby == null) {
                        EmptyChartBox(
                            accentStart,
                            stringResource(Res.string.chart_no_baby),
                            stringResource(Res.string.chart_no_baby_desc),
                            "👶"
                        )
                    } else if (allRecords.isEmpty()) {
                        EmptyChartBox(
                            accentStart,
                            stringResource(Res.string.chart_no_measurement),
                            stringResource(Res.string.chart_no_measurement_desc),
                            "📊"
                        )
                    } else {
                        GrowthChartCanvas(
                            records     = allRecords,
                            filter      = chartFilter,
                            accentStart = accentStart,
                            accentEnd   = accentEnd
                        )
                    }

                    // ── Legend ────────────────────────────────────────────────
                    if (selectedBaby != null) {
                        ChartLegend(
                            babyName    = selectedBaby.fullName,
                            filter      = chartFilter,
                            accentStart = accentStart,
                            accentEnd   = accentEnd
                        )
                    }
                }
            }

            // ── Latest Measurement card ───────────────────────────────────────
            if (selectedBaby != null) {
                LatestMeasurementCard(
                    latestGrowth = latestGrowth,
                    accentStart  = accentStart,
                    accentEnd    = accentEnd,
                    onViewAll    = { onViewAllMeasurements(selectedBaby.babyId) },
                    onAddMeasure = { onAddMeasurement(selectedBaby.babyId) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GROWTH CHART CANVAS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GrowthChartCanvas(
    records     : List<GrowthRecordResponse>,
    filter      : ChartFilter,
    accentStart : Color,
    accentEnd   : Color
) {
    val dimensions = LocalDimensions.current
    val maxMonths  = 24

    val parentRecords = remember(records) { records.filter { it.measuredByName.isNullOrBlank() } }
    val teamRecords   = remember(records) { records.filter { !it.measuredByName.isNullOrBlank() } }

    val whoRef = when (filter) {
        ChartFilter.WEIGHT -> whoWeight
        ChartFilter.HEIGHT -> whoHeight
        ChartFilter.HEAD   -> whoHead
        ChartFilter.ALL    -> whoWeight
    }

    val showW  = filter == ChartFilter.ALL || filter == ChartFilter.WEIGHT
    val showH  = filter == ChartFilter.ALL || filter == ChartFilter.HEIGHT
    val showHC = filter == ChartFilter.ALL || filter == ChartFilter.HEAD

    val allValues = buildList {
        if (showW)  addAll(records.mapNotNull { it.weight?.toFloat() })
        if (showH)  addAll(records.mapNotNull { it.height?.toFloat() })
        if (showHC) addAll(records.mapNotNull { it.headCircumference?.toFloat() })
        addAll(whoRef.values.map { it.first.toFloat() })
        addAll(whoRef.values.map { it.third.toFloat() })
    }
    val yMin = (allValues.minOrNull() ?: 0f) * 0.95f
    val yMax = (allValues.maxOrNull() ?: 100f) * 1.05f

    // Y-axis labels
    val yLabels = (0..5).map { i -> yMin + (yMax - yMin) / 5f * (5 - i) }

    val ageMonthsLabel = stringResource(Res.string.chart_age_months_label)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.avatarLarge * 4)
    ) {
        // ── Y-axis labels ──────────────────────────────────────────────────────
        yLabels.forEachIndexed { i, v ->
            val topFraction = i.toFloat() / 5f
            Text(
                text = v.toInt().toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.45f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(
                        top = (topFraction * 204).dp,   // 204 ≈ chart height minus padding
                        end = dimensions.spacingXSmall
                    ),
                textAlign = TextAlign.End
            )
        }

        // ── Chart canvas ──────────────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize().padding(dimensions.spacingXSmall)) {
            val w  = size.width;  val h  = size.height
            val cL = 44f;         val cR = w - 8f
            val cT = 8f;          val cB = h - 28f
            val cW = cR - cL;     val cH = cB - cT

            fun xOf(mo: Int)  = cL + (mo.toFloat() / maxMonths) * cW
            fun yOf(v: Float) = cB - ((v - yMin) / (yMax - yMin)) * cH

            // ── Grid ──────────────────────────────────────────────────────────
            val gc = Color.Gray.copy(0.1f)
            for (i in 0..5) {
                val y = cT + cH / 5f * i
                drawLine(gc, Offset(cL, y), Offset(cR, y), 1f)
            }
            listOf(0, 4, 8, 12, 16, 20, 24).forEach { mo ->
                drawLine(gc, Offset(xOf(mo), cT), Offset(xOf(mo), cB), 1f)
            }

            // ── Axes ──────────────────────────────────────────────────────────
            val ac = Color.Gray.copy(0.3f)
            drawLine(ac, Offset(cL, cT), Offset(cL, cB), 1.5f)
            drawLine(ac, Offset(cL, cB), Offset(cR, cB), 1.5f)

            val stdDash  = PathEffect.dashPathEffect(floatArrayOf(7f, 4f))
            val dataDash = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))

            // ── WHO reference curves ──────────────────────────────────────────
            val whoSorted = whoRef.keys.sorted()

            if (filter == ChartFilter.ALL) {
                // In "All" mode: only a faint WHO-50th weight reference
                val path50 = Path()
                whoSorted.forEachIndexed { i, mo ->
                    val v = whoRef[mo]!!.second.toFloat()
                    if (i == 0) path50.moveTo(xOf(mo), yOf(v))
                    else        path50.lineTo(xOf(mo), yOf(v))
                }
                drawPath(
                    path50,
                    WHO_50_COLOR.copy(alpha = 0.20f),
                    style = Stroke(1f, pathEffect = stdDash)
                )
            } else {
                // Single-metric tabs: all 3 WHO standard curves
                val curves: List<Pair<Color, (Triple<Double, Double, Double>) -> Float>> = listOf(
                    WHO_97_COLOR to { t: Triple<Double, Double, Double> -> t.third.toFloat()  },
                    WHO_50_COLOR to { t: Triple<Double, Double, Double> -> t.second.toFloat() },
                    WHO_3_COLOR  to { t: Triple<Double, Double, Double> -> t.first.toFloat()  }
                )
                curves.forEach { (col, get) ->
                    val path = Path()
                    whoSorted.forEachIndexed { i, mo ->
                        val v = get(whoRef[mo]!!)
                        if (i == 0) path.moveTo(xOf(mo), yOf(v))
                        else        path.lineTo(xOf(mo), yOf(v))
                    }
                    drawPath(path, col, style = Stroke(1.5f, pathEffect = stdDash))
                }
            }

            // ── Helper: draw a data series line ───────────────────────────────
            fun drawDataLine(
                recs  : List<GrowthRecordResponse>,
                get   : (GrowthRecordResponse) -> Double?,
                color : Color,
                dashed: Boolean = false,
                stroke: Float   = 2.5f
            ) {
                val pts = recs
                    .filter { get(it) != null }
                    .sortedBy { it.ageInMonths }
                    .map {
                        Offset(
                            xOf(it.ageInMonths.coerceIn(0, maxMonths)),
                            yOf(get(it)!!.toFloat())
                        )
                    }

                if (pts.size >= 2) {
                    val path = Path().apply {
                        moveTo(pts.first().x, pts.first().y)
                        pts.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(
                        path,
                        color,
                        style = Stroke(stroke, pathEffect = if (dashed) dataDash else null)
                    )
                }
                val dotRadius   = if (dashed) 4f else 5f
                val innerRadius = if (dashed) 2.5f else 3f
                pts.forEach {
                    drawCircle(color, dotRadius, it)
                    drawCircle(Color.White, innerRadius, it)
                }
            }

            // ── Data lines ────────────────────────────────────────────────────
            when (filter) {
                ChartFilter.ALL -> {
                    drawDataLine(parentRecords, { it.weight },            accentStart)
                    drawDataLine(parentRecords, { it.height },            accentEnd)
                    drawDataLine(parentRecords, { it.headCircumference }, HEAD_COLOR)
                    drawDataLine(teamRecords,   { it.weight },            accentStart, dashed = true)
                    drawDataLine(teamRecords,   { it.height },            accentEnd,   dashed = true)
                    drawDataLine(teamRecords,   { it.headCircumference }, HEAD_COLOR,  dashed = true)
                }
                ChartFilter.WEIGHT -> {
                    drawDataLine(parentRecords, { it.weight }, accentStart)
                    drawDataLine(teamRecords,   { it.weight }, TEAM_COLOR)
                }
                ChartFilter.HEIGHT -> {
                    drawDataLine(parentRecords, { it.height }, accentStart)
                    drawDataLine(teamRecords,   { it.height }, TEAM_COLOR)
                }
                ChartFilter.HEAD -> {
                    drawDataLine(parentRecords, { it.headCircumference }, accentStart)
                    drawDataLine(teamRecords,   { it.headCircumference }, TEAM_COLOR)
                }
            }
        } // end Canvas

        // ── Age axis labels ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(
                    start  = dimensions.iconLarge + dimensions.spacingXSmall,
                    bottom = dimensions.spacingXSmall,
                    end    = dimensions.spacingXSmall + dimensions.spacingXSmall
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

    } // end Box

    Text(
        ageMonthsLabel,
        style     = MaterialTheme.typography.labelSmall,
        color     = MaterialTheme.colorScheme.onSurface.copy(0.4f),
        modifier  = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// LEGEND
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChartLegend(
    babyName   : String,
    filter     : ChartFilter,
    accentStart: Color,
    accentEnd  : Color
) {
    val dimensions = LocalDimensions.current

    val legendParentAdded  = stringResource(Res.string.chart_legend_parent_added)
    val legendTeamAdded    = stringResource(Res.string.chart_legend_team_added)
    val legendWhoRef       = stringResource(Res.string.chart_legend_who_ref_weight)
    val legendWhoAvg       = stringResource(Res.string.chart_legend_who_avg)
    val legendBabyMeasure  = stringResource(Res.string.chart_legend_baby_measurements)
    val legendWhoStandards = stringResource(Res.string.chart_legend_who_standards)
    val weightKg           = stringResource(Res.string.chart_legend_weight_kg)
    val heightCm           = stringResource(Res.string.chart_legend_height_cm)
    val headCm             = stringResource(Res.string.chart_legend_head_cm)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(accentStart.copy(0.04f), RoundedCornerShape(dimensions.cardCornerRadius))
            .border(1.dp, accentStart.copy(0.1f), RoundedCornerShape(dimensions.cardCornerRadius))
            .padding(dimensions.spacingMedium),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall + 1.dp)
    ) {
        Text(
            stringResource(Res.string.chart_legend_title),
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface
        )

        when (filter) {

            ChartFilter.ALL -> {
                LegendSectionHeader(legendParentAdded)
                LegendRow(accentStart, weightKg, dashed = false, showDot = true)
                LegendRow(accentEnd,   heightCm, dashed = false, showDot = true)
                LegendRow(HEAD_COLOR,  headCm,   dashed = false, showDot = true)

                Spacer(Modifier.height(dimensions.spacingXSmall))

                LegendSectionHeader(legendTeamAdded)
                LegendRow(accentStart, weightKg, dashed = true, showDot = true)
                LegendRow(accentEnd,   heightCm, dashed = true, showDot = true)
                LegendRow(HEAD_COLOR,  headCm,   dashed = true, showDot = true)

                Spacer(Modifier.height(dimensions.spacingXSmall))

                LegendSectionHeader(legendWhoRef)
                LegendRow(WHO_50_COLOR.copy(alpha = 0.5f), legendWhoAvg, dashed = true, showDot = false)
            }

            ChartFilter.WEIGHT,
            ChartFilter.HEIGHT,
            ChartFilter.HEAD -> {
                val (metricLabel, metricUnit) = when (filter) {
                    ChartFilter.WEIGHT -> stringResource(Res.string.chart_filter_weight) to "kg"
                    ChartFilter.HEIGHT -> stringResource(Res.string.chart_filter_height) to "cm"
                    ChartFilter.HEAD   -> stringResource(Res.string.chart_filter_head)   to "cm"
                    else               -> "" to ""
                }

                LegendSectionHeader(legendBabyMeasure)
                LegendRow(
                    color   = accentStart,
                    label   = "👪  ${stringResource(Res.string.chart_legend_parent_prefix)} — $metricLabel ($metricUnit)",
                    dashed  = false,
                    showDot = true
                )
                LegendRow(
                    color   = TEAM_COLOR,
                    label   = "🏥  ${stringResource(Res.string.chart_legend_team_prefix)} — $metricLabel ($metricUnit)",
                    dashed  = false,
                    showDot = true
                )

                Spacer(Modifier.height(dimensions.spacingXSmall))

                LegendSectionHeader(legendWhoStandards)
                LegendRow(WHO_97_COLOR, stringResource(Res.string.chart_legend_who97), dashed = true, showDot = true)
                LegendRow(WHO_50_COLOR, stringResource(Res.string.chart_legend_who50), dashed = true, showDot = true)
                LegendRow(WHO_3_COLOR,  stringResource(Res.string.chart_legend_who3),  dashed = true, showDot = true)
            }
        }
    }
}

@Composable
private fun LegendSectionHeader(text: String) {
    Text(
        text,
        style         = MaterialTheme.typography.labelSmall,
        fontWeight    = FontWeight.Bold,
        color         = MaterialTheme.colorScheme.onSurface.copy(0.6f),
        letterSpacing = 0.3.sp
    )
}

@Composable
private fun LegendRow(
    color  : Color,
    label  : String,
    dashed : Boolean,
    showDot: Boolean
) {
    val dimensions = LocalDimensions.current
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        Canvas(Modifier.size(width = dimensions.spacingLarge + dimensions.spacingXSmall, height = dimensions.spacingSmall + dimensions.spacingXSmall)) {
            val y  = size.height / 2
            val pe = if (dashed) PathEffect.dashPathEffect(floatArrayOf(5f, 3f)) else null
            drawLine(color, Offset(0f, y), Offset(size.width, y), 2f, pathEffect = pe)
            if (!dashed) {
                drawCircle(color,       3.5f, Offset(size.width / 2, y))
                drawCircle(Color.White, 2f,   Offset(size.width / 2, y))
            }
        }

        Text(
            label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurface.copy(0.75f),
            modifier = Modifier.weight(1f)
        )

        if (showDot) {
            Box(Modifier.size(dimensions.spacingSmall + dimensions.spacingXSmall).background(color, CircleShape))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LATEST MEASUREMENT CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LatestMeasurementCard(
    latestGrowth: GrowthRecordResponse?,
    accentStart : Color,
    accentEnd   : Color,
    onViewAll   : () -> Unit,
    onAddMeasure: () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensions.screenPadding,
                vertical   = dimensions.spacingSmall
            ),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius + dimensions.spacingXSmall),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(dimensions.cardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Text(
                    stringResource(Res.string.chart_latest_measurement),
                    style         = MaterialTheme.typography.labelMedium,
                    fontWeight    = FontWeight.Bold,
                    color         = accentStart,
                    letterSpacing = 0.8.sp
                )
                if (latestGrowth != null) {
                    OutlinedButton(
                        onClick        = onViewAll,
                        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                        border         = BorderStroke(1.dp, accentStart.copy(0.5f)),
                        colors         = ButtonDefaults.outlinedButtonColors(contentColor = accentStart),
                        contentPadding = PaddingValues(
                            horizontal = dimensions.spacingSmall + dimensions.spacingXSmall,
                            vertical   = dimensions.spacingXSmall
                        )
                    ) {
                        Text(
                            stringResource(Res.string.chart_view_all),
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (latestGrowth == null) {
                // ── Empty state ───────────────────────────────────────────────
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensions.spacingMedium),
                    Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📏", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(dimensions.spacingSmall))
                        Text(
                            stringResource(Res.string.chart_no_measurement),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = accentStart
                        )
                        Text(
                            stringResource(Res.string.chart_no_measurement_desc),
                            style     = MaterialTheme.typography.bodySmall,
                            color     = MaterialTheme.colorScheme.onSurface.copy(0.55f),
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(
                                horizontal = dimensions.spacingSmall,
                                vertical   = dimensions.spacingXSmall
                            )
                        )
                    }
                }
            } else {
                // ── Date + recorder tag ───────────────────────────────────────
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    MeasureRow("📅", formatChartDate(latestGrowth.measurementDate))
                    val measurer = latestGrowth.measuredByName
                    if (!measurer.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .background(
                                    TEAM_COLOR.copy(0.12f),
                                    RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                )
                                .border(
                                    1.dp,
                                    TEAM_COLOR.copy(0.3f),
                                    RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                )
                                .padding(
                                    horizontal = dimensions.spacingXSmall + dimensions.spacingXSmall,
                                    vertical   = dimensions.spacingXSmall / 2
                                )
                        ) {
                            Text(
                                "🏥 $measurer",
                                style = MaterialTheme.typography.labelSmall,
                                color = TEAM_COLOR
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(
                                    accentStart.copy(0.10f),
                                    RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                )
                                .border(
                                    1.dp,
                                    accentStart.copy(0.25f),
                                    RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                )
                                .padding(
                                    horizontal = dimensions.spacingXSmall + dimensions.spacingXSmall,
                                    vertical   = dimensions.spacingXSmall / 2
                                )
                        ) {
                            Text(
                                "👪 ${stringResource(Res.string.chart_legend_parent_prefix)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentStart
                            )
                        }
                    }
                }

                // ── Three measurements (always visible, — if null) ─────────────
                MeasureRow(
                    icon = "⚖️",
                    text = if (latestGrowth.weight != null)
                        stringResource(Res.string.chart_weight_unit, latestGrowth.weight.toString())
                    else stringResource(Res.string.chart_weight_empty)
                )
                MeasureRow(
                    icon = "📏",
                    text = if (latestGrowth.height != null)
                        stringResource(Res.string.chart_height_unit, latestGrowth.height.toString())
                    else stringResource(Res.string.chart_height_empty)
                )
                MeasureRow(
                    icon = "🔵",
                    text = if (latestGrowth.headCircumference != null)
                        stringResource(Res.string.chart_head_unit, latestGrowth.headCircumference.toString())
                    else stringResource(Res.string.chart_head_empty)
                )

                // ── Percentiles ───────────────────────────────────────────────
                val hasPerc = latestGrowth.weightPercentile != null ||
                        latestGrowth.heightPercentile != null ||
                        latestGrowth.headCircumferencePercentile != null
                if (hasPerc) {
                    HorizontalDivider(
                        color    = accentStart.copy(0.1f),
                        modifier = Modifier.padding(vertical = dimensions.spacingXSmall)
                    )
                    Text(
                        "📊 " + stringResource(Res.string.chart_percentiles),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    latestGrowth.weightPercentile?.let {
                        PctRow(stringResource(Res.string.chart_filter_weight), it)
                    }
                    latestGrowth.heightPercentile?.let {
                        PctRow(stringResource(Res.string.chart_filter_height), it)
                    }
                    latestGrowth.headCircumferencePercentile?.let {
                        PctRow(stringResource(Res.string.chart_filter_head), it)
                    }
                }
            }

            HorizontalDivider(color = accentStart.copy(0.1f))

            // ── Footer: history label + Add Measurement button ────────────────
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Text(
                    stringResource(Res.string.chart_measurement_history),
                    style         = MaterialTheme.typography.labelSmall,
                    color         = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimensions.buttonCornerRadius))
                        .background(Brush.horizontalGradient(listOf(accentStart, accentEnd)))
                        .clickable { onAddMeasure() }
                        .padding(
                            horizontal = dimensions.spacingMedium - dimensions.spacingXSmall,
                            vertical   = dimensions.spacingSmall
                        ),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall + dimensions.spacingXSmall)
                ) {
                    Text(
                        stringResource(Res.string.chart_add_measurement),
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    )
                    Icon(
                        Icons.Default.Add,
                        null,
                        tint     = Color.White,
                        modifier = Modifier.size(dimensions.iconSmall)
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
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        Text(icon, style = MaterialTheme.typography.bodyMedium)
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(0.85f)
        )
    }
}

@Composable
private fun PctRow(label: String, pct: Int) {
    val customColors = MaterialTheme.customColors
    val (status, color) = when {
        pct > 85 -> stringResource(Res.string.chart_above_avg) to customColors.warning
        pct < 15 -> stringResource(Res.string.chart_below_avg) to MaterialTheme.colorScheme.error
        else     -> stringResource(Res.string.chart_normal)    to customColors.success
    }
    val dimensions = LocalDimensions.current
    Row(
        Modifier.fillMaxWidth(),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        Text(
            "$label: ${pct}th",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(0.75f)
        )
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
        ) {
            Text("($status)", style = MaterialTheme.typography.bodySmall, color = color)
            Icon(
                Icons.Default.CheckCircle,
                null,
                tint     = color,
                modifier = Modifier.size(dimensions.iconSmall - dimensions.spacingXSmall / 2)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EMPTY CHART BOX
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyChartBox(accentStart: Color, title: String, desc: String, emoji: String) {
    val dimensions = LocalDimensions.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.avatarLarge * 3)
            .background(accentStart.copy(0.05f), RoundedCornerShape(dimensions.cardCornerRadius))
            .border(1.dp, accentStart.copy(0.15f), RoundedCornerShape(dimensions.cardCornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(
                title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = accentStart
            )
            Text(
                desc,
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurface.copy(0.55f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(
                    horizontal = dimensions.spacingLarge,
                    vertical   = dimensions.spacingXSmall
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ALL MEASUREMENTS SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AllMeasurementsScreen(
    babyId   : String,
    babyName : String,
    isFemale : Boolean,
    viewModel: HomeViewModel,
    onBack   : () -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val accentStart  = customColors.accentGradientStart
    val accentEnd    = customColors.accentGradientEnd
    val state        = viewModel.uiState
    val records      = (state.allGrowthRecords[babyId] ?: emptyList())
        .sortedByDescending { it.measurementDate }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(accentStart, accentEnd)))
                    .padding(
                        horizontal = dimensions.spacingSmall,
                        vertical   = dimensions.spacingSmall
                    )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Text(
                        stringResource(Res.string.all_measures_title),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                        modifier   = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                Color.White.copy(0.22f),
                                RoundedCornerShape(dimensions.buttonCornerRadius)
                            )
                            .padding(
                                horizontal = dimensions.spacingSmall + dimensions.spacingXSmall,
                                vertical   = dimensions.spacingXSmall
                            )
                    ) {
                        Text(
                            "${if (isFemale) "👶" else "👦"} $babyName",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = Color.White,
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
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = MaterialTheme.colorScheme.onBackground.copy(0.55f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(
                    horizontal = dimensions.screenPadding,
                    vertical   = dimensions.spacingMedium
                ),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                items(records, key = { it.recordId }) { record ->
                    MeasurementCard(
                        record      = record,
                        accentStart = accentStart,
                        onDelete    = { viewModel.deleteGrowthRecord(babyId, record.recordId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MeasurementCard(
    record     : GrowthRecordResponse,
    accentStart: Color,
    onDelete   : () -> Unit
) {
    val dimensions = LocalDimensions.current
    var showDel by remember { mutableStateOf(false) }

    if (showDel) {
        AlertDialog(
            onDismissRequest = { showDel = false },
            title   = { Text(stringResource(Res.string.chart_delete_measurement_title)) },
            text    = {
                Text(
                    stringResource(
                        Res.string.chart_delete_measurement_desc,
                        formatChartDate(record.measurementDate)
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
                TextButton(onClick = { showDel = false }) {
                    Text(stringResource(Res.string.add_measure_cancel))
                }
            }
        )
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(dimensions.spacingXSmall / 2)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.Top
        ) {
            // Date badge
            val parts = record.measurementDate.split("-")
            Box(
                modifier = Modifier
                    .size(dimensions.avatarMedium + dimensions.spacingXSmall)
                    .background(
                        accentStart.copy(0.12f),
                        RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                    )
                    .border(
                        1.dp,
                        accentStart.copy(0.3f),
                        RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        parts.getOrNull(2) ?: "--",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = accentStart
                    )
                    Text(
                        monthAbbrev(parts.getOrNull(1)?.toIntOrNull() ?: 0),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentStart.copy(0.8f)
                    )
                }
            }

            Spacer(Modifier.width(dimensions.spacingSmall + dimensions.spacingXSmall))

            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall - 1.dp)
            ) {
                val measurer = record.measuredByName
                if (!measurer.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .background(
                                TEAM_COLOR.copy(0.12f),
                                RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                            )
                            .padding(
                                horizontal = dimensions.spacingXSmall + dimensions.spacingXSmall,
                                vertical   = dimensions.spacingXSmall / 2
                            ),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("🏥", style = MaterialTheme.typography.labelSmall)
                        Text(
                            measurer,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = TEAM_COLOR,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .background(
                                accentStart.copy(0.10f),
                                RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                            )
                            .padding(
                                horizontal = dimensions.spacingXSmall + dimensions.spacingXSmall,
                                vertical   = dimensions.spacingXSmall / 2
                            ),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("👪", style = MaterialTheme.typography.labelSmall)
                        Text(
                            stringResource(Res.string.chart_legend_parent_prefix),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentStart
                        )
                    }
                }

                Spacer(Modifier.height(dimensions.spacingXSmall / 2))

                Text(
                    "⚖️  ${record.weight?.let { "$it kg" } ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                )
                Text(
                    "📏  ${record.height?.let { "$it cm" } ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                )
                Text(
                    "🔵  ${record.headCircumference?.let { "$it cm" } ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                )
            }

            // Delete icon
            IconButton(
                onClick  = { showDel = true },
                modifier = Modifier.size(dimensions.iconLarge)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.all_measures_delete),
                    tint     = MaterialTheme.colorScheme.error.copy(0.6f),
                    modifier = Modifier.size(dimensions.iconMedium)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

private fun monthAbbrev(month: Int): String = when (month) {
    1 -> "JAN"; 2 -> "FEB"; 3 -> "MAR"; 4 -> "APR"
    5 -> "MAY"; 6 -> "JUN"; 7 -> "JUL"; 8 -> "AUG"
    9 -> "SEP"; 10 -> "OCT"; 11 -> "NOV"; 12 -> "DEC"
    else -> "---"
}

private fun formatChartDate(raw: String): String {
    return try {
        val parts = raw.split("-")
        if (parts.size == 3) "${parts[2]} ${monthAbbrev(parts[1].toInt())} ${parts[0]}"
        else raw
    } catch (_: Exception) { raw }
}
package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchDetailScreen(
    bench           : VaccinationBenchUi,
    babyName        : String,
    isLoading       : Boolean,
    onBack          : () -> Unit,
    onAssign        : () -> Unit,
    onCreateSchedule: () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    var showConfirm  by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(Res.string.bench_assign_confirm_title)) },
            text  = { Text(stringResource(Res.string.bench_assign_confirm_body, babyName)) },
            confirmButton = {
                Button(
                    onClick = { showConfirm = false; onAssign(); onCreateSchedule() },
                    colors  = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
                ) { Text(stringResource(Res.string.bench_assign_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(stringResource(Res.string.bench_assign_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.bench_detail_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = customColors.accentGradientStart.copy(0.12f)
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = dimensions.spacingSmall) {
                Button(
                    onClick  = { showConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.screenPadding)
                        .height(dimensions.buttonHeight),
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors   = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart),
                    enabled  = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color       = MaterialTheme.colorScheme.onPrimary,  // WAS: Color.White
                            modifier    = Modifier.size(dimensions.iconMedium),
                            strokeWidth = dimensions.borderWidthMedium          // WAS: 2.dp
                        )
                    } else {
                        Text(
                            text       = stringResource(Res.string.bench_assign_for, babyName),
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // ── Hero gradient header ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                customColors.accentGradientStart.copy(0.85f),
                                customColors.accentGradientEnd.copy(0.70f)
                            )
                        )
                    )
                    .padding(dimensions.spacingLarge)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                    Text(
                        text       = "🏥 ${bench.type}",
                        style      = MaterialTheme.typography.labelMedium,
                        color      = MaterialTheme.colorScheme.onPrimary.copy(0.85f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text       = bench.nameEn,
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text  = "${bench.governorate} • ${bench.district}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(0.8f)
                    )

                    bench.distanceKm?.let { km ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                tint     = customColors.accentGradientEnd,
                                modifier = Modifier.size(dimensions.benchDistanceIconSize) // WAS: 16.dp
                            )
                            Text(
                                text       = stringResource(Res.string.bench_distance_km, km),
                                style      = MaterialTheme.typography.labelMedium,
                                color      = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(dimensions.spacingMedium))

            // ── Info sections ─────────────────────────────────────────────────
            DetailSection(title = stringResource(Res.string.bench_section_location)) { // WAS: "📍 Location"
                DetailRow(Icons.Default.LocationOn, stringResource(Res.string.bench_label_governorate), bench.governorate)
                DetailRow(Icons.Default.Place,      stringResource(Res.string.bench_label_district),    bench.district)
                bench.addressEn?.let   { DetailRow(Icons.Default.Home,       stringResource(Res.string.bench_label_address),    it) }
                bench.directionEn?.let { DetailRow(Icons.Default.Navigation, stringResource(Res.string.bench_label_directions), it) }
            }

            DetailSection(title = stringResource(Res.string.bench_section_contact)) {  // WAS: "📞 Contact"
                bench.phone?.let { DetailRow(Icons.Default.Phone, stringResource(Res.string.bench_label_phone), it) }
                    ?: Text(
                        stringResource(Res.string.bench_no_phone),
                        modifier = Modifier.padding(horizontal = dimensions.screenPadding),
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
            }

            DetailSection(title = stringResource(Res.string.bench_section_hours)) {    // WAS: "🕐 Working Hours"
                DetailRow(
                    Icons.Default.AccessTime,
                    stringResource(Res.string.bench_label_hours),
                    "${bench.workingHoursStart} – ${bench.workingHoursEnd}"
                )
                DetailRow(
                    Icons.Default.CalendarToday,
                    stringResource(Res.string.bench_label_days),
                    bench.workingDays.joinToString(", ")
                )
            }

            DetailSection(title = stringResource(Res.string.bench_section_services)) { // WAS: "💉 Vaccination Days"
                DetailRow(
                    Icons.Default.Vaccines,
                    stringResource(Res.string.bench_label_vax_days),
                    bench.vaccinationDays.joinToString(", ").ifEmpty { stringResource(Res.string.bench_not_specified) }
                )
                if (bench.vaccinesAvailable.isNotEmpty()) {
                    DetailRow(
                        Icons.Default.MedicalServices,
                        stringResource(Res.string.bench_label_vaccines),
                        bench.vaccinesAvailable.joinToString(", ")
                    )
                }
            }

            Spacer(Modifier.height(dimensions.spacingXXLarge))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION + ROW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DetailSection(
    title  : String,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimensions = LocalDimensions.current
    Column(
        modifier = Modifier.padding(
            horizontal = dimensions.screenPadding,
            vertical   = dimensions.spacingSmall
        )
    ) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground,
            modifier   = Modifier.padding(bottom = dimensions.spacingSmall)
        )
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(dimensions.cardCornerRadius),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(dimensions.cardElevation / 4)
        ) {
            Column(modifier = Modifier.padding(dimensions.spacingMedium)) {
                content()
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensions.detailRowVertPadding),            // WAS: 4.dp
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
        verticalAlignment     = Alignment.Top
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier
                .size(dimensions.detailIconSize)                              // WAS: 18.dp
                .padding(top = dimensions.detailIconTopPadding),             // WAS: 2.dp
            tint               = customColors.accentGradientStart
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
            )
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
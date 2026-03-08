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
    bench: VaccinationBenchUi,
    babyName: String,
    isLoading: Boolean,
    onBack: () -> Unit,
    onAssign: () -> Unit,
    onCreateSchedule: () -> Unit
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(Res.string.bench_assign_confirm_title)) },
            text = {
                Text(stringResource(Res.string.bench_assign_confirm_body, babyName))
            },
            confirmButton = {
                Button(
                    onClick = { showConfirm = false; onAssign(); onCreateSchedule() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.accentGradientStart
                    )
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
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { showConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.screenPadding)
                        .height(dimensions.buttonHeight),
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.accentGradientStart
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.bench_assign_for, babyName),
                            style = MaterialTheme.typography.labelLarge,
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
                                customColors.accentGradientStart.copy(0.18f),
                                customColors.accentGradientEnd.copy(0.08f)
                            )
                        )
                    )
                    .padding(dimensions.spacingLarge)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = customColors.accentGradientStart.copy(0.15f)
                    ) {
                        Text(
                            text = "🏥 ${bench.type}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = customColors.accentGradientStart,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = bench.nameEn,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = bench.nameAr,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                    )
                    if (bench.nameKuSorani.isNotBlank()) {
                        Text(
                            text = bench.nameKuSorani,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.5f)
                        )
                    }
                    bench.distanceKm?.let {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                tint = customColors.accentGradientEnd,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "📍 %.1f km away".toInt().toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = customColors.accentGradientEnd,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(dimensions.spacingMedium))

            // ── Info sections ─────────────────────────────────────────────────
            val padH = PaddingValues(horizontal = dimensions.screenPadding)

            DetailSection(title = "📍 Location") {
                DetailRow(Icons.Default.LocationOn, "Governorate", bench.governorate)
                DetailRow(Icons.Default.Place, "District", bench.district)
                bench.addressEn?.let { DetailRow(Icons.Default.Home, "Address", it) }
                bench.directionEn?.let { DetailRow(Icons.Default.Navigation, "Directions", it) }
            }

            DetailSection(title = "📞 Contact") {
                bench.phone?.let { DetailRow(Icons.Default.Phone, "Phone", it) }
                    ?: Text(
                        "No phone listed",
                        modifier = Modifier.padding(horizontal = dimensions.screenPadding),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
            }

            DetailSection(title = "🕐 Working Hours") {
                DetailRow(Icons.Default.AccessTime, "Hours",
                    "${bench.workingHoursStart} – ${bench.workingHoursEnd}")
                DetailRow(Icons.Default.CalendarToday, "Days",
                    bench.workingDays.joinToString(", "))
            }

            DetailSection(title = "💉 Vaccination Days") {
                DetailRow(Icons.Default.Vaccines, "Vaccination days",
                    bench.vaccinationDays.joinToString(", ").ifEmpty { "Not specified" })
                if (bench.vaccinesAvailable.isNotEmpty()) {
                    DetailRow(Icons.Default.MedicalServices, "Vaccines",
                        bench.vaccinesAvailable.joinToString(", "))
                }
            }

            Spacer(Modifier.height(dimensions.spacingXXLarge))
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimensions = LocalDimensions.current
    Column(
        modifier = Modifier.padding(
            horizontal = dimensions.screenPadding,
            vertical = dimensions.spacingSmall
        )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = dimensions.spacingSmall)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensions.cardCornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(dimensions.spacingMedium)) {
                content()
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    val dimensions = LocalDimensions.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp).padding(top = 2.dp),
            tint = MaterialTheme.customColors.accentGradientStart
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
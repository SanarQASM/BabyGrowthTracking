// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/com/babygrowth/presentation/screens/home/screen/BenchRequestDetailScreen.kt
// Replaces the old BenchDetailScreen for the parent flow.
// The "Assign" button is now a "Send Request" button.

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
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.data.network.VaccinationBenchUi
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchRequestDetailScreen(
    bench          : VaccinationBenchUi,
    babyName       : String,
    isSubmitting   : Boolean,
    onBack         : () -> Unit,
    onSendRequest  : () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    var showConfirm  by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(Res.string.bench_send_request_confirm_title)) },
            text  = { Text(stringResource(Res.string.bench_send_request_confirm_body, babyName, bench.nameEn)) },
            confirmButton = {
                Button(
                    onClick = { showConfirm = false; onSendRequest() },
                    colors  = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
                ) { Text(stringResource(Res.string.bench_send_request_confirm_ok)) }
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
                title = { Text(stringResource(Res.string.bench_detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(tonalElevation = dimensions.cardElevation + dimensions.spacingSmall) {
                Button(
                    onClick  = { showConfirm = true },
                    enabled  = !isSubmitting,
                    modifier = Modifier.fillMaxWidth().padding(dimensions.screenPadding),
                    colors   = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart),
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(dimensions.iconSmall + dimensions.borderWidthMedium),
                            strokeWidth = dimensions.borderWidthMedium,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(dimensions.spacingSmall))
                    }
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(dimensions.iconSmall))
                    Spacer(Modifier.width(dimensions.spacingSmall))
                    Text(text = stringResource(Res.string.bench_send_request_action), fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                customColors.accentGradientStart.copy(0.15f),
                                customColors.accentGradientEnd.copy(0.05f)
                            )
                        )
                    )
                    .padding(dimensions.screenPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏥", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(dimensions.spacingSmall))
                    Text(text = bench.nameEn, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = bench.type, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    bench.distanceKm?.let { km ->
                        Spacer(Modifier.height(dimensions.spacingSmall))
                        Surface(shape = RoundedCornerShape(50), color = customColors.accentGradientStart.copy(0.85f)) {
                            Row(
                                modifier              = Modifier.padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingXSmall),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = customColors.accentGradientEnd, modifier = Modifier.size(dimensions.benchDistanceIconSize))
                                Text(text = stringResource(Res.string.bench_distance_km, km), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(dimensions.spacingMedium))

            // Info sections
            BenchDetailSectionCard(title = stringResource(Res.string.bench_section_location)) {
                BenchDetailRow(Icons.Default.LocationOn, stringResource(Res.string.bench_label_governorate), bench.governorate)
                BenchDetailRow(Icons.Default.Place, stringResource(Res.string.bench_label_district), bench.district)
                bench.addressEn?.let { BenchDetailRow(Icons.Default.Home, stringResource(Res.string.bench_label_address), it) }
            }
            BenchDetailSectionCard(title = stringResource(Res.string.bench_section_contact)) {
                bench.phone?.let { BenchDetailRow(Icons.Default.Phone, stringResource(Res.string.bench_label_phone), it) }
                    ?: Text(stringResource(Res.string.bench_no_phone), modifier = Modifier.padding(horizontal = dimensions.screenPadding), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
            }
            BenchDetailSectionCard(title = stringResource(Res.string.bench_section_hours)) {
                BenchDetailRow(Icons.Default.AccessTime, stringResource(Res.string.bench_label_hours), "${bench.workingHoursStart} – ${bench.workingHoursEnd}")
                BenchDetailRow(Icons.Default.CalendarToday, stringResource(Res.string.bench_label_days), bench.workingDays.joinToString(", "))
            }
            BenchDetailSectionCard(title = stringResource(Res.string.bench_section_services)) {
                BenchDetailRow(Icons.Default.Vaccines, stringResource(Res.string.bench_label_vax_days), bench.vaccinationDays.joinToString(", ").ifEmpty { stringResource(Res.string.bench_not_specified) })
                if (bench.vaccinesAvailable.isNotEmpty()) {
                    BenchDetailRow(Icons.Default.MedicalServices, stringResource(Res.string.bench_label_vaccines), bench.vaccinesAvailable.joinToString(", "))
                }
            }

            Spacer(Modifier.height(dimensions.spacingXXLarge))
        }
    }
}

@Composable
private fun BenchDetailSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    Column(modifier = Modifier.padding(vertical = dimensions.spacingSmall)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = customColors.accentGradientStart, modifier = Modifier.padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall))
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = dimensions.screenPadding), shape = RoundedCornerShape(dimensions.cardCornerRadius), elevation = CardDefaults.cardElevation(dimensions.borderWidthThin)) {
            Column(modifier = Modifier.padding(vertical = dimensions.spacingSmall), content = content)
        }
    }
}

@Composable
private fun BenchDetailRow(icon: ImageVector, label: String, value: String) {
    val dimensions = LocalDimensions.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = dimensions.screenPadding, vertical = dimensions.detailRowVertPadding),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
        verticalAlignment     = Alignment.Top
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(0.5f), modifier = Modifier.size(dimensions.detailIconSize).padding(top = dimensions.detailIconTopPadding))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
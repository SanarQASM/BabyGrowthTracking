// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminBenchesScreen.kt

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.VaccinationBenchUi
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.*
import androidx.compose.runtime.rememberCoroutineScope

// ─────────────────────────────────────────────────────────────────────────────
// AdminBenchesScreen
// Admin can view, create, and deactivate health centers.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminBenchesScreen(
    apiService : ApiService,
    modifier   : Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val scope        = rememberCoroutineScope()

    var benches         by remember { mutableStateOf<List<VaccinationBenchUi>>(emptyList()) }
    var isLoading       by remember { mutableStateOf(true) }
    var showAddDialog   by remember { mutableStateOf(false) }
    var pendingDelete   by remember { mutableStateOf<VaccinationBenchUi?>(null) }
    var snackMsg        by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    fun loadBenches() {
        scope.launch {
            isLoading = true
            val result = apiService.getAllBenches()
            if (result is ApiResult.Success) benches = result.data
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadBenches() }
    LaunchedEffect(snackMsg) {
        snackMsg?.let { snackbarHostState.showSnackbar(it); snackMsg = null }
    }

    // ── Delete confirmation ───────────────────────────────────────────────────
    pendingDelete?.let { bench ->
        AdminConfirmDialog(
            title        = stringResource(Res.string.admin_bench_delete_title),
            message      = stringResource(Res.string.admin_bench_delete_message, bench.nameEn),
            confirmLabel = stringResource(Res.string.admin_action_delete),
            onConfirm    = {
                scope.launch {
                    // Deactivate bench via API
                    apiService.deactivateBench(bench.benchId)
                    snackMsg = "admin_bench_deleted_success"
                    loadBenches()
                }
                pendingDelete = null
            },
            onDismiss    = { pendingDelete = null }
        )
    }

    // ── Add dialog ────────────────────────────────────────────────────────────
    if (showAddDialog) {
        AddBenchDialog(
            onDismiss = { showAddDialog = false },
            onSave    = { req ->
                scope.launch {
                    apiService.createBench(req)
                    snackMsg = "admin_bench_created_success"
                    loadBenches()
                }
                showAddDialog = false
            }
        )
    }

    Scaffold(
        modifier     = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = dimensions.screenPadding),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Spacer(Modifier.height(dimensions.spacingSmall))

            // Title row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = stringResource(Res.string.admin_bench_title),
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.weight(1f)
                )
                FilledIconButton(
                    onClick = { showAddDialog = true },
                    colors  = IconButtonDefaults.filledIconButtonColors(containerColor = customColors.accentGradientStart)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.admin_bench_add_action), tint = MaterialTheme.colorScheme.onPrimary)
                }
            }

            Text(
                text  = stringResource(Res.string.admin_bench_count, benches.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            } else if (benches.isEmpty()) {
                AdminEmptyState(icon = Icons.Default.LocalHospital, message = stringResource(Res.string.admin_bench_no_results))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                    contentPadding      = PaddingValues(vertical = dimensions.spacingSmall)
                ) {
                    items(benches, key = { it.benchId }) { bench ->
                        AdminBenchCard(
                            bench    = bench,
                            onDelete = { pendingDelete = bench }
                        )
                    }
                    item { Spacer(Modifier.height(dimensions.spacingXLarge)) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bench card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminBenchCard(bench: VaccinationBenchUi, onDelete: () -> Unit) {
    val dimensions = LocalDimensions.current

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevationSmall),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(bench.nameEn, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(bench.nameAr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${bench.governorate} · ${bench.district}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                    AdminStatusBadge(label = bench.type, color = MaterialTheme.customColors.accentGradientStart)
                    if (!bench.isActive) {
                        AdminStatusBadge(label = "Inactive", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(Res.string.admin_delete_cd), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add bench dialog
// ─────────────────────────────────────────────────────────────────────────────

data class CreateBenchFormRequest(
    val nameEn          : String,
    val nameAr          : String,
    val governorate     : String,
    val district        : String,
    val addressEn       : String,
    val latitude        : String,
    val longitude       : String,
    val phone           : String,
    val workingDays     : String,   // comma-separated e.g. "Sunday,Monday"
    val vaccinationDays : String,   // comma-separated
    val workingHoursStart: String,
    val workingHoursEnd  : String,
    val vaccinesAvailable: String   // comma-separated
)

@Composable
private fun AddBenchDialog(
    onDismiss : () -> Unit,
    onSave    : (CreateBenchFormRequest) -> Unit
) {
    val dimensions = LocalDimensions.current

    var nameEn           by remember { mutableStateOf("") }
    var nameAr           by remember { mutableStateOf("") }
    var governorate      by remember { mutableStateOf("") }
    var district         by remember { mutableStateOf("") }
    var addressEn        by remember { mutableStateOf("") }
    var latitude         by remember { mutableStateOf("") }
    var longitude        by remember { mutableStateOf("") }
    var phone            by remember { mutableStateOf("") }
    var workingDays      by remember { mutableStateOf("Sunday,Monday,Tuesday,Wednesday,Thursday") }
    var vaccinationDays  by remember { mutableStateOf("Sunday,Tuesday,Thursday") }
    var hoursStart       by remember { mutableStateOf("08:00") }
    var hoursEnd         by remember { mutableStateOf("14:00") }
    var vaccines         by remember { mutableStateOf("") }
    var error            by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title            = { Text(stringResource(Res.string.admin_bench_add_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = dimensions.avatarLarge * 5)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                @Composable
                fun field(value: String, onChange: (String) -> Unit, label: String, required: Boolean = false) {
                    OutlinedTextField(
                        value         = value,
                        onValueChange = onChange,
                        label         = { Text(if (required) "$label *" else label) },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        shape         = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.customColors.accentGradientStart)
                    )
                }

                field(nameEn, { nameEn = it }, stringResource(Res.string.admin_bench_field_name_en), required = true)
                field(nameAr, { nameAr = it }, stringResource(Res.string.admin_bench_field_name_ar), required = true)
                field(governorate, { governorate = it }, stringResource(Res.string.admin_bench_field_governorate), required = true)
                field(district, { district = it }, stringResource(Res.string.admin_bench_field_district))
                field(addressEn, { addressEn = it }, stringResource(Res.string.admin_bench_field_address_en))
                field(latitude, { latitude = it }, stringResource(Res.string.admin_bench_field_latitude), required = true)
                field(longitude, { longitude = it }, stringResource(Res.string.admin_bench_field_longitude), required = true)
                field(phone, { phone = it }, stringResource(Res.string.admin_bench_field_phone))
                field(workingDays, { workingDays = it }, stringResource(Res.string.admin_bench_field_working_days))
                field(vaccinationDays, { vaccinationDays = it }, stringResource(Res.string.admin_bench_field_vac_days))
                field(hoursStart, { hoursStart = it }, stringResource(Res.string.admin_bench_field_hours_start))
                field(hoursEnd, { hoursEnd = it }, stringResource(Res.string.admin_bench_field_hours_end))
                field(vaccines, { vaccines = it }, stringResource(Res.string.admin_bench_field_vaccines))

                error?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameEn.isBlank() || nameAr.isBlank() || governorate.isBlank() || latitude.isBlank() || longitude.isBlank()) {
                        error = "Please fill all required fields (*)"
                        return@Button
                    }
                    if (latitude.toDoubleOrNull() == null || longitude.toDoubleOrNull() == null) {
                        error = "Latitude and longitude must be valid numbers"
                        return@Button
                    }
                    onSave(
                        CreateBenchFormRequest(
                            nameEn           = nameEn.trim(),
                            nameAr           = nameAr.trim(),
                            governorate      = governorate.trim(),
                            district         = district.trim(),
                            addressEn        = addressEn.trim(),
                            latitude         = latitude.trim(),
                            longitude        = longitude.trim(),
                            phone            = phone.trim(),
                            workingDays      = workingDays.trim(),
                            vaccinationDays  = vaccinationDays.trim(),
                            workingHoursStart = hoursStart.trim(),
                            workingHoursEnd   = hoursEnd.trim(),
                            vaccinesAvailable = vaccines.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.customColors.accentGradientStart)
            ) { Text(stringResource(Res.string.admin_bench_add_action)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) }
        }
    )
}
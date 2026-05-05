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
import androidx.compose.ui.unit.dp
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.UserResponse
// FIX: was importing VaccinationBenchUi from the wrong package
// (com.babygrowth.presentation.screens.home.model). The canonical definition
// lives in data.network — that is the type ApiService.getAllBenches() returns.
import org.example.project.babygrowthtrackingapplication.data.network.VaccinationBenchUi
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource
import kotlin.math.pow

private val ALL_DAYS = listOf(
    "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
)

private data class AddBenchFormState(
    val nameEn                  : String      = "",
    val nameAr                  : String      = "",
    val governorate             : String      = "",
    val district                : String      = "",
    val addressEn               : String      = "",
    val phone                   : String      = "",
    val latitude                : Double?     = null,
    val longitude               : Double?     = null,
    val selectedWorkingDays     : Set<String> = setOf("Sunday","Monday","Tuesday","Wednesday","Thursday"),
    val selectedVaccinationDays : Set<String> = setOf("Sunday","Tuesday","Thursday"),
    val workingHoursStart       : String      = "08:00",
    val workingHoursEnd         : String      = "14:00",
    val vaccinesText            : String      = "",
    val teamMemberId            : String?     = null,
    val teamMemberName          : String      = "",
    val error                   : String?     = null,
    val showTeamDropdown        : Boolean     = false,
    val showMapPicker           : Boolean     = false,
)

// ─────────────────────────────────────────────────────────────────────────────
// CreateBenchFormRequest — plain data class (not @Serializable itself;
// ApiService converts it to the internal @Serializable CreateBenchRequest).
// ─────────────────────────────────────────────────────────────────────────────
data class CreateBenchFormRequest(
    val nameEn            : String,
    val nameAr            : String,
    val governorate       : String,
    val district          : String,
    val addressEn         : String,
    val latitude          : Double,
    val longitude         : Double,
    val phone             : String,
    val workingDays       : List<String>,
    val vaccinationDays   : List<String>,
    val workingHoursStart : String,
    val workingHoursEnd   : String,
    val vaccinesAvailable : List<String>,
    val teamMemberId      : String? = null,
)

// ─────────────────────────────────────────────────────────────────────────────
// KMP-safe coordinate formatter
// FIX: String.format() / "%.4f".format() is JVM-only and unresolved in KMP.
// roundTo(4) from kotlin.math rounds to 4 decimal places; we build the string
// manually so it compiles on all KMP targets (Android, iOS, Desktop).
// ─────────────────────────────────────────────────────────────────────────────
private fun Double.toCoordString(decimals: Int = 4): String {
    val factor = 10.0.pow(decimals)
    val rounded = kotlin.math.round(this * factor) / factor
    // Convert to string and pad/trim to exactly `decimals` decimal places.
    val raw = rounded.toString()
    val dotIndex = raw.indexOf('.')
    return if (dotIndex < 0) {
        raw + "." + "0".repeat(decimals)
    } else {
        val currentDecimals = raw.length - dotIndex - 1
        when {
            currentDecimals < decimals -> raw + "0".repeat(decimals - currentDecimals)
            currentDecimals > decimals -> raw.substring(0, dotIndex + decimals + 1)
            else                       -> raw
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminBenchesScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminBenchesScreen(
    apiService : ApiService,
    modifier   : Modifier = Modifier,
) {
    val dimensions        = LocalDimensions.current
    val customColors      = MaterialTheme.customColors
    val scope             = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // FIX: VaccinationBenchUi now resolves to data.network.VaccinationBenchUi,
    // matching the return type of ApiService.getAllBenches(). No more type mismatch.
    var benches       by remember { mutableStateOf<List<VaccinationBenchUi>>(emptyList()) }
    var teamMembers   by remember { mutableStateOf<List<UserResponse>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<VaccinationBenchUi?>(null) }
    var snackMsg      by remember { mutableStateOf<String?>(null) }

    fun loadBenches() {
        scope.launch {
            isLoading = true
            val result = apiService.getAllBenches()
            if (result is ApiResult.Success) benches = result.data
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        val benchDeferred = scope.async { apiService.getAllBenches() }
        val teamDeferred  = scope.async { apiService.getUsersByRole("VACCINATION_TEAM") }

        val benchResult = benchDeferred.await()
        val teamResult  = teamDeferred.await()

        if (benchResult is ApiResult.Success) benches     = benchResult.data
        if (teamResult  is ApiResult.Success) teamMembers = teamResult.data

        isLoading = false
    }

    LaunchedEffect(snackMsg) {
        snackMsg?.let { snackbarHostState.showSnackbar(it); snackMsg = null }
    }

    // Delete confirmation
    pendingDelete?.let { bench ->
        AdminConfirmDialog(
            title        = "Deactivate Health Center",
            message      = "Deactivate ${bench.nameEn}? It will no longer appear to parents. You can reactivate it later.",
            confirmLabel = "Deactivate",
            onConfirm    = {
                scope.launch {
                    apiService.deactivateBench(bench.benchId)
                    snackMsg = "Bench deactivated"
                    loadBenches()
                }
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }

    // Add bench dialog
    if (showAddDialog) {
        AddBenchDialog(
            teamMembers = teamMembers,
            onDismiss   = { showAddDialog = false },
            onSave      = { state ->
                scope.launch {
                    val req = CreateBenchFormRequest(
                        nameEn            = state.nameEn,
                        nameAr            = state.nameAr,
                        governorate       = state.governorate,
                        district          = state.district,
                        addressEn         = state.addressEn,
                        latitude          = state.latitude ?: 0.0,
                        longitude         = state.longitude ?: 0.0,
                        phone             = state.phone,
                        workingDays       = state.selectedWorkingDays.toList(),
                        vaccinationDays   = state.selectedVaccinationDays.toList(),
                        workingHoursStart = state.workingHoursStart,
                        workingHoursEnd   = state.workingHoursEnd,
                        vaccinesAvailable = state.vaccinesText
                            .split(",").map { it.trim() }.filter { it.isNotBlank() },
                        teamMemberId      = state.teamMemberId,
                    )
                    val result = apiService.createBench(req)
                    snackMsg = if (result is ApiResult.Success) "Bench created" else "Failed to create bench"
                    if (result is ApiResult.Success) loadBenches()
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
                    colors  = IconButtonDefaults.filledIconButtonColors(
                        containerColor = customColors.accentGradientStart
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(Res.string.admin_bench_add_action),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Text(
                text  = stringResource(Res.string.admin_bench_count, benches.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            when {
                isLoading    -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = customColors.accentGradientStart)
                    }
                }
                benches.isEmpty() -> {
                    AdminEmptyState(
                        icon    = Icons.Default.LocalHospital,
                        message = stringResource(Res.string.admin_bench_no_results)
                    )
                }
                else -> {
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
}

// ─────────────────────────────────────────────────────────────────────────────
// Bench card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminBenchCard(
    bench    : VaccinationBenchUi,
    onDelete : () -> Unit,
) {
    val dimensions = LocalDimensions.current

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevationSmall),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = bench.nameEn,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text     = bench.nameAr,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text  = "${bench.governorate} · ${bench.district}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                if (bench.latitude != 0.0 || bench.longitude != 0.0) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint     = MaterialTheme.customColors.accentGradientStart,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                        // FIX: replaced "%.4f".format(...) with toCoordString()
                        // "%.4f".format() is JVM-only; toCoordString() works on all KMP targets.
                        Text(
                            text  = "${bench.latitude.toCoordString()}, ${bench.longitude.toCoordString()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }
                }

                Spacer(Modifier.height(dimensions.spacingXSmall))

                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                    AdminStatusBadge(
                        label = bench.type,
                        color = MaterialTheme.customColors.accentGradientStart
                    )
                    if (!bench.isActive) {
                        AdminStatusBadge(
                            label = "Inactive",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // FIX: was bench.teamMemberName?.let { ... } ?: run { ... } which failed
                // type-inference ("Cannot infer type for R/T", "Inapplicable ELVIS_CALL")
                // because the two branches returned incompatible inferred types.
                // Replaced with an explicit if/else block — no ambiguity for the compiler.
                Spacer(Modifier.height(dimensions.spacingXSmall))
                val memberName: String? = bench.teamMemberName
                if (memberName != null) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                    ) {
                        Icon(
                            Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint     = MaterialTheme.customColors.accentGradientEnd,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                        Text(
                            text  = memberName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.customColors.accentGradientEnd
                        )
                    }
                } else {
                    Text(
                        text  = "⚠ No team member assigned",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = stringResource(Res.string.admin_delete_cd),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AddBenchDialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBenchDialog(
    teamMembers : List<UserResponse>,
    onDismiss   : () -> Unit,
    onSave      : (AddBenchFormState) -> Unit,
) {
    val dimensions = LocalDimensions.current
    var form by remember { mutableStateOf(AddBenchFormState()) }

    if (form.showMapPicker) {
        LatLngPickerDialog(
            initialLat = form.latitude  ?: 36.19,
            initialLng = form.longitude ?: 44.01,
            onConfirm  = { lat, lng ->
                form = form.copy(latitude = lat, longitude = lng, showMapPicker = false)
            },
            onDismiss  = { form = form.copy(showMapPicker = false) }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(Res.string.admin_bench_add_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                BenchTextField(form.nameEn,      { form = form.copy(nameEn = it) },      stringResource(Res.string.admin_bench_field_name_en),     required = true)
                BenchTextField(form.nameAr,      { form = form.copy(nameAr = it) },      stringResource(Res.string.admin_bench_field_name_ar),     required = true)
                BenchTextField(form.governorate, { form = form.copy(governorate = it) }, stringResource(Res.string.admin_bench_field_governorate), required = true)
                BenchTextField(form.district,    { form = form.copy(district = it) },    stringResource(Res.string.admin_bench_field_district))
                BenchTextField(form.addressEn,   { form = form.copy(addressEn = it) },   stringResource(Res.string.admin_bench_field_address_en))
                BenchTextField(form.phone,       { form = form.copy(phone = it) },       stringResource(Res.string.admin_bench_field_phone))

                // Location picker
                Text(
                    text  = "Location *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                OutlinedButton(
                    onClick  = { form = form.copy(showMapPicker = true) },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.customColors.accentGradientStart
                    )
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        modifier = Modifier.size(dimensions.iconSmall)
                    )
                    Spacer(Modifier.width(dimensions.spacingXSmall))
                    // FIX: replaced "%.4f".format(...) with toCoordString() — KMP safe.
                    val locationLabel = if (form.latitude != null && form.longitude != null)
                        "📍 ${form.latitude!!.toCoordString()}, ${form.longitude!!.toCoordString()}"
                    else
                        "Tap to pick location on map"
                    Text(text = locationLabel, style = MaterialTheme.typography.bodySmall)
                }

                // Working hours
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    BenchTextField(
                        value    = form.workingHoursStart,
                        onChange = { form = form.copy(workingHoursStart = it) },
                        label    = "Hours Start",
                        modifier = Modifier.weight(1f)
                    )
                    BenchTextField(
                        value    = form.workingHoursEnd,
                        onChange = { form = form.copy(workingHoursEnd = it) },
                        label    = "Hours End",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Working days
                Text(
                    "Working Days",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                DayMultiSelect(
                    selectedDays = form.selectedWorkingDays,
                    onToggle     = { day ->
                        form = form.copy(
                            selectedWorkingDays = if (day in form.selectedWorkingDays)
                                form.selectedWorkingDays - day
                            else
                                form.selectedWorkingDays + day
                        )
                    }
                )

                // Vaccination days
                Text(
                    "Vaccination Days",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                DayMultiSelect(
                    selectedDays = form.selectedVaccinationDays,
                    onToggle     = { day ->
                        form = form.copy(
                            selectedVaccinationDays = if (day in form.selectedVaccinationDays)
                                form.selectedVaccinationDays - day
                            else
                                form.selectedVaccinationDays + day
                        )
                    }
                )

                BenchTextField(
                    value    = form.vaccinesText,
                    onChange = { form = form.copy(vaccinesText = it) },
                    label    = stringResource(Res.string.admin_bench_field_vaccines)
                )

                // Team member dropdown
                Text(
                    "Assign Team Member (optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                ExposedDropdownMenuBox(
                    expanded         = form.showTeamDropdown,
                    onExpandedChange = { form = form.copy(showTeamDropdown = !form.showTeamDropdown) }
                ) {
                    OutlinedTextField(
                        value         = form.teamMemberName.ifBlank { "None" },
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Team Member") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(form.showTeamDropdown) },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape  = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.customColors.accentGradientStart
                        )
                    )
                    ExposedDropdownMenu(
                        expanded         = form.showTeamDropdown,
                        onDismissRequest = { form = form.copy(showTeamDropdown = false) }
                    ) {
                        DropdownMenuItem(
                            text    = { Text("None") },
                            onClick = {
                                form = form.copy(
                                    teamMemberId     = null,
                                    teamMemberName   = "",
                                    showTeamDropdown = false
                                )
                            }
                        )
                        teamMembers.forEach { member ->
                            DropdownMenuItem(
                                text    = { Text("${member.fullName} (${member.email})") },
                                onClick = {
                                    form = form.copy(
                                        teamMemberId     = member.userId,
                                        teamMemberName   = member.fullName,
                                        showTeamDropdown = false
                                    )
                                }
                            )
                        }
                    }
                }

                form.error?.let {
                    Text(
                        text  = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        form.nameEn.isBlank() || form.nameAr.isBlank() || form.governorate.isBlank() ->
                            form = form.copy(error = "Please fill required fields (marked *)")
                        form.latitude == null || form.longitude == null ->
                            form = form.copy(error = "Please pick a location on the map")
                        form.selectedWorkingDays.isEmpty() ->
                            form = form.copy(error = "Select at least one working day")
                        form.selectedVaccinationDays.isEmpty() ->
                            form = form.copy(error = "Select at least one vaccination day")
                        else -> onSave(form)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.customColors.accentGradientStart
                )
            ) {
                Text(stringResource(Res.string.admin_bench_add_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.btn_cancel))
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// DayMultiSelect
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DayMultiSelect(
    selectedDays : Set<String>,
    onToggle     : (String) -> Unit,
) {
    val dimensions = LocalDimensions.current
    val abbrevs = mapOf(
        "Sunday"    to "Sun", "Monday" to "Mon", "Tuesday"   to "Tue",
        "Wednesday" to "Wed", "Thursday" to "Thu", "Friday"  to "Fri",
        "Saturday"  to "Sat"
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
        verticalArrangement   = Arrangement.spacedBy(dimensions.spacingXSmall)
    ) {
        ALL_DAYS.forEach { day ->
            FilterChip(
                selected = day in selectedDays,
                onClick  = { onToggle(day) },
                label    = {
                    Text(abbrevs[day] ?: day, style = MaterialTheme.typography.labelSmall)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.customColors.accentGradientStart,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LatLngPickerDialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LatLngPickerDialog(
    initialLat : Double,
    initialLng : Double,
    onConfirm  : (Double, Double) -> Unit,
    onDismiss  : () -> Unit,
) {
    val dimensions = LocalDimensions.current
    var latText by remember { mutableStateOf(if (initialLat != 36.19) initialLat.toCoordString(6) else "") }
    var lngText by remember { mutableStateOf(if (initialLng != 44.01) initialLng.toCoordString(6) else "") }
    var error   by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint     = MaterialTheme.customColors.accentGradientStart,
                modifier = Modifier.size(dimensions.iconLarge)
            )
        },
        title = { Text("Pick Location", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                Surface(
                    shape = RoundedCornerShape(dimensions.cardCornerRadius),
                    color = MaterialTheme.customColors.accentGradientStart.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier              = Modifier.padding(dimensions.spacingSmall),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint     = MaterialTheme.customColors.accentGradientStart,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                        Text(
                            text  = "Enter GPS coordinates. Find them on Google Maps by long-pressing the location.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.customColors.accentGradientStart
                        )
                    }
                }

                OutlinedTextField(
                    value         = latText,
                    onValueChange = { latText = it; error = null },
                    label         = { Text("Latitude  (e.g. 36.1911)") },
                    singleLine    = true,
                    leadingIcon   = {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors   = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.customColors.accentGradientStart
                    )
                )

                OutlinedTextField(
                    value         = lngText,
                    onValueChange = { lngText = it; error = null },
                    label         = { Text("Longitude (e.g. 44.0127)") },
                    singleLine    = true,
                    leadingIcon   = {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors   = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.customColors.accentGradientStart
                    )
                )

                error?.let {
                    Text(
                        text  = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lng = lngText.toDoubleOrNull()
                    when {
                        lat == null || lng == null -> error = "Please enter valid numeric coordinates"
                        lat !in -90.0..90.0        -> error = "Latitude must be between -90 and 90"
                        lng !in -180.0..180.0      -> error = "Longitude must be between -180 and 180"
                        else                       -> onConfirm(lat, lng)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.customColors.accentGradientStart
                )
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(dimensions.iconSmall))
                Spacer(Modifier.width(dimensions.spacingXSmall))
                Text("Confirm Location")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.btn_cancel))
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// BenchTextField — reusable helper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BenchTextField(
    value    : String,
    onChange : (String) -> Unit,
    label    : String,
    required : Boolean  = false,
    modifier : Modifier = Modifier.fillMaxWidth(),
) {
    val dimensions = LocalDimensions.current
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        label         = { Text(if (required) "$label *" else label) },
        modifier      = modifier,
        singleLine    = true,
        shape         = RoundedCornerShape(dimensions.buttonCornerRadius),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.customColors.accentGradientStart
        )
    )
}
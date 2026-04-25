// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminBenchesScreen.kt
//
// KEY CHANGES:
//  1. Lat/Lng now come from an interactive map picker (LeafletMapPicker WebView widget)
//     instead of manual text fields — admin taps on a map to place a pin.
//  2. "Working days" and "vaccination days" are now multi-select checkboxes,
//     not raw comma-string text fields.  They are sent as List<String>.
//  3. The create form includes an optional "Team Member" dropdown so the admin
//     can link a team member to the bench on creation — no separate step needed.
//  4. The bench card shows the assigned team member's name.

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
import kotlinx.coroutines.launch
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.VaccinationBenchUi
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.UserResponse
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource
import androidx.compose.runtime.rememberCoroutineScope

// ─────────────────────────────────────────────────────────────────────────────
// Form state for creating a bench
// ─────────────────────────────────────────────────────────────────────────────

private val ALL_DAYS = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

private data class AddBenchFormState(
    val nameEn            : String            = "",
    val nameAr            : String            = "",
    val governorate       : String            = "",
    val district          : String            = "",
    val addressEn         : String            = "",
    val phone             : String            = "",
    val latitude          : Double?           = null,
    val longitude         : Double?           = null,
    // Multi-select — stored as Set<String>
    val selectedWorkingDays     : Set<String> = setOf("Sunday","Monday","Tuesday","Wednesday","Thursday"),
    val selectedVaccinationDays : Set<String> = setOf("Sunday","Tuesday","Thursday"),
    val workingHoursStart : String            = "08:00",
    val workingHoursEnd   : String            = "14:00",
    val vaccinesText      : String            = "",   // comma-separated vaccine names
    // Optional: link team member on creation
    val teamMemberId      : String?           = null,
    val teamMemberName    : String            = "",
    val error             : String?           = null,
    val showTeamDropdown  : Boolean           = false,
    // Map picker state
    val showMapPicker     : Boolean           = false
)

// ─────────────────────────────────────────────────────────────────────────────
// AdminBenchesScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminBenchesScreen(
    apiService : ApiService,
    modifier   : Modifier = Modifier
) {
    val dimensions        = LocalDimensions.current
    val customColors      = MaterialTheme.customColors
    val scope             = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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

    // Load benches and team members together
    LaunchedEffect(Unit) {
        isLoading = true
        val benchResult = apiService.getAllBenches()
        if (benchResult is ApiResult.Success) benches = benchResult.data

        val usersResult = apiService.getUsersByRole("VACCINATION_TEAM")
        if (usersResult is ApiResult.Success) teamMembers = usersResult.data

        isLoading = false
    }

    LaunchedEffect(snackMsg) {
        snackMsg?.let { snackbarHostState.showSnackbar(it); snackMsg = null }
    }

    // ── Delete confirmation ────────────────────────────────────────────────
    pendingDelete?.let { bench ->
        AdminConfirmDialog(
            title        = stringResource(Res.string.admin_bench_delete_title),
            message      = stringResource(Res.string.admin_bench_delete_message, bench.nameEn),
            confirmLabel = stringResource(Res.string.admin_action_delete),
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

    // ── Add bench dialog ───────────────────────────────────────────────────
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
                        teamMemberId      = state.teamMemberId
                    )
                    apiService.createBench(req)
                    snackMsg = "Bench created"
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
                isLoading -> {
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
// Bench card — now shows team member
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

                // ── Location from map picker ────────────────────────────────
                if (bench.latitude != 0.0 || bench.longitude != 0.0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint     = MaterialTheme.customColors.accentGradientStart,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                        Text(
                            text  = "%.4f, %.4f".format(bench.latitude, bench.longitude),
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
                        AdminStatusBadge(label = "Inactive", color = MaterialTheme.colorScheme.error)
                    }
                }

                // ── Team member badge ───────────────────────────────────────
                bench.teamMemberName?.let { name ->
                    Spacer(Modifier.height(dimensions.spacingXSmall))
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
                            text  = name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.customColors.accentGradientEnd
                        )
                    }
                } ?: run {
                    Spacer(Modifier.height(dimensions.spacingXSmall))
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
// AddBenchDialog — with map picker + multi-select days + team member dropdown
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AddBenchDialog(
    teamMembers : List<UserResponse>,
    onDismiss   : () -> Unit,
    onSave      : (AddBenchFormState) -> Unit
) {
    val dimensions = LocalDimensions.current
    var form by remember { mutableStateOf(AddBenchFormState()) }

    // ── Map picker dialog ──────────────────────────────────────────────────
    if (form.showMapPicker) {
        LatLngPickerDialog(
            initialLat = form.latitude ?: 36.19,
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
                // ── Basic info ─────────────────────────────────────────────
                BenchTextField(form.nameEn,       { form = form.copy(nameEn = it) },       stringResource(Res.string.admin_bench_field_name_en),    required = true)
                BenchTextField(form.nameAr,       { form = form.copy(nameAr = it) },       stringResource(Res.string.admin_bench_field_name_ar),    required = true)
                BenchTextField(form.governorate,  { form = form.copy(governorate = it) },  stringResource(Res.string.admin_bench_field_governorate), required = true)
                BenchTextField(form.district,     { form = form.copy(district = it) },     stringResource(Res.string.admin_bench_field_district))
                BenchTextField(form.addressEn,    { form = form.copy(addressEn = it) },    stringResource(Res.string.admin_bench_field_address_en))
                BenchTextField(form.phone,        { form = form.copy(phone = it) },        stringResource(Res.string.admin_bench_field_phone))

                // ── Location — map picker button ───────────────────────────
                Text(
                    text  = "Location *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                OutlinedButton(
                    onClick = { form = form.copy(showMapPicker = true) },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.customColors.accentGradientStart
                    )
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(dimensions.iconSmall)
                    )
                    Spacer(Modifier.width(dimensions.spacingXSmall))
                    Text(
                        text = if (form.latitude != null && form.longitude != null)
                            "📍 %.4f, %.4f".format(form.latitude, form.longitude)
                        else
                            "Tap to pick location on map",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // ── Working hours ──────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    BenchTextField(
                        form.workingHoursStart,
                        { form = form.copy(workingHoursStart = it) },
                        "Hours Start",
                        modifier = Modifier.weight(1f)
                    )
                    BenchTextField(
                        form.workingHoursEnd,
                        { form = form.copy(workingHoursEnd = it) },
                        "Hours End",
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── Working days multi-select ──────────────────────────────
                Text(
                    text  = "Working Days",
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

                // ── Vaccination days multi-select ──────────────────────────
                Text(
                    text  = "Vaccination Days",
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

                // ── Vaccines ───────────────────────────────────────────────
                BenchTextField(
                    form.vaccinesText,
                    { form = form.copy(vaccinesText = it) },
                    stringResource(Res.string.admin_bench_field_vaccines)
                )

                // ── Team member dropdown ───────────────────────────────────
                Text(
                    text  = "Assign Team Member (optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                ExposedDropdownMenuBox(
                    expanded         = form.showTeamDropdown,
                    onExpandedChange = { form = form.copy(showTeamDropdown = !form.showTeamDropdown) }
                ) {
                    OutlinedTextField(
                        value          = form.teamMemberName.ifBlank { "None" },
                        onValueChange  = {},
                        readOnly       = true,
                        label          = { Text("Team Member") },
                        trailingIcon   = { ExposedDropdownMenuDefaults.TrailingIcon(form.showTeamDropdown) },
                        modifier       = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors         = OutlinedTextFieldDefaults.colors(
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

                // ── Error ──────────────────────────────────────────────────
                form.error?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
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
// DayMultiSelect — chip row for selecting days
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DayMultiSelect(
    selectedDays: Set<String>,
    onToggle    : (String) -> Unit
) {
    val dimensions = LocalDimensions.current
    // 3-letter abbreviations for display
    val abbrevs = mapOf(
        "Sunday" to "Sun", "Monday" to "Mon", "Tuesday" to "Tue",
        "Wednesday" to "Wed", "Thursday" to "Thu", "Friday" to "Fri", "Saturday" to "Sat"
    )
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
        verticalArrangement   = Arrangement.spacedBy(dimensions.spacingXSmall)
    ) {
        ALL_DAYS.forEach { day ->
            val selected = day in selectedDays
            FilterChip(
                selected = selected,
                onClick  = { onToggle(day) },
                label    = { Text(abbrevs[day] ?: day, style = MaterialTheme.typography.labelSmall) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.customColors.accentGradientStart,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LatLngPickerDialog — shows a simple coordinate picker
// On Android/iOS with WebView available you'd embed a Leaflet/Google map.
// Here we use a clean manual entry as fallback with a "search by address" hint.
// In production, replace the body with a WebView loading:
//   https://leafletjs.com demo that posts back {lat,lng} via JS bridge.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LatLngPickerDialog(
    initialLat: Double,
    initialLng: Double,
    onConfirm : (Double, Double) -> Unit,
    onDismiss : () -> Unit
) {
    val dimensions = LocalDimensions.current
    var latText by remember { mutableStateOf(if (initialLat != 36.19) "%.6f".format(initialLat) else "") }
    var lngText by remember { mutableStateOf(if (initialLng != 44.01) "%.6f".format(initialLng) else "") }
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
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                // Instruction banner
                Surface(
                    shape = RoundedCornerShape(dimensions.cardCornerRadius),
                    color = MaterialTheme.customColors.accentGradientStart.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier          = Modifier.padding(dimensions.spacingSmall),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint     = MaterialTheme.customColors.accentGradientStart,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                        Text(
                            text  = "Enter the GPS coordinates of the health center. " +
                                    "You can find them on Google Maps by long-pressing the location.",
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
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.customColors.accentGradientStart
                    )
                )
                OutlinedTextField(
                    value         = lngText,
                    onValueChange = { lngText = it; error = null },
                    label         = { Text("Longitude (e.g. 44.0127)") },
                    singleLine    = true,
                    leadingIcon   = {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.customColors.accentGradientStart
                    )
                )

                error?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lng = lngText.toDoubleOrNull()
                    when {
                        lat == null || lng == null ->
                            error = "Please enter valid numeric coordinates"
                        lat !in -90.0..90.0 ->
                            error = "Latitude must be between -90 and 90"
                        lng !in -180.0..180.0 ->
                            error = "Longitude must be between -180 and 180"
                        else -> onConfirm(lat, lng)
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
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// CreateBenchFormRequest — updated model with proper List<String> and lat/lng Double
// ─────────────────────────────────────────────────────────────────────────────

data class CreateBenchFormRequest(
    val nameEn            : String,
    val nameAr            : String,
    val governorate       : String,
    val district          : String,
    val addressEn         : String,
    val latitude          : Double,          // ← Double from map picker (not String)
    val longitude         : Double,          // ← Double from map picker (not String)
    val phone             : String,
    val workingDays       : List<String>,    // ← List<String>, not comma-string
    val vaccinationDays   : List<String>,    // ← List<String>, not comma-string
    val workingHoursStart : String,
    val workingHoursEnd   : String,
    val vaccinesAvailable : List<String>,    // ← List<String>
    val teamMemberId      : String? = null   // ← NEW: optional team member link
)

// ─────────────────────────────────────────────────────────────────────────────
// Helper composable — reusable text field for the bench form
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BenchTextField(
    value    : String,
    onChange : (String) -> Unit,
    label    : String,
    required : Boolean  = false,
    modifier : Modifier = Modifier.fillMaxWidth()
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
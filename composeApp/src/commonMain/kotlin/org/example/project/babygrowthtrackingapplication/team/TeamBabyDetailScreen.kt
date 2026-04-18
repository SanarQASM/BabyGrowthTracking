// composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/team/TeamBabyDetailScreen.kt

package org.example.project.babygrowthtrackingapplication.team

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.VaccinationFilter
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.MeasurementHistoryList
import org.example.project.babygrowthtrackingapplication.data.network.*
import org.example.project.babygrowthtrackingapplication.theme.*
import org.example.project.babygrowthtrackingapplication.ui.components.*
import kotlin.time.ExperimentalTime

// ─────────────────────────────────────────────────────────────────────────────
// Baby Detail Screen — 3 sub-tabs: Profile / Vaccinations / Growth
// ─────────────────────────────────────────────────────────────────────────────

enum class TeamDetailTab { PROFILE, VACCINATIONS, GROWTH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamBabyDetailScreen(
    baby     : TeamBabyItem,
    viewModel: TeamVaccinationViewModel,
    onBack   : () -> Unit
) {
    val state        = viewModel.uiState
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val snackbar     = remember { SnackbarHostState() }

    var selectedTab  by remember { mutableStateOf(TeamDetailTab.PROFILE) }

    // Load detail on entry
    LaunchedEffect(baby.babyId) { viewModel.loadBabyDetail(baby) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearSuccess() }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // ── Detail Header ─────────────────────────────────────────────────
            TeamDetailHeader(
                baby         = baby,
                onBack       = onBack,
                customColors = customColors,
                dimensions   = dimensions
            )

            // ── Sub-tab Row ───────────────────────────────────────────────────
            TeamDetailTabRow(
                selectedTab  = selectedTab,
                onSelect     = { selectedTab = it },
                customColors = customColors,
                dimensions   = dimensions
            )

            // ── Sub-tab Content ───────────────────────────────────────────────
            AnimatedContent(
                targetState  = selectedTab,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                label        = "detail_tab_content"
            ) { tab ->
                when (tab) {
                    TeamDetailTab.PROFILE      -> TeamProfileTab(baby = baby, dimensions = dimensions, customColors = customColors)
                    TeamDetailTab.VACCINATIONS -> TeamVaccinationsTab(viewModel = viewModel, babyId = baby.babyId)
                    TeamDetailTab.GROWTH       -> TeamGrowthTab(viewModel = viewModel, baby = baby)
                }
            }
        }
    }

    // Dialogs from detail view
    state.completeForm?.let { form ->
        TeamCompleteDialogForDetail(
            form         = form,
            onDismiss    = viewModel::dismissCompleteDialog,
            onChange     = viewModel::updateCompleteForm,
            onSubmit     = viewModel::submitCompleteVaccination,
            dimensions   = dimensions,
            customColors = customColors
        )
    }

    if (state.showAddMeasurement) {
        TeamAddMeasurementDialog(
            babyId       = baby.babyId,
            onDismiss    = viewModel::dismissAddMeasurement,
            onSave       = { babyId, w, h, hc, d ->
                viewModel.addMeasurement(babyId, w, h, hc, d)
            },
            dimensions   = dimensions,
            customColors = customColors
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Detail Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TeamDetailHeader(
    baby        : TeamBabyItem,
    onBack      : () -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    val isFemale    = baby.gender.equals("GIRL", ignoreCase = true)
    val genderEmoji = if (isFemale) "👧" else "👦"
    val gradientStart = if (isFemale) customColors.accentGradientStart else customColors.accentGradientEnd
    val gradientEnd   = if (isFemale) customColors.accentGradientEnd   else customColors.accentGradientStart

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(gradientStart, gradientEnd)))
    ) {
        Column {
            // Back button row
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.spacingSmall, vertical = dimensions.spacingXSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Text(
                    text  = "Baby Profile",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(0.85f)
                )
            }

            // Baby info row
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(
                        start   = dimensions.screenPadding,
                        end     = dimensions.screenPadding,
                        bottom  = dimensions.spacingMedium
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier         = Modifier
                        .size(dimensions.avatarLarge)
                        .background(Color.White.copy(0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(genderEmoji, style = MaterialTheme.typography.displaySmall)
                }

                Spacer(Modifier.width(dimensions.spacingMedium))

                Column {
                    Text(
                        text       = baby.fullName,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    Text(
                        text  = "${baby.ageInMonths} months old • ${if (isFemale) "Girl" else "Boy"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(0.85f)
                    )
                    if (baby.parentName.isNotBlank()) {
                        Text(
                            text  = "👤 ${baby.parentName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(0.75f)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Detail Tab Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TeamDetailTabRow(
    selectedTab : TeamDetailTab,
    onSelect    : (TeamDetailTab) -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    Surface(
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = dimensions.cardElevationSmall
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingXSmall)
        ) {
            data class TabInfo(val tab: TeamDetailTab, val emoji: String, val label: String)
            val tabs = listOf(
                TabInfo(TeamDetailTab.PROFILE,      "👶", "Profile"),
                TabInfo(TeamDetailTab.VACCINATIONS, "💉", "Vaccines"),
                TabInfo(TeamDetailTab.GROWTH,       "📏", "Growth")
            )
            tabs.forEach { info ->
                val selected = selectedTab == info.tab
                val bgColor by animateColorAsState(
                    if (selected) customColors.accentGradientStart.copy(0.12f) else Color.Transparent,
                    label = "detail_tab_bg"
                )
                val fgColor by animateColorAsState(
                    if (selected) customColors.accentGradientStart else MaterialTheme.colorScheme.onSurface.copy(0.5f),
                    label = "detail_tab_fg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(dimensions.buttonCornerRadius))
                        .background(bgColor)
                        .clickable { onSelect(info.tab) }
                        .padding(vertical = dimensions.spacingSmall),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                    ) {
                        Text(info.emoji, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text       = info.label,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color      = fgColor
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile Tab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TeamProfileTab(
    baby        : TeamBabyItem,
    dimensions  : Dimensions,
    customColors: CustomColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        // Baby information section
        DetailSectionCard(
            title      = "Baby Information",
            emoji      = "👶",
            dimensions = dimensions,
            customColors = customColors
        ) {
            DetailRow("Full Name",   baby.fullName,   "📋", dimensions)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f), thickness = dimensions.hairlineDividerThickness)
            DetailRow("Date of Birth", baby.dateOfBirth, "🎂", dimensions)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f), thickness = dimensions.hairlineDividerThickness)
            DetailRow("Age",         "${baby.ageInMonths} months", "⏰", dimensions)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f), thickness = dimensions.hairlineDividerThickness)
            DetailRow(
                "Gender",
                if (baby.gender.equals("GIRL", ignoreCase = true)) "Girl 👧" else "Boy 👦",
                "⚧️",
                dimensions
            )
        }

        // Parent information section
        DetailSectionCard(
            title      = "Parent Information",
            emoji      = "👨‍👩‍👧",
            dimensions = dimensions,
            customColors = customColors
        ) {
            DetailRow("Parent Name", baby.parentName.ifBlank { "—" }, "👤", dimensions)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f), thickness = dimensions.hairlineDividerThickness)
            DetailRow("Phone", baby.parentPhone.ifBlank { "—" }, "📞", dimensions)
        }

        // Bench information
        DetailSectionCard(
            title      = "Assigned Health Center",
            emoji      = "🏥",
            dimensions = dimensions,
            customColors = customColors
        ) {
            DetailRow("Bench", baby.benchName.ifBlank { "—" }, "📍", dimensions)
        }

        // Vaccine status
        val statusColor = when (baby.vaccineStatus) {
            TeamVaccineStatus.OVERDUE    -> MaterialTheme.colorScheme.error
            TeamVaccineStatus.DUE_SOON   -> customColors.warning
            TeamVaccineStatus.UP_TO_DATE -> customColors.success
            TeamVaccineStatus.NO_SCHEDULE-> MaterialTheme.colorScheme.onSurface.copy(0.4f)
        }
        val statusLabel = when (baby.vaccineStatus) {
            TeamVaccineStatus.OVERDUE    -> "Has overdue vaccinations ⚠️"
            TeamVaccineStatus.DUE_SOON   -> "Vaccination due soon ⏰"
            TeamVaccineStatus.UP_TO_DATE -> "All vaccinations up to date ✅"
            TeamVaccineStatus.NO_SCHEDULE-> "No vaccination schedule yet 📋"
        }

        Surface(
            shape = RoundedCornerShape(dimensions.cardCornerRadius),
            color = statusColor.copy(0.08f),
            border = BorderStroke(dimensions.borderWidthThin, statusColor.copy(0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text      = statusLabel,
                style     = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color     = statusColor,
                modifier  = Modifier.padding(dimensions.spacingMedium)
            )
        }

        Spacer(Modifier.height(dimensions.spacingXXLarge))
    }
}

@Composable
private fun DetailSectionCard(
    title      : String,
    emoji      : String,
    dimensions : Dimensions,
    customColors: CustomColors,
    content    : @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(dimensions.cardElevationSmall)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .background(customColors.accentGradientStart.copy(0.06f))
                    .padding(dimensions.spacingMedium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
            ) {
                Text(emoji, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = customColors.accentGradientStart
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensions.spacingXSmall),
                content = content
            )
        }
    }
}

@Composable
private fun DetailRow(
    label     : String,
    value     : String,
    emoji     : String,
    dimensions: Dimensions
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.profileInfoRowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(dimensions.profileInfoIconWidth))
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(0.55f),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.onSurface,
            modifier   = Modifier.weight(0.6f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Vaccinations Tab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TeamVaccinationsTab(
    viewModel: TeamVaccinationViewModel,
    babyId   : String
) {
    val state        = viewModel.uiState
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter chips
        LazyRow(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
        ) {
            val filters = listOf(
                VaccinationFilter.ALL      to "All",
                VaccinationFilter.UPCOMING to "Upcoming",
                VaccinationFilter.COMPLETED to "Completed",
                VaccinationFilter.OVERDUE  to "Overdue"
            )
            items(filters) { (filter, label) ->
                FilterChip(
                    selected = state.detailVacFilter == filter,
                    onClick  = { viewModel.setDetailVacFilter(filter) },
                    label    = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = customColors.accentGradientStart,
                        selectedLabelColor     = Color.White,
                        containerColor         = customColors.accentGradientStart.copy(0.06f),
                        labelColor             = customColors.accentGradientStart
                    )
                )
            }
        }

        when {
            state.detailSchedulesLoading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            }
            else -> {
                val filtered = state.detailSchedules.filter { schedule ->
                    when (state.detailVacFilter) {
                        VaccinationFilter.ALL       -> true
                        VaccinationFilter.UPCOMING  -> schedule.status == "UPCOMING" || schedule.status == "DUE_SOON"
                        VaccinationFilter.COMPLETED -> schedule.status == "COMPLETED"
                        VaccinationFilter.OVERDUE   -> schedule.status == "OVERDUE"
                    }
                }

                if (filtered.isEmpty()) {
                    TeamEmptyState(
                        emoji      = "💉",
                        title      = "No vaccinations in this category",
                        subtitle   = "Change the filter to see others",
                        dimensions = dimensions
                    )
                } else {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(
                            horizontal = dimensions.screenPadding,
                            vertical   = dimensions.spacingSmall
                        ),
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                    ) {
                        items(filtered, key = { it.scheduleId }) { schedule ->
                            TeamVaccineCard(
                                schedule     = schedule,
                                onComplete   = { viewModel.openCompleteDialog(schedule.scheduleId) },
                                onMissed     = { viewModel.markAsMissed(schedule.scheduleId) },
                                onReschedule = { /* TODO: reschedule flow */ },
                                customColors = customColors,
                                dimensions   = dimensions
                            )
                        }
                        item { Spacer(Modifier.height(dimensions.spacingXXLarge)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamVaccineCard(
    schedule    : org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.VaccinationScheduleUi,
    onComplete  : () -> Unit,
    onMissed    : () -> Unit,
    onReschedule: () -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    val isCompleted = schedule.status == "COMPLETED"
    val isMissed    = schedule.status == "MISSED"
    val isOverdue   = schedule.status == "OVERDUE"
    val isDone      = isCompleted || isMissed

    val statusColor = when (schedule.status) {
        "COMPLETED"  -> customColors.success
        "MISSED"     -> MaterialTheme.colorScheme.error
        "OVERDUE"    -> customColors.warning
        "DUE_SOON"   -> customColors.info
        "RESCHEDULED"-> MaterialTheme.colorScheme.tertiary
        else         -> customColors.accentGradientStart
    }
    val statusEmoji = when (schedule.status) {
        "COMPLETED"  -> "✅"
        "MISSED"     -> "❌"
        "OVERDUE"    -> "⚠️"
        "DUE_SOON"   -> "⏰"
        "RESCHEDULED"-> "🔄"
        else         -> "📅"
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surface.copy(0.7f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isDone) 0.dp else dimensions.cardElevationSmall)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dose circle
                Box(
                    modifier         = Modifier
                        .size(dimensions.vaccinationCardIconSize)
                        .background(statusColor.copy(0.12f), CircleShape)
                        .border(dimensions.borderWidthThin, statusColor.copy(0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(statusEmoji, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text       = "D${schedule.doseNumber}",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = statusColor
                        )
                    }
                }

                Spacer(Modifier.width(dimensions.spacingSmall))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = schedule.vaccineName,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (isDone) MaterialTheme.colorScheme.onSurface.copy(0.55f)
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        text  = "Age ${schedule.recommendedAgeMonths} months",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.45f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(dimensions.filterTabCorner),
                    color = statusColor.copy(0.1f)
                ) {
                    Text(
                        text      = schedule.status.replace("_", " "),
                        style     = MaterialTheme.typography.labelSmall,
                        color     = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier  = Modifier.padding(horizontal = dimensions.spacingXSmall * 2, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(dimensions.spacingXSmall))

            // Date info
            Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                Text(
                    text  = "📅 Scheduled: ${schedule.scheduledDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
                schedule.completedDate?.let {
                    Text(
                        text  = "✅ Done: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = customColors.success
                    )
                }
            }

            // Action buttons — only for non-completed/missed
            if (!isDone) {
                Spacer(Modifier.height(dimensions.spacingSmall))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Button(
                        onClick        = onComplete,
                        modifier       = Modifier.weight(1f),
                        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors         = ButtonDefaults.buttonColors(containerColor = customColors.success),
                        contentPadding = PaddingValues(vertical = dimensions.spacingXSmall)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(dimensions.iconSmall))
                        Spacer(Modifier.width(dimensions.spacingXSmall))
                        Text("Complete", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick        = onMissed,
                        modifier       = Modifier.weight(1f),
                        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                        border         = BorderStroke(dimensions.borderWidthThin, MaterialTheme.colorScheme.error.copy(0.5f)),
                        colors         = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(vertical = dimensions.spacingXSmall)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(dimensions.iconSmall))
                        Spacer(Modifier.width(dimensions.spacingXSmall))
                        Text("Missed", style = MaterialTheme.typography.labelSmall)
                    }

                    if (isOverdue) {
                        IconButton(
                            onClick   = onReschedule,
                            modifier  = Modifier
                                .size(dimensions.addButtonSize + dimensions.spacingXSmall)
                                .background(customColors.accentGradientStart.copy(0.1f), RoundedCornerShape(dimensions.buttonCornerRadius))
                        ) {
                            Icon(
                                Icons.Default.Refresh, null,
                                tint     = customColors.accentGradientStart,
                                modifier = Modifier.size(dimensions.iconSmall)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Growth Tab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TeamGrowthTab(
    viewModel: TeamVaccinationViewModel,
    baby     : TeamBabyItem
) {
    val state        = viewModel.uiState
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Column(modifier = Modifier.fillMaxSize()) {
        // Add measurement button
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick        = { viewModel.openAddMeasurement(baby.babyId) },
                shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors         = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart),
                contentPadding = PaddingValues(
                    horizontal = dimensions.spacingMedium,
                    vertical   = dimensions.spacingSmall
                )
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(dimensions.iconSmall))
                Spacer(Modifier.width(dimensions.spacingXSmall))
                Text("Add Measurement", style = MaterialTheme.typography.labelMedium)
            }
        }

        when {
            state.detailGrowthLoading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            }
            state.detailGrowthRecords.isEmpty() -> {
                TeamEmptyState(
                    emoji      = "📏",
                    title      = "No measurements yet",
                    subtitle   = "Tap 'Add Measurement' to record the first one",
                    dimensions = dimensions
                )
            }
            else -> {
                // Latest measurement summary
                state.detailGrowthRecords.firstOrNull()?.let { latest ->
                    Card(
                        modifier  = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensions.screenPadding),
                        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
                        colors    = CardDefaults.cardColors(containerColor = customColors.accentGradientStart.copy(0.06f)),
                        border    = BorderStroke(dimensions.borderWidthThin, customColors.accentGradientStart.copy(0.2f))
                    ) {
                        Column(modifier = Modifier.padding(dimensions.spacingMedium)) {
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📊", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.width(dimensions.spacingXSmall))
                                Text(
                                    text       = "Latest Measurement",
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = customColors.accentGradientStart
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text  = latest.measurementDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                                )
                            }
                            Spacer(Modifier.height(dimensions.spacingSmall))
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                latest.weight?.let {
                                    GrowthMetric("⚖️", "Weight", "$it kg", customColors.accentGradientStart)
                                }
                                latest.height?.let {
                                    GrowthMetric("📏", "Height", "$it cm", customColors.accentGradientEnd)
                                }
                                latest.headCircumference?.let {
                                    GrowthMetric("🔵", "Head", "$it cm", customColors.info)
                                }
                            }
                            // Team badge
                            if (latest.addedByTeam) {
                                Spacer(Modifier.height(dimensions.spacingXSmall))
                                Surface(
                                    shape = RoundedCornerShape(dimensions.filterTabCorner),
                                    color = customColors.info.copy(0.1f)
                                ) {
                                    Text(
                                        text  = "🏥 Recorded by team",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = customColors.info,
                                        modifier = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(dimensions.spacingSmall))

                // History list
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(
                        horizontal = dimensions.screenPadding,
                        vertical   = dimensions.spacingXSmall
                    ),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    items(state.detailGrowthRecords, key = { it.recordId }) { record ->
                        GrowthRecordRow(record = record, customColors = customColors, dimensions = dimensions)
                    }
                    item { Spacer(Modifier.height(dimensions.spacingXXLarge)) }
                }
            }
        }
    }
}

@Composable
private fun GrowthMetric(emoji: String, label: String, value: String, color: Color) {
    val dimensions = LocalDimensions.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, style = MaterialTheme.typography.bodyMedium)
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color      = color
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
        )
    }
}

@Composable
private fun GrowthRecordRow(
    record      : GrowthRecordResponse,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(dimensions.borderWidthThin, MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(customColors.accentGradientStart.copy(0.08f), RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp))
                    .padding(horizontal = dimensions.spacingSmall, vertical = dimensions.spacingXSmall)
            ) {
                val parts = record.measurementDate.split("-")
                Text(
                    text  = parts.getOrElse(2) { "--" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = customColors.accentGradientStart
                )
                Text(
                    text  = parts.getOrElse(1) { "--" } + "/" + parts.getOrElse(0) { "--" }.takeLast(2),
                    style = MaterialTheme.typography.labelSmall,
                    color = customColors.accentGradientStart.copy(0.7f)
                )
            }

            Spacer(Modifier.width(dimensions.spacingSmall))

            Row(
                modifier              = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                record.weight?.let {
                    Text("⚖️ $it kg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                }
                record.height?.let {
                    Text("📏 $it cm", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                }
                record.headCircumference?.let {
                    Text("🔵 $it cm", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                }
            }

            if (record.addedByTeam) {
                Text("🏥", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dialogs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TeamCompleteDialogForDetail(
    form        : CompleteVaccinationForm,
    onDismiss   : () -> Unit,
    onChange    : ((CompleteVaccinationForm) -> CompleteVaccinationForm) -> Unit,
    onSubmit    : () -> Unit,
    dimensions  : Dimensions,
    customColors: CustomColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
            ) {
                Text("💉")
                Text("Mark as Completed", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                OutlinedTextField(
                    value         = form.administeredDate,
                    onValueChange = { v -> onChange { it.copy(administeredDate = v) } },
                    label         = { Text("Date (YYYY-MM-DD)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = form.batchNumber,
                    onValueChange = { v -> onChange { f -> f.copy(batchNumber = v) } },
                    label         = { Text("Batch Number (optional)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = form.location,
                    onValueChange = { v -> onChange { f -> f.copy(location = v) } },
                    label         = { Text("Location") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = form.notes,
                    onValueChange = { v -> onChange { f -> f.copy(notes = v) } },
                    label         = { Text("Notes (optional)") },
                    singleLine    = false,
                    maxLines      = 3,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !form.isLoading,
                colors  = ButtonDefaults.buttonColors(containerColor = customColors.success)
            ) {
                if (form.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(dimensions.iconSmall),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Complete ✅")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalTime::class)
@Composable
private fun TeamAddMeasurementDialog(
    babyId      : String,
    onDismiss   : () -> Unit,
    onSave      : (String, Double?, Double?, Double?, String) -> Unit,
    dimensions  : Dimensions,
    customColors: CustomColors
) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var headCirc by remember { mutableStateOf("") }
    var date by remember {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        mutableStateOf("${now.year}-${now.month.number.toString().padStart(2,'0')}-${now.day.toString().padStart(2,'0')}")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
            ) {
                Text("📏")
                Text("Add Measurement", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                OutlinedTextField(
                    value         = date,
                    onValueChange = { date = it },
                    label         = { Text("Date (YYYY-MM-DD)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = weight,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?\$"))) weight = it },
                    label         = { Text("Weight (kg)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = height,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?\$"))) height = it },
                    label         = { Text("Height (cm)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = headCirc,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?\$"))) headCirc = it },
                    label         = { Text("Head Circumference (cm)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(babyId, weight.toDoubleOrNull(), height.toDoubleOrNull(), headCirc.toDoubleOrNull(), date)
                },
                enabled = (weight.isNotBlank() || height.isNotBlank() || headCirc.isNotBlank()) && date.isNotBlank(),
                colors  = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
            ) {
                Text("Save 💾")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.VaccinationFilter
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.VaccinationScheduleUi
import org.example.project.babygrowthtrackingapplication.data.network.*
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
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

    var selectedTab by remember { mutableStateOf(TeamDetailTab.PROFILE) }

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
            TeamDetailHeader(baby = baby, onBack = onBack, customColors = customColors, dimensions = dimensions)
            TeamDetailTabRow(selectedTab = selectedTab, onSelect = { selectedTab = it }, customColors = customColors, dimensions = dimensions)
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
            onSave       = { babyId, w, h, hc, d -> viewModel.addMeasurement(babyId, w, h, hc, d) },
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
    val isFemale      = baby.gender.equals("GIRL", ignoreCase = true)
    val genderEmoji   = if (isFemale) stringResource(Res.string.team_emoji_girl) else stringResource(Res.string.team_emoji_boy)
    val gradientStart = if (isFemale) customColors.accentGradientStart else customColors.accentGradientEnd
    val gradientEnd   = if (isFemale) customColors.accentGradientEnd   else customColors.accentGradientStart

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(gradientStart, gradientEnd)))
    ) {
        Column {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.spacingSmall, vertical = dimensions.spacingXSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.common_back),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text  = stringResource(Res.string.team_baby_profile_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }

            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(
                        start  = dimensions.screenPadding,
                        end    = dimensions.screenPadding,
                        bottom = dimensions.spacingMedium
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier         = Modifier
                        .size(dimensions.avatarLarge)
                        .background(customColors.glassOverlay, CircleShape),
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
                        color      = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text  = stringResource(
                            Res.string.team_baby_age_gender,
                            baby.ageInMonths,
                            if (isFemale) stringResource(Res.string.gender_female) else stringResource(Res.string.gender_male)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                    if (baby.parentName.isNotBlank()) {
                        Text(
                            text  = stringResource(Res.string.team_parent_prefix, baby.parentName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
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
            data class TabInfo(val tab: TeamDetailTab, val emojiRes: String, val labelRes: String)

            val tabs = listOf(
                TabInfo(TeamDetailTab.PROFILE,      stringResource(Res.string.team_emoji_baby),       stringResource(Res.string.team_detail_tab_profile)),
                TabInfo(TeamDetailTab.VACCINATIONS, stringResource(Res.string.team_emoji_vaccine),    stringResource(Res.string.team_detail_tab_vaccines)),
                TabInfo(TeamDetailTab.GROWTH,       stringResource(Res.string.team_emoji_growth),     stringResource(Res.string.team_detail_tab_growth))
            )

            tabs.forEach { info ->
                val selected = selectedTab == info.tab
                val bgColor by animateColorAsState(
                    targetValue   = if (selected) customColors.accentGradientStart.copy(alpha = 0.12f) else Color.Transparent,
                    label         = "detail_tab_bg"
                )
                val fgColor by animateColorAsState(
                    targetValue   = if (selected) customColors.accentGradientStart else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    label         = "detail_tab_fg"
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
                        Text(info.emojiRes, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text       = info.labelRes,
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
    val isFemale    = baby.gender.equals("GIRL", ignoreCase = true)
    val genderLabel = if (isFemale) stringResource(Res.string.team_gender_girl) else stringResource(Res.string.team_gender_boy)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        // Baby information section
        DetailSectionCard(
            title        = stringResource(Res.string.team_section_baby_info),
            emoji        = stringResource(Res.string.team_emoji_baby),
            dimensions   = dimensions,
            customColors = customColors
        ) {
            DetailRow(stringResource(Res.string.team_field_full_name),   baby.fullName,                     stringResource(Res.string.team_emoji_clipboard), dimensions)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = dimensions.hairlineDividerThickness)
            DetailRow(stringResource(Res.string.team_field_dob),         baby.dateOfBirth,                  stringResource(Res.string.team_emoji_birthday),  dimensions)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = dimensions.hairlineDividerThickness)
            DetailRow(stringResource(Res.string.team_field_age),         stringResource(Res.string.team_age_months_value, baby.ageInMonths), stringResource(Res.string.team_emoji_clock), dimensions)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = dimensions.hairlineDividerThickness)
            DetailRow(stringResource(Res.string.team_field_gender),      genderLabel,                       stringResource(Res.string.team_emoji_gender),    dimensions)
        }

        // Parent information section
        DetailSectionCard(
            title        = stringResource(Res.string.team_section_parent_info),
            emoji        = stringResource(Res.string.team_emoji_family),
            dimensions   = dimensions,
            customColors = customColors
        ) {
            DetailRow(stringResource(Res.string.team_field_parent_name), baby.parentName.ifBlank { stringResource(Res.string.team_value_dash) }, stringResource(Res.string.team_emoji_person), dimensions)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = dimensions.hairlineDividerThickness)
            DetailRow(stringResource(Res.string.team_field_phone),       baby.parentPhone.ifBlank { stringResource(Res.string.team_value_dash) }, stringResource(Res.string.team_emoji_phone), dimensions)
        }

        // Bench information
        DetailSectionCard(
            title        = stringResource(Res.string.team_section_health_center),
            emoji        = stringResource(Res.string.team_hospital_emoji),
            dimensions   = dimensions,
            customColors = customColors
        ) {
            DetailRow(stringResource(Res.string.team_field_bench), baby.benchName.ifBlank { stringResource(Res.string.team_value_dash) }, stringResource(Res.string.team_emoji_pin), dimensions)
        }

        // Vaccine status card
        val statusColor = when (baby.vaccineStatus) {
            TeamVaccineStatus.OVERDUE    -> MaterialTheme.colorScheme.error
            TeamVaccineStatus.DUE_SOON   -> customColors.warning
            TeamVaccineStatus.UP_TO_DATE -> customColors.success
            TeamVaccineStatus.NO_SCHEDULE-> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        }
        val statusLabel = when (baby.vaccineStatus) {
            TeamVaccineStatus.OVERDUE    -> stringResource(Res.string.team_status_overdue_desc)
            TeamVaccineStatus.DUE_SOON   -> stringResource(Res.string.team_status_due_soon_desc)
            TeamVaccineStatus.UP_TO_DATE -> stringResource(Res.string.team_status_up_to_date_desc)
            TeamVaccineStatus.NO_SCHEDULE-> stringResource(Res.string.team_status_no_schedule_desc)
        }

        Surface(
            shape    = RoundedCornerShape(dimensions.cardCornerRadius),
            color    = statusColor.copy(alpha = 0.08f),
            border   = BorderStroke(dimensions.borderWidthThin, statusColor.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text       = statusLabel,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = statusColor,
                modifier   = Modifier.padding(dimensions.spacingMedium)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .background(customColors.accentGradientStart.copy(alpha = 0.06f))
                    .padding(dimensions.spacingMedium),
                verticalAlignment     = Alignment.CenterVertically,
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
                content  = content
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
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
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

    // ✅ Resolved here — safely inside @Composable scope
    val filters = listOf(
        VaccinationFilter.ALL       to stringResource(Res.string.admin_vax_tab_all),
        VaccinationFilter.UPCOMING  to stringResource(Res.string.admin_vax_tab_upcoming),
        VaccinationFilter.COMPLETED to stringResource(Res.string.admin_vax_tab_completed),
        VaccinationFilter.OVERDUE   to stringResource(Res.string.admin_vax_tab_overdue)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
        ) {
            // ✅ filters is now a plain List<Pair<...>> — no @Composable calls here
            items(filters) { (filter, label) ->
                FilterChip(
                    selected = state.detailVacFilter == filter,
                    onClick  = { viewModel.setDetailVacFilter(filter) },
                    label    = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = customColors.accentGradientStart,
                        selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                        containerColor         = customColors.accentGradientStart.copy(alpha = 0.06f),
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
                        emoji      = stringResource(Res.string.team_emoji_vaccine),
                        title      = stringResource(Res.string.team_empty_vac_title),
                        subtitle   = stringResource(Res.string.team_empty_vac_subtitle),
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
                                onReschedule = { },
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
    schedule    : VaccinationScheduleUi,
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
        "COMPLETED"   -> customColors.success
        "MISSED"      -> MaterialTheme.colorScheme.error
        "OVERDUE"     -> customColors.warning
        "DUE_SOON"    -> customColors.info
        "RESCHEDULED" -> MaterialTheme.colorScheme.tertiary
        else          -> customColors.accentGradientStart
    }
    val statusEmoji = when (schedule.status) {
        "COMPLETED"   -> stringResource(Res.string.team_emoji_completed)
        "MISSED"      -> stringResource(Res.string.team_emoji_missed)
        "OVERDUE"     -> stringResource(Res.string.team_emoji_overdue)
        "DUE_SOON"    -> stringResource(Res.string.team_emoji_due_soon)
        "RESCHEDULED" -> stringResource(Res.string.team_emoji_rescheduled)
        else          -> stringResource(Res.string.team_emoji_scheduled)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isDone) dimensions.cardElevationSmall * 0f else dimensions.cardElevationSmall)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier         = Modifier
                        .size(dimensions.vaccinationCardIconSize)
                        .background(statusColor.copy(alpha = 0.12f), CircleShape)
                        .border(dimensions.borderWidthThin, statusColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(statusEmoji, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text       = stringResource(Res.string.team_dose_label, schedule.doseNumber),
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
                        color      = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        text  = stringResource(Res.string.team_age_recommended_months, schedule.recommendedAgeMonths),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(dimensions.filterTabCorner),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text      = schedule.status.replace("_", " "),
                        style     = MaterialTheme.typography.labelSmall,
                        color     = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier  = Modifier.padding(
                            horizontal = dimensions.spacingXSmall * 2,
                            vertical   = dimensions.spacingXSmall / 2
                        )
                    )
                }
            }

            Spacer(Modifier.height(dimensions.spacingXSmall))

            Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                Text(
                    text  = stringResource(Res.string.team_scheduled_date_prefix, schedule.scheduledDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                schedule.completedDate?.let {
                    Text(
                        text  = stringResource(Res.string.team_completed_date_prefix, it),
                        style = MaterialTheme.typography.labelSmall,
                        color = customColors.success
                    )
                }
            }

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
                        Text(stringResource(Res.string.team_action_complete), style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick        = onMissed,
                        modifier       = Modifier.weight(1f),
                        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                        border         = BorderStroke(dimensions.borderWidthThin, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        colors         = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(vertical = dimensions.spacingXSmall)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(dimensions.iconSmall))
                        Spacer(Modifier.width(dimensions.spacingXSmall))
                        Text(stringResource(Res.string.team_action_missed), style = MaterialTheme.typography.labelSmall)
                    }

                    if (isOverdue) {
                        IconButton(
                            onClick  = onReschedule,
                            modifier = Modifier
                                .size(dimensions.addButtonSize + dimensions.spacingXSmall)
                                .background(customColors.accentGradientStart.copy(alpha = 0.1f), RoundedCornerShape(dimensions.buttonCornerRadius))
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
        Row(
            modifier              = Modifier
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
                Text(stringResource(Res.string.team_add_measurement), style = MaterialTheme.typography.labelMedium)
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
                    emoji      = stringResource(Res.string.team_emoji_growth),
                    title      = stringResource(Res.string.team_empty_growth_title),
                    subtitle   = stringResource(Res.string.team_empty_growth_subtitle),
                    dimensions = dimensions
                )
            }
            else -> {
                state.detailGrowthRecords.firstOrNull()?.let { latest ->
                    Card(
                        modifier  = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensions.screenPadding),
                        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
                        colors    = CardDefaults.cardColors(containerColor = customColors.accentGradientStart.copy(alpha = 0.06f)),
                        border    = BorderStroke(dimensions.borderWidthThin, customColors.accentGradientStart.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(dimensions.spacingMedium)) {
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(Res.string.team_emoji_chart), style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.width(dimensions.spacingXSmall))
                                Text(
                                    text       = stringResource(Res.string.team_latest_measurement),
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = customColors.accentGradientStart
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text  = latest.measurementDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            Spacer(Modifier.height(dimensions.spacingSmall))
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                latest.weight?.let {
                                    GrowthMetric(stringResource(Res.string.team_emoji_weight), stringResource(Res.string.team_metric_weight), stringResource(Res.string.team_weight_value, it), customColors.accentGradientStart)
                                }
                                latest.height?.let {
                                    GrowthMetric(stringResource(Res.string.team_emoji_height), stringResource(Res.string.team_metric_height), stringResource(Res.string.team_height_value, it), customColors.accentGradientEnd)
                                }
                                latest.headCircumference?.let {
                                    GrowthMetric(stringResource(Res.string.team_emoji_head), stringResource(Res.string.team_metric_head), stringResource(Res.string.team_head_value, it), customColors.info)
                                }
                            }
                            if (latest.addedByTeam) {
                                Spacer(Modifier.height(dimensions.spacingXSmall))
                                Surface(
                                    shape = RoundedCornerShape(dimensions.filterTabCorner),
                                    color = customColors.info.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text     = stringResource(Res.string.team_recorded_by_team),
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = customColors.info,
                                        modifier = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = dimensions.spacingXSmall / 2)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(dimensions.spacingSmall))

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
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
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
        border    = BorderStroke(dimensions.borderWidthThin, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val parts = record.measurementDate.split("-")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(customColors.accentGradientStart.copy(alpha = 0.08f), RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp))
                    .padding(horizontal = dimensions.spacingSmall, vertical = dimensions.spacingXSmall)
            ) {
                Text(
                    text       = parts.getOrElse(2) { "--" },
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = customColors.accentGradientStart
                )
                Text(
                    text  = parts.getOrElse(1) { "--" } + "/" + parts.getOrElse(0) { "--" }.takeLast(2),
                    style = MaterialTheme.typography.labelSmall,
                    color = customColors.accentGradientStart.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.width(dimensions.spacingSmall))

            Row(
                modifier              = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                record.weight?.let {
                    Text(stringResource(Res.string.team_record_weight, it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
                record.height?.let {
                    Text(stringResource(Res.string.team_record_height, it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
                record.headCircumference?.let {
                    Text(stringResource(Res.string.team_record_head, it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
            }

            if (record.addedByTeam) {
                Text(stringResource(Res.string.team_hospital_emoji), style = MaterialTheme.typography.bodySmall)
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
                Text(stringResource(Res.string.team_emoji_vaccine))
                Text(stringResource(Res.string.team_dialog_complete_title), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                OutlinedTextField(
                    value         = form.administeredDate,
                    onValueChange = { v -> onChange { it.copy(administeredDate = v) } },
                    label         = { Text(stringResource(Res.string.team_field_date_hint)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = form.batchNumber,
                    onValueChange = { v -> onChange { f -> f.copy(batchNumber = v) } },
                    label         = { Text(stringResource(Res.string.team_field_batch)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = form.location,
                    onValueChange = { v -> onChange { f -> f.copy(location = v) } },
                    label         = { Text(stringResource(Res.string.team_field_location)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = form.notes,
                    onValueChange = { v -> onChange { f -> f.copy(notes = v) } },
                    label         = { Text(stringResource(Res.string.team_field_notes)) },
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
                        color        = MaterialTheme.colorScheme.onPrimary,
                        modifier     = Modifier.size(dimensions.iconSmall),
                        strokeWidth  = dimensions.borderWidthMedium
                    )
                } else {
                    Text(stringResource(Res.string.team_action_complete_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) }
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
    var weight   by remember { mutableStateOf("") }
    var height   by remember { mutableStateOf("") }
    var headCirc by remember { mutableStateOf("") }
    var date     by remember {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        mutableStateOf("${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.day.toString().padStart(2, '0')}")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
            ) {
                Text(stringResource(Res.string.team_emoji_growth))
                Text(stringResource(Res.string.team_add_measurement), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                OutlinedTextField(
                    value         = date,
                    onValueChange = { date = it },
                    label         = { Text(stringResource(Res.string.team_field_date_hint)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = weight,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?\$"))) weight = it },
                    label         = { Text(stringResource(Res.string.team_field_weight)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = height,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?\$"))) height = it },
                    label         = { Text(stringResource(Res.string.team_field_height)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = headCirc,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?\$"))) headCirc = it },
                    label         = { Text(stringResource(Res.string.team_field_head)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { onSave(babyId, weight.toDoubleOrNull(), height.toDoubleOrNull(), headCirc.toDoubleOrNull(), date) },
                enabled  = (weight.isNotBlank() || height.isNotBlank() || headCirc.isNotBlank()) && date.isNotBlank(),
                colors   = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
            ) {
                Text(stringResource(Res.string.team_save_measurement))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) }
        }
    )
}
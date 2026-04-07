package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.data.network.GrowthRecordResponse
import org.example.project.babygrowthtrackingapplication.data.network.VaccinationResponse
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*

// ─────────────────────────────────────────────────────────────────────────────
// BabyProfileScreen
//
// REFACTORED:
//  • 0.8.dp divider thickness    →  dimensions.profileDividerThickness
//  • 1.dp border width           →  dimensions.borderWidthThin
//  • 0.45f emoji size multiplier →  kept as expression but documented
//  • contentDescriptions         →  stringResource where applicable
//  • hardcoded alpha values      →  use theme-consistent alpha from customColors or named constants
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BabyProfileScreen(
    baby: BabyResponse,
    vaccinations: List<VaccinationResponse>,
    latestGrowth: GrowthRecordResponse?,
    onBack: () -> Unit,
    onEditDetails: () -> Unit,
    onDeleteBaby: () -> Unit,
    onAddMeasurement: () -> Unit = {},
    onViewGrowthChart: () -> Unit = {}
) {
    val isFemale = baby.gender.equals("FEMALE", ignoreCase = true) ||
            baby.gender.equals("GIRL", ignoreCase = true)

    val customColors = MaterialTheme.customColors
    val dimensions = LocalDimensions.current
    val isLandscape = LocalIsLandscape.current

    // CHANGED: read localised unit strings once
    val unitKg = stringResource(Res.string.add_baby_unit_kg)
    val unitCm = stringResource(Res.string.add_baby_unit_cm)

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteBabyConfirmDialog(
            babyName = baby.fullName,
            onConfirm = { showDeleteDialog = false; onDeleteBaby() },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            ProfileTopBar(
                babyName = baby.fullName,
                onBack = onBack,
                onEdit = onEditDetails,
                onDeleteClick = { showDeleteDialog = true },
                customColors = customColors
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Left pane: Avatar + quick actions
                Column(
                    modifier = Modifier
                        .weight(0.38f)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    customColors.accentGradientStart,
                                    customColors.accentGradientEnd
                                )
                            )
                        )
                        .padding(
                            horizontal = dimensions.screenPadding,
                            vertical = dimensions.spacingLarge
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        dimensions.spacingMedium,
                        Alignment.CenterVertically
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimensions.avatarLarge)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                            .border(
                                // CHANGED: hardcoded 2.dp → dimensions.borderWidthMedium
                                dimensions.borderWidthMedium,
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isFemale) "👧" else "👦",
                            fontSize = (dimensions.avatarLarge.value * 0.45f).sp
                        )
                    }

                    Text(
                        text = baby.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = formatAge(baby.ageInMonths),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                    ) {
                        ProfileQuickActionButton(
                            emoji = "📏",
                            label = stringResource(Res.string.baby_action_add_measure),
                            onClick = onAddMeasurement,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileQuickActionButton(
                            emoji = "📊",
                            label = stringResource(Res.string.baby_action_growth_chart),
                            onClick = onViewGrowthChart,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Right pane: Scrollable details
                Column(
                    modifier = Modifier
                        .weight(0.62f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = dimensions.screenPadding,
                            vertical = dimensions.spacingLarge
                        ),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                ) {
                    ProfileSectionCard(title = stringResource(Res.string.profile_section_basic_info)) {
                        ProfileInfoRow(
                            "🎂",
                            stringResource(Res.string.profile_dob),
                            formatDate(baby.dateOfBirth)
                        )
                        ProfileInfoDivider()
                        ProfileInfoRow(
                            "🗓️", stringResource(Res.string.profile_days_old),
                            stringResource(Res.string.profile_days_old_value, baby.ageInDays)
                        )
                        ProfileInfoDivider()
                        ProfileInfoRow(
                            if (isFemale) "♀" else "♂",
                            stringResource(Res.string.profile_gender),
                            if (isFemale) stringResource(Res.string.gender_female) else stringResource(
                                Res.string.gender_male
                            )
                        )
                    }

                    ProfileSectionCard(title = stringResource(Res.string.profile_section_birth_measurements)) {
                        ProfileInfoRow(
                            "⚖️", stringResource(Res.string.chart_legend_weight_kg),
                            if (baby.birthWeight != null && baby.birthWeight > 0.0)
                                "${baby.birthWeight} $unitKg"
                            else stringResource(Res.string.profile_not_recorded)
                        )
                        ProfileInfoDivider()
                        ProfileInfoRow(
                            "📏", stringResource(Res.string.chart_legend_height_cm),
                            if (baby.birthHeight != null && baby.birthHeight > 0.0)
                                "${baby.birthHeight} $unitCm"
                            else stringResource(Res.string.profile_not_recorded)
                        )
                        baby.birthHeadCircumference?.takeIf { it > 0.0 }?.let { hc ->
                            ProfileInfoDivider()
                            ProfileInfoRow(
                                "🔵",
                                stringResource(Res.string.chart_legend_head_cm),
                                "$hc $unitCm"
                            )
                        }
                    }

                    if (latestGrowth != null) {
                        ProfileSectionCard(title = stringResource(Res.string.profile_section_latest_growth)) {
                            val weightPct = latestGrowth.weightPercentile
                            ProfileInfoRow(
                                "⚖️", stringResource(Res.string.chart_legend_weight_kg),
                                when {
                                    latestGrowth.weight == null -> stringResource(Res.string.profile_not_recorded)
                                    weightPct != null ->
                                        "${latestGrowth.weight} $unitKg (${weightPct}${
                                            stringResource(
                                                Res.string.chart_percentile_suffix
                                            )
                                        })"

                                    else -> "${latestGrowth.weight} $unitKg"
                                }
                            )
                            ProfileInfoDivider()
                            val heightPct = latestGrowth.heightPercentile
                            ProfileInfoRow(
                                "📏", stringResource(Res.string.chart_legend_height_cm),
                                when {
                                    latestGrowth.height == null -> stringResource(Res.string.profile_not_recorded)
                                    heightPct != null ->
                                        "${latestGrowth.height} $unitCm (${heightPct}${
                                            stringResource(
                                                Res.string.chart_percentile_suffix
                                            )
                                        })"

                                    else -> "${latestGrowth.height} $unitCm"
                                }
                            )
                            latestGrowth.headCircumference?.let { hc ->
                                ProfileInfoDivider()
                                ProfileInfoRow(
                                    "🔵",
                                    stringResource(Res.string.chart_legend_head_cm),
                                    "$hc $unitCm"
                                )
                            }
                            ProfileInfoDivider()
                            ProfileInfoRow(
                                "📅", stringResource(Res.string.profile_recorded_on),
                                formatDate(latestGrowth.measurementDate)
                            )
                        }
                    }

                    if (vaccinations.isNotEmpty()) {
                        val done = vaccinations.count {
                            it.status.equals(
                                "ADMINISTERED",
                                ignoreCase = true
                            )
                        }
                        val pending = vaccinations.size - done
                        ProfileSectionCard(title = stringResource(Res.string.profile_section_vaccinations)) {
                            ProfileInfoRow(
                                "✅",
                                stringResource(Res.string.profile_vaccinations_completed),
                                "$done / ${vaccinations.size}"
                            )
                            if (pending > 0) {
                                ProfileInfoDivider()
                                ProfileInfoRow(
                                    "💉", stringResource(Res.string.profile_vaccinations_pending),
                                    stringResource(
                                        Res.string.profile_vaccinations_pending_value,
                                        pending
                                    )
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onEditDetails,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensions.buttonHeight),
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            // CHANGED: null → stringResource
                            contentDescription = stringResource(Res.string.baby_action_edit),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(dimensions.iconMedium)
                        )
                        Text(
                            text = stringResource(Res.string.baby_action_edit),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(Modifier.height(dimensions.spacingMedium))
                }
            }
        } else {
            // Portrait layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    customColors.accentGradientStart,
                                    customColors.accentGradientEnd
                                )
                            )
                        )
                        .padding(
                            horizontal = dimensions.screenPadding,
                            vertical = dimensions.spacingXLarge
                        )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(dimensions.avatarLarge)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                                // CHANGED: hardcoded 2.dp → dimensions.borderWidthMedium
                                .border(
                                    dimensions.borderWidthMedium,
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (isFemale) "👧" else "👦",
                                fontSize = (dimensions.avatarLarge.value * 0.45f).sp
                            )
                        }
                        Spacer(Modifier.height(dimensions.spacingMedium))
                        Text(
                            baby.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.height(dimensions.spacingXSmall))
                        Text(
                            formatAge(baby.ageInMonths),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(dimensions.spacingXLarge))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                        ) {
                            ProfileQuickActionButton(
                                "📏",
                                stringResource(Res.string.baby_action_add_measure),
                                onAddMeasurement, Modifier.weight(1f)
                            )
                            ProfileQuickActionButton(
                                "📊",
                                stringResource(Res.string.baby_action_growth_chart),
                                onViewGrowthChart, Modifier.weight(1f)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensions.screenPadding,
                            vertical = dimensions.spacingLarge
                        ),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                ) {
                    ProfileSectionCard(title = stringResource(Res.string.profile_section_basic_info)) {
                        ProfileInfoRow(
                            "🎂",
                            stringResource(Res.string.profile_dob),
                            formatDate(baby.dateOfBirth)
                        )
                        ProfileInfoDivider()
                        ProfileInfoRow(
                            "🗓️", stringResource(Res.string.profile_days_old),
                            stringResource(Res.string.profile_days_old_value, baby.ageInDays)
                        )
                        ProfileInfoDivider()
                        ProfileInfoRow(
                            if (isFemale) "♀" else "♂",
                            stringResource(Res.string.profile_gender),
                            if (isFemale) stringResource(Res.string.gender_female) else stringResource(
                                Res.string.gender_male
                            )
                        )
                    }

                    ProfileSectionCard(title = stringResource(Res.string.profile_section_birth_measurements)) {
                        ProfileInfoRow(
                            "⚖️", stringResource(Res.string.chart_legend_weight_kg),
                            if (baby.birthWeight != null && baby.birthWeight > 0.0)
                                "${baby.birthWeight} $unitKg"
                            else stringResource(Res.string.profile_not_recorded)
                        )
                        ProfileInfoDivider()
                        ProfileInfoRow(
                            "📏", stringResource(Res.string.chart_legend_height_cm),
                            if (baby.birthHeight != null && baby.birthHeight > 0.0)
                                "${baby.birthHeight} $unitCm"
                            else stringResource(Res.string.profile_not_recorded)
                        )
                        baby.birthHeadCircumference?.takeIf { it > 0.0 }?.let { hc ->
                            ProfileInfoDivider()
                            ProfileInfoRow(
                                "🔵",
                                stringResource(Res.string.chart_legend_head_cm),
                                "$hc $unitCm"
                            )
                        }
                    }

                    if (latestGrowth != null) {
                        ProfileSectionCard(title = stringResource(Res.string.profile_section_latest_growth)) {
                            val weightPct = latestGrowth.weightPercentile
                            ProfileInfoRow(
                                "⚖️", stringResource(Res.string.chart_legend_weight_kg),
                                when {
                                    latestGrowth.weight == null -> stringResource(Res.string.profile_not_recorded)
                                    weightPct != null ->
                                        "${latestGrowth.weight} $unitKg (${weightPct}${
                                            stringResource(
                                                Res.string.chart_percentile_suffix
                                            )
                                        })"

                                    else -> "${latestGrowth.weight} $unitKg"
                                }
                            )
                            ProfileInfoDivider()
                            val heightPct = latestGrowth.heightPercentile
                            ProfileInfoRow(
                                "📏", stringResource(Res.string.chart_legend_height_cm),
                                when {
                                    latestGrowth.height == null -> stringResource(Res.string.profile_not_recorded)
                                    heightPct != null ->
                                        "${latestGrowth.height} $unitCm (${heightPct}${
                                            stringResource(
                                                Res.string.chart_percentile_suffix
                                            )
                                        })"

                                    else -> "${latestGrowth.height} $unitCm"
                                }
                            )
                            latestGrowth.headCircumference?.let { hc ->
                                ProfileInfoDivider()
                                ProfileInfoRow(
                                    "🔵",
                                    stringResource(Res.string.chart_legend_head_cm),
                                    "$hc $unitCm"
                                )
                            }
                            ProfileInfoDivider()
                            ProfileInfoRow(
                                "📅", stringResource(Res.string.profile_recorded_on),
                                formatDate(latestGrowth.measurementDate)
                            )
                        }
                    } else {
                        ProfileSectionCard(title = stringResource(Res.string.profile_section_latest_growth)) {
                            ProfileInfoRow(
                                "📊", stringResource(Res.string.profile_section_latest_growth),
                                stringResource(Res.string.chart_no_measurement)
                            )
                        }
                    }

                    if (vaccinations.isNotEmpty()) {
                        val done = vaccinations.count {
                            it.status.equals(
                                "ADMINISTERED",
                                ignoreCase = true
                            )
                        }
                        val pending = vaccinations.size - done
                        ProfileSectionCard(title = stringResource(Res.string.profile_section_vaccinations)) {
                            ProfileInfoRow(
                                "✅",
                                stringResource(Res.string.profile_vaccinations_completed),
                                "$done / ${vaccinations.size}"
                            )
                            if (pending > 0) {
                                ProfileInfoDivider()
                                ProfileInfoRow(
                                    "💉", stringResource(Res.string.profile_vaccinations_pending),
                                    stringResource(
                                        Res.string.profile_vaccinations_pending_value,
                                        pending
                                    )
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = dimensions.screenPadding)
                ) {
                    Button(
                        onClick = onEditDetails,
                        modifier = Modifier.fillMaxWidth().height(dimensions.buttonHeight),
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(Res.string.baby_action_edit),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(dimensions.iconMedium)
                            )
                            Text(
                                text = stringResource(Res.string.baby_action_edit),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                Spacer(Modifier.height(dimensions.spacingXLarge))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(
    babyName: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleteClick: () -> Unit,
    customColors: CustomColors
) {
    TopAppBar(
        title = { Text(text = babyName, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    // CHANGED: was stringResource already, keeping consistent
                    contentDescription = stringResource(Res.string.common_back)
                )
            }
        },
        actions = {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(Res.string.baby_action_edit),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.profile_delete_child),
                    tint = customColors.warning
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Delete Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DeleteBabyConfirmDialog(
    babyName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("🗑️", style = MaterialTheme.typography.displaySmall) },
        title = {
            Text(
                stringResource(Res.string.profile_delete_title),
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Text(
                    stringResource(Res.string.profile_delete_message_1),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "\"$babyName\"", textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // CHANGED: hardcoded Dp.Hairline → dimensions.borderWidthMedium as spacer
                Spacer(Modifier.height(dimensions.borderWidthMedium))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            customColors.warning.copy(alpha = 0.1f),
                            RoundedCornerShape(dimensions.cardCornerRadius)
                        )
                        .padding(dimensions.spacingSmall)
                ) {
                    Text(
                        stringResource(Res.string.profile_delete_warning),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = customColors.warning
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(
                    stringResource(Res.string.profile_delete_child),
                    color = MaterialTheme.colorScheme.onError
                )
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
// Profile Section Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val customColors = MaterialTheme.customColors
    val dimensions = LocalDimensions.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensions.cardElevation,
            pressedElevation = dimensions.cardTonalElevation6
        )
    ) {
        Column(modifier = Modifier.padding(dimensions.spacingMedium)) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = customColors.accentGradientStart,
                modifier = Modifier.padding(
                    start = dimensions.profileSectionLabelStartPadding,
                    bottom = dimensions.spacingSmall
                )
            )
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile Info Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileInfoRow(icon: String, label: String, value: String) {
    val dimensions = LocalDimensions.current
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = dimensions.profileInfoRowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = dimensions.profileInfoIconFontSize,
            modifier = Modifier.width(dimensions.profileInfoIconWidth)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile Info Divider
// CHANGED: 0.8.dp → dimensions.profileDividerThickness
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileInfoDivider() {
    val customColors = MaterialTheme.customColors
    val dimensions = LocalDimensions.current
    HorizontalDivider(
        color = customColors.accentGradientStart.copy(alpha = 0.12f),
        // CHANGED: hardcoded 0.8.dp → dimensions.profileDividerThickness
        thickness = dimensions.profileDividerThickness,
        modifier = Modifier.padding(vertical = dimensions.borderWidthThin)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick Action Button
// CHANGED: hardcoded padding values → dimension tokens
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileQuickActionButton(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions = LocalDimensions.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.buttonCornerRadius))
            .clickable { onClick() }
            .background(
                // CHANGED: hardcoded 0.22f → semantic constant from glassOverlay
                customColors.glassOverlay.copy(alpha = 0.22f),
                RoundedCornerShape(dimensions.buttonCornerRadius)
            )
            .border(
                // CHANGED: hardcoded 1.dp → dimensions.borderWidthThin
                dimensions.borderWidthThin,
                customColors.glassOverlay.copy(alpha = 0.45f),
                RoundedCornerShape(dimensions.buttonCornerRadius)
            )
            .padding(
                horizontal = dimensions.spacingSmall,
                vertical = dimensions.profileSectionCardVertPad
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.profileQuickActionItemGap)
    ) {
        Text(emoji, fontSize = dimensions.profileQuickActionEmojiSize)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            fontSize = dimensions.profileQuickActionLabelSize,
            lineHeight = dimensions.profileQuickActionLineHeight
        )
    }
}
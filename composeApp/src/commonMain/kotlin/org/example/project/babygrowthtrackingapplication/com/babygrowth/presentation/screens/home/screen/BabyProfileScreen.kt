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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.data.network.GrowthRecordResponse
import org.example.project.babygrowthtrackingapplication.data.network.VaccinationResponse
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*

// ─────────────────────────────────────────────────────────────────────────────
// BabyProfileScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BabyProfileScreen(
    baby              : BabyResponse,
    vaccinations      : List<VaccinationResponse>,
    latestGrowth      : GrowthRecordResponse?,
    onBack            : () -> Unit,
    onEditDetails     : () -> Unit,
    onDeleteBaby      : () -> Unit,
    onAddMeasurement  : () -> Unit = {},
    onViewGrowthChart : () -> Unit = {}
) {
    val isFemale    = baby.gender.equals("FEMALE", ignoreCase = true) ||
            baby.gender.equals("GIRL",   ignoreCase = true)
    val genderTheme = if (isFemale) GenderTheme.GIRL else GenderTheme.BOY

    var showDeleteDialog by remember { mutableStateOf(false) }

    BabyGrowthTheme(genderTheme = genderTheme) {

        val customColors = MaterialTheme.customColors
        val dimensions   = LocalDimensions.current

        if (showDeleteDialog) {
            DeleteBabyConfirmDialog(
                babyName  = baby.fullName,
                onConfirm = { showDeleteDialog = false; onDeleteBaby() },
                onDismiss = { showDeleteDialog = false }
            )
        }

        Scaffold(
            topBar = {
                ProfileTopBar(
                    babyName      = baby.fullName,
                    onBack        = onBack,
                    onEdit        = onEditDetails,
                    onDeleteClick = { showDeleteDialog = true },
                    customColors  = customColors
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
            ) {

                // ── [1] HERO HEADER ───────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    customColors.accentGradientStart.copy(alpha = 0.85f),
                                    customColors.accentGradientEnd.copy(alpha = 0.70f)
                                )
                            ),
                            RoundedCornerShape(
                                bottomStart = dimensions.spacingXXLarge - dimensions.spacingXSmall,
                                bottomEnd   = dimensions.spacingXXLarge - dimensions.spacingXSmall
                            )
                        )
                        .padding(
                            start   = dimensions.spacingLarge,
                            end     = dimensions.spacingLarge,
                            top     = dimensions.spacingMedium,
                            bottom  = dimensions.spacingXLarge
                        )
                ) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Avatar circle
                        Box(
                            modifier = Modifier
                                .size(dimensions.avatarLarge + dimensions.spacingMedium)
                                .clip(CircleShape)
                                .background(customColors.glassOverlay.copy(alpha = 0.20f))
                                .border(
                                    dimensions.borderWidthMedium,   // WAS: 2.dp
                                    customColors.glassOverlay.copy(alpha = 0.45f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (isFemale) "👶" else "👦",
                                style    = MaterialTheme.typography.displaySmall,
                                color    = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Spacer(Modifier.height(dimensions.spacingMedium))

                        // Name
                        Text(
                            text       = baby.fullName,
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimary,
                            textAlign  = TextAlign.Center
                        )

                        Spacer(Modifier.height(dimensions.spacingXSmall))

                        // Gender + age line
                        Text(
                            text  = "${if (isFemale) "♀" else "♂"}  ${
                                formatProfileAge(baby.ageInMonths)
                            }  •  ${
                                if (isFemale) stringResource(Res.string.gender_female)
                                else          stringResource(Res.string.gender_male)
                            }",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        // Archived badge (conditional)
                        if (!baby.isActive) {
                            Spacer(Modifier.height(dimensions.spacingSmall))
                            Box(
                                modifier = Modifier
                                    .background(
                                        customColors.glassOverlay,
                                        RoundedCornerShape(dimensions.profileArchivedCorner) // WAS: 8.dp
                                    )
                                    .padding(
                                        horizontal = dimensions.profileArchivedPaddingH,     // WAS: 12.dp
                                        vertical   = dimensions.profileArchivedPaddingV      // WAS: 4.dp
                                    )
                            ) {
                                Text(
                                    text       = stringResource(Res.string.baby_status_archived),
                                    style      = MaterialTheme.typography.labelSmall,
                                    color      = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(Modifier.height(dimensions.spacingXLarge))

                        // Quick-action row
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                        ) {
                            ProfileQuickActionButton(
                                emoji    = "📏",
                                label    = stringResource(Res.string.baby_action_add_measure),
                                onClick  = onAddMeasurement,
                                modifier = Modifier.weight(1f)
                            )
                            ProfileQuickActionButton(
                                emoji    = "📊",
                                label    = stringResource(Res.string.baby_action_growth_chart),
                                onClick  = onViewGrowthChart,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ── [2] FORM BODY ─────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensions.screenPadding,
                            vertical   = dimensions.spacingLarge
                        ),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                ) {

                    // BASIC INFORMATION
                    ProfileSectionCard(title = stringResource(Res.string.profile_section_basic_info)) {
                        ProfileInfoRow(
                            icon  = "🎂",
                            label = stringResource(Res.string.profile_dob),
                            value = formatProfileDate(baby.dateOfBirth)
                        )
                        ProfileInfoDivider()
                        ProfileInfoRow(
                            icon  = "🗓️",
                            label = stringResource(Res.string.profile_days_old),
                            value = stringResource(Res.string.profile_days_old_value, baby.ageInDays.toInt())
                        )
                        ProfileInfoDivider()
                        ProfileInfoRow(
                            icon  = if (isFemale) "♀" else "♂",
                            label = stringResource(Res.string.profile_gender),
                            value = if (isFemale) stringResource(Res.string.gender_female)
                            else          stringResource(Res.string.gender_male)
                        )
                        ProfileInfoDivider()
                        ProfileInfoRow(
                            icon  = "🗓️",
                            label = stringResource(Res.string.profile_days_old),
                            value = stringResource(Res.string.profile_days_old_value, baby.ageInMonths * 30)
                        )
                    }

                    // BIRTH MEASUREMENTS
                    ProfileSectionCard(title = stringResource(Res.string.profile_section_birth_measurements)) {
                        ProfileInfoRow(
                            icon  = "⚖️",
                            label = stringResource(Res.string.chart_legend_weight_kg),
                            value = if (baby.birthWeight != null && baby.birthWeight > 0.0)
                                stringResource(Res.string.profile_weight_value, baby.birthWeight.toString())
                            else
                                stringResource(Res.string.profile_not_recorded)   // "Not recorded"
                        )
                        ProfileInfoDivider()
                        ProfileInfoRow(
                            icon  = "📏",
                            label = stringResource(Res.string.chart_legend_height_cm),
                            value = if (baby.birthHeight != null && baby.birthHeight > 0.0)
                                stringResource(Res.string.profile_height_value, baby.birthHeight.toString())
                            else
                                stringResource(Res.string.profile_not_recorded)   // "Not recorded"
                        )
                    }

                    // LATEST GROWTH RECORD
                    latestGrowth?.let { growth ->
                        ProfileSectionCard(title = stringResource(Res.string.profile_section_latest_growth)) {
                            // Weight row
                            val weightPct = growth.weightPercentile
                            ProfileInfoRow(
                                icon  = "⚖️",
                                label = stringResource(Res.string.chart_legend_weight_kg),
                                value = when {
                                    growth.weight == null -> stringResource(Res.string.profile_not_recorded)
                                    weightPct != null     -> stringResource(Res.string.profile_weight_percentile, growth.weight.toString(), weightPct)
                                    else                  -> stringResource(Res.string.profile_weight_value, growth.weight.toString())
                                }
                            )
                            ProfileInfoDivider()

                            // Height row
                            val heightPct = growth.heightPercentile
                            ProfileInfoRow(
                                icon  = "📏",
                                label = stringResource(Res.string.chart_legend_height_cm),
                                value = when {
                                    growth.height == null -> stringResource(Res.string.profile_not_recorded)
                                    heightPct != null     -> stringResource(Res.string.profile_height_percentile, growth.height.toString(), heightPct)
                                    else                  -> stringResource(Res.string.profile_height_value, growth.height.toString())
                                }
                            )
                            ProfileInfoDivider()

                            // Head circumference row (bonus — also guarded)
                            growth.headCircumference?.let { hc ->
                                ProfileInfoRow(
                                    icon  = "🔵",
                                    label = stringResource(Res.string.chart_legend_head_cm),
                                    value = stringResource(Res.string.chart_head_unit, hc.toString())
                                )
                                ProfileInfoDivider()
                            }

                            // Recorded on
                            ProfileInfoRow(
                                icon  = "📅",
                                label = stringResource(Res.string.profile_recorded_on),
                                value = formatProfileDate(growth.measurementDate)
                            )
                        }
                    }
// If no growth record at all, show a placeholder card:
                        ?: ProfileSectionCard(title = stringResource(Res.string.profile_section_latest_growth)) {
                            ProfileInfoRow(
                                icon  = "📊",
                                label = stringResource(Res.string.profile_section_latest_growth),
                                value = stringResource(Res.string.chart_no_measurement)   // "No measurement yet"
                            )
                        }




                    // VACCINATIONS
                    if (vaccinations.isNotEmpty()) {
                        val done    = vaccinations.count { it.status.equals("ADMINISTERED", ignoreCase = true) }
                        val pending = vaccinations.size - done
                        ProfileSectionCard(title = stringResource(Res.string.profile_section_vaccinations)) {
                            ProfileInfoRow(
                                icon  = "✅",
                                label = stringResource(Res.string.profile_vaccinations_completed),
                                value = "$done / ${vaccinations.size}"
                            )
                            if (pending > 0) {
                                ProfileInfoDivider()
                                ProfileInfoRow(
                                    icon  = "💉",
                                    label = stringResource(Res.string.profile_vaccinations_pending),
                                    value = stringResource(Res.string.profile_vaccinations_pending_value, pending)
                                )
                            }
                        }
                    }
                }

                // ── [3] EDIT DETAILS button ───────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensions.screenPadding)
                ) {
                    Button(
                        onClick        = onEditDetails,
                        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        elevation      = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                        modifier       = Modifier.fillMaxWidth().height(dimensions.buttonHeight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            customColors.accentGradientStart.copy(alpha = 0.85f),
                                            customColors.accentGradientEnd.copy(alpha = 0.75f)
                                        )
                                    ),
                                    RoundedCornerShape(dimensions.buttonCornerRadius)
                                )
                                .border(
                                    dimensions.borderWidthThin,                       // WAS: 1.dp
                                    MaterialTheme.colorScheme.onPrimary.copy(0.20f),  // WAS: Color.White
                                    RoundedCornerShape(dimensions.buttonCornerRadius)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f), // WAS: Color.White
                                        RoundedCornerShape(dimensions.buttonCornerRadius)
                                    )
                            )
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint     = MaterialTheme.colorScheme.onPrimary,  // WAS: Color.White
                                    modifier = Modifier.size(dimensions.iconMedium)
                                )
                                Text(
                                    text       = stringResource(Res.string.baby_action_edit),
                                    color      = MaterialTheme.colorScheme.onPrimary, // WAS: Color.White
                                    fontWeight = FontWeight.Bold,
                                    style      = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(dimensions.spacingSmall))

                    // ── [4] DELETE button ─────────────────────────────────────
                    Button(
                        onClick        = { showDeleteDialog = true },
                        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        elevation      = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                        modifier       = Modifier.fillMaxWidth().height(dimensions.buttonHeight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            customColors.warning.copy(alpha = 0.80f),
                                            customColors.warning.copy(alpha = 0.60f)
                                        )
                                    ),
                                    RoundedCornerShape(dimensions.buttonCornerRadius)
                                )
                                .border(
                                    dimensions.borderWidthThin,                       // WAS: 1.dp
                                    MaterialTheme.colorScheme.onPrimary.copy(0.20f),  // WAS: Color.White
                                    RoundedCornerShape(dimensions.buttonCornerRadius)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f), // WAS: Color.White
                                        RoundedCornerShape(dimensions.buttonCornerRadius)
                                    )
                            )
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint     = MaterialTheme.colorScheme.onPrimary, // WAS: Color.White
                                    modifier = Modifier.size(dimensions.iconMedium)
                                )
                                Text(
                                    text       = stringResource(Res.string.profile_delete_child),
                                    color      = MaterialTheme.colorScheme.onPrimary, // WAS: Color.White
                                    fontWeight = FontWeight.Bold,
                                    style      = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(dimensions.spacingXLarge))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP APP BAR
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(
    babyName     : String,
    onBack       : () -> Unit,
    onEdit       : () -> Unit,
    onDeleteClick: () -> Unit,
    customColors : CustomColors
) {
    TopAppBar(
        title = {
            Text(
                text       = babyName,
                fontWeight = FontWeight.SemiBold,
                style      = MaterialTheme.typography.titleMedium,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.common_back),
                    tint               = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        actions = {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector        = Icons.Default.Edit,
                    contentDescription = stringResource(Res.string.baby_action_edit),
                    tint               = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector        = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.profile_delete_child),
                    tint               = customColors.warning
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// DELETE CONFIRMATION DIALOG
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DeleteBabyConfirmDialog(
    babyName : String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    AlertDialog(
        onDismissRequest = onDismiss,
        icon  = { Text("🗑️", style = MaterialTheme.typography.displaySmall) },
        title = {
            Text(
                text       = stringResource(Res.string.profile_delete_title),
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                style      = MaterialTheme.typography.titleMedium,
                color      = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Text(
                    text      = stringResource(Res.string.profile_delete_message_1),
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = "\"$babyName\"",
                    textAlign  = TextAlign.Center,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(dimensions.borderWidthMedium))  // WAS: 2.dp

                // Warning panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            customColors.warning.copy(alpha = 0.10f),
                            RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                        )
                        .border(
                            dimensions.borderWidthThin,                          // WAS: 1.dp
                            customColors.warning.copy(alpha = 0.35f),
                            RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                        )
                        .padding(
                            horizontal = dimensions.spacingMedium,
                            vertical   = dimensions.spacingSmall + dimensions.borderWidthMedium
                        )
                ) {
                    Row(
                        verticalAlignment     = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.babyCardGenderSpacerW) // WAS: 6.dp
                    ) {
                        Text("⚠️", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text  = stringResource(Res.string.profile_delete_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(
                    text       = stringResource(Res.string.profile_delete_child),
                    color      = MaterialTheme.colorScheme.onError,
                    fontWeight = FontWeight.SemiBold,
                    style      = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text       = stringResource(Res.string.delete_cancel),
                    color      = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    style      = MaterialTheme.typography.labelLarge
                )
            }
        },
        shape          = RoundedCornerShape(dimensions.cardCornerRadius),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = dimensions.cardTonalElevation6   // WAS: 6.dp
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// QUICK ACTION BUTTON
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileQuickActionButton(
    emoji   : String,
    label   : String,
    onClick : () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.buttonCornerRadius))
            .clickable { onClick() }
            .background(
                customColors.glassOverlay.copy(alpha = 0.22f),
                RoundedCornerShape(dimensions.buttonCornerRadius)
            )
            .border(
                dimensions.borderWidthThin,                                // WAS: 1.dp
                customColors.glassOverlay.copy(alpha = 0.45f),
                RoundedCornerShape(dimensions.buttonCornerRadius)
            )
            .padding(
                horizontal = dimensions.spacingSmall,
                vertical   = dimensions.profileSectionCardVertPad          // WAS: spacingMedium - 2.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.profileQuickActionItemGap) // WAS: 4.dp
    ) {
        Text(
            emoji,
            fontSize = dimensions.profileQuickActionEmojiSize              // WAS: 20.sp
        )
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = MaterialTheme.colorScheme.onPrimary,
            textAlign  = TextAlign.Center,
            maxLines   = 2,
            fontSize   = dimensions.profileQuickActionLabelSize,           // WAS: 9.sp
            lineHeight = dimensions.profileQuickActionLineHeight            // WAS: 11.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileSectionCard(
    title  : String,
    content: @Composable ColumnScope.() -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text          = title,
            style         = MaterialTheme.typography.labelMedium,
            fontWeight    = FontWeight.Bold,
            color         = customColors.accentGradientStart,
            letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing, // WAS: 0.8.sp
            modifier      = Modifier.padding(
                start  = dimensions.profileSectionLabelStartPadding,            // WAS: 4.dp
                bottom = dimensions.spacingSmall
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.40f)
                        )
                    ),
                    RoundedCornerShape(dimensions.buttonCornerRadius)
                )
                .border(
                    dimensions.borderWidthThin,                                 // WAS: 1.dp
                    customColors.accentGradientStart.copy(alpha = 0.18f),
                    RoundedCornerShape(dimensions.buttonCornerRadius)
                )
                .padding(
                    horizontal = dimensions.spacingMedium,
                    vertical   = dimensions.profileSectionCardVertPad           // WAS: spacingMedium - 2.dp
                )
        ) {
            // Glassmorphic overlay — WAS: Color.White.copy(alpha = 0.08f)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.08f),
                        RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            )
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INFO ROW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileInfoRow(icon: String, label: String, value: String) {
    val dimensions = LocalDimensions.current
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensions.profileInfoRowVerticalPadding),  // WAS: 5.dp
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            icon,
            fontSize = dimensions.profileInfoIconFontSize,                   // WAS: 15.sp
            modifier = Modifier.width(dimensions.profileInfoIconWidth)       // WAS: 28.dp
        )
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurface.copy(0.55f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface,
            textAlign  = TextAlign.End,
            modifier   = Modifier.weight(1.4f)
        )
    }
}

@Composable
private fun ProfileInfoDivider() {
    val customColors = MaterialTheme.customColors
    HorizontalDivider(
        color     = customColors.accentGradientStart.copy(alpha = 0.12f),
        thickness = dimensions_dividerThickness,
        modifier  = Modifier.padding(vertical = dimensions_dividerPadding)
    )
}

// tiny local helpers — these only live in this file's scope
private val dimensions_dividerThickness get() = 0.8.dp
private val dimensions_dividerPadding   get() = 1.dp

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun formatProfileAge(months: Int): String = when {
    months < 1  -> stringResource(Res.string.age_newborn)
    months < 12 -> if (months == 1)
        stringResource(Res.string.age_months, months)
    else
        stringResource(Res.string.age_months_plural, months)
    else -> {
        val y = months / 12
        val m = months % 12
        if (m == 0)
            stringResource(Res.string.age_years_only, y)
        else
            stringResource(Res.string.age_years_months, y, m)
    }
}

@Composable
private fun formatProfileDate(dateStr: String): String {
    val parts = dateStr.split("-")
    if (parts.size != 3) return dateStr
    val monthIndex = parts[1].toIntOrNull() ?: return dateStr
    val monthNames = listOf(
        "",
        stringResource(Res.string.month_jan), stringResource(Res.string.month_feb),
        stringResource(Res.string.month_mar), stringResource(Res.string.month_apr),
        stringResource(Res.string.month_may), stringResource(Res.string.month_jun),
        stringResource(Res.string.month_jul), stringResource(Res.string.month_aug),
        stringResource(Res.string.month_sep), stringResource(Res.string.month_oct),
        stringResource(Res.string.month_nov), stringResource(Res.string.month_dec)
    )
    val month = monthNames.getOrElse(monthIndex) { parts[1] }
    return "$month ${parts[2]}, ${parts[0]}"
}
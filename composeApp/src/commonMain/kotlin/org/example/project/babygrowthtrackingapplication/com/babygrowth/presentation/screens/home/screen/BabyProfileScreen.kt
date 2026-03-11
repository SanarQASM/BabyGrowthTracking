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
    val isFemale = baby.gender.equals("FEMALE", ignoreCase = true) ||
            baby.gender.equals("GIRL",   ignoreCase = true)

    // FIX: Removed the two lines below and the BabyGrowthTheme wrapper that followed:
    //   val genderTheme = if (isFemale) GenderTheme.GIRL else GenderTheme.BOY
    //   BabyGrowthTheme(genderTheme = genderTheme) { ... }
    //
    // WHY IT WAS BROKEN:
    //   The wrapper called BabyGrowthTheme(genderTheme) WITHOUT passing darkTheme,
    //   so darkTheme defaulted to isSystemInDarkTheme() — the OS setting — which
    //   completely ignored the user's in-app dark/light toggle on this screen.
    //   It also silently overrode the user's chosen gender theme from Settings.
    //
    // THE FIX:
    //   Remove the wrapper. This screen already lives inside the root BabyGrowthTheme
    //   in App.kt which correctly receives both genderTheme and isDarkMode from
    //   PreferencesManager. All theme values now flow through naturally.

    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    var showDeleteDialog by remember { mutableStateOf(false) }

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

            // ── [1] HERO HEADER ───────────────────────────────────────────────
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
                        vertical   = dimensions.spacingXLarge
                    )
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar circle
                    Box(
                        modifier         = Modifier
                            .size(dimensions.avatarLarge)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                            .border(
                                dimensions.borderWidthMedium,
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text     = if (isFemale) "👧" else "👦",
                            fontSize = (dimensions.avatarLarge.value * 0.45f).sp
                        )
                    }

                    Spacer(Modifier.height(dimensions.spacingMedium))

                    Text(
                        text       = baby.fullName,
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(Modifier.height(dimensions.spacingXSmall))

                    Text(
                        text  = formatProfileAge(baby.ageInMonths),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )

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

            // ── [2] FORM BODY ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensions.screenPadding,
                        vertical   = dimensions.spacingLarge
                    ),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {

                // ── BASIC INFORMATION ─────────────────────────────────────────
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
                        value = "${baby.ageInDays} days"
                    )
                    ProfileInfoDivider()
                    ProfileInfoRow(
                        icon  = if (isFemale) "♀" else "♂",
                        label = stringResource(Res.string.profile_gender),
                        value = if (isFemale) stringResource(Res.string.gender_female)
                        else          stringResource(Res.string.gender_male)
                    )
                }

                // ── BIRTH MEASUREMENTS ────────────────────────────────────────
                ProfileSectionCard(title = stringResource(Res.string.profile_section_birth_measurements)) {
                    ProfileInfoRow(
                        icon  = "⚖️",
                        label = stringResource(Res.string.chart_legend_weight_kg),
                        value = if (baby.birthWeight != null && baby.birthWeight > 0.0)
                            "${baby.birthWeight} kg"
                        else
                            stringResource(Res.string.profile_not_recorded)
                    )
                    ProfileInfoDivider()
                    ProfileInfoRow(
                        icon  = "📏",
                        label = stringResource(Res.string.chart_legend_height_cm),
                        value = if (baby.birthHeight != null && baby.birthHeight > 0.0)
                            "${baby.birthHeight} cm"
                        else
                            stringResource(Res.string.profile_not_recorded)
                    )
                    baby.birthHeadCircumference?.takeIf { it > 0.0 }?.let { hc ->
                        ProfileInfoDivider()
                        ProfileInfoRow(
                            icon  = "🔵",
                            label = stringResource(Res.string.chart_legend_head_cm),
                            value = "$hc cm"
                        )
                    }
                }

                // ── LATEST GROWTH RECORD ──────────────────────────────────────
                if (latestGrowth != null) {
                    ProfileSectionCard(title = stringResource(Res.string.profile_section_latest_growth)) {
                        val weightPct = latestGrowth.weightPercentile
                        ProfileInfoRow(
                            icon  = "⚖️",
                            label = stringResource(Res.string.chart_legend_weight_kg),
                            value = when {
                                latestGrowth.weight == null -> stringResource(Res.string.profile_not_recorded)
                                weightPct != null           -> "${latestGrowth.weight} kg (${weightPct}th %ile)"
                                else                        -> "${latestGrowth.weight} kg"
                            }
                        )
                        ProfileInfoDivider()

                        val heightPct = latestGrowth.heightPercentile
                        ProfileInfoRow(
                            icon  = "📏",
                            label = stringResource(Res.string.chart_legend_height_cm),
                            value = when {
                                latestGrowth.height == null -> stringResource(Res.string.profile_not_recorded)
                                heightPct != null           -> "${latestGrowth.height} cm (${heightPct}th %ile)"
                                else                        -> "${latestGrowth.height} cm"
                            }
                        )

                        latestGrowth.headCircumference?.let { hc ->
                            ProfileInfoDivider()
                            ProfileInfoRow(
                                icon  = "🔵",
                                label = stringResource(Res.string.chart_legend_head_cm),
                                value = "$hc cm"
                            )
                        }

                        ProfileInfoDivider()
                        ProfileInfoRow(
                            icon  = "📅",
                            label = stringResource(Res.string.profile_recorded_on),
                            value = formatProfileDate(latestGrowth.measurementDate)
                        )
                    }
                } else {
                    ProfileSectionCard(title = stringResource(Res.string.profile_section_latest_growth)) {
                        ProfileInfoRow(
                            icon  = "📊",
                            label = stringResource(Res.string.profile_section_latest_growth),
                            value = stringResource(Res.string.chart_no_measurement)
                        )
                    }
                }

                // ── VACCINATIONS ──────────────────────────────────────────────
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
                                value = "$pending remaining"
                            )
                        }
                    }
                }
            }

            // ── [3] EDIT DETAILS button ───────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.screenPadding)
            ) {
                Button(
                    onClick  = onEditDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensions.buttonHeight),
                    shape  = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.accentGradientStart
                    )
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(dimensions.iconMedium)
                        )
                        Text(
                            text       = stringResource(Res.string.baby_action_edit),
                            color      = MaterialTheme.colorScheme.onPrimary,
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

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
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
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.common_back)
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
                Spacer(Modifier.height(dimensions.borderWidthMedium))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            customColors.warning.copy(alpha = 0.1f),
                            RoundedCornerShape(dimensions.cardCornerRadius)
                        )
                        .padding(dimensions.spacingSmall)
                ) {
                    Text(
                        text      = stringResource(Res.string.profile_delete_warning),
                        textAlign = TextAlign.Center,
                        style     = MaterialTheme.typography.bodySmall,
                        color     = customColors.warning
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text  = stringResource(Res.string.profile_delete_child),
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
// SECTION CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileSectionCard(
    title  : String,
    content: @Composable ColumnScope.() -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensions.cardElevation,
            pressedElevation = dimensions.cardTonalElevation6
        )
    ) {
        Column(modifier = Modifier.padding(dimensions.spacingMedium)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color      = customColors.accentGradientStart,
                modifier   = Modifier.padding(
                    start  = dimensions.profileSectionLabelStartPadding,
                    bottom = dimensions.spacingSmall
                )
            )
            content()
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
            .padding(vertical = dimensions.profileInfoRowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = icon,
            fontSize = dimensions.profileInfoIconFontSize,
            modifier = Modifier.width(dimensions.profileInfoIconWidth)
        )
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
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

// tiny local helpers — only live in this file's scope
private val dimensions_dividerThickness get() = 0.8.dp
private val dimensions_dividerPadding   get() = 1.dp

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
                dimensions.borderWidthThin,
                customColors.glassOverlay.copy(alpha = 0.45f),
                RoundedCornerShape(dimensions.buttonCornerRadius)
            )
            .padding(
                horizontal = dimensions.spacingSmall,
                vertical   = dimensions.profileSectionCardVertPad
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.profileQuickActionItemGap)
    ) {
        Text(
            emoji,
            fontSize = dimensions.profileQuickActionEmojiSize
        )
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = MaterialTheme.colorScheme.onPrimary,
            textAlign  = TextAlign.Center,
            maxLines   = 2,
            fontSize   = dimensions.profileQuickActionLabelSize,
            lineHeight = dimensions.profileQuickActionLineHeight
        )
    }
}

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
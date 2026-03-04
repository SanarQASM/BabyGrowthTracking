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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
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
// Layout mirrors AddBabyScreen structure:
//   [1] Gradient hero header with rounded bottom corners  (= AddBabyScreen header)
//   [2] Section cards in white-background body            (= FormSectionCard)
//   [3] Primary gradient action button                    (= Save button)
//   [4] Warning gradient delete button                    (= Reset button)
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
            baby.gender.equals("GIRL", ignoreCase = true)

    val genderTheme = if (isFemale) GenderTheme.GIRL else GenderTheme.BOY

    var showDeleteDialog by remember { mutableStateOf(false) }

    BabyGrowthTheme(genderTheme = genderTheme) {

        val customColors = MaterialTheme.customColors
        val dimensions   = LocalDimensions.current

        // ── Delete confirmation dialog ────────────────────────────────────────
        if (showDeleteDialog) {
            DeleteBabyConfirmDialog(
                babyName  = baby.fullName,
                onConfirm = {
                    showDeleteDialog = false
                    onDeleteBaby()
                },
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

                // ────────────────────────────────────────────────────────────
                // [1] HERO HEADER — gradient + rounded bottom corners
                //     Matches AddBabyScreen's top gradient banner
                // ────────────────────────────────────────────────────────────
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
                                bottomStart = 35.dp,
                                bottomEnd   = 35.dp
                            )
                        )
                        .padding(
                            start  = dimensions.spacingLarge,
                            end    = dimensions.spacingLarge,
                            top    = dimensions.spacingMedium,
                            bottom = dimensions.spacingXLarge
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
                                    2.dp,
                                    customColors.glassOverlay.copy(alpha = 0.50f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isFemale) "👶" else "👦", fontSize = 42.sp)
                        }

                        Spacer(Modifier.height(dimensions.spacingMedium))

                        // Full name
                        Text(
                            text       = baby.fullName,
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimary,
                            textAlign  = TextAlign.Center
                        )

                        Spacer(Modifier.height(4.dp))

                        // Age + gender pill
                        Box(
                            modifier = Modifier
                                .background(
                                    customColors.glassOverlay.copy(alpha = 0.22f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(
                                    horizontal = dimensions.spacingMedium,
                                    vertical   = 5.dp
                                )
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text  = if (isFemale) "♀" else "♂",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text  = "${formatProfileAge(baby.ageInMonths)} • ${
                                        if (isFemale) stringResource(Res.string.gender_female)
                                        else          stringResource(Res.string.gender_male)
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        // Archived badge (conditional)
                        if (!baby.isActive) {
                            Spacer(Modifier.height(dimensions.spacingSmall))
                            Box(
                                modifier = Modifier
                                    .background(
                                        customColors.glassOverlay,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
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

                        // Quick-action row  (2 glass pill buttons)
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

                // ────────────────────────────────────────────────────────────
                // [2] FORM BODY — same horizontal padding + spacing as
                //     AddBabyScreen's scrollable form area
                // ────────────────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensions.screenPadding,
                            vertical   = dimensions.spacingLarge
                        ),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                ) {

                    // ── BASIC INFORMATION ──────────────────────────────────
                    ProfileSectionCard(
                        title = stringResource(Res.string.profile_section_basic_info)
                    ) {
                        ProfileInfoRow(
                            icon  = "🎂",
                            label = stringResource(Res.string.profile_dob),
                            value = formatProfileDate(baby.dateOfBirth)
                        )
                        ProfileInfoDivider()
                        ProfileInfoRow(
                            icon  = "📅",
                            label = stringResource(Res.string.profile_age),
                            value = formatProfileAge(baby.ageInMonths)
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
                            icon  = "📅",
                            label = stringResource(Res.string.profile_days_old),
                            value = stringResource(Res.string.profile_days_old_value, baby.ageInDays)
                        )
                    }

                    // ── BIRTH MEASUREMENTS ─────────────────────────────────
                    ProfileSectionCard(
                        title = stringResource(Res.string.profile_section_birth_measurements)
                    ) {
                        if (baby.birthWeight == null && baby.birthHeight == null) {
                            ProfileInfoRow(
                                icon  = "ℹ️",
                                label = stringResource(Res.string.profile_not_recorded),
                                value = stringResource(Res.string.profile_not_recorded_value)
                            )
                        } else {
                            baby.birthWeight?.let { w ->
                                ProfileInfoRow(
                                    icon  = "⚖️",
                                    label = stringResource(Res.string.baby_birth_weight),
                                    value = stringResource(Res.string.profile_weight_value, w.toString())
                                )
                                if (baby.birthHeight != null) ProfileInfoDivider()
                            }
                            baby.birthHeight?.let { h ->
                                ProfileInfoRow(
                                    icon  = "📏",
                                    label = stringResource(Res.string.baby_birth_height),
                                    value = stringResource(Res.string.profile_height_value, h.toString())
                                )
                            }
                        }
                    }

                    // ── LATEST GROWTH RECORD ───────────────────────────────
                    latestGrowth?.let { growth ->
                        ProfileSectionCard(
                            title = stringResource(Res.string.profile_section_latest_growth)
                        ) {
                            growth.height?.let { h ->
                                val heightValue = if (growth.heightPercentile != null)
                                    stringResource(
                                        Res.string.profile_height_percentile,
                                        h.toString(),
                                        growth.heightPercentile
                                    )
                                else
                                    stringResource(Res.string.profile_height_value, h.toString())

                                ProfileInfoRow(
                                    icon  = "📏",
                                    label = stringResource(Res.string.baby_birth_height),
                                    value = heightValue
                                )
                                ProfileInfoDivider()
                            }
                            growth.weight?.let { w ->
                                val weightValue = if (growth.weightPercentile != null)
                                    stringResource(
                                        Res.string.profile_weight_percentile,
                                        w.toString(),
                                        growth.weightPercentile
                                    )
                                else
                                    stringResource(Res.string.profile_weight_value, w.toString())

                                ProfileInfoRow(
                                    icon  = "⚖️",
                                    label = stringResource(Res.string.baby_birth_weight),
                                    value = weightValue
                                )
                                ProfileInfoDivider()
                            }
                            ProfileInfoRow(
                                icon  = "📅",
                                label = stringResource(Res.string.profile_recorded_on),
                                value = growth.createdAt?.let { formatProfileDate(it) } ?: "—"  // ✅ handles null
                            )
                        }
                    }

                    // ── VACCINATIONS ───────────────────────────────────────
                    if (vaccinations.isNotEmpty()) {
                        val done    = vaccinations.count {
                            it.status.equals("ADMINISTERED", ignoreCase = true)
                        }
                        val pending = vaccinations.count {
                            it.status.equals("PENDING", ignoreCase = true)
                        }
                        val nextVax = vaccinations.firstOrNull {
                            it.status.equals("PENDING", ignoreCase = true)
                        }

                        ProfileSectionCard(
                            title = stringResource(Res.string.profile_section_vaccinations)
                        ) {
                            ProfileInfoRow(
                                icon  = "✅",
                                label = stringResource(Res.string.profile_vaccinations_completed),
                                value = stringResource(
                                    Res.string.baby_stat_vaccines,
                                    done,
                                    vaccinations.size
                                )
                            )
                            ProfileInfoDivider()
                            ProfileInfoRow(
                                icon  = "⏳",
                                label = stringResource(Res.string.profile_vaccinations_pending),
                                value = stringResource(
                                    Res.string.profile_vaccinations_pending_value,
                                    pending
                                )
                            )
                            nextVax?.let { vax ->
                                ProfileInfoDivider()
                                ProfileInfoRow(
                                    icon  = "💉",
                                    label = stringResource(Res.string.home_next_vaccine_label),
                                    value = "${vax.vaccineName} · ${
                                        formatProfileDate(vax.scheduledDate)
                                    }"
                                )
                            }
                        }
                    }

                    // ── MONTH PROGRESS ─────────────────────────────────────
                    ProfileSectionCard(
                        title = stringResource(Res.string.profile_section_month_progress)
                    ) {
                        val progressAccent = customColors.accentGradientStart
                        val monthProgress  = ((baby.ageInDays % 30).toInt().coerceIn(0, 30)) / 30f

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text  = stringResource(
                                    Res.string.profile_age_days,
                                    baby.ageInDays
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.55f)
                            )
                            Text(
                                text       = stringResource(
                                    Res.string.profile_month_progress,
                                    baby.ageInMonths
                                ),
                                style      = MaterialTheme.typography.labelSmall,
                                color      = progressAccent.copy(0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.height(dimensions.spacingSmall))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(progressAccent.copy(0.12f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(monthProgress)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                progressAccent,
                                                progressAccent.copy(0.55f)
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    Spacer(Modifier.height(dimensions.spacingSmall))

                    // ────────────────────────────────────────────────────────
                    // [3] EDIT DETAILS — primary gradient button
                    //     Identical to AddBabyScreen's "Save" button
                    // ────────────────────────────────────────────────────────
                    Button(
                        onClick        = onEditDetails,
                        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors         = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(),
                        elevation      = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                        modifier       = Modifier
                            .fillMaxWidth()
                            .height(dimensions.buttonHeight)
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
                                    1.dp,
                                    Color.White.copy(alpha = 0.20f),
                                    RoundedCornerShape(dimensions.buttonCornerRadius)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Glassmorphic white overlay — same as PrimaryButton component
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Color.White.copy(alpha = 0.15f),
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
                                    tint     = Color.White,
                                    modifier = Modifier.size(dimensions.iconMedium)
                                )
                                Text(
                                    text       = stringResource(Res.string.baby_action_edit),
                                    color      = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style      = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(dimensions.spacingSmall))

                    // ────────────────────────────────────────────────────────
                    // [4] DELETE CHILD — warning gradient button
                    //     Mirrors AddBabyScreen's "Reset" / DangerButton pattern
                    // ────────────────────────────────────────────────────────
                    Button(
                        onClick        = { showDeleteDialog = true },
                        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors         = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(),
                        elevation      = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                        modifier       = Modifier
                            .fillMaxWidth()
                            .height(dimensions.buttonHeight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            customColors.warning.copy(alpha = 0.85f),
                                            customColors.warning.copy(alpha = 0.70f)
                                        )
                                    ),
                                    RoundedCornerShape(dimensions.buttonCornerRadius)
                                )
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.20f),
                                    RoundedCornerShape(dimensions.buttonCornerRadius)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Color.White.copy(alpha = 0.15f),
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
                                    tint     = Color.White,
                                    modifier = Modifier.size(dimensions.iconMedium)
                                )
                                Text(
                                    text       = stringResource(Res.string.profile_delete_child),
                                    color      = Color.White,
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
        icon  = { Text("🗑️", fontSize = 40.sp) },
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
                // "Are you sure you want to permanently delete"
                Text(
                    text      = stringResource(Res.string.profile_delete_message_1),
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Baby name in bold
                Text(
                    text       = "\"$babyName\"",
                    textAlign  = TextAlign.Center,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(2.dp))

                // Warning panel — same glass-border style as section cards
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            customColors.warning.copy(alpha = 0.10f),
                            RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp)
                        )
                        .border(
                            1.dp,
                            customColors.warning.copy(alpha = 0.35f),
                            RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp)
                        )
                        .padding(
                            horizontal = dimensions.spacingMedium,
                            vertical   = dimensions.spacingSmall + 2.dp
                        )
                ) {
                    Row(
                        verticalAlignment     = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("⚠️", fontSize = 13.sp)
                        Text(
                            text       = stringResource(Res.string.profile_delete_warning),
                            textAlign  = TextAlign.Start,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = customColors.warning,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            // "Yes, Delete" — same DangerButton gradient pattern
            Button(
                onClick        = onConfirm,
                shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                elevation      = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    customColors.warning.copy(alpha = 0.90f),
                                    customColors.warning.copy(alpha = 0.75f)
                                )
                            ),
                            RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.20f),
                            RoundedCornerShape(dimensions.buttonCornerRadius)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(dimensions.buttonCornerRadius)
                            )
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint     = Color.White,
                            modifier = Modifier.size(dimensions.iconMedium)
                        )
                        Text(
                            text       = stringResource(Res.string.profile_delete_confirm),
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            style      = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        },
        dismissButton = {
            // "Cancel" — same SecondaryButton translucent ghost pattern
            Button(
                onClick        = onDismiss,
                shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                elevation      = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                )
                            ),
                            RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                            RoundedCornerShape(dimensions.buttonCornerRadius)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = stringResource(Res.string.profile_delete_cancel),
                        color      = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        style      = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        shape          = RoundedCornerShape(dimensions.cardCornerRadius),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// QUICK ACTION BUTTON — glass pill in the hero header
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
                1.dp,
                customColors.glassOverlay.copy(alpha = 0.45f),
                RoundedCornerShape(dimensions.buttonCornerRadius)
            )
            .padding(
                horizontal = dimensions.spacingSmall,
                vertical   = dimensions.spacingMedium - 2.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = MaterialTheme.colorScheme.onPrimary,
            textAlign  = TextAlign.Center,
            maxLines   = 2,
            fontSize   = 9.sp,
            lineHeight = 11.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION CARD — mirrors AddBabyScreen's FormSectionCard:
//   labelled section title + glassmorphic rounded container
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileSectionCard(
    title  : String,
    content: @Composable ColumnScope.() -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Column(modifier = Modifier.fillMaxWidth()) {

        // Section label — uppercase accent colour, same as FormSectionCard
        Text(
            text          = title,
            style         = MaterialTheme.typography.labelMedium,
            fontWeight    = FontWeight.Bold,
            color         = customColors.accentGradientStart,
            letterSpacing = 0.8.sp,
            modifier      = Modifier.padding(
                start  = 4.dp,
                bottom = dimensions.spacingSmall
            )
        )

        // Glass container — matches GlassmorphicTextField + FormSectionCard background
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
                    1.dp,
                    customColors.accentGradientStart.copy(alpha = 0.18f),
                    RoundedCornerShape(dimensions.buttonCornerRadius)
                )
                .padding(
                    horizontal = dimensions.spacingMedium,
                    vertical   = dimensions.spacingMedium - 2.dp
                )
        ) {
            // Glassmorphic white overlay (matches GlassmorphicTextField)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            )
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INFO ROW — icon + label on left, value right-aligned
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileInfoRow(icon: String, label: String, value: String) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 15.sp, modifier = Modifier.width(28.dp))
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
        thickness = 0.8.dp,
        modifier  = Modifier.padding(vertical = 1.dp)
    )
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
        stringResource(Res.string.month_jan),
        stringResource(Res.string.month_feb),
        stringResource(Res.string.month_mar),
        stringResource(Res.string.month_apr),
        stringResource(Res.string.month_may),
        stringResource(Res.string.month_jun),
        stringResource(Res.string.month_jul),
        stringResource(Res.string.month_aug),
        stringResource(Res.string.month_sep),
        stringResource(Res.string.month_oct),
        stringResource(Res.string.month_nov),
        stringResource(Res.string.month_dec)
    )
    val month = monthNames.getOrElse(monthIndex) { parts[1] }
    return "$month ${parts[2]}"
}
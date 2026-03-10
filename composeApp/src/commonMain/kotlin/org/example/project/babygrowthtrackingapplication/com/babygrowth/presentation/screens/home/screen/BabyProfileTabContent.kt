package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.data.network.GrowthRecordResponse
import org.example.project.babygrowthtrackingapplication.data.network.VaccinationResponse
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HomeViewModel

enum class BabyFilter { ALL, ACTIVE, ARCHIVED }

// ─────────────────────────────────────────────────────────────────────────────
// BabyProfileTabContent
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BabyProfileTabContent(
    viewModel        : HomeViewModel,
    onAddBaby        : () -> Unit = {},
    onSeeProfile     : (BabyResponse) -> Unit = {},
    onEditDetails    : (BabyResponse) -> Unit = {},
    onAddMeasurement : (BabyResponse) -> Unit = {},
    onViewGrowthChart: (BabyResponse) -> Unit = {}
) {
    val state        = viewModel.uiState
    val dimensions   = LocalDimensions.current
    var searchQuery  by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(BabyFilter.ALL) }
    val customColors = MaterialTheme.customColors

    var archiveConfirmBaby   by remember { mutableStateOf<BabyResponse?>(null) }
    var unarchiveConfirmBaby by remember { mutableStateOf<BabyResponse?>(null) }

    val labelChild    = stringResource(Res.string.baby_count_child)
    val labelChildren = stringResource(Res.string.baby_count_children)
    val labelActive   = stringResource(Res.string.baby_filter_active)
    val labelArchived = stringResource(Res.string.baby_filter_archived)

    val displayedBabies = remember(state.babies, searchQuery, activeFilter) {
        state.babies
            .filter { baby ->
                when (activeFilter) {
                    BabyFilter.ALL      -> true
                    BabyFilter.ACTIVE   -> baby.isActive
                    BabyFilter.ARCHIVED -> !baby.isActive
                }
            }
            .filter { baby ->
                searchQuery.isBlank() || baby.fullName.contains(searchQuery, ignoreCase = true)
            }
    }

    val countLabel = remember(displayedBabies, activeFilter) {
        val count = displayedBabies.size
        val noun  = if (count == 1) labelChild else labelChildren
        when (activeFilter) {
            BabyFilter.ALL      -> "$count $noun"
            BabyFilter.ACTIVE   -> "$count $labelActive $noun"
            BabyFilter.ARCHIVED -> "$count $labelArchived $noun"
        }
    }

    // ── Archive confirm dialog ────────────────────────────────────────────────
    archiveConfirmBaby?.let { baby ->
        AlertDialog(
            onDismissRequest = { archiveConfirmBaby = null },
            icon  = { Text("📦", style = MaterialTheme.typography.displaySmall) },
            title = {
                Text(
                    text       = "Archive ${baby.fullName}?",
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    style      = MaterialTheme.typography.titleMedium
                )
            },
            text  = {
                Text(
                    text      = "This child will be moved to the archived list. You can restore them at any time using the Archived filter.",
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick  = { viewModel.archiveBaby(baby.babyId); archiveConfirmBaby = null },
                    shape    = RoundedCornerShape(LocalDimensions.current.buttonCornerRadius),
                    colors   = ButtonDefaults.buttonColors(containerColor = customColors.warning),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📦  Archive", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = { archiveConfirmBaby = null },
                    shape    = RoundedCornerShape(LocalDimensions.current.buttonCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel") }
            },
            shape          = RoundedCornerShape(LocalDimensions.current.cardCornerRadius),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // ── Unarchive confirm dialog ──────────────────────────────────────────────
    unarchiveConfirmBaby?.let { baby ->
        AlertDialog(
            onDismissRequest = { unarchiveConfirmBaby = null },
            icon  = { Text("♻️", style = MaterialTheme.typography.displaySmall) },
            title = {
                Text(
                    text       = "Restore ${baby.fullName}?",
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    style      = MaterialTheme.typography.titleMedium
                )
            },
            text  = {
                Text(
                    text      = "This child will be moved back to the active list.",
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick  = { viewModel.unarchiveBaby(baby.babyId); unarchiveConfirmBaby = null },
                    shape    = RoundedCornerShape(LocalDimensions.current.buttonCornerRadius),
                    colors   = ButtonDefaults.buttonColors(containerColor = customColors.success),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("♻️  Restore", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = { unarchiveConfirmBaby = null },
                    shape    = RoundedCornerShape(LocalDimensions.current.buttonCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel") }
            },
            shape          = RoundedCornerShape(LocalDimensions.current.cardCornerRadius),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BabySearchBar(
            query         = searchQuery,
            hint          = stringResource(Res.string.baby_search_hint),
            onQueryChange = { searchQuery = it },
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingMedium)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(
                        topStart = dimensions.spacingXXLarge - dimensions.spacingXSmall,  // WAS: 35.dp
                        topEnd   = dimensions.spacingXXLarge - dimensions.spacingXSmall
                    )
                )
        ) {
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(dimensions.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                    ) {
                        Spacer(Modifier.height(dimensions.spacingSmall))
                        repeat(2) { BabyCardSkeleton() }
                    }
                }

                state.babies.isEmpty() -> {
                    NoBabiesBabyTab(onAddBaby = onAddBaby)
                }

                else -> {
                    val snackbarHostState = remember { SnackbarHostState() }
                    LaunchedEffect(state.actionMessage) {
                        state.actionMessage?.let {
                            snackbarHostState.showSnackbar(it)
                            viewModel.clearActionMessage()
                        }
                    }

                    Box(Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier        = Modifier.fillMaxSize(),
                            contentPadding  = PaddingValues(
                                start  = dimensions.screenPadding,
                                end    = dimensions.screenPadding,
                                top    = dimensions.spacingLarge,
                                bottom = 100.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                        ) {
                            item {
                                BabyFilterTabs(selected = activeFilter, onSelect = { activeFilter = it })
                                Spacer(Modifier.height(dimensions.spacingMedium))
                            }

                            item {
                                // ── Count label row + Add baby button ─────────
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text       = countLabel,
                                        style      = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = MaterialTheme.colorScheme.onBackground
                                    )

                                    // ── FIX 3: Add text label below the + button ──
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(dimensions.addButtonSize)                   // WAS: 36.dp
                                                .background(
                                                    customColors.accentGradientStart.copy(0.15f),
                                                    RoundedCornerShape(dimensions.chipCornerRadius)
                                                )
                                                .border(
                                                    dimensions.borderWidthThin,
                                                    customColors.accentGradientStart.copy(0.3f),
                                                    RoundedCornerShape(dimensions.chipCornerRadius)
                                                )
                                                .clickable { onAddBaby() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = stringResource(Res.string.home_add_more_child),
                                                tint     = customColors.accentGradientStart,
                                                modifier = Modifier.size(dimensions.iconMedium)
                                            )
                                        }
                                        // ← NEW: label below the + button (matches HomeTab UX)
                                        Spacer(Modifier.height(dimensions.spacingXSmall - dimensions.borderWidthThin))
                                        Text(
                                            text      = stringResource(Res.string.home_add_more_child),
                                            style     = MaterialTheme.typography.labelSmall,
                                            color     = MaterialTheme.colorScheme.onBackground.copy(0.65f),
                                            textAlign = TextAlign.Center,
                                            maxLines  = 2,
                                            fontSize  = dimensions.homeSmallTextSize,
                                            lineHeight= dimensions.homeSmallLineHeight,
                                            modifier  = Modifier.widthIn(max = dimensions.addButtonSize + dimensions.spacingMedium)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(dimensions.spacingSmall))
                            }

                            if (displayedBabies.isEmpty()) {
                                item { EmptyFilterResult(filter = activeFilter) }
                            } else {
                                items(displayedBabies) { baby ->
                                    BabyCard(
                                        baby             = baby,
                                        vaccinations     = state.upcomingVaccinations[baby.babyId]
                                            ?: emptyList(),
                                        latestGrowth      = state.latestGrowthRecords[baby.babyId],
                                        onSeeProfile      = { onSeeProfile(baby) },
                                        onArchive         = { archiveConfirmBaby   = baby },
                                        onUnarchive       = { unarchiveConfirmBaby = baby },
                                        onEditDetails     = { onEditDetails(baby) },
                                        onAddMeasurement  = { onAddMeasurement(baby) },
                                        onViewGrowthChart = { onViewGrowthChart(baby) }
                                    )
                                }
                            }
                        }

                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier  = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = dimensions.screenPadding)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SEARCH BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BabySearchBar(
    query        : String,
    hint         : String,
    onQueryChange: (String) -> Unit,
    modifier     : Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Row(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(dimensions.chipCornerRadius)
            )
            .border(
                dimensions.borderWidthThin,
                customColors.accentGradientStart.copy(0.2f),
                RoundedCornerShape(dimensions.chipCornerRadius)
            )
            .padding(
                horizontal = dimensions.spacingMedium,
                vertical   = dimensions.spacingSmall + dimensions.spacingXSmall
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint               = customColors.accentGradientStart.copy(0.6f),
            modifier           = Modifier.size(dimensions.iconMedium)
        )
        Spacer(Modifier.width(dimensions.spacingSmall))
        BasicTextField(
            value         = query,
            onValueChange = onQueryChange,
            modifier      = Modifier.weight(1f),
            textStyle     = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine    = true,
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                }
                inner()
            }
        )
        if (query.isNotEmpty()) {
            IconButton(
                onClick  = { onQueryChange("") },
                modifier = Modifier.size(dimensions.iconMedium)
            ) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FILTER TABS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BabyFilterTabs(selected: BabyFilter, onSelect: (BabyFilter) -> Unit) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    val labels = mapOf(
        BabyFilter.ALL      to stringResource(Res.string.baby_filter_all),
        BabyFilter.ACTIVE   to stringResource(Res.string.baby_filter_active),
        BabyFilter.ARCHIVED to stringResource(Res.string.baby_filter_archived)
    )

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall + dimensions.borderWidthMedium)
    ) {
        BabyFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) customColors.accentGradientStart.copy(0.18f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                        RoundedCornerShape(dimensions.filterTabCorner)
                    )
                    .border(
                        width = if (isSelected) dimensions.borderWidthThin + 0.5.dp else dimensions.borderWidthThin,
                        color = if (isSelected) customColors.accentGradientStart.copy(0.5f)
                        else MaterialTheme.colorScheme.onSurface.copy(0.15f),
                        shape = RoundedCornerShape(dimensions.filterTabCorner)
                    )
                    .clickable { onSelect(filter) }
                    .padding(
                        horizontal = dimensions.spacingLarge - dimensions.borderWidthMedium,
                        vertical   = dimensions.spacingSmall + dimensions.borderWidthThin
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = labels[filter] ?: filter.name,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color      = if (isSelected) customColors.accentGradientStart
                    else MaterialTheme.colorScheme.onSurface.copy(0.7f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BABY CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BabyCard(
    baby             : BabyResponse,
    vaccinations     : List<VaccinationResponse>,
    latestGrowth     : GrowthRecordResponse?,
    onSeeProfile     : () -> Unit,
    onArchive        : () -> Unit,
    onUnarchive      : () -> Unit,
    onEditDetails    : () -> Unit,
    onAddMeasurement : () -> Unit,
    onViewGrowthChart: () -> Unit
) {
    val customColors        = MaterialTheme.customColors
    val dimensions          = LocalDimensions.current
    var quickActionExpanded by remember { mutableStateOf(false) }

    val isFemale = baby.gender.equals("FEMALE", ignoreCase = true) ||
            baby.gender.equals("GIRL",   ignoreCase = true)

    val cardBg: Brush = when {
        !baby.isActive && isFemale ->
            Brush.horizontalGradient(listOf(
                customColors.accentGradientStart.copy(alpha = 0.15f),
                customColors.accentGradientEnd.copy(alpha = 0.20f)
            ))
        !baby.isActive ->
            Brush.horizontalGradient(listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surface
            ))
        isFemale ->
            Brush.horizontalGradient(listOf(
                customColors.accentGradientStart.copy(alpha = 0.8f),
                customColors.accentGradientEnd.copy(alpha = 0.7f)
            ))
        else ->
            Brush.horizontalGradient(listOf(
                customColors.accentGradientEnd.copy(alpha = 0.7f),
                customColors.accentGradientStart.copy(alpha = 0.6f)
            ))
    }

    val doneVaccines  = vaccinations.count { it.status.equals("ADMINISTERED", ignoreCase = true) }
    val totalVaccines = vaccinations.size

    // ── FIX 2: measurement priority — same as HomeTab ────────────────────────
    // Birth measurement first, then latest growth, then null (hidden if both absent)
    val displayWeight = baby.birthWeight ?: latestGrowth?.weight
    val displayHeight = baby.birthHeight ?: latestGrowth?.height
    val displayHead   = baby.birthHeadCircumference ?: latestGrowth?.headCircumference

    val heightPct = latestGrowth?.heightPercentile
    val weightPct = latestGrowth?.weightPercentile

    // ── FIX 1: use ONE clip — Card shape matches Box background shape ─────────
    // The old code had Card(shape=18.dp) wrapping Box(clip=18.dp hardcoded),
    // creating a double-clip that showed a sharp inner rectangle.
    // Fix: remove the inner clip entirely — Card already clips its content.
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.chartCardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)  // ← FIXED: no tonal overlay rectangle
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // ← FIX 1: NO .clip() here — Card already clips with chartCardCornerRadius.
                // The old .clip(RoundedCornerShape(18.dp)) was hardcoded and mismatched
                // the Card's RoundedCornerShape(dimensions.chartCardCornerRadius),
                // causing a visible rectangular inner border on non-compact screens.
                .background(cardBg)
                .padding(dimensions.babyCardInnerPadding)                   // WAS: 16.dp
        ) {
            Column {
                // ── Header: avatar + name + archived badge + 3-dot menu ───────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(dimensions.babyCardAvatarSize)             // WAS: 52.dp
                            .background(customColors.glassOverlay, CircleShape)
                            .border(
                                dimensions.borderWidthMedium,                // WAS: 2.dp
                                customColors.glassOverlay.copy(alpha = 0.5f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isFemale) "👶" else "👦",
                            fontSize = dimensions.babyCardEmojiSize          // WAS: 26.sp
                        )
                    }

                    Spacer(Modifier.width(dimensions.babyCardAvatarNameGap)) // WAS: 12.dp

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text       = baby.fullName,
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onPrimary
                            )
                            if (!baby.isActive) {
                                Spacer(Modifier.width(dimensions.babyCardNameBadgeGap)) // WAS: 8.dp
                                Box(
                                    modifier = Modifier
                                        .background(
                                            customColors.glassOverlay,
                                            RoundedCornerShape(dimensions.babyCardInlineBadgeCorner)
                                        )
                                        .padding(
                                            horizontal = dimensions.babyCardInlineBadgePaddingH,
                                            vertical   = dimensions.babyCardInlineBadgePaddingV
                                        )
                                ) {
                                    Text(
                                        text       = stringResource(Res.string.baby_status_archived),
                                        style      = MaterialTheme.typography.labelSmall,
                                        color      = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            // Auto-archived badge (baby ≥ 6 years)
                            if (!baby.isActive && baby.ageInMonths >= 72) {
                                Spacer(Modifier.width(dimensions.spacingXSmall))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.error.copy(0.15f),
                                            RoundedCornerShape(dimensions.babyCardInlineBadgeCorner)
                                        )
                                        .padding(
                                            horizontal = dimensions.babyCardInlineBadgePaddingH,
                                            vertical   = dimensions.babyCardInlineBadgePaddingV
                                        )
                                ) {
                                    Text(
                                        text       = "6+ yrs",
                                        style      = MaterialTheme.typography.labelSmall,
                                        color      = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // 3-dot menu
                    Box {
                        IconButton(onClick = { quickActionExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(Res.string.baby_quick_action),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        DropdownMenu(
                            expanded         = quickActionExpanded,
                            onDismissRequest = { quickActionExpanded = false }
                        ) {
                            BabyCardDropdownContent(
                                isActive      = baby.isActive,
                                onSeeProfile  = { quickActionExpanded = false; onSeeProfile() },
                                onEditDetails = { quickActionExpanded = false; onEditDetails() },
                                onAddMeasure  = { quickActionExpanded = false; onAddMeasurement() },
                                onGrowthChart = { quickActionExpanded = false; onViewGrowthChart() },
                                onArchive     = { quickActionExpanded = false; onArchive() },
                                onUnarchive   = { quickActionExpanded = false; onUnarchive() }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(dimensions.babyCardSpacerAfterAvatar)) // WAS: 10.dp

                // Gender + age row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = if (isFemale) "♀" else "♂",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(0.9f)
                    )
                    Spacer(Modifier.width(dimensions.babyCardGenderSpacerW))   // WAS: 6.dp
                    Text(
                        text  = "${formatAgeBaby(baby.ageInMonths)} • ${
                            if (isFemale) stringResource(Res.string.gender_female)
                            else          stringResource(Res.string.gender_male)
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(0.9f)
                    )
                }

                Spacer(Modifier.height(dimensions.babyCardSpacerAfterAvatar)) // WAS: 10.dp

                // ── FIX 2: 3 measurement chips — same style as HomeTab ────────
                // Old code used BabyStatRow with stringResource(%s format) which showed "%scm".
                // New code uses inline chip Row matching HomeTabContent's BabyStatChip pattern.
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    MeasurementChip(
                        icon        = "⚖️",
                        label       = stringResource(Res.string.baby_birth_weight),
                        value       = displayWeight?.let { "$it kg" } ?: "—",
                        accentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier    = Modifier.weight(1f)
                    )
                    MeasurementChip(
                        icon        = "📏",
                        label       = stringResource(Res.string.baby_birth_height),
                        value       = displayHeight?.let { "$it cm" } ?: "—",
                        accentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier    = Modifier.weight(1f)
                    )
                    MeasurementChip(
                        icon        = "🔵",
                        label       = stringResource(Res.string.add_measure_head),
                        value       = displayHead?.let { "$it cm" } ?: "—",
                        accentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier    = Modifier.weight(1f)
                    )
                }

                // Vaccine row (only if there are vaccines)
                if (totalVaccines > 0) {
                    Spacer(Modifier.height(dimensions.babyCardStatSpacer))     // WAS: 3.dp
                    BabyStatRow(
                        "💉",
                        stringResource(Res.string.baby_stat_vaccines, doneVaccines, totalVaccines)
                    )
                }

                // Percentile sub-row (only if available)
                if (heightPct != null || weightPct != null) {
                    Spacer(Modifier.height(dimensions.babyCardStatSpacer))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                    ) {
                        heightPct?.let {
                            Text(
                                "📊 Height: ${it}th %ile",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(0.7f)
                            )
                        }
                        weightPct?.let {
                            Text(
                                "⚖️ Weight: ${it}th %ile",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(0.7f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(dimensions.babyCardBottomSpacer))       // WAS: 14.dp

                // Action row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    OutlinedButton(
                        onClick  = onSeeProfile,
                        shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                        border   = BorderStroke(
                            dimensions.borderWidthThin,                        // WAS: 1.dp
                            MaterialTheme.colorScheme.onPrimary.copy(0.5f)
                        ),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringResource(Res.string.baby_see_profile),
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick  = onAddMeasurement,
                        shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(0.15f),
                            contentColor   = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringResource(Res.string.baby_action_add_measure),
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MEASUREMENT CHIP  (FIX 2 — matches HomeTab BabyStatChip exactly)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MeasurementChip(
    icon       : String,
    label      : String,
    value      : String,
    accentColor: Color,
    modifier   : Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.spacingSmall))
            .background(accentColor.copy(0.15f))
            .padding(
                horizontal = dimensions.spacingSmall,
                vertical   = dimensions.spacingXSmall + 1.dp
            ),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
    ) {
        Text(icon, fontSize = (dimensions.iconSmall.value - 4).sp)
        Column {
            Text(
                label,
                style    = MaterialTheme.typography.labelSmall,
                color    = accentColor.copy(0.6f),
                fontSize = dimensions.homeSmallTextSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                value,
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color      = accentColor,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DROPDOWN CONTENT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BabyCardDropdownContent(
    isActive     : Boolean,
    onSeeProfile : () -> Unit,
    onEditDetails: () -> Unit,
    onAddMeasure : () -> Unit,
    onGrowthChart: () -> Unit,
    onArchive    : () -> Unit,
    onUnarchive  : () -> Unit
) {
    val customColors = MaterialTheme.customColors
    QuickActionItem("👤", stringResource(Res.string.baby_see_profile),        onSeeProfile)
    QuickActionItem("✏️", stringResource(Res.string.baby_action_edit),        onEditDetails)
    QuickActionItem("📏", stringResource(Res.string.baby_action_add_measure), onAddMeasure)
    QuickActionItem("📊", stringResource(Res.string.baby_action_growth_chart),onGrowthChart)
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        color    = MaterialTheme.colorScheme.onSurface.copy(0.1f)
    )
    if (isActive) {
        QuickActionItem("📦", stringResource(Res.string.baby_action_archive),   onArchive,   customColors.warning)
    } else {
        QuickActionItem("♻️", stringResource(Res.string.baby_action_unarchive), onUnarchive, customColors.success)
    }
}

@Composable
private fun QuickActionItem(
    icon   : String,
    label  : String,
    onClick: () -> Unit,
    color  : Color = MaterialTheme.colorScheme.onSurface
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(icon, style = MaterialTheme.typography.titleMedium)
                Text(
                    label,
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = color,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        onClick = onClick
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// STAT ROW  (kept for vaccines row — text format only, no Double formatting)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BabyStatRow(icon: String, text: String) {
    val dimensions = LocalDimensions.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(dimensions.babyCardGenderSpacerW))  // WAS: 6.dp
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(0.9f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EMPTY STATES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoBabiesBabyTab(onAddBaby: () -> Unit) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier            = Modifier.padding(dimensions.spacingXLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("👶", fontSize = dimensions.noBabiesEmojiSize)              // WAS: 64.sp
            Spacer(Modifier.height(dimensions.spacingMedium))
            Text(
                text       = stringResource(Res.string.home_no_babies_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(
                text      = stringResource(Res.string.home_no_babies_desc),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onBackground.copy(0.55f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(dimensions.spacingXLarge))
            Button(
                onClick = onAddBaby,
                shape   = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors  = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(dimensions.iconMedium))
                Spacer(Modifier.width(dimensions.babyCardGenderSpacerW))     // WAS: 6.dp
                Text(stringResource(Res.string.home_add_first_baby), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EmptyFilterResult(filter: BabyFilter) {
    val dimensions = LocalDimensions.current
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensions.spacingXLarge + dimensions.spacingLarge / 2),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text     = if (filter == BabyFilter.ARCHIVED) "📦" else "🔍",
            fontSize = dimensions.noBabiesEmojiSize                 // WAS: 48.sp
        )
        Spacer(Modifier.height(dimensions.spacingMedium))
        Text(
            text = when (filter) {
                BabyFilter.ACTIVE   -> stringResource(Res.string.baby_empty_active)
                BabyFilter.ARCHIVED -> stringResource(Res.string.baby_empty_archived)
                BabyFilter.ALL      -> stringResource(Res.string.baby_empty_search)
            },
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onBackground.copy(0.55f),
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SKELETON
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BabyCardSkeleton() {
    val dimensions = LocalDimensions.current
    val shimmer by rememberInfiniteTransition(label = "sk").animateFloat(
        initialValue  = 0.2f,
        targetValue   = 0.5f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label         = "sk_alpha"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.buttonHeight * 3.3f)                          // WAS: 180.dp
            .background(
                MaterialTheme.colorScheme.onBackground.copy(shimmer),
                RoundedCornerShape(dimensions.chartCardCornerRadius)          // WAS: 18.dp
            )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// AGE FORMATTER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun formatAgeBaby(months: Int): String = when {
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
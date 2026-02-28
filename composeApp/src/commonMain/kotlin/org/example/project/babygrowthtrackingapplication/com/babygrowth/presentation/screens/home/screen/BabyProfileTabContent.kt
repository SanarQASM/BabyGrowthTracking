package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
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

@Composable
fun BabyProfileTabContent(
    viewModel    : HomeViewModel,
    onAddBaby    : () -> Unit = {},
    onSeeProfile : (BabyResponse) -> Unit = {}   // ← NEW: navigates to BabyProfileScreen
) {
    val state        = viewModel.uiState
    val dimensions   = LocalDimensions.current
    var searchQuery  by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(BabyFilter.ALL) }

    val customColors = MaterialTheme.customColors

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
                    RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp)
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
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    customColors.accentGradientStart.copy(0.15f),
                                                    RoundedCornerShape(dimensions.spacingSmall + 2.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    customColors.accentGradientStart.copy(0.3f),
                                                    RoundedCornerShape(dimensions.spacingSmall + 2.dp)
                                                )
                                                .clickable { onAddBaby() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = stringResource(Res.string.baby_add_more_child),
                                                tint     = customColors.accentGradientStart,
                                                modifier = Modifier.size(dimensions.iconMedium)
                                            )
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text      = stringResource(Res.string.baby_add_more_child),
                                            style     = MaterialTheme.typography.labelSmall,
                                            color     = MaterialTheme.colorScheme.onBackground.copy(0.55f),
                                            textAlign = TextAlign.Center,
                                            fontSize  = 9.sp
                                        )
                                    }
                                }
                                Spacer(Modifier.height(dimensions.spacingSmall / 2))
                            }

                            if (displayedBabies.isEmpty()) {
                                item { EmptyFilterResult(filter = activeFilter) }
                            } else {
                                items(items = displayedBabies, key = { it.babyId }) { baby ->
                                    BabyCard(
                                        baby              = baby,
                                        vaccinations      = state.upcomingVaccinations[baby.babyId] ?: emptyList(),
                                        latestGrowth      = state.latestGrowthRecords[baby.babyId],
                                        onSeeProfile      = { onSeeProfile(baby) },
                                        onArchive         = { viewModel.archiveBaby(baby.babyId) },
                                        onUnarchive       = { viewModel.unarchiveBaby(baby.babyId) },
                                        onEditDetails     = { /* navigate to edit baby */ },
                                        onAddMeasurement  = { /* navigate to add measurement */ },
                                        onViewGrowthChart = { /* navigate to growth chart */ }
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
                RoundedCornerShape(dimensions.logoSize * 0.07f)
            )
            .border(
                1.dp,
                customColors.accentGradientStart.copy(0.2f),
                RoundedCornerShape(dimensions.logoSize * 0.07f)
            )
            .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall / 2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = hint,
            tint     = MaterialTheme.colorScheme.onSurface.copy(0.45f),
            modifier = Modifier.size(dimensions.iconMedium)
        )
        Spacer(Modifier.width(dimensions.spacingSmall))
        BasicTextField(
            value         = query,
            onValueChange = onQueryChange,
            modifier      = Modifier
                .weight(1f)
                .padding(vertical = dimensions.spacingSmall + 2.dp),
            singleLine    = true,
            textStyle     = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
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
                    Icons.Default.Close,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                    modifier = Modifier.size(dimensions.iconMedium - 4.dp)
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
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall + 2.dp)
    ) {
        BabyFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) customColors.accentGradientStart.copy(0.18f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                        RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) customColors.accentGradientStart.copy(0.5f)
                        else MaterialTheme.colorScheme.onSurface.copy(0.15f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(filter) }
                    .padding(
                        horizontal = dimensions.spacingLarge - 2.dp,
                        vertical   = dimensions.spacingSmall + 1.dp
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
    val customColors = MaterialTheme.customColors
    var quickActionExpanded by remember { mutableStateOf(false) }

    val isFemale = baby.gender.equals("FEMALE", ignoreCase = true) ||
            baby.gender.equals("GIRL", ignoreCase = true)

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
    val displayHeight = latestGrowth?.height ?: baby.birthHeight
    val displayWeight = latestGrowth?.weight ?: baby.birthWeight
    val heightPct     = latestGrowth?.heightPercentile
    val weightPct     = latestGrowth?.weightPercentile

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(customColors.glassOverlay, CircleShape)
                            .border(2.dp, customColors.glassOverlay.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isFemale) "👶" else "👦", fontSize = 26.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text       = baby.fullName,
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onPrimary
                            )
                            if (!baby.isActive) {
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(customColors.glassOverlay, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text       = stringResource(Res.string.baby_status_archived),
                                        style      = MaterialTheme.typography.labelSmall,
                                        color      = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = if (isFemale) "♀" else "♂",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(0.9f)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = "${formatAgeBaby(baby.ageInMonths)} • ${
                            if (isFemale) stringResource(Res.string.gender_female)
                            else          stringResource(Res.string.gender_male)
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(0.9f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                displayHeight?.let { h ->
                    val pct = if (heightPct != null) " (${heightPct}th %ile)" else ""
                    BabyStatRow("📊", stringResource(Res.string.baby_stat_height, h.toString()) + pct)
                }
                displayWeight?.let { w ->
                    Spacer(Modifier.height(3.dp))
                    val pct = if (weightPct != null) " (${weightPct}th %ile)" else ""
                    BabyStatRow("📊", stringResource(Res.string.baby_stat_weight, w.toString()) + pct)
                }
                if (totalVaccines > 0) {
                    Spacer(Modifier.height(3.dp))
                    BabyStatRow(
                        "💉",
                        stringResource(Res.string.baby_stat_vaccines, doneVaccines, totalVaccines)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ── See Profile button ────────────────────────────────────
                    OutlinedButton(
                        onClick  = onSeeProfile,   // ← calls the wired lambda
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(LocalDimensions.current.buttonCornerRadius),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border   = BorderStroke(1.dp, customColors.glassOverlay.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text       = stringResource(Res.string.baby_see_profile),
                            fontWeight = FontWeight.SemiBold,
                            style      = MaterialTheme.typography.labelLarge
                        )
                    }

                    // ── Quick Action dropdown ─────────────────────────────────
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick  = { quickActionExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(LocalDimensions.current.buttonCornerRadius),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border   = BorderStroke(1.dp, customColors.glassOverlay.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text       = stringResource(Res.string.baby_quick_action),
                                fontWeight = FontWeight.SemiBold,
                                style      = MaterialTheme.typography.labelLarge
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        QuickActionMenu(
                            expanded      = quickActionExpanded,
                            isActive      = baby.isActive,
                            onDismiss     = { quickActionExpanded = false },
                            onArchive     = { quickActionExpanded = false; onArchive() },
                            onUnarchive   = { quickActionExpanded = false; onUnarchive() },
                            onEdit        = { quickActionExpanded = false; onEditDetails() },
                            onAddMeasure  = { quickActionExpanded = false; onAddMeasurement() },
                            onGrowthChart = { quickActionExpanded = false; onViewGrowthChart() }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QUICK ACTION MENU
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickActionMenu(
    expanded     : Boolean,
    isActive     : Boolean,
    onDismiss    : () -> Unit,
    onArchive    : () -> Unit,
    onUnarchive  : () -> Unit,
    onEdit       : () -> Unit,
    onAddMeasure : () -> Unit,
    onGrowthChart: () -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    DropdownMenu(
        expanded         = expanded,
        onDismissRequest = onDismiss,
        modifier         = Modifier
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(dimensions.buttonCornerRadius)
            )
            .border(
                1.dp,
                customColors.accentGradientStart.copy(0.3f),
                RoundedCornerShape(dimensions.buttonCornerRadius)
            )
    ) {
        QuickActionItem("✏️", stringResource(Res.string.baby_action_edit),         onEdit)
        QuickActionItem("📏", stringResource(Res.string.baby_action_add_measure),  onAddMeasure)
        QuickActionItem("📊", stringResource(Res.string.baby_action_growth_chart), onGrowthChart)

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color    = MaterialTheme.colorScheme.onSurface.copy(0.1f)
        )

        if (isActive) {
            QuickActionItem(
                "📦",
                stringResource(Res.string.baby_action_archive),
                onArchive,
                customColors.warning
            )
        } else {
            QuickActionItem(
                "♻️",
                stringResource(Res.string.baby_action_unarchive),
                onUnarchive,
                customColors.success
            )
        }
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
// STAT ROW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BabyStatRow(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(6.dp))
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
            Text("👶", fontSize = 64.sp)
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
                colors  = ButtonDefaults.buttonColors(
                    containerColor = customColors.accentGradientStart
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconMedium)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(Res.string.home_add_first_baby),
                    fontWeight = FontWeight.SemiBold
                )
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
            .padding(vertical = dimensions.spacingXLarge * 1.5f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text     = if (filter == BabyFilter.ARCHIVED) "📦" else "🔍",
            fontSize = 48.sp
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
    val shimmer by rememberInfiniteTransition(label = "sk").animateFloat(
        initialValue  = 0.2f,
        targetValue   = 0.5f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label         = "sk_alpha"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                MaterialTheme.colorScheme.onBackground.copy(shimmer),
                RoundedCornerShape(18.dp)
            )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPER — age formatter (Composable so it can call stringResource)
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
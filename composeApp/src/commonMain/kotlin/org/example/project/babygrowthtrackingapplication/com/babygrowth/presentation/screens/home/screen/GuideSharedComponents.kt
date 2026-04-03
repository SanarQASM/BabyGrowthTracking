package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.LocalIsRTL
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.readingTextAlign
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*

// ── Top App Bar ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideTopBar(title: String, onBack: () -> Unit) {
    val customColors = MaterialTheme.customColors
    TopAppBar(
        title = {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = customColors.accentGradientStart
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.common_back),
                    tint               = customColors.accentGradientStart
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = customColors.accentGradientStart.copy(alpha = 0.12f)
        )
    )
}

// ── Guide subtitle header ────────────────────────────────────────────────

@Composable
fun GuideSubtitleHeader(
    subtitle     : String,
    babies       : List<BabyResponse>,
    selectedIndex: Int,
    onSelectBaby : (Int) -> Unit,
    modifier     : Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    var expanded     by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(
                    customColors.accentGradientStart.copy(alpha = 0.14f),
                    customColors.accentGradientEnd.copy(alpha = 0.06f)
                ))
            )
            .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingMedium)
    ) {
        Text(
            text       = subtitle,
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground,
            textAlign  = readingTextAlign()
        )

        if (babies.isNotEmpty()) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(
                text          = stringResource(Res.string.sleep_guide_select_child),
                style         = MaterialTheme.typography.labelSmall,
                color         = customColors.accentGradientStart,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(dimensions.spacingXSmall))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensions.buttonCornerRadius / 2))
                    .background(customColors.accentGradientStart.copy(alpha = 0.12f))
                    .clickable { expanded = !expanded }
                    .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingSmall + dimensions.borderWidthMedium)
            ) {
                val selected = babies.getOrNull(selectedIndex)
                val isFemale = selected?.gender?.lowercase()?.let {
                    it == "female" || it == "girl"
                } ?: false
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Text(
                        if (isFemale) "👧" else "👦",
                        fontSize = dimensions.iconMedium.value.sp
                    )
                    Text(
                        text     = selected?.fullName ?: stringResource(Res.string.home_select_child_hint),
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text("▾", color = customColors.accentGradientStart, fontSize = dimensions.iconSmall.value.sp)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    babies.forEachIndexed { i, baby ->
                        val fem = baby.gender.lowercase().let { it == "female" || it == "girl" }
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(if (fem) "👧" else "👦")
                                    Text(baby.fullName)
                                }
                            },
                            onClick = { onSelectBaby(i); expanded = false }
                        )
                    }
                }
            }
        }
    }
}

// ── Category selector ────────────────────────────────────────────────────

data class GuideCategoryItem(
    val id    : String,
    val icon  : String,
    val label : String
)

@Composable
fun GuideCategorySelector(
    categories      : List<GuideCategoryItem>,
    selectedId      : String,
    onSelectCategory: (String) -> Unit,
    modifier        : Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall)
    ) {
        Text(
            text          = stringResource(Res.string.sleep_guide_select_category),
            style         = MaterialTheme.typography.labelSmall,
            color         = customColors.accentGradientStart,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(dimensions.spacingSmall))
        val rows = categories.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
            rows.forEach { row ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    row.forEach { cat ->
                        GuideCategoryTile(
                            item     = cat,
                            selected = cat.id == selectedId,
                            onClick  = { onSelectCategory(cat.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GuideCategoryTile(
    item    : GuideCategoryItem,
    selected: Boolean,
    onClick : () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "cat_scale"
    )
    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(dimensions.cardCornerRadius))
            .background(
                if (selected) customColors.accentGradientStart.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = if (selected) dimensions.borderWidthMedium else 0.dp,
                color = if (selected) customColors.accentGradientStart else Color.Transparent,
                shape = RoundedCornerShape(dimensions.cardCornerRadius)
            )
            .clickable(onClick = onClick)
            .padding(
                vertical   = dimensions.spacingSmall + dimensions.borderWidthMedium,
                horizontal = dimensions.spacingSmall
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(dimensions.guideCategoryIconBoxSize)
                .background(customColors.accentGradientStart.copy(alpha = 0.10f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(item.icon, fontSize = dimensions.guideCategoryIconFontSize)
        }
        Spacer(Modifier.height(dimensions.spacingXSmall + dimensions.borderWidthThin))
        Text(
            text       = item.label,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color      = if (selected) customColors.accentGradientStart
            else MaterialTheme.colorScheme.onSurface,
            textAlign  = TextAlign.Center,
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis
        )
    }
}

// ── Pill tabs ─────────────────────────────────────────────────────────────

fun deduplicateTabs(tabs: List<GuideTab>): List<GuideTab> {
    val seen = mutableSetOf<String>()
    return tabs.filter { seen.add(it.id) }
}

@Composable
fun GuidePillTabs(
    tabs       : List<GuideTab>,
    selectedId : String,
    langCode   : String,
    onSelectTab: (String) -> Unit,
    modifier   : Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val dedupedTabs  = remember(tabs) { deduplicateTabs(tabs) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        dedupedTabs.forEach { tab ->
            val selected = tab.id == selectedId
            val bg by animateColorAsState(
                if (selected) customColors.accentGradientStart else Color.Transparent,
                label = "tab_bg"
            )
            val textColor by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                label = "tab_text"
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .border(
                        dimensions.borderWidthThin,
                        customColors.accentGradientStart.copy(alpha = if (selected) 0f else 0.3f),
                        RoundedCornerShape(50)
                    )
                    .clickable { onSelectTab(tab.id) }
                    .padding(
                        horizontal = dimensions.guideTabPillPaddingH,
                        vertical   = dimensions.guideTabPillPaddingV
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = tab.label.get(langCode),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color      = textColor
                )
            }
        }
    }
}

// ── Guide content card ────────────────────────────────────────────────────

@Composable
fun GuideContentCard(
    item         : GuideItem,
    langCode     : String,
    feedbackState: CardFeedbackState,
    onUseful     : () -> Unit,
    onUseless    : () -> Unit,
    modifier     : Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val isDark       = LocalIsDarkTheme.current

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = if (isDark)
                MaterialTheme.colorScheme.surface
            else
                customColors.accentGradientStart.copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium)
        ) {
            Text(
                text       = item.title.get(langCode).uppercase(),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = customColors.accentGradientStart,
                textAlign  = readingTextAlign()
            )
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(
                text      = item.description.get(langCode),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurface,
                textAlign = readingTextAlign()
            )
            item.tip?.get(langCode)?.let { tip ->
                if (tip.isNotBlank()) {
                    Spacer(Modifier.height(dimensions.spacingSmall))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimensions.spacingSmall))
                            .background(customColors.accentGradientStart.copy(alpha = 0.10f))
                            .padding(
                                horizontal = dimensions.spacingSmall + dimensions.spacingXSmall,
                                vertical   = dimensions.spacingSmall
                            ),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                        verticalAlignment     = Alignment.Top
                    ) {
                        Text("💡", fontSize = dimensions.iconSmall.value.sp)
                        Text(
                            text      = tip,
                            style     = MaterialTheme.typography.bodySmall,
                            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            textAlign = readingTextAlign()
                        )
                    }
                }
            }
            Spacer(Modifier.height(dimensions.spacingMedium))
            GuideFeedbackRow(
                feedbackState = feedbackState,
                onUseful      = onUseful,
                onUseless     = onUseless
            )
        }
    }
}

// ── Feedback row ──────────────────────────────────────────────────────────

@Composable
fun GuideFeedbackRow(
    feedbackState: CardFeedbackState,
    onUseful     : () -> Unit,
    onUseless    : () -> Unit,
    modifier     : Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Row(
        modifier              = modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        Text(
            text     = stringResource(Res.string.sleep_guide_useful_for, feedbackState.usefulCount.toInt()),
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.weight(1f)
        )
        FeedbackPill(
            label     = stringResource(Res.string.sleep_guide_useless),
            selected  = feedbackState.userVote == UserVote.USELESS,
            isLoading = feedbackState.isLoading,
            colorSel  = MaterialTheme.colorScheme.error,
            colorText = MaterialTheme.colorScheme.error,
            onClick   = onUseless
        )
        FeedbackPill(
            label     = stringResource(Res.string.sleep_guide_useful),
            selected  = feedbackState.userVote == UserVote.USEFUL,
            isLoading = feedbackState.isLoading,
            colorSel  = customColors.accentGradientStart,
            colorText = customColors.accentGradientStart,
            onClick   = onUseful
        )
    }
}

@Composable
private fun FeedbackPill(
    label    : String,
    selected : Boolean,
    isLoading: Boolean,
    colorSel : Color,
    colorText: Color,
    onClick  : () -> Unit
) {
    val dimensions = LocalDimensions.current
    val bgAlpha by animateFloatAsState(
        targetValue   = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label         = "pill_bg_alpha"
    )
    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "pill_scale"
    )
    val currentTextColor by animateColorAsState(
        targetValue = if (selected) colorSel else colorText.copy(alpha = 0.6f),
        label       = "pill_text_color"
    )
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(colorSel.copy(alpha = bgAlpha * 0.18f))
            .border(
                width = if (selected) dimensions.borderWidthThin + 0.5.dp else dimensions.borderWidthThin,
                color = if (selected) colorSel else colorText.copy(alpha = 0.35f),
                shape = RoundedCornerShape(50)
            )
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(
                horizontal = dimensions.guideFeedbackPillPaddingH,
                vertical   = dimensions.guideFeedbackPillPaddingV
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(dimensions.iconSmall),
                strokeWidth = dimensions.borderWidthThin + 0.5.dp,
                color       = colorText
            )
        } else {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
            ) {
                if (selected) {
                    Text(
                        "✓",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = currentTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color      = currentTextColor
                )
            }
        }
    }
}
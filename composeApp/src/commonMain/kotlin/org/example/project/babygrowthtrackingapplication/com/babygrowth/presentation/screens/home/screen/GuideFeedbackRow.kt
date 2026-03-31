package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*

// ── Color constants ───────────────────────────────────────────────────────────

val GuidePurple      = Color(0xFF7A2C78)
val GuidePurpleLight = Color(0xFF9B3A99)
val GuidePink        = Color(0xFFE56399)

// ── Baby info for guide screens ───────────────────────────────────────────────

data class BabyInfo(
    val id: String,
    val name: String,
    val gender: String,
    val ageMonths: Int
)

// ── 1. GuideFeedbackRow ──────────────────────────────────────────────────────

@Composable
fun GuideFeedbackRow(
    vote    : GuideVote,
    onUseful: () -> Unit,
    onUseless: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    Row(
        modifier             = modifier.fillMaxWidth(),
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text      = stringResource(Res.string.sleep_guide_useful_for, vote.usefulCount),
            style     = MaterialTheme.typography.labelSmall,
            fontStyle = FontStyle.Italic,
            textDecoration = TextDecoration.Underline,
            color     = GuidePink,
            modifier  = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
            val usefulFilled  = vote.userVote == VoteType.USEFUL
            val uselessFilled = vote.userVote == VoteType.USELESS
            Button(
                onClick = onUseful,
                shape   = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = if (usefulFilled) GuidePink else Color.Transparent,
                    contentColor   = if (usefulFilled) Color.White else GuidePink
                ),
                border  = if (!usefulFilled) BorderStroke(1.dp, GuidePink) else null,
                contentPadding = PaddingValues(horizontal = dimensions.spacingMedium, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(stringResource(Res.string.sleep_guide_useful), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onUseless,
                shape   = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = if (uselessFilled) Color(0xFF888888) else Color.Transparent,
                    contentColor   = if (uselessFilled) Color.White else MaterialTheme.colorScheme.onSurface.copy(0.6f)
                ),
                border  = if (!uselessFilled) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                contentPadding = PaddingValues(horizontal = dimensions.spacingMedium, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(stringResource(Res.string.sleep_guide_useless), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── 2. GuideSectionCard ──────────────────────────────────────────────────────

@Composable
fun GuideSectionCard(
    title   : String,
    modifier: Modifier = Modifier,
    content : @Composable ColumnScope.() -> Unit
) {
    val dimensions = LocalDimensions.current
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFF8B3A8B)),
        elevation = CardDefaults.cardElevation(dimensions.cardElevation)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(GuidePurple, GuidePurpleLight)))
                    .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingSmall + 2.dp)
            ) {
                Text(
                    text       = title.uppercase(),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    letterSpacing = 1.sp
                )
            }
            Column(
                modifier = Modifier.padding(dimensions.spacingMedium),
                content  = content
            )
        }
    }
}

// ── 3. GuideStrategyCard ─────────────────────────────────────────────────────

@Composable
fun GuideStrategyCard(
    sectionLabel: String,
    title       : String,
    description : String,
    tip         : String,
    vote        : GuideVote,
    onUseful    : () -> Unit,
    onUseless   : () -> Unit,
    modifier    : Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    GuideSectionCard(title = sectionLabel, modifier = modifier) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color      = Color.White
        )
        Spacer(Modifier.height(dimensions.spacingXSmall))
        Text(
            text  = description,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(0.85f)
        )
        if (tip.isNotBlank()) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensions.spacingSmall))
                    .background(GuidePink.copy(0.15f))
                    .padding(dimensions.spacingSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
            ) {
                Text("💡", fontSize = 14.sp)
                Text(
                    text  = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.9f)
                )
            }
        }
        Spacer(Modifier.height(dimensions.spacingSmall))
        HorizontalDivider(color = Color.White.copy(0.2f))
        Spacer(Modifier.height(dimensions.spacingSmall))
        GuideFeedbackRow(vote = vote, onUseful = onUseful, onUseless = onUseless)
    }
}

// ── 4. GuideChildSelector ────────────────────────────────────────────────────

@Composable
fun GuideChildSelector(
    babies       : List<BabyInfo>,
    selectedIndex: Int,
    onSelect     : (Int) -> Unit,
    modifier     : Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    var expanded by remember { mutableStateOf(false) }
    val selected = babies.getOrNull(selectedIndex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.cardCornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { expanded = !expanded }
            .padding(horizontal = dimensions.spacingMedium,
                vertical   = dimensions.spacingSmall + dimensions.spacingXSmall)
    ) {
        Row(
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            val isFemale = selected?.gender?.let {
                it.equals("FEMALE", ignoreCase = true) || it.equals("GIRL", ignoreCase = true)
            } ?: false
            Text(
                text     = if (selected != null) (if (isFemale) "👧" else "👦") else "👶",
                fontSize = dimensions.iconMedium.value.sp
            )
            Text(
                text     = selected?.name ?: stringResource(Res.string.sleep_guide_select_child),
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint     = customColors.accentGradientStart,
                modifier = Modifier.size(dimensions.iconMedium)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            babies.forEachIndexed { index, baby ->
                val isFemale = baby.gender.equals("FEMALE", ignoreCase = true) ||
                        baby.gender.equals("GIRL", ignoreCase = true)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isFemale) "👧" else "👦", fontSize = dimensions.iconMedium.value.sp)
                            Spacer(Modifier.width(dimensions.spacingSmall))
                            Column {
                                Text(baby.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text  = stringResource(Res.string.sleep_guide_months, baby.ageMonths),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                                )
                            }
                        }
                    },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ── 5. GuideCategoryGrid ─────────────────────────────────────────────────────

@Composable
fun GuideCategoryGrid(
    categories   : List<Pair<String, String>>,  // (emoji, label)
    selectedIndex: Int,
    onSelect     : (Int) -> Unit,
    modifier     : Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val cols = 2
    val rows = (categories.size + cols - 1) / cols

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                for (col in 0 until cols) {
                    val idx = row * cols + col
                    if (idx < categories.size) {
                        val (emoji, label) = categories[idx]
                        val isSelected = idx == selectedIndex
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(dimensions.cardCornerRadius))
                                .background(if (isSelected) GuidePink.copy(0.08f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) GuidePink else Color.Transparent,
                                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                                )
                                .clickable { onSelect(idx) }
                                .padding(dimensions.spacingSmall),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(dimensions.avatarLarge + dimensions.spacingSmall)
                                    .background(
                                        if (isSelected) GuidePink.copy(0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) GuidePink else MaterialTheme.colorScheme.outline.copy(0.3f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = (dimensions.iconXLarge.value - 14f).sp)
                            }
                            Spacer(Modifier.height(dimensions.spacingXSmall))
                            Text(
                                text       = label,
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color      = if (isSelected) GuidePink else MaterialTheme.colorScheme.onBackground,
                                textAlign  = TextAlign.Center,
                                maxLines   = 2,
                                overflow   = TextOverflow.Ellipsis
                            )
                            if (isSelected) {
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier.size(width = 24.dp, height = 2.dp)
                                        .background(GuidePink, RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ── 6. GuideEnvironmentCard ──────────────────────────────────────────────────

@Composable
fun GuideEnvironmentCard(
    item     : EnvironmentItem,
    vote     : GuideVote,
    onUseful : () -> Unit,
    onUseless: () -> Unit,
    modifier : Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    GuideSectionCard(title = item.title, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Text(item.icon, fontSize = 22.sp)
            Text(
                text       = item.value,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White
            )
        }
        Spacer(Modifier.height(dimensions.spacingSmall))
        Text(
            text       = stringResource(Res.string.sleep_guide_why_matters),
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = GuidePink
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = item.why,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(0.85f)
        )
        if (item.tips.isNotEmpty()) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(
                text       = stringResource(Res.string.sleep_guide_tips),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color      = GuidePink
            )
            item.tips.forEach { tip ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text("✓", color = GuidePink, fontSize = 12.sp)
                    Text(tip, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.85f))
                }
            }
        }
        if (item.tip.isNotBlank()) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensions.spacingSmall))
                    .background(GuidePink.copy(0.15f))
                    .padding(dimensions.spacingSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
            ) {
                Text("💡", fontSize = 14.sp)
                Text(item.tip, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.9f))
            }
        }
        Spacer(Modifier.height(dimensions.spacingSmall))
        HorizontalDivider(color = Color.White.copy(0.2f))
        Spacer(Modifier.height(dimensions.spacingSmall))
        GuideFeedbackRow(vote = vote, onUseful = onUseful, onUseless = onUseless)
    }
}

// ── 7. LullabyCard ───────────────────────────────────────────────────────────

@Composable
fun LullabyCard(
    lullaby  : Lullaby,
    isPlaying: Boolean,
    vote     : GuideVote,
    onPlayPause: () -> Unit,
    onDownload : () -> Unit,
    onUseful   : () -> Unit,
    onUseless  : () -> Unit,
    modifier   : Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = if (isPlaying) Color(0xFF6A1B69) else Color(0xFF8B3A8B)
        ),
        border    = if (isPlaying) BorderStroke(2.dp, GuidePink) else null,
        elevation = CardDefaults.cardElevation(if (isPlaying) 8.dp else dimensions.cardElevation)
    ) {
        Column(modifier = Modifier.padding(dimensions.spacingMedium)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.radialGradient(listOf(GuidePurpleLight, GuidePurple)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎵", fontSize = 20.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = lullaby.title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    )
                    Text(
                        text  = lullaby.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.7f)
                    )
                    Text(
                        text  = stringResource(Res.string.sleep_guide_duration, lullaby.duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = GuidePink
                    )
                }
            }
            Spacer(Modifier.height(dimensions.spacingSmall))
            Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                Button(
                    onClick = onPlayPause,
                    shape   = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors  = ButtonDefaults.buttonColors(containerColor = GuidePink),
                    contentPadding = PaddingValues(horizontal = dimensions.spacingMedium, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) stringResource(Res.string.sleep_guide_pause) else stringResource(Res.string.sleep_guide_play),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isPlaying) stringResource(Res.string.sleep_guide_pause) else stringResource(Res.string.sleep_guide_play), style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onDownload,
                    shape   = RoundedCornerShape(dimensions.buttonCornerRadius),
                    border  = BorderStroke(1.dp, GuidePink),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = GuidePink),
                    contentPadding = PaddingValues(horizontal = dimensions.spacingMedium, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = stringResource(Res.string.sleep_guide_download), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(Res.string.sleep_guide_download), style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(dimensions.spacingSmall))
            HorizontalDivider(color = Color.White.copy(0.2f))
            Spacer(Modifier.height(dimensions.spacingSmall))
            GuideFeedbackRow(vote = vote, onUseful = onUseful, onUseless = onUseless)
        }
    }
}

// ── 8. LullabyPlayerBar ──────────────────────────────────────────────────────

@Composable
fun LullabyPlayerBar(
    lullabyTitle: String,
    isPlaying   : Boolean,
    progress    : Float,
    duration    : String,
    onPlayPause : () -> Unit,
    onStop      : () -> Unit,
    onSeek      : (Float) -> Unit,
    modifier    : Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val elapsed = formatProgress(progress, duration)

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(topStart = dimensions.cardCornerRadius, topEnd = dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFF1C0C1C)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(dimensions.spacingMedium)) {
            Text(
                text       = stringResource(Res.string.sleep_guide_now_playing),
                style      = MaterialTheme.typography.labelSmall,
                color      = GuidePink,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎵", fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    text       = lullabyTitle,
                    style      = MaterialTheme.typography.titleSmall,
                    color      = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f),
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(elapsed, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
                Slider(
                    value           = progress,
                    onValueChange   = onSeek,
                    modifier        = Modifier.weight(1f).padding(horizontal = 4.dp),
                    colors          = SliderDefaults.colors(
                        thumbColor            = GuidePink,
                        activeTrackColor      = GuidePink,
                        inactiveTrackColor    = Color.White.copy(0.2f)
                    )
                )
                Text(duration, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* previous */ }) {
                    Icon(Icons.Default.SkipPrevious, null, tint = Color.White.copy(0.7f))
                }
                IconButton(onClick = { /* rewind */ }) {
                    Icon(Icons.Default.Replay10, null, tint = Color.White.copy(0.7f))
                }
                IconButton(
                    onClick  = onPlayPause,
                    modifier = Modifier
                        .size(48.dp)
                        .background(GuidePink, CircleShape)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) stringResource(Res.string.sleep_guide_pause) else stringResource(Res.string.sleep_guide_play),
                        tint     = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onStop) {
                    Icon(Icons.Default.Stop, null, tint = Color.White.copy(0.7f))
                }
                IconButton(onClick = { /* next */ }) {
                    Icon(Icons.Default.SkipNext, null, tint = Color.White.copy(0.7f))
                }
                IconButton(onClick = { /* shuffle */ }) {
                    Icon(Icons.Default.Shuffle, null, tint = Color.White.copy(0.5f))
                }
            }
        }
    }
}

private fun formatProgress(progress: Float, duration: String): String {
    val parts = duration.split(":")
    if (parts.size < 2) return "0:00"
    val totalMin = parts[0].toIntOrNull() ?: 0
    val totalSec = parts[1].toIntOrNull() ?: 0
    val totalSeconds = totalMin * 60 + totalSec
    val elapsed = (progress * totalSeconds).toInt()
    val m = elapsed / 60
    val s = elapsed % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

// ── 9. GuideTabRow ───────────────────────────────────────────────────────────

@Composable
fun GuideTabRow(
    tabs         : List<String>,
    selectedIndex: Int,
    onSelect     : (Int) -> Unit,
    modifier     : Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    LazyRow(
        modifier            = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
        contentPadding      = PaddingValues(horizontal = 2.dp)
    ) {
        itemsIndexed(tabs) { idx, label ->
            val selected = idx == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) GuidePink else Color.Transparent)
                    .border(1.dp, GuidePink, RoundedCornerShape(20.dp))
                    .clickable { onSelect(idx) }
                    .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingXSmall + 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color      = if (selected) Color.White else GuidePink
                )
            }
        }
    }
}
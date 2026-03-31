package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*

// ═══════════════════════════════════════════════════════════════════════════
// LullabyComponents.kt
//
// All lullaby-specific UI: lullaby cards, the now-playing bar, and the
// mini player controls.
// ═══════════════════════════════════════════════════════════════════════════

// ── Lullabies section ─────────────────────────────────────────────────────

@Composable
fun LullabiesContent(
    items        : List<GuideItem>,
    langCode     : String,
    playerState  : LullabyPlayerState,
    feedbackMap  : Map<String, CardFeedbackState>,
    guideType    : String,
    onPlay       : (GuideItem) -> Unit,
    onTogglePause: () -> Unit,
    onStop       : () -> Unit,
    onDownload   : (GuideItem) -> Unit,
    onUseful     : (String) -> Unit,
    onUseless    : (String) -> Unit,
    modifier     : Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Column(modifier = modifier) {
        // Section header
        Text(
            text      = stringResource(Res.string.sleep_guide_lullabies_title),
            style     = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color     = customColors.accentGradientStart,
            modifier  = Modifier.padding(horizontal = dimensions.screenPadding,
                vertical = dimensions.spacingSmall)
        )

        // Lullaby cards
        Column(
            modifier            = Modifier.padding(horizontal = dimensions.screenPadding),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            items.forEach { item ->
                LullabyCard(
                    item          = item,
                    langCode      = langCode,
                    isPlaying     = playerState.currentItemId == item.id && playerState.isPlaying,
                    isCurrentItem = playerState.currentItemId == item.id,
                    feedbackState = feedbackMap[item.id] ?: CardFeedbackState(item.id),
                    onPlay        = { onPlay(item) },
                    onTogglePause = onTogglePause,
                    onDownload    = { onDownload(item) },
                    onUseful      = { onUseful(item.id) },
                    onUseless     = { onUseless(item.id) }
                )
            }
        }

        // Now-playing bar (bottom of lullabies section)
        AnimatedVisibility(
            visible  = playerState.currentItemId != null,
            enter    = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit     = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            val currentItem = items.find { it.id == playerState.currentItemId }
            if (currentItem != null) {
                NowPlayingBar(
                    item          = currentItem,
                    langCode      = langCode,
                    playerState   = playerState,
                    onTogglePause = onTogglePause,
                    onStop        = onStop,
                    modifier      = Modifier.padding(top = dimensions.spacingMedium)
                )
            }
        }
    }
}

// ── Individual lullaby card ───────────────────────────────────────────────

@Composable
private fun LullabyCard(
    item         : GuideItem,
    langCode     : String,
    isPlaying    : Boolean,
    isCurrentItem: Boolean,
    feedbackState: CardFeedbackState,
    onPlay       : () -> Unit,
    onTogglePause: () -> Unit,
    onDownload   : () -> Unit,
    onUseful     : () -> Unit,
    onUseless    : () -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val isDark       = LocalIsDarkTheme.current

    // Gentle pulse animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "lullaby_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue   = 0.08f,
        targetValue    = 0.18f,
        animationSpec  = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = if (isCurrentItem)
                customColors.accentGradientStart.copy(alpha = if (isPlaying) pulseAlpha else 0.10f)
            else if (isDark) MaterialTheme.colorScheme.surface
            else customColors.accentGradientStart.copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(if (isCurrentItem) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                // Music note icon
                Text("🎵", fontSize = 20.sp)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = item.title.get(langCode),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    // Description (tradition + duration)
                    Text(
                        text  = item.description.get(langCode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(dimensions.spacingSmall + 4.dp))

            // Play / Download row
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                // Play / Pause button
                OutlinedButton(
                    onClick = if (isCurrentItem) onTogglePause else onPlay,
                    shape   = RoundedCornerShape(50),
                    colors  = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isPlaying) customColors.accentGradientStart
                        else Color.Transparent,
                        contentColor   = if (isPlaying) MaterialTheme.colorScheme.onPrimary
                        else customColors.accentGradientStart
                    ),
                    border  = BorderStroke(1.dp, customColors.accentGradientStart),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text       = if (isPlaying)
                            stringResource(Res.string.sleep_guide_pause)
                        else
                            stringResource(Res.string.sleep_guide_play),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Download button
                OutlinedButton(
                    onClick        = onDownload,
                    shape          = RoundedCornerShape(50),
                    colors         = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    border         = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text  = stringResource(Res.string.sleep_guide_download),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(Modifier.height(dimensions.spacingSmall + 4.dp))

            // Feedback row
            GuideFeedbackRow(
                feedbackState = feedbackState,
                onUseful      = onUseful,
                onUseless     = onUseless
            )
        }
    }
}

// ── Now-playing mini bar ──────────────────────────────────────────────────

@Composable
fun NowPlayingBar(
    item         : GuideItem,
    langCode     : String,
    playerState  : LullabyPlayerState,
    onTogglePause: () -> Unit,
    onStop       : () -> Unit,
    modifier     : Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Card(
        modifier  = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.screenPadding),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = customColors.accentGradientStart
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium)
        ) {
            // Label
            Text(
                text  = stringResource(Res.string.sleep_guide_now_playing),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(2.dp))

            // Track title
            Text(
                text       = item.title.get(langCode),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimary,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(dimensions.spacingSmall))

            // Progress bar
            LinearProgressIndicator(
                progress  = { playerState.progress },
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(50)),
                color     = MaterialTheme.colorScheme.onPrimary,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
            )

            Spacer(Modifier.height(4.dp))

            // Time labels
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    playerState.positionFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    playerState.durationFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }

            Spacer(Modifier.height(dimensions.spacingSmall))

            // Controls: ⏮ ▶/⏸ ⏹
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Restart / rewind
                IconButton(onClick = { /* seek to 0 */ }) {
                    Text("⏮", fontSize = 20.sp)
                }

                Spacer(Modifier.width(8.dp))

                // Play / pause
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                        .clickable(onClick = onTogglePause),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = if (playerState.isPlaying) "⏸" else "▶",
                        fontSize = 20.sp
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Stop
                IconButton(onClick = onStop) {
                    Text("⏹", fontSize = 20.sp)
                }
            }
        }
    }
}
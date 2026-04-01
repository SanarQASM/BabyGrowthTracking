package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
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
//
// HOW AUDIO WORKS:
//   • GuideViewModel holds a LullabyPlayer (platform expect/actual).
//   • Calling viewModel.playLullaby(itemId, duration) starts the player.
//   • The ViewModel emits position updates via updatePosition(seconds).
//   • The NowPlayingBar reflects playerState (progress, time labels).
//   • Download triggers the platform download helper.
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
    onSeek       : (Int) -> Unit,
    onDownload   : (GuideItem) -> Unit,
    onUseful     : (String) -> Unit,
    onUseless    : (String) -> Unit,
    modifier     : Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Column(modifier = modifier) {
        Text(
            text       = stringResource(Res.string.sleep_guide_lullabies_title),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = customColors.accentGradientStart,
            modifier   = Modifier.padding(
                horizontal = dimensions.screenPadding,
                vertical   = dimensions.spacingSmall
            )
        )

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
                    playerState   = playerState,
                    feedbackState = feedbackMap[item.id] ?: CardFeedbackState(item.id),
                    onPlay        = { onPlay(item) },
                    onTogglePause = onTogglePause,
                    onStop        = onStop,
                    onSeek        = onSeek,
                    onDownload    = { onDownload(item) },
                    onUseful      = { onUseful(item.id) },
                    onUseless     = { onUseless(item.id) }
                )
            }
        }

        // Sticky now-playing bar at bottom of lullabies section
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
                    onSeek        = onSeek,
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
    playerState  : LullabyPlayerState,
    feedbackState: CardFeedbackState,
    onPlay       : () -> Unit,
    onTogglePause: () -> Unit,
    onStop       : () -> Unit,
    onSeek       : (Int) -> Unit,
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
        targetValue    = 0.20f,
        animationSpec  = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // FIX: elevation = 0, border = null — no card border outline
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = when {
                isCurrentItem && isPlaying ->
                    customColors.accentGradientStart.copy(alpha = pulseAlpha)
                isCurrentItem             ->
                    customColors.accentGradientStart.copy(alpha = 0.10f)
                isDark                    -> MaterialTheme.colorScheme.surface
                else                      -> customColors.accentGradientStart.copy(alpha = 0.06f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = null
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
                // Animated music note
                val noteScale by infiniteTransition.animateFloat(
                    initialValue   = 1f,
                    targetValue    = if (isPlaying) 1.2f else 1f,
                    animationSpec  = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                    label          = "note_scale"
                )
                Text(
                    text     = "🎵",
                    fontSize = 20.sp,
                    modifier = Modifier.scale(if (isPlaying) noteScale else 1f)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = item.title.get(langCode),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        text  = item.description.get(langCode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Duration chip
                item.media?.let { media ->
                    val dur = playerState.formatTime(media.duration_seconds)
                    Text(
                        text  = dur,
                        style = MaterialTheme.typography.labelSmall,
                        color = customColors.accentGradientStart.copy(alpha = 0.8f)
                    )
                }
            }

            // Mini progress bar when this card is current
            if (isCurrentItem && playerState.durationSeconds > 0) {
                Spacer(Modifier.height(dimensions.spacingSmall))
                MiniSeekBar(
                    progress  = playerState.progress,
                    duration  = playerState.durationSeconds,
                    onSeek    = onSeek,
                    color     = customColors.accentGradientStart
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        playerState.positionFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        playerState.durationFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(Modifier.height(dimensions.spacingSmall + 4.dp))

            // Controls row: Play/Pause | Stop | Download
            Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {

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
                    border         = BorderStroke(1.dp, customColors.accentGradientStart),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text       = when {
                            isCurrentItem && isPlaying -> stringResource(Res.string.sleep_guide_pause)
                            isCurrentItem             -> stringResource(Res.string.sleep_guide_play)
                            else                      -> stringResource(Res.string.sleep_guide_play)
                        },
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Stop button (only visible when this card is playing)
                AnimatedVisibility(visible = isCurrentItem) {
                    OutlinedButton(
                        onClick        = onStop,
                        shape          = RoundedCornerShape(50),
                        colors         = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border         = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("⏹", style = MaterialTheme.typography.labelMedium)
                    }
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

// ── Draggable seek bar ────────────────────────────────────────────────────

@Composable
private fun MiniSeekBar(
    progress : Float,
    duration : Int,
    onSeek   : (Int) -> Unit,
    color    : Color
) {
    var dragging     by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(progress) }
    val displayProg  = if (dragging) dragProgress else progress

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .pointerInput(duration) {
                detectHorizontalDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd   = {
                        dragging = false
                        onSeek((dragProgress * duration).toInt().coerceIn(0, duration))
                    },
                    onDragCancel = { dragging = false },
                    onHorizontalDrag = { _, delta ->
                        dragProgress = (dragProgress + delta / size.width).coerceIn(0f, 1f)
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.2f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(displayProg.coerceIn(0f, 1f))
                .height(4.dp)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        // Thumb
        Box(
            modifier = Modifier
                .size(14.dp)
                .align(Alignment.CenterStart)
                .offset(x = (maxWidth * displayProg.coerceIn(0f, 1f)) - 7.dp)
                .clip(CircleShape)
                .background(color)
        )
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
    onSeek       : (Int) -> Unit,
    modifier     : Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    // FIX: elevation = 0 / no border on the now-playing card either
    Card(
        modifier  = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.screenPadding),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = customColors.accentGradientStart),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium)
        ) {
            Text(
                text  = stringResource(Res.string.sleep_guide_now_playing),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(2.dp))

            Text(
                text       = item.title.get(langCode),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimary,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(dimensions.spacingSmall))

            // Draggable seek bar in now-playing bar
            MiniSeekBar(
                progress = playerState.progress,
                duration = playerState.durationSeconds,
                onSeek   = onSeek,
                color    = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(Modifier.height(4.dp))

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

            // Controls row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Restart / rewind to 0
                IconButton(onClick = { onSeek(0) }) {
                    Text("⏮", fontSize = 20.sp)
                }

                Spacer(Modifier.width(8.dp))

                // Rewind 10 s
                IconButton(onClick = {
                    onSeek((playerState.positionSeconds - 10).coerceAtLeast(0))
                }) {
                    Text("−10s", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary)
                }

                Spacer(Modifier.width(8.dp))

                // Play / Pause
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f))
                        .clickable(onClick = onTogglePause),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = if (playerState.isPlaying) "⏸" else "▶",
                        fontSize = 22.sp
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Fast-forward 10 s
                IconButton(onClick = {
                    onSeek((playerState.positionSeconds + 10).coerceAtMost(playerState.durationSeconds))
                }) {
                    Text("+10s", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary)
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
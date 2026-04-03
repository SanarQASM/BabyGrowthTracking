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
// ═══════════════════════════════════════════════════════════════════════════

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
                val noteScale by infiniteTransition.animateFloat(
                    initialValue   = 1f,
                    targetValue    = if (isPlaying) 1.2f else 1f,
                    animationSpec  = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                    label          = "note_scale"
                )
                Text(
                    text     = "🎵",
                    fontSize = dimensions.lullabyNoteEmojiSize,
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

                item.media?.let { media ->
                    val dur = playerState.formatTime(media.duration_seconds)
                    Text(
                        text  = dur,
                        style = MaterialTheme.typography.labelSmall,
                        color = customColors.accentGradientStart.copy(alpha = 0.8f)
                    )
                }
            }

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

            Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))

            Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                OutlinedButton(
                    onClick = if (isCurrentItem) onTogglePause else onPlay,
                    shape   = RoundedCornerShape(50),
                    colors  = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isPlaying) customColors.accentGradientStart
                        else Color.Transparent,
                        contentColor   = if (isPlaying) MaterialTheme.colorScheme.onPrimary
                        else customColors.accentGradientStart
                    ),
                    border         = BorderStroke(dimensions.borderWidthThin, customColors.accentGradientStart),
                    contentPadding = PaddingValues(
                        horizontal = dimensions.spacingMedium,
                        vertical   = dimensions.spacingXSmall + dimensions.borderWidthThin
                    )
                ) {
                    Text(
                        text       = when {
                            isCurrentItem && isPlaying -> stringResource(Res.string.sleep_guide_pause)
                            else                       -> stringResource(Res.string.sleep_guide_play)
                        },
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                AnimatedVisibility(visible = isCurrentItem) {
                    OutlinedButton(
                        onClick        = onStop,
                        shape          = RoundedCornerShape(50),
                        colors         = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border         = BorderStroke(
                            dimensions.borderWidthThin,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(
                            horizontal = dimensions.spacingSmall + dimensions.spacingXSmall,
                            vertical   = dimensions.spacingXSmall + dimensions.borderWidthThin
                        )
                    ) {
                        Text(stringResource(Res.string.lullaby_stop), style = MaterialTheme.typography.labelMedium)
                    }
                }

                OutlinedButton(
                    onClick        = onDownload,
                    shape          = RoundedCornerShape(50),
                    colors         = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    border         = BorderStroke(
                        dimensions.borderWidthThin,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(
                        horizontal = dimensions.spacingMedium,
                        vertical   = dimensions.spacingXSmall + dimensions.borderWidthThin
                    )
                ) {
                    Text(
                        text  = stringResource(Res.string.sleep_guide_download),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))

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
    val dimensions   = LocalDimensions.current
    var dragging     by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(progress) }
    val displayProg  = if (dragging) dragProgress else progress

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.iconSmall + dimensions.spacingXSmall)
            .pointerInput(duration) {
                detectHorizontalDragGestures(
                    onDragStart  = { dragging = true },
                    onDragEnd    = {
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
                .height(dimensions.lullabySeekBarHeight)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.2f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(displayProg.coerceIn(0f, 1f))
                .height(dimensions.lullabySeekBarHeight)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        // Thumb
        Box(
            modifier = Modifier
                .size(dimensions.lullabyThumbSize)
                .align(Alignment.CenterStart)
                .offset(x = (maxWidth * displayProg.coerceIn(0f, 1f)) - (dimensions.lullabyThumbSize / 2))
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
            Spacer(Modifier.height(dimensions.borderWidthMedium))

            Text(
                text       = item.title.get(langCode),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimary,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(dimensions.spacingSmall))

            MiniSeekBar(
                progress = playerState.progress,
                duration = playerState.durationSeconds,
                onSeek   = onSeek,
                color    = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(Modifier.height(dimensions.spacingXSmall))

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

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Restart
                IconButton(onClick = { onSeek(0) }) {
                    Text(stringResource(Res.string.lullaby_seek_restart), fontSize = dimensions.lullabyNoteEmojiSize)
                }

                Spacer(Modifier.width(dimensions.spacingSmall))

                // Rewind 10 s
                IconButton(onClick = {
                    onSeek((playerState.positionSeconds - 10).coerceAtLeast(0))
                }) {
                    Text(
                        stringResource(Res.string.lullaby_seek_back_10),
                        fontSize = dimensions.lullabySkipLabelSize,
                        color    = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(Modifier.width(dimensions.spacingSmall))

                // Play / Pause
                Box(
                    modifier = Modifier
                        .size(dimensions.lullabyPlayCircleSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f))
                        .clickable(onClick = onTogglePause),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = if (playerState.isPlaying)
                            stringResource(Res.string.lullaby_pause_symbol)
                        else
                            stringResource(Res.string.lullaby_play_symbol),
                        fontSize = (dimensions.lullabyNoteEmojiSize.value + 2).sp
                    )
                }

                Spacer(Modifier.width(dimensions.spacingSmall))

                // Fast-forward 10 s
                IconButton(onClick = {
                    onSeek((playerState.positionSeconds + 10).coerceAtMost(playerState.durationSeconds))
                }) {
                    Text(
                        stringResource(Res.string.lullaby_seek_forward_10),
                        fontSize = dimensions.lullabySkipLabelSize,
                        color    = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(Modifier.width(dimensions.spacingSmall))

                // Stop
                IconButton(onClick = onStop) {
                    Text(stringResource(Res.string.lullaby_stop), fontSize = dimensions.lullabyNoteEmojiSize)
                }
            }
        }
    }
}
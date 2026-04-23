// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/com/babygrowth/presentation/screens/home/screen/BenchRequestStatusScreen.kt

package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.data.network.BenchRequestStatusUi
import org.example.project.babygrowthtrackingapplication.data.network.BenchRequestUi
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// BenchRequestStatusScreen
//
// Shown to a parent when:
//  1. PENDING  — waiting for team to accept/reject
//  2. REJECTED — team rejected; show reason; let parent pick another bench
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BenchRequestStatusScreen(
    request        : BenchRequestUi,
    babyName       : String,
    isSubmitting   : Boolean,
    onCancelRequest: () -> Unit,
    onPickOtherBench: () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    when (request.status) {
        BenchRequestStatusUi.PENDING  -> PendingRequestCard(
            request         = request,
            babyName        = babyName,
            isSubmitting    = isSubmitting,
            onCancelRequest = onCancelRequest
        )
        BenchRequestStatusUi.REJECTED -> RejectedRequestCard(
            request          = request,
            babyName         = babyName,
            onPickOtherBench = onPickOtherBench
        )
        else -> { /* ACCEPTED or CANCELLED — caller handles routing away */ }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pending — animated pulse while waiting
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PendingRequestCard(
    request        : BenchRequestUi,
    babyName       : String,
    isSubmitting   : Boolean,
    onCancelRequest: () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    // Pulse animation for the waiting indicator
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale by pulseAnim.animateFloat(
        initialValue  = 0.85f,
        targetValue   = 1.15f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(dimensions.screenPadding),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius * 2),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(dimensions.cardElevation)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            // Pulsing icon
            Box(
                modifier         = Modifier
                    .size(dimensions.avatarLarge * scale)
                    .clip(CircleShape)
                    .background(customColors.accentGradientStart.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint               = customColors.accentGradientStart,
                    modifier           = Modifier.size(dimensions.iconXLarge)
                )
            }

            Text(
                text       = stringResource(Res.string.bench_request_pending_title),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )

            Text(
                text      = stringResource(Res.string.bench_request_pending_body, babyName, request.benchNameEn),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            // Bench info chip
            Surface(
                shape = RoundedCornerShape(50),
                color = customColors.accentGradientStart.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier              = Modifier.padding(
                        horizontal = dimensions.spacingMedium,
                        vertical   = dimensions.spacingSmall
                    ),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint               = customColors.accentGradientStart,
                        modifier           = Modifier.size(dimensions.iconSmall)
                    )
                    Text(
                        text       = request.benchNameEn,
                        style      = MaterialTheme.typography.labelMedium,
                        color      = customColors.accentGradientStart,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text  = stringResource(Res.string.bench_request_pending_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(dimensions.spacingSmall))

            // Cancel button
            OutlinedButton(
                onClick  = onCancelRequest,
                enabled  = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border   = androidx.compose.foundation.BorderStroke(
                    dimensions.borderWidthThin,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(dimensions.iconSmall),
                        strokeWidth = dimensions.borderWidthMedium,
                        color       = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(stringResource(Res.string.bench_request_cancel_action))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Rejected — show reason, offer to pick another bench
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RejectedRequestCard(
    request         : BenchRequestUi,
    babyName        : String,
    onPickOtherBench: () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(dimensions.screenPadding),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius * 2),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(dimensions.cardElevation)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            Box(
                modifier         = Modifier
                    .size(dimensions.avatarLarge)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.size(dimensions.iconXLarge)
                )
            }

            Text(
                text       = stringResource(Res.string.bench_request_rejected_title),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.error,
                textAlign  = TextAlign.Center
            )

            Text(
                text      = stringResource(Res.string.bench_request_rejected_body, request.benchNameEn),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            // Reject reason box
            if (!request.rejectReason.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(dimensions.cardCornerRadius),
                    color    = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(dimensions.spacingMedium),
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                    ) {
                        Text(
                            text       = stringResource(Res.string.bench_request_reject_reason_label),
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text  = request.rejectReason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(dimensions.spacingSmall))

            Button(
                onClick  = onPickOtherBench,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors   = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier           = Modifier.size(dimensions.iconSmall)
                )
                Spacer(Modifier.width(dimensions.spacingSmall))
                Text(
                    text       = stringResource(Res.string.bench_request_pick_other_action),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
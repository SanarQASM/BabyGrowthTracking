// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/team/TeamRequestsTab.kt

package org.example.project.babygrowthtrackingapplication.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.data.network.BenchRequestStatusUi
import org.example.project.babygrowthtrackingapplication.data.network.BenchRequestUi
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// TeamRequestsTab
// Shows pending & historical join requests from parents for this bench.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TeamRequestsTab(viewModel: TeamRequestsViewModel) {
    val state        = viewModel.uiState
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    // ── Reject dialog ─────────────────────────────────────────────────────────
    if (state.showRejectDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRejectDialog,
            title = {
                Text(
                    stringResource(Res.string.team_request_reject_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    Text(
                        stringResource(Res.string.team_request_reject_body, state.selectedRequest?.babyName ?: ""),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value         = state.rejectReason,
                        onValueChange = viewModel::onRejectReasonChange,
                        label         = { Text(stringResource(Res.string.team_request_reject_reason_hint)) },
                        modifier      = Modifier.fillMaxWidth(),
                        maxLines      = 4,
                        shape         = RoundedCornerShape(dimensions.cardCornerRadius),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColors.accentGradientStart
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick  = viewModel::confirmReject,
                    enabled  = state.rejectReason.isNotBlank() && !state.reviewSubmitting,
                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (state.reviewSubmitting) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(dimensions.iconSmall),
                            strokeWidth = dimensions.borderWidthMedium,
                            color       = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        Text(stringResource(Res.string.team_request_reject_confirm))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRejectDialog) {
                    Text(stringResource(Res.string.btn_cancel))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Header with pending count badge
        Surface(
            color           = MaterialTheme.colorScheme.surface,
            shadowElevation = dimensions.cardElevationSmall
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = stringResource(Res.string.team_requests_title),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (state.pendingRequests.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text     = "${state.pendingRequests.size}",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = dimensions.borderWidthMedium)
                        )
                    }
                }
            }
        }

        when {
            state.requestsLoading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            }
            state.allRequests.isEmpty() -> {
                TeamEmptyState(
                    emoji      = stringResource(Res.string.team_requests_empty_emoji),
                    title      = stringResource(Res.string.team_requests_empty_title),
                    subtitle   = stringResource(Res.string.team_requests_empty_subtitle),
                    dimensions = dimensions
                )
            }
            else -> {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(
                        horizontal = dimensions.screenPadding,
                        vertical   = dimensions.spacingSmall
                    ),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    // Pending section
                    if (state.pendingRequests.isNotEmpty()) {
                        item {
                            Text(
                                text       = stringResource(Res.string.team_requests_section_pending),
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.error,
                                modifier   = Modifier.padding(vertical = dimensions.spacingXSmall)
                            )
                        }
                        items(state.pendingRequests, key = { it.requestId }) { req ->
                            RequestCard(
                                request         = req,
                                isSubmitting    = state.reviewSubmitting,
                                onAccept        = { viewModel.acceptRequest(req.requestId) },
                                onReject        = { viewModel.openRejectDialog(req) }
                            )
                        }
                        item { Spacer(Modifier.height(dimensions.spacingSmall)) }
                    }

                    // History section
                    val history = state.allRequests.filter { it.status != BenchRequestStatusUi.PENDING }
                    if (history.isNotEmpty()) {
                        item {
                            Text(
                                text       = stringResource(Res.string.team_requests_section_history),
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier   = Modifier.padding(vertical = dimensions.spacingXSmall)
                            )
                        }
                        items(history, key = { it.requestId + "_hist" }) { req ->
                            HistoryRequestCard(req)
                        }
                    }

                    item { Spacer(Modifier.height(dimensions.spacingXXLarge)) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pending request card — accept / reject actions
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RequestCard(
    request     : BenchRequestUi,
    isSubmitting: Boolean,
    onAccept    : () -> Unit,
    onReject    : () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(dimensions.cardElevationSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ChildCare,
                    contentDescription = null,
                    tint               = customColors.accentGradientStart,
                    modifier           = Modifier.size(dimensions.iconMedium)
                )
                Spacer(Modifier.width(dimensions.spacingSmall))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = request.babyName,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        text  = request.createdAt?.take(10) ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text       = stringResource(Res.string.bench_request_status_pending),
                        style      = MaterialTheme.typography.labelSmall,
                        color      = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = dimensions.borderWidthMedium)
                    )
                }
            }

            if (!request.notes.isNullOrBlank()) {
                Text(
                    text  = stringResource(Res.string.team_request_note_prefix, request.notes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Button(
                    onClick        = onAccept,
                    enabled        = !isSubmitting,
                    modifier       = Modifier.weight(1f),
                    shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors         = ButtonDefaults.buttonColors(containerColor = customColors.success),
                    contentPadding = PaddingValues(vertical = dimensions.spacingXSmall)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(dimensions.iconSmall))
                    Spacer(Modifier.width(dimensions.spacingXSmall))
                    Text(stringResource(Res.string.team_request_accept_action))
                }
                OutlinedButton(
                    onClick        = onReject,
                    enabled        = !isSubmitting,
                    modifier       = Modifier.weight(1f),
                    shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                    border         = androidx.compose.foundation.BorderStroke(
                        dimensions.borderWidthThin,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    ),
                    colors         = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(vertical = dimensions.spacingXSmall)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(dimensions.iconSmall))
                    Spacer(Modifier.width(dimensions.spacingXSmall))
                    Text(stringResource(Res.string.team_request_reject_action))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// History card — accepted / rejected / cancelled (read-only)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HistoryRequestCard(request: BenchRequestUi) {
    val dimensions = LocalDimensions.current

    val (statusLabel, statusColor) = when (request.status) {
        BenchRequestStatusUi.ACCEPTED  ->
            stringResource(Res.string.bench_request_status_accepted) to Color(0xFF22C55E)
        BenchRequestStatusUi.REJECTED  ->
            stringResource(Res.string.bench_request_status_rejected) to MaterialTheme.colorScheme.error
        BenchRequestStatusUi.CANCELLED ->
            stringResource(Res.string.bench_request_status_cancelled) to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        else ->
            stringResource(Res.string.bench_request_status_pending) to MaterialTheme.colorScheme.error
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = request.babyName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text  = request.createdAt?.take(10) ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                if (request.status == BenchRequestStatusUi.REJECTED && !request.rejectReason.isNullOrBlank()) {
                    Text(
                        text  = stringResource(Res.string.team_request_reject_reason_label, request.rejectReason),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = statusColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text       = statusLabel,
                    style      = MaterialTheme.typography.labelSmall,
                    color      = statusColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = dimensions.borderWidthMedium)
                )
            }
        }
    }
}
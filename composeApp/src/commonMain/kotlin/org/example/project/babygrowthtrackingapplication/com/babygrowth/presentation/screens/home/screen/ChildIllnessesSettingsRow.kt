package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChildIllnessesSettingsRow(
    illnessCount : Int,
    activeCount  : Int,
    babyName     : String,
    onClick      : () -> Unit
) {
    val dimensions = LocalDimensions.current
    val hasRecords = illnessCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = dimensions.devSettingsRowPaddingH,
                vertical   = dimensions.spacingMedium
            ),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        Box(
            modifier         = Modifier
                .size(dimensions.devSettingsIconBoxSize)
                .clip(RoundedCornerShape(dimensions.devSettingsIconCorner))
                .background(
                    if (hasRecords) Color(0xFF22C55E).copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = if (hasRecords)
                    stringResource(Res.string.child_illnesses_emoji_hospital)
                else
                    stringResource(Res.string.child_illnesses_emoji_caduceus),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = stringResource(Res.string.child_illnesses_title),
                style      = MaterialTheme.typography.bodyMedium,
                color      = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = when {
                    !hasRecords     -> stringResource(Res.string.child_illnesses_summary_empty_hint)
                    activeCount > 0 -> stringResource(
                        Res.string.child_illnesses_summary_active_total,
                        activeCount,
                        illnessCount
                    )
                    else            -> stringResource(
                        Res.string.child_illnesses_records_all_resolved,
                        illnessCount
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    !hasRecords     -> MaterialTheme.colorScheme.error
                    activeCount > 0 -> Color(0xFFFFA726)
                    else            -> Color(0xFF22C55E)
                }
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            null,
            tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(dimensions.devSettingsChevronSize)
        )
    }
}
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
import androidx.compose.ui.unit.dp
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions

/**
 * Row shown in Settings > Information section for Child Illnesses.
 * Shown below FamilyHistorySettingsRow.
 * Shows active illness count if records exist, or "No records" warning.
 */
@Composable
fun ChildIllnessesSettingsRow(
    illnessCount : Int,      // total number of illness records
    activeCount  : Int,      // number of active (unresolved) illnesses
    babyName     : String,
    onClick      : () -> Unit
) {
    val dimensions = LocalDimensions.current
    val hasRecords = illnessCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = dimensions.spacingMedium),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (hasRecords) Color(0xFF22C55E).copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = if (hasRecords) "🏥" else "⚕️",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Labels
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = "Child Illnesses",
                style      = MaterialTheme.typography.bodyMedium,
                color      = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = when {
                    !hasRecords -> "No illness records — tap to add"
                    activeCount > 0 -> "$activeCount active · $illnessCount total record(s)"
                    else -> "$illnessCount record(s) — all resolved"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    !hasRecords  -> MaterialTheme.colorScheme.error
                    activeCount > 0 -> Color(0xFFFFA726) // warning orange for active illnesses
                    else         -> Color(0xFF22C55E)    // green for all resolved
                }
            )
        }

        // Chevron
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(18.dp)
        )
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// ADD this composable to ChildDevSharedComponents.kt
// (or inline it into SettingsTabContent.kt as a private composable)
// ─────────────────────────────────────────────────────────────────────────────

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
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.jetbrains.compose.resources.stringResource

/**
 * Row shown in Settings > Information section for child development screens.
 * Displays progress: how many milestone months have been assessed.
 */
@Composable
fun ChildDevSettingsRowItem(
    emoji        : String,
    title        : String,
    assessedCount: Int,
    totalMonths  : Int = 5,
    onClick      : () -> Unit
) {
    val dimensions = LocalDimensions.current
    val hasData    = assessedCount > 0
    val complete   = assessedCount >= totalMonths

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
                    when {
                        complete -> Color(0xFF22C55E).copy(alpha = 0.12f)
                        hasData  -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else     -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, style = MaterialTheme.typography.bodyMedium)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.bodyMedium,
                color      = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = when {
                    !hasData -> stringResource(Res.string.child_dev_settings_not_started)
                    complete -> stringResource(Res.string.child_dev_settings_complete, totalMonths)
                    else     -> stringResource(Res.string.child_dev_settings_partial, assessedCount, totalMonths)
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    complete -> Color(0xFF22C55E)
                    hasData  -> MaterialTheme.colorScheme.primary
                    else     -> MaterialTheme.colorScheme.error
                }
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(18.dp)
        )
    }
}
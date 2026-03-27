package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
 * Row shown in Settings > Information section for Family History.
 * Shows "Information set" chip if data exists, or "Not set" warning.
 */
@Composable
fun FamilyHistorySettingsRow(
    isSet    : Boolean,
    babyName : String,
    onClick  : () -> Unit
) {
    val dimensions = LocalDimensions.current

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
            modifier         = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isSet) Color(0xFF22C55E).copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = if (isSet) "✅" else "⚠️",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Labels
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = stringResource(Res.string.settings_family_history),
                style      = MaterialTheme.typography.bodyMedium,
                color      = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text  = if (isSet)
                    stringResource(Res.string.settings_family_history_set)
                else
                    stringResource(Res.string.settings_family_history_not_set),
                style = MaterialTheme.typography.bodySmall,
                color = if (isSet)
                    Color(0xFF22C55E)
                else
                    MaterialTheme.colorScheme.error
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
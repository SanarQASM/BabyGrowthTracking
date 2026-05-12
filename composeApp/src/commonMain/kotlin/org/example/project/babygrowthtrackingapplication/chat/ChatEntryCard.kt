// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/chat/ChatEntryCard.kt
package org.example.project.babygrowthtrackingapplication.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.babygrowthtrackingapplication.theme.CustomColors
import org.example.project.babygrowthtrackingapplication.theme.Dimensions
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors

// ─────────────────────────────────────────────────────────────────────────────
// ChatEntryCard
//
// A self-contained, tap-to-open card placed inside the Feeding Guide,
// Sleep Guide, and any other tool screen.  It shows the group name and a
// short invitation prompt.
//
// Usage (inside any guide / tool screen):
//
//   ChatEntryCard(
//       onOpenChat = { /* navigate to ChatScreen */ }
//   )
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ChatEntryCard(
    onOpenChat : () -> Unit,
    modifier   : Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    ChatEntryCardContent(
        onOpenChat   = onOpenChat,
        dimensions   = dimensions,
        customColors = customColors,
        modifier     = modifier
    )
}

@Composable
private fun ChatEntryCardContent(
    onOpenChat   : () -> Unit,
    dimensions   : Dimensions,
    customColors : CustomColors,
    modifier     : Modifier = Modifier
) {
    Surface(
        modifier  = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.cardCornerRadius))
            .clickable(onClick = onOpenChat),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        color     = Color.Transparent,
        shadowElevation = dimensions.cardElevationSmall
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            customColors.accentGradientStart.copy(alpha = 0.13f),
                            customColors.accentGradientEnd.copy(alpha = 0.07f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensions.spacingMedium,
                        vertical   = dimensions.spacingMedium
                    ),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(dimensions.avatarMedium)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    customColors.accentGradientStart.copy(alpha = 0.85f),
                                    customColors.accentGradientEnd.copy(alpha = 0.80f)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = "👪",
                        fontSize = (dimensions.avatarMedium.value * 0.48f).sp
                    )
                }

                // Text block
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "Parents Community",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = "Share tips and questions with other parents",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
                    )
                }

                // Arrow / chat icon
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Open group chat",
                    tint               = customColors.accentGradientStart,
                    modifier           = Modifier.size(dimensions.iconMedium)
                )
            }
        }
    }
}
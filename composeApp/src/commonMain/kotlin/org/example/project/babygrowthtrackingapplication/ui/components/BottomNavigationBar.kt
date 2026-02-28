package org.example.project.babygrowthtrackingapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors

/**
 * Custom Bottom Navigation Bar with Telegram-style design
 *
 * Features:
 * ✅ Glassmorphism effect
 * ✅ Smooth animations
 * ✅ Background indicator for selected tab
 * ✅ Icon + Text layout
 * ✅ Gradient colors matching app theme
 */

enum class NavigationTab {
    HOME,
    BABY,
    CHARTS,
    HEALTH_RECORD,
    BENCH,       // ← NEW: Hospital / Bench tab
    SETTINGS
}

data class NavigationItem(
    val tab: NavigationTab,
    val icon: String, // Emoji icon
    val label: String
)

@Composable
fun BottomNavigationBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    // Navigation items — now 6 tabs
    val items = remember {
        listOf(
            NavigationItem(NavigationTab.HOME,          "🏠",  "Home"),
            NavigationItem(NavigationTab.BABY,          "👶",  "Baby"),
            NavigationItem(NavigationTab.CHARTS,        "📊",  "Charts"),
            NavigationItem(NavigationTab.HEALTH_RECORD, "⚕️", "Health"),
            NavigationItem(NavigationTab.BENCH,         "🏥",  "Bench"),   // ← NEW
            NavigationItem(NavigationTab.SETTINGS,      "⚙️", "Settings")
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shadowElevation = 8.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = item.tab == selectedTab

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background pill for selected item
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(width = 56.dp, height = 56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                customColors.accentGradientStart.copy(alpha = 0.15f),
                                                customColors.accentGradientEnd.copy(alpha = 0.15f)
                                            )
                                        )
                                    )
                            )
                        }

                        NavigationButton(
                            item = item,
                            isSelected = isSelected,
                            onClick = { onTabSelected(item.tab) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationButton(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.6f,
        animationSpec = tween(200),
        label = "alpha"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = item.icon,
            fontSize = (22 * scale).sp,
            modifier = Modifier.alpha(alpha)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) {
                customColors.accentGradientStart
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.alpha(alpha)
        )
    }
}
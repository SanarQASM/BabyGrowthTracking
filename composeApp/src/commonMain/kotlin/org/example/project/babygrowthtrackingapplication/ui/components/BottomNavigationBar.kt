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
import androidx.compose.ui.unit.sp
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*

/**
 * Custom Bottom Navigation Bar with Telegram-style design
 *
 * REFACTORED:
 *  - Removed hardcoded height = 80.dp      → dimensions.navBarHeight
 *  - Removed hardcoded padding(4.dp, 8.dp) → dimensions.navBarPaddingH / navBarPaddingV
 *  - Removed hardcoded size(56.dp, 56.dp)  → dimensions.navBarPillSize
 *  - Removed hardcoded RoundedCornerShape(14.dp) → dimensions.navBarPillCorner
 *  - Removed hardcoded .padding(2.dp)      → dimensions.navButtonPadding
 *  - Removed hardcoded Spacer(2.dp)        → dimensions.navIconLabelGap
 *  - Removed hardcoded fontSize = 10.sp    → dimensions.navBarLabelSize
 *  - Replaced hardcoded tab label strings  → stringResource(Res.string.nav_tab_*)
 */

enum class NavigationTab {
    HOME,
    BABY,
    CHARTS,
    HEALTH_RECORD,
    BENCH,
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
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    // Navigation items — strings from resources
    val items = listOf(
        NavigationItem(NavigationTab.HOME,          "🏠",  stringResource(Res.string.nav_tab_home)),
        NavigationItem(NavigationTab.BABY,          "👶",  stringResource(Res.string.nav_tab_baby)),
        NavigationItem(NavigationTab.CHARTS,        "📊",  stringResource(Res.string.nav_tab_charts)),
        NavigationItem(NavigationTab.HEALTH_RECORD, "⚕️", stringResource(Res.string.nav_tab_health)),
        NavigationItem(NavigationTab.BENCH,         "🏥",  stringResource(Res.string.nav_tab_bench)),
        NavigationItem(NavigationTab.SETTINGS,      "⚙️", stringResource(Res.string.nav_tab_settings)),
    )

    Surface(
        modifier        = modifier
            .fillMaxWidth()
            .height(dimensions.navBarHeight),   // WAS: 80.dp
        shadowElevation = dimensions.cardElevation + dimensions.cardElevation, // ≈ 8.dp
        color           = Color.Transparent
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
                    .padding(
                        horizontal = dimensions.navBarPaddingH,   // WAS: 4.dp
                        vertical   = dimensions.navBarPaddingV    // WAS: 8.dp
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = item.tab == selectedTab

                    Box(
                        modifier        = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background pill for selected item
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(
                                        width  = dimensions.navBarPillSize,  // WAS: 56.dp
                                        height = dimensions.navBarPillSize   // WAS: 56.dp
                                    )
                                    .clip(RoundedCornerShape(dimensions.navBarPillCorner)) // WAS: 14.dp
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
                            item       = item,
                            isSelected = isSelected,
                            onClick    = { onTabSelected(item.tab) },
                            modifier   = Modifier.fillMaxWidth()
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
    val dimensions   = LocalDimensions.current

    val scale by animateFloatAsState(
        targetValue  = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue   = if (isSelected) 1f else 0.6f,
        animationSpec = tween(200),
        label         = "alpha"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                onClick           = onClick,
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(dimensions.navButtonPadding),    // WAS: 2.dp
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text     = item.icon,
            fontSize = (dimensions.navBarHeight.value * 0.275f * scale).sp, // ≈ 22sp * scale
            modifier = Modifier.alpha(alpha)
        )

        Spacer(modifier = Modifier.height(dimensions.navIconLabelGap))    // WAS: 2.dp

        Text(
            text       = item.label,
            fontSize   = dimensions.navBarLabelSize,                      // WAS: 10.sp
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (isSelected) {
                customColors.accentGradientStart
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            },
            textAlign  = TextAlign.Center,
            maxLines   = 1,
            modifier   = Modifier.alpha(alpha)
        )
    }
}
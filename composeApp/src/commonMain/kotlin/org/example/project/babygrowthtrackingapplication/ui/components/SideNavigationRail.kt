package org.example.project.babygrowthtrackingapplication.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*

/**
 * Side Navigation Rail used in landscape mode and on EXPANDED (desktop/tablet)
 * layouts.  It mirrors the tab structure of [BottomNavigationBar] but renders
 * vertically on the left edge of the screen.
 *
 * Touch targets are all ≥ 48dp to satisfy the quality checklist.
 */
@Composable
fun SideNavigationRail(
    selectedTab  : NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier     : Modifier = Modifier,
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val items = listOf(
        NavigationItem(NavigationTab.HOME,          "🏠", stringResource(Res.string.nav_tab_home)),
        NavigationItem(NavigationTab.BABY,          "👶", stringResource(Res.string.nav_tab_baby)),
        NavigationItem(NavigationTab.CHARTS,        "📊", stringResource(Res.string.nav_tab_charts)),
        NavigationItem(NavigationTab.HEALTH_RECORD, "⚕️", stringResource(Res.string.nav_tab_health)),
        NavigationItem(NavigationTab.SETTINGS,      "⚙️", stringResource(Res.string.nav_tab_settings)),
    )

    Column(
        modifier = modifier
            .width(dimensions.railWidth)
            .fillMaxHeight()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
            )
            .padding(vertical = dimensions.spacingSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        // Small app-brand dot at the top of the rail
        Box(
            modifier = Modifier
                .size(dimensions.railIconSize)
                .clip(RoundedCornerShape(50))
                .background(customColors.accentGradientStart.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = "🍼",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(dimensions.spacingMedium))

        items.forEach { item ->
            RailItem(
                item         = item,
                selected     = item.tab == selectedTab,
                onTabSelected = onTabSelected,
                dimensions   = dimensions,
                customColors = customColors,
            )
        }
    }
}

@Composable
private fun RailItem(
    item         : NavigationItem,
    selected     : Boolean,
    onTabSelected: (NavigationTab) -> Unit,
    dimensions   : org.example.project.babygrowthtrackingapplication.theme.Dimensions,
    customColors : org.example.project.babygrowthtrackingapplication.theme.CustomColors,
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) customColors.accentGradientStart.copy(alpha = 0.15f)
        else         Color.Transparent,
        animationSpec = tween(200),
        label = "rail_item_bg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) customColors.accentGradientStart
        else         MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        animationSpec = tween(200),
        label = "rail_item_fg",
    )

    Column(
        modifier = Modifier
            .width(dimensions.railWidth)
            // Minimum 48dp touch target (quality checklist ✓)
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(
                indication           = null,
                interactionSource    = remember { MutableInteractionSource() },
                onClick              = { onTabSelected(item.tab) },
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text  = item.icon,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text       = item.label,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color      = contentColor,
            maxLines   = 1,
        )
    }
}
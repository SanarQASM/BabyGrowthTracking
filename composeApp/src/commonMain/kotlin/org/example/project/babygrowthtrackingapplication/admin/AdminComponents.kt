// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminComponents.kt

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// AdminStatCard  — dashboard KPI tile
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminStatCard(
    label    : String,
    value    : String,
    icon     : ImageVector,
    tintColor: Color,
    modifier : Modifier = Modifier,
    onClick  : (() -> Unit)? = null,
) {
    val dimensions = LocalDimensions.current
    Card(
        modifier  = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensions.iconLarge + dimensions.spacingSmall)
                        .clip(RoundedCornerShape(dimensions.spacingSmall))
                        .background(tintColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = tintColor,
                        modifier           = Modifier.size(dimensions.iconMedium)
                    )
                }
                if (onClick != null) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier           = Modifier.size(dimensions.iconSmall)
                    )
                }
            }
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            Text(
                text     = label,
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminSearchBar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminSearchBar(
    query        : String,
    onQueryChange: (String) -> Unit,
    hint         : String,
    modifier     : Modifier = Modifier,
) {
    val dimensions = LocalDimensions.current
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        placeholder   = {
            Text(
                hint,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingIcon   = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = stringResource(Res.string.admin_search_clear_cd),
                        tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        },
        singleLine = true,
        shape      = RoundedCornerShape(dimensions.buttonCornerRadius),
        colors     = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = MaterialTheme.customColors.accentGradientStart,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor     = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor   = MaterialTheme.colorScheme.onSurface,
            cursorColor          = MaterialTheme.customColors.accentGradientStart
        ),
        modifier = modifier.fillMaxWidth()
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminFilterTabs — scrollable chip-style tabs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun <T> AdminFilterTabs(
    tabs        : List<Pair<T, String>>,
    selectedTab : T,
    onTabSelect : (T) -> Unit,
    modifier    : Modifier = Modifier,
) {
    val dimensions = LocalDimensions.current
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        tabs.forEach { (tab, label) ->
            val selected = tab == selectedTab
            FilterChip(
                selected = selected,
                onClick  = { onTabSelect(tab) },
                label    = {
                    Text(
                        text     = label,
                        style    = MaterialTheme.typography.labelMedium,
                        color    = if (selected) MaterialTheme.colorScheme.onPrimary
                        else          MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.customColors.accentGradientStart,
                    containerColor         = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminConfirmDialog — generic delete/action confirmation dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminConfirmDialog(
    title        : String,
    message      : String,
    confirmLabel : String,
    onConfirm    : () -> Unit,
    onDismiss    : () -> Unit,
    confirmColor : Color = MaterialTheme.colorScheme.error,
) {
    val dimensions = LocalDimensions.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon             = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint               = confirmColor,
                modifier           = Modifier.size(dimensions.iconLarge)
            )
        },
        title = {
            Text(
                text       = title,
                fontWeight = FontWeight.Bold,
                style      = MaterialTheme.typography.titleMedium,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
        },
        text = {
            Text(
                text  = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) {
                Text(confirmLabel, color = MaterialTheme.colorScheme.onError)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(Res.string.admin_confirm_cancel))
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminEmptyState — shown when a list has no results
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminEmptyState(
    icon     : ImageVector = Icons.Default.SearchOff,
    message  : String,
    modifier : Modifier = Modifier,
) {
    val dimensions = LocalDimensions.current
    Column(
        modifier              = modifier
            .fillMaxWidth()
            .padding(vertical = dimensions.spacingXLarge),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier           = Modifier.size(dimensions.iconXLarge)
        )
        Text(
            text     = message,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminSectionHeader — small titled separator
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminSectionHeader(title: String, modifier: Modifier = Modifier) {
    val dimensions = LocalDimensions.current
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelLarge,
        color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.padding(
            horizontal = dimensions.screenPadding,
            vertical   = dimensions.spacingXSmall
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminStatusBadge — coloured pill badge for statuses
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminStatusBadge(label: String, color: Color) {
    val dimensions = LocalDimensions.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(dimensions.spacingMedium))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = dimensions.spacingSmall, vertical = dimensions.borderWidthThin),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = color,
            fontWeight = FontWeight.SemiBold,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
        )
    }
}
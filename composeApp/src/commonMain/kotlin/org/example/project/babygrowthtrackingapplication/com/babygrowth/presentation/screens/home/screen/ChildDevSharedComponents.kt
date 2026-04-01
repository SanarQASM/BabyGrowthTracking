package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// ChildDevTopBar — shared top app bar for both dev screens
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDevTopBar(
    title       : String,
    subtitle    : String,
    emoji       : String,
    onBack      : () -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Text(emoji, style = MaterialTheme.typography.titleMedium)
                Column {
                    Text(
                        text       = title,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = customColors.accentGradientStart
                    )
                    Text(
                        text  = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = customColors.accentGradientStart.copy(0.6f)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.common_back),
                    tint = customColors.accentGradientStart
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor             = customColors.accentGradientStart.copy(0.12f),
            titleContentColor          = customColors.accentGradientStart,
            navigationIconContentColor = customColors.accentGradientStart
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// ChildDevHeaderCard — informational header at the top of each screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ChildDevHeaderCard(
    title       : String,
    subtitle    : String,
    emoji       : String,
    customColors: CustomColors,
    dimensions  : Dimensions,
    modifier    : Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = customColors.accentGradientStart.copy(0.1f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(
                        customColors.accentGradientStart.copy(0.18f),
                        customColors.accentGradientEnd.copy(0.08f)
                    ))
                )
                .padding(dimensions.spacingMedium),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(dimensions.spacingMedium))
                    .background(customColors.accentGradientStart.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, style = MaterialTheme.typography.headlineSmall)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DevCheckItem — single milestone checkbox row
// Three-state: null (not assessed), true (achieved), false (not achieved)
// Tapping cycles: null → true → false → null
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DevCheckItem(
    label   : String,
    checked : Boolean?,
    onToggle: () -> Unit,
    cc      : CustomColors,
    d       : Dimensions,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = when (checked) {
            true  -> Color(0xFF22C55E).copy(0.12f)
            false -> MaterialTheme.colorScheme.error.copy(0.08f)
            null  -> MaterialTheme.colorScheme.surface.copy(0.15f)
        },
        animationSpec = tween(200),
        label = "dev_check_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = when (checked) {
            true  -> Color(0xFF22C55E).copy(0.5f)
            false -> MaterialTheme.colorScheme.error.copy(0.4f)
            null  -> MaterialTheme.colorScheme.onSurface.copy(0.15f)
        },
        animationSpec = tween(200),
        label = "dev_check_border"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(d.buttonCornerRadius - 4.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(d.buttonCornerRadius - 4.dp))
            .clickable { onToggle() }
            .padding(horizontal = d.spacingMedium, vertical = d.spacingSmall + 2.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(d.spacingMedium)
    ) {
        // State indicator circle
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when (checked) {
                        true  -> Color(0xFF22C55E)
                        false -> MaterialTheme.colorScheme.error
                        null  -> MaterialTheme.colorScheme.onSurface.copy(0.15f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when (checked) {
                true  -> Icon(Icons.Default.Check, null,
                    tint = Color.White, modifier = Modifier.size(16.dp))
                false -> Icon(Icons.Default.Close, null,
                    tint = Color.White, modifier = Modifier.size(16.dp))
                null  -> Box(
                    modifier = Modifier.size(8.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(0.4f))
                )
            }
        }

        // Label
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurface.copy(
                if (checked == null) 0.6f else 0.9f
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ChildDevSaveButton — shared Save + Cancel buttons
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ChildDevSaveButton(
    isSaving    : Boolean,
    onSave      : () -> Unit,
    onCancel    : () -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
        Button(
            onClick        = onSave,
            enabled        = !isSaving,
            modifier       = Modifier.fillMaxWidth().height(dimensions.buttonHeight),
            shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
            colors         = ButtonDefaults.buttonColors(
                containerColor         = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(
                            customColors.accentGradientStart.copy(0.85f),
                            customColors.accentGradientEnd.copy(0.70f)
                        )),
                        RoundedCornerShape(dimensions.buttonCornerRadius)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(customColors.glassOverlay,
                            RoundedCornerShape(dimensions.buttonCornerRadius))
                )
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(dimensions.iconMedium),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = dimensions.spacingXSmall / 2
                    )
                } else {
                    Text(
                        text       = stringResource(Res.string.child_dev_save),
                        style      = MaterialTheme.typography.titleMedium,
                        color      = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        OutlinedButton(
            onClick  = onCancel,
            enabled  = !isSaving,
            modifier = Modifier.fillMaxWidth().height(dimensions.buttonHeight),
            shape    = RoundedCornerShape(dimensions.buttonCornerRadius)
        ) {
            Text(
                text       = stringResource(Res.string.btn_cancel),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ChildDevSettingsRow — entry point from settings tab
// Shows progress as "N/5 months assessed" or "Not started"
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ChildDevSettingsRow(
    titleRes     : Int,       // string resource id
    emojiRes     : Int,       // string resource id
    assessedCount: Int,       // how many milestone months have data
    totalMonths  : Int = 5,
    babyName     : String,
    onClick      : () -> Unit
) {
    val dimensions = LocalDimensions.current
    val hasData    = assessedCount > 0
    val complete   = assessedCount == totalMonths

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = dimensions.spacingMedium),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when {
                        complete -> Color(0xFF22C55E).copy(0.12f)
                        hasData  -> MaterialTheme.colorScheme.primary.copy(0.12f)
                        else     -> MaterialTheme.colorScheme.errorContainer.copy(0.3f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = stringResource(Res.string.child_dev_vision_emoji), // reuse for now
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Column(Modifier.weight(1f)) {
            Text(
                text       = stringResource(Res.string.child_dev_vision_motor_title),
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
            Icons.Default.ChevronRight, null,
            tint     = MaterialTheme.colorScheme.onSurface.copy(0.3f),
            modifier = Modifier.size(18.dp)
        )
    }
}
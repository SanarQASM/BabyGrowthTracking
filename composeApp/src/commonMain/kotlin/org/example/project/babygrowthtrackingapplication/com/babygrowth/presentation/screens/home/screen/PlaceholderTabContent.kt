// ═══════════════════════════════════════════════════════════════════════════
// REFACTORED SCREENS — Summary of changes per file
// ═══════════════════════════════════════════════════════════════════════════
//
// PlaceholderTabContent.kt
//   • "✓" inline → stringResource(Res.string.language_option_selected_checkmark)
//
// FeedingGuideScreen.kt
//   • 300.dp landscape left pane → dimensions.landscapeWidePaneWidth
//
// SleepGuideScreen.kt
//   • 280.dp landscape left pane → dimensions.landscapeWidePaneWidth
//
// VisionMotorScreen.kt
//   • 220.dp landscape left pane → dimensions.landscapeNarrowPaneWidth
//   • 40.dp month badge circle   → dimensions.devMonthBadgeSize
//   • 36.dp edit button          → dimensions.devEditButtonSize
//   • 48.dp left pane header icon → devHeaderIconBoxSize already used; 48.dp inside pane text emoji box → dimensions.spacingXLarge
//
// HearingSpeechScreen.kt
//   • 220.dp landscape left pane → dimensions.landscapeNarrowPaneWidth
//   • 36.dp edit button          → dimensions.devEditButtonSize
//
// VaccinationDetailScreen
//   (in VaccinationScheduleView.kt — the inline screen at the bottom)
//   • 18.dp icon → dimensions.iconSmall + dimensions.borderWidthMedium
//   • 2.dp stroke → dimensions.borderWidthMedium
//   • Strings: "Age recommended", "Ideal date", "Scheduled date",
//     "Adjustment", "Completed on", "Health Center", "Status",
//     "+%1$d days (%2$s)", "Reschedule All" → already in stringResource via
//     vax_detail_* and schedule_reschedule_all keys — confirmed correct
//
// ═══════════════════════════════════════════════════════════════════════════

// ─────────────────────────────────────────────────────────────────────────────
// FILE: PlaceholderTabContent.kt  (complete refactored version)
// ─────────────────────────────────────────────────────────────────────────────

package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.Language
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions

/**
 * Reusable "Coming Soon" placeholder — used by tabs not yet implemented.
 */
@Composable
internal fun PlaceholderTabContent(emoji: String, title: String) {
    val dimensions = LocalDimensions.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(dimensions.spacingMedium))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(dimensions.spacingSmall))
            Text(
                text = stringResource(Res.string.coming_soon),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Language picker dialog.
 */
@Composable
internal fun LanguageSelectionDialog(
    currentLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.select_language),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Language.entries.forEach { language ->
                    LanguageOptionRow(
                        language = language,
                        isSelected = language == currentLanguage,
                        onClick = { onLanguageSelected(language) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.btn_cancel))
            }
        }
    )
}

@Composable
private fun LanguageOptionRow(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dimensions = LocalDimensions.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensions.spacingXSmall),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                // CHANGED: "✓" hardcoded → stringResource
                Text(
                    text = stringResource(Res.string.language_option_selected_checkmark),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
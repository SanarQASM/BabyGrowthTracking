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
 * Internal to the home package so each tab file can call it directly.
 *
 * REFACTORED:
 *  - Replaced `fontSize = 64.sp` → MaterialTheme.typography.displayMedium
 *  - Replaced `Spacer height = 16.dp` → dimensions.spacingMedium
 *  - Replaced `Spacer height = 8.dp` → dimensions.spacingSmall
 */
@Composable
internal fun PlaceholderTabContent(emoji: String, title: String) {
    // ADDED: pull responsive dimensions from the composition local
    val dimensions = LocalDimensions.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // WAS: fontSize = 64.sp  →  Now uses typography scale (displayMedium = 45sp)
            Text(text = emoji, style = MaterialTheme.typography.displayMedium)

            // WAS: Modifier.height(16.dp)  →  dimensions.spacingMedium (16dp compact, 20dp medium, 24dp expanded)
            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // WAS: Modifier.height(8.dp)  →  dimensions.spacingSmall (8dp compact, 12dp medium, 16dp expanded)
            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            Text(
                text  = stringResource(Res.string.coming_soon),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Language picker dialog — kept here so HomeScreen.kt stays clean.
 *
 * REFACTORED:
 *  - Replaced `padding(vertical = 4.dp)` → dimensions.spacingXSmall
 *  - Replaced `padding(16.dp)` → dimensions.spacingMedium
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
                        language   = language,
                        isSelected = language == currentLanguage,
                        onClick    = { onLanguageSelected(language) }
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
    // ADDED: pull responsive dimensions
    val dimensions = LocalDimensions.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // WAS: padding(vertical = 4.dp)  →  dimensions.spacingXSmall
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
                // WAS: padding(16.dp)  →  dimensions.spacingMedium
                .padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = language.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Text(
                    text  = "✓",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
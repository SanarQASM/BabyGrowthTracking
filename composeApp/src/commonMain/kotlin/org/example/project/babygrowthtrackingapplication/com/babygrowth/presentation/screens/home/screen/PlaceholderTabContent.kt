package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.Language

/**
 * Reusable "Coming Soon" placeholder — used by tabs not yet implemented.
 * Internal to the home package so each tab file can call it directly.
 */
@Composable
internal fun PlaceholderTabContent(emoji: String, title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                .padding(16.dp),
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
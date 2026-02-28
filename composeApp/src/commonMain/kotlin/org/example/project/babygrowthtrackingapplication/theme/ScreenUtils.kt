package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * Common interface for getting screen dimensions
 * Platform-specific implementations in androidMain, iosMain, etc.
 */
@Composable
expect fun getScreenWidth(): Dp

@Composable
expect fun getScreenHeight(): Dp


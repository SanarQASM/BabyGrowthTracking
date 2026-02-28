package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.browser.window

@Composable
actual fun getScreenWidth(): Dp = window.innerWidth.dp

@Composable
actual fun getScreenHeight(): Dp = window.innerHeight.dp
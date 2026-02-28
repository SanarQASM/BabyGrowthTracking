package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import platform.UIKit.UIScreen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenWidth(): Dp {
    val screenWidth = UIScreen.mainScreen.bounds.size.width
    val scale = UIScreen.mainScreen.scale
    return (screenWidth / scale).dp
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenHeight(): Dp {
    val screenHeight = UIScreen.mainScreen.bounds.size.height
    val scale = UIScreen.mainScreen.scale
    return (screenHeight / scale).dp
}
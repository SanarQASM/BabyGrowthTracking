package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import java.awt.Toolkit

@Composable
actual fun getScreenWidth(): Dp {
    val density = LocalDensity.current
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    return with(density) { screenSize.width.toDp() }
}

@Composable
actual fun getScreenHeight(): Dp {
    val density = LocalDensity.current
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    return with(density) { screenSize.height.toDp() }
}
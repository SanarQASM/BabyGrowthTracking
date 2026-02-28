package org.example.project.babygrowthtrackingapplication

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        App()
    }
}
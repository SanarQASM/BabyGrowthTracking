package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import androidx.compose.runtime.Composable
@Composable
actual fun rememberCurrentLocale(language: Language): String {
    // createWebCurrentLocale is defined in your existing wasmJsMain / jsMain
    // source set — this delegates to it correctly now instead of ignoring it.
    return createWebCurrentLocale(language)
}
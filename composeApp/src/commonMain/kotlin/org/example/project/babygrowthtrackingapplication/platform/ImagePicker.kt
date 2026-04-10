package org.example.project.babygrowthtrackingapplication.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.example.project.babygrowthtrackingapplication.theme.Dimensions

// ─────────────────────────────────────────────────────────────────────────────
// Image Picker Button — expect/actual pattern via platform
// ─────────────────────────────────────────────────────────────────────────────

@Composable
expect fun ImagePickerButton(
    remaining  : Int,
    dimensions : Dimensions,
    onPicked   : (List<ByteArray>) -> Unit
)

// ─────────────────────────────────────────────────────────────────────────────
// ByteArrayImage — renders ByteArray as an image using platform painter
// ─────────────────────────────────────────────────────────────────────────────

@Composable
expect fun ByteArrayImage(
    bytes             : ByteArray,
    modifier          : Modifier     = Modifier,
    contentScale      : ContentScale = ContentScale.Crop,
    contentDescription: String       = ""
)
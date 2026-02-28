// File: composeApp/src/commonMain/.../platform/ImagePickerHelper.kt

package org.example.project.babygrowthtrackingapplication.platform

import androidx.compose.runtime.Composable

expect class ImagePickerLauncher {
    fun launch()
}

@Composable
expect fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray) -> Unit
): ImagePickerLauncher
// File: composeApp/src/iosMain/.../platform/ImagePickerHelper.ios.kt

package org.example.project.babygrowthtrackingapplication.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class ImagePickerLauncher(private val launch: () -> Unit) {
    actual fun launch() = launch.invoke()
}

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray) -> Unit
): ImagePickerLauncher {
    // TODO: integrate PHPickerViewController via UIKitView when iOS is fully targeted
    return remember { ImagePickerLauncher {} }
}
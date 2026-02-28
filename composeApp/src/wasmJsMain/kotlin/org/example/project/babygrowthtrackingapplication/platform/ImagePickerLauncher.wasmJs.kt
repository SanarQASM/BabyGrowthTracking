// File: composeApp/src/wasmJsMain/.../platform/ImagePickerHelper.wasmJs.kt

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
    // TODO: WasmJs does not yet support the full browser DOM APIs that jsMain uses.
    // Implement once Kotlin/Wasm DOM interop matures.
    return remember { ImagePickerLauncher {} }
}

actual class ImageUploadService actual constructor() {
    actual suspend fun uploadBabyPhoto(bytes: ByteArray): String? {
        TODO("Not yet implemented")
    }
}
// File: composeApp/src/desktopMain/.../platform/ImagePickerHelper.desktop.kt

package org.example.project.babygrowthtrackingapplication.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class ImagePickerLauncher(private val launch: () -> Unit) {
    actual fun launch() = launch.invoke()
}

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray) -> Unit
): ImagePickerLauncher {
    return remember {
        ImagePickerLauncher {
            // Desktop: open a native Swing file chooser on a background thread
            Thread {
                val chooser = JFileChooser().apply {
                    dialogTitle   = "Select Baby Photo"
                    fileSelectionMode = JFileChooser.FILES_ONLY
                    isAcceptAllFileFilterUsed = false
                    addChoosableFileFilter(
                        FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "webp")
                    )
                }
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    val bytes = chooser.selectedFile.readBytes()
                    onImagePicked(bytes)
                }
            }.start()
        }
    }
}

actual class ImageUploadService actual constructor() {
    actual suspend fun uploadBabyPhoto(bytes: ByteArray): String? {
        TODO("Not yet implemented")
    }
}
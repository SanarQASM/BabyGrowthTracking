// File: composeApp/src/androidMain/.../platform/ImagePickerHelper.android.kt

package org.example.project.babygrowthtrackingapplication.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

actual class ImagePickerLauncher(private val launch: () -> Unit) {
    actual fun launch() = launch.invoke()
}

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray) -> Unit
): ImagePickerLauncher {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { resolvedUri ->
            val bytes = resolvedUri.toCompressedJpegBytes(context)
            if (bytes != null) onImagePicked(bytes)
        }
    }

    return remember { ImagePickerLauncher { launcher.launch("image/*") } }
}

private fun Uri.toCompressedJpegBytes(context: Context, maxSizePx: Int = 1024): ByteArray? =
    try {
        val inputStream = context.contentResolver.openInputStream(this) ?: return null
        val original    = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val scaled = if (original.width > maxSizePx || original.height > maxSizePx) {
            val ratio = maxSizePx.toFloat() / maxOf(original.width, original.height)
            original.scale((original.width * ratio).toInt(), (original.height * ratio).toInt())
        } else {
            original
        }

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        out.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
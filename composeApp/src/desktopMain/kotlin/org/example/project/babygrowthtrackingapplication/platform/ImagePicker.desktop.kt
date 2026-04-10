package org.example.project.babygrowthtrackingapplication.platform

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.memory_add_images
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.skia.Image as SkiaImage
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter

// ─────────────────────────────────────────────────────────────────────────────
// Desktop — ImagePickerButton
// Opens an AWT FileDialog (native OS file picker) to select image files.
// Runs file I/O on Dispatchers.IO then delivers bytes on Dispatchers.Main.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
actual fun ImagePickerButton(
    remaining  : Int,
    dimensions : Dimensions,
    onPicked   : (List<ByteArray>) -> Unit
) {
    val customColors = MaterialTheme.customColors
    val scope        = rememberCoroutineScope()

    OutlinedButton(
        onClick = {
            scope.launch(Dispatchers.IO) {
                val dialog = FileDialog(null as Frame?, "Select Images", FileDialog.LOAD).apply {
                    isMultipleMode = true
                    filenameFilter = FilenameFilter { _, name ->
                        name.lowercase().let {
                            it.endsWith(".jpg") || it.endsWith(".jpeg") ||
                                    it.endsWith(".png") || it.endsWith(".webp") ||
                                    it.endsWith(".bmp")
                        }
                    }
                    isVisible = true   // blocks until dialog is closed
                }
                val bytesList = (dialog.files ?: emptyArray())
                    .take(remaining)
                    .mapNotNull { file ->
                        try { file.readBytes() } catch (e: Exception) { null }
                    }
                withContext(Dispatchers.Main) {
                    if (bytesList.isNotEmpty()) onPicked(bytesList)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
        colors   = ButtonDefaults.outlinedButtonColors(
            contentColor = customColors.accentGradientStart
        )
    ) {
        Icon(
            Icons.Default.AddPhotoAlternate,
            contentDescription = null,
            modifier = Modifier.size(dimensions.iconMedium)
        )
        Spacer(Modifier.width(dimensions.spacingSmall))
        Text(
            stringResource(Res.string.memory_add_images),
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Desktop — ByteArrayImage
// Decodes bytes with Skia directly — no AWT/ImageIO needed.
// SkiaImage.makeFromEncoded() handles JPEG, PNG, WebP, BMP, GIF.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
actual fun ByteArrayImage(
    bytes             : ByteArray,
    modifier          : Modifier,
    contentScale      : ContentScale,
    contentDescription: String
) {
    val imageBitmap = remember(bytes) {
        try {
            SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    if (imageBitmap != null) {
        Image(
            bitmap             = imageBitmap,
            contentDescription = contentDescription,
            modifier           = modifier,
            contentScale       = contentScale
        )
    }
}
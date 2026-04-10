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
import kotlinx.browser.document
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.memory_add_images
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.skia.Image as SkiaImage
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// ─────────────────────────────────────────────────────────────────────────────
// Web JS — ImagePickerButton
// Creates a hidden <input type="file"> element and triggers a click on it.
// Reads selected files as ArrayBuffer via FileReader → ByteArray.
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
            scope.launch {
                val bytesList = pickImagesFromBrowser(remaining)
                if (bytesList.isNotEmpty()) onPicked(bytesList)
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
// Browser file picker via hidden <input>
// ─────────────────────────────────────────────────────────────────────────────

private suspend fun pickImagesFromBrowser(maxFiles: Int): List<ByteArray> =
    suspendCoroutine { cont ->
        val input = (document.createElement("input") as HTMLInputElement).apply {
            type     = "file"
            accept   = "image/*"
            multiple = true
        }

        input.onchange = { _ ->
            val files = input.files

            if (files == null || files.length == 0) {
                cont.resume(emptyList())
            } else {
                val count = minOf(files.length, maxFiles)
                val results  = mutableListOf<ByteArray>()
                var finished = 0

                for (i in 0 until count) {
                    // Use .item(i) for FileList access
                    val file   = files.item(i) ?: continue
                    val reader = FileReader()

                    reader.onload  = { event ->
                        val arrayBuffer = (event.target.asDynamic().result) as? org.khronos.webgl.ArrayBuffer
                        if (arrayBuffer != null) {
                            // FIX: In Kotlin/JS, ByteArray maps directly to Int8Array.
                            // Using unsafeCast provides a zero-copy conversion and avoids the 
                            // resolution conflict between FileList.get and Int8Array access.
                            val bytes = org.khronos.webgl.Int8Array(arrayBuffer).unsafeCast<ByteArray>()
                            results.add(bytes)
                        }
                        finished++
                        if (finished == count) cont.resume(results)
                        null
                    }

                    reader.onerror = { _ ->
                        finished++
                        if (finished == count) cont.resume(results)
                        null
                    }

                    reader.readAsArrayBuffer(file)
                }
            }
            null
        }
        input.click()
    }

// ─────────────────────────────────────────────────────────────────────────────
// Web JS — ByteArrayImage
// Decodes via Skia (available in Compose for Web/Wasm targets).
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

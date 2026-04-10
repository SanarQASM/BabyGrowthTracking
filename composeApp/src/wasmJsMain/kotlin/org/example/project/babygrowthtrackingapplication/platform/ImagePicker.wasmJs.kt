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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.memory_add_images
import org.jetbrains.skia.Image as SkiaImage
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// ─────────────────────────────────────────────────────────────────────────────
// Web Wasm JS — ImagePickerButton
//
// Wasm JS cannot use kotlinx.browser directly for file picking, so we
// delegate entirely to a JS helper that creates and clicks a hidden
// <input type="file"> and returns the selected files as Base64 strings
// (strings cross the Wasm ↔ JS boundary cleanly).
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
                val bytesList = pickImagesWasm(remaining)
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
// File picker via JS interop
// The JS side reads each file as Base64 and resolves a Promise.
// We await that Promise from Kotlin/Wasm via suspendCoroutine.
// ─────────────────────────────────────────────────────────────────────────────

private suspend fun pickImagesWasm(maxFiles: Int): List<ByteArray> =
    suspendCoroutine { cont ->
        jsPickImages(maxFiles) { base64Array ->
            val bytesList = base64Array
                .toList()
                .mapNotNull { b64 ->
                    try {
                        @OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
                        kotlin.io.encoding.Base64.decode(b64)
                    } catch (e: Exception) { null }
                }
            cont.resume(bytesList)
        }
    }

// ─────────────────────────────────────────────────────────────────────────────
// JS interop — file picker + FileReader → Base64 results
// ─────────────────────────────────────────────────────────────────────────────

@JsFun("""(maxFiles, callback) => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.multiple = true;
    input.onchange = () => {
        const files = Array.from(input.files || []).slice(0, maxFiles);
        if (files.length === 0) { callback([]); return; }
        const results = [];
        let done = 0;
        files.forEach(file => {
            const reader = new FileReader();
            reader.onload = e => {
                // result is "data:image/...;base64,<data>" — strip the prefix
                const b64 = e.target.result.split(',')[1];
                results.push(b64);
                if (++done === files.length) callback(results);
            };
            reader.onerror = () => {
                if (++done === files.length) callback(results);
            };
            reader.readAsDataURL(file);
        });
    };
    input.click();
}""")
private external fun jsPickImages(maxFiles: Int, callback: (Array<String>) -> Unit)

// ─────────────────────────────────────────────────────────────────────────────
// Web Wasm JS — ByteArrayImage
// Uses Skia (bundled with Compose for Web/Wasm) — identical to JS target.
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
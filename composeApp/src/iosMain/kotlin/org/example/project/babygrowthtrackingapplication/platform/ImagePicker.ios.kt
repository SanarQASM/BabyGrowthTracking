// composeApp/src/iosMain/.../platform/ImagePicker.ios.kt
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
import kotlinx.cinterop.*
import org.example.project.babygrowthtrackingapplication.theme.Dimensions
import platform.UIKit.*
import platform.CoreGraphics.*
import platform.Foundation.*
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.memory_add_images
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.skia.Image as SkiaImage
import platform.darwin.NSObject

@Composable
actual fun ImagePickerButton(
    remaining  : Int,
    dimensions : Dimensions,
    onPicked   : (List<ByteArray>) -> Unit
) {
    val customColors = MaterialTheme.customColors
    var showPicker by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick  = { showPicker = true },
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

    if (showPicker) {
        IOSImagePicker(
            onPicked  = { bytesList ->
                showPicker = false
                val limited = bytesList.take(remaining)
                if (limited.isNotEmpty()) onPicked(limited)
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun IOSImagePicker(
    onPicked  : (List<ByteArray>) -> Unit,
    onDismiss : () -> Unit
) {
    DisposableEffect(Unit) {
        val picker = UIImagePickerController()
        picker.sourceType =
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        picker.allowsEditing = false

        val delegate = object : NSObject(),
            UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {

            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image = didFinishPickingMediaWithInfo[
                    UIImagePickerControllerOriginalImage
                ] as? UIImage

                // Use UIImageJPEGRepresentation for reliable JPEG bytes
                val bytes = image?.let {
                    UIImageJPEGRepresentation(it, 0.85)?.toByteArray()
                }
                picker.dismissViewControllerAnimated(true) {
                    onPicked(listOfNotNull(bytes))
                }
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true) { onDismiss() }
            }
        }

        picker.delegate = delegate
        val rootVc = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootVc?.presentViewController(picker, animated = true, completion = null)

        onDispose { }
    }
}

// ── ByteArrayImage — iOS
// Uses Skia (bundled with Compose Multiplatform) for reliable decoding.
// This replaces the broken CGContext-based approach that returned empty bitmaps.
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

// ─── Helpers ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val result = ByteArray(this.length.toInt())
    result.usePinned { pinned ->
        platform.posix.memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return result
}
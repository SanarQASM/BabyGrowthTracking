package org.example.project.babygrowthtrackingapplication.platform

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.ImageBitmap
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
import platform.darwin.NSObject

// ─────────────────────────────────────────────────────────────────────────────
// iOS — ImagePickerButton
// Presents UIImagePickerController via a UIViewController wrapper.
// ─────────────────────────────────────────────────────────────────────────────

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

// ─────────────────────────────────────────────────────────────────────────────
// iOS UIImagePickerController wrapper via Compose interop
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IOSImagePicker(
    onPicked  : (List<ByteArray>) -> Unit,
    onDismiss : () -> Unit
) {
    // Use UIViewControllerRepresentable equivalent via ComposeUIViewController
    // The picker delegate converts UIImage → JPEG bytes and returns them.
    DisposableEffect(Unit) {
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        picker.allowsEditing = false

        val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {

            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image = didFinishPickingMediaWithInfo[
                    UIImagePickerControllerOriginalImage
                ] as? UIImage
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

        onDispose {
            // Picker dismisses itself via delegate callbacks
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// iOS — ByteArrayImage
// Decodes via UIImage → CGImage → ImageBitmap
// ─────────────────────────────────────────────────────────────────────────────

@Composable
actual fun ByteArrayImage(
    bytes             : ByteArray,
    modifier          : Modifier,
    contentScale      : ContentScale,
    contentDescription: String
) {
    val imageBitmap = remember(bytes) { bytes.toImageBitmap() }
    if (imageBitmap != null) {
        Image(
            bitmap             = imageBitmap,
            contentDescription = contentDescription,
            modifier           = modifier,
            contentScale       = contentScale
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun ByteArray.toImageBitmap(): ImageBitmap? {
    val nsData = this.toNSData()
    val uiImage = UIImage(data = nsData) ?: return null
    return uiImage.toImageBitmap()
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toImageBitmap(): ImageBitmap? {
    val cgImage = this.CGImage ?: return null
    val width   = CGImageGetWidth(cgImage).toInt()
    val height  = CGImageGetHeight(cgImage).toInt()
    val pixels  = IntArray(width * height)

    pixels.usePinned { pinned ->
        val ctx = CGBitmapContextCreate(
            data             = pinned.addressOf(0),
            width            = width.toULong(),
            height           = height.toULong(),
            bitsPerComponent = 8u,
            bytesPerRow      = (width * 4).toULong(),
            space            = CGColorSpaceCreateDeviceRGB(),
            bitmapInfo       = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
        )
        CGContextDrawImage(ctx, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), cgImage)
        CGContextRelease(ctx)
    }

    return ImageBitmap(width, height).also { bmp ->
        bmp.prepareToDraw()
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData =
    NSData.create(bytes = this.refTo(0) as COpaquePointer?, length = this.size.toULong())

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val result = ByteArray(this.length.toInt())
    result.usePinned { pinned ->
        platform.posix.memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return result
}
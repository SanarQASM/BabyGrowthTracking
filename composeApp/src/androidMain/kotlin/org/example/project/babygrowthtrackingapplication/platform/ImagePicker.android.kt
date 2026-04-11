// composeApp/src/androidMain/.../platform/ImagePicker.android.kt
package org.example.project.babygrowthtrackingapplication.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import org.example.project.babygrowthtrackingapplication.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.memory_add_images
import org.example.project.babygrowthtrackingapplication.theme.customColors
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

@Composable
actual fun ImagePickerButton(
    remaining  : Int,
    dimensions : Dimensions,
    onPicked   : (List<ByteArray>) -> Unit
) {
    val context      = LocalContext.current
    val customColors = MaterialTheme.customColors

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val bytesList = uris.take(remaining).mapNotNull { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val original = BitmapFactory.decodeStream(stream) ?: return@use null
                    // Compress + resize to max 1024px to keep storage manageable
                    val maxPx = 1024
                    val scaled = if (original.width > maxPx || original.height > maxPx) {
                        val ratio = maxPx.toFloat() / maxOf(original.width, original.height)
                        original.scale(
                            (original.width  * ratio).toInt(),
                            (original.height * ratio).toInt()
                        )
                    } else original
                    val out = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    out.toByteArray()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        if (bytesList.isNotEmpty()) onPicked(bytesList)
    }

    OutlinedButton(
        onClick  = { launcher.launch("image/*") },
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

@Composable
actual fun ByteArrayImage(
    bytes             : ByteArray,
    modifier          : Modifier,
    contentScale      : ContentScale,
    contentDescription: String
) {
    val bitmap = remember(bytes) {
        try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    if (bitmap != null) {
        Image(
            bitmap             = bitmap,
            contentDescription = contentDescription,
            modifier           = modifier,
            contentScale       = contentScale
        )
    }
}
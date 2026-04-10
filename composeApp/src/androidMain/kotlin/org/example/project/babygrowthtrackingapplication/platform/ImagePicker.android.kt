package org.example.project.babygrowthtrackingapplication.platform

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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import org.example.project.babygrowthtrackingapplication.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.memory_add_images
import org.example.project.babygrowthtrackingapplication.theme.customColors

// ─────────────────────────────────────────────────────────────────────────────
// Android — ImagePickerButton
// Uses ActivityResultContracts.GetMultipleContents to let the user pick
// up to [remaining] images from the device gallery.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
actual fun ImagePickerButton(
    remaining  : Int,
    dimensions : Dimensions,
    onPicked   : (List<ByteArray>) -> Unit
) {
    val context = LocalContext.current
    val customColors = MaterialTheme.customColors

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val bytesList = uris.take(remaining).mapNotNull { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } catch (e: Exception) {
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

// ─────────────────────────────────────────────────────────────────────────────
// Android — ByteArrayImage
// Decodes the byte array to a Bitmap via BitmapFactory and renders it.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
actual fun ByteArrayImage(
    bytes             : ByteArray,
    modifier          : Modifier,
    contentScale      : ContentScale,
    contentDescription: String
) {
    val bitmap = remember(bytes) {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
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
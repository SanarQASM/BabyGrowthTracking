// File: composeApp/src/androidMain/.../platform/ImageUploadService.android.kt

package org.example.project.babygrowthtrackingapplication.platform

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

actual class ImageUploadService actual constructor() {

    actual suspend fun uploadBabyPhoto(bytes: ByteArray): String? = try {
        val fileName   = "babies/${System.currentTimeMillis()}.jpg"
        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child(fileName)

        storageRef.putBytes(bytes).await()
        storageRef.downloadUrl.await().toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
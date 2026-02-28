// File: composeApp/src/commonMain/.../platform/ImageUploadService.kt

package org.example.project.babygrowthtrackingapplication.platform

/**
 * Platform-agnostic image upload service.
 * Android actual uses Firebase Storage.
 * All other platforms use a no-op stub until implemented.
 */
expect class ImageUploadService() {
    /**
     * Uploads [bytes] (JPEG) to remote storage.
     * Returns the public download URL, or null on failure.
     */
    suspend fun uploadBabyPhoto(bytes: ByteArray): String?
}
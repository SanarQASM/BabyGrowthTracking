package org.example.project.babygrowthtrackingapplication.platform

// ─────────────────────────────────────────────────────────────────────────────
// MemoryLocalStorage — expect declaration
//
// Each platform provides an `actual` implementation that saves/loads
// binary image data from local device storage.
// ─────────────────────────────────────────────────────────────────────────────

expect class MemoryLocalStorage() {

    /**
     * Save image bytes to local storage under a unique key.
     * @param key  Unique identifier (e.g. "memory_{memoryId}_{index}")
     * @param bytes Raw image bytes (JPEG/PNG)
     * @return true if saved successfully
     */
    suspend fun saveImage(key: String, bytes: ByteArray): Boolean

    /**
     * Load image bytes from local storage by key.
     * @return image bytes or null if not found / unavailable
     */
    suspend fun loadImage(key: String): ByteArray?

    /**
     * Delete a single image by key.
     */
    suspend fun deleteImage(key: String): Boolean

    /**
     * Delete all images whose key starts with [prefix].
     * Used to delete all images of a memory when the memory is deleted.
     */
    suspend fun deleteImagesByPrefix(prefix: String): Boolean

    /**
     * Check whether an image exists in local storage.
     */
    suspend fun imageExists(key: String): Boolean

    /**
     * List all image keys that start with [prefix].
     */
    suspend fun listKeys(prefix: String): List<String>
}
package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.*
import org.example.project.babygrowthtrackingapplication.platform.MemoryLocalStorage
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// ─────────────────────────────────────────────────────────────────────────────
// MemoryUiImage — a single image in a memory, with local availability status
// ─────────────────────────────────────────────────────────────────────────────

data class MemoryUiImage(
    val index       : Int,
    val localKey    : String,   // "memory_{memoryId}_{index}"
    val caption     : String?   = null,
    val bytes       : ByteArray? = null,   // null = not available locally
    val isAvailable : Boolean   = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MemoryUiImage) return false
        return index == other.index && localKey == other.localKey
    }
    override fun hashCode() = localKey.hashCode()
}

// ─────────────────────────────────────────────────────────────────────────────
// MemoryUiItem — a memory with its locally resolved images
// ─────────────────────────────────────────────────────────────────────────────

data class MemoryUiItem(
    val memoryId    : String,
    val babyId      : String,
    val babyName    : String,
    val title       : String,
    val description : String?   = null,
    val memoryDate  : String,   // "YYYY-MM-DD"
    val ageInMonths : Int?      = null,
    val ageInDays   : Int?      = null,
    val images      : List<MemoryUiImage> = emptyList(),
    val hasMissingImages: Boolean = false
)

// ─────────────────────────────────────────────────────────────────────────────
// Add-Memory Form State
// ─────────────────────────────────────────────────────────────────────────────

data class AddMemoryFormState(
    val title            : String              = "",
    val description      : String              = "",
    val memoryDate       : String              = "",
    val selectedBytes    : List<ByteArray>     = emptyList(),   // max 10
    val captions         : List<String>        = emptyList(),
    val isLoading        : Boolean             = false,
    val errorMessage     : String?             = null,
    val successMessage   : String?             = null,
    val isSaved          : Boolean             = false,
    val titleError       : String?             = null,
    val dateError        : String?             = null
) {
    val imageCount: Int get() = selectedBytes.size
    val canAddMore: Boolean get() = imageCount < MAX_IMAGES

    companion object {
        const val MAX_IMAGES = 10
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MemoryUiState
// ─────────────────────────────────────────────────────────────────────────────

data class MemoryUiState(
    val isLoading           : Boolean              = true,
    val memories            : List<MemoryUiItem>   = emptyList(),
    val selectedBabyId      : String?              = null,
    val babies              : List<BabyResponse>   = emptyList(),
    val errorMessage        : String?              = null,
    val actionMessage       : String?              = null,
    // View image full-screen
    val viewingImage        : MemoryUiImage?       = null,
    val viewingMemoryTitle  : String               = "",
    // Form
    val showAddForm         : Boolean              = false,
    val addForm             : AddMemoryFormState   = AddMemoryFormState(),
    // Delete confirm
    val deletingMemoryId    : String?              = null
) {
    val selectedBabyName: String
        get() = babies.find { it.babyId == selectedBabyId }?.fullName ?: ""

    val filteredMemories: List<MemoryUiItem>
        get() = if (selectedBabyId == null) memories
        else memories.filter { it.babyId == selectedBabyId }

    val hasAnyMissingImages: Boolean
        get() = filteredMemories.any { it.hasMissingImages }
}

// ─────────────────────────────────────────────────────────────────────────────
// MemoryViewModel
// ─────────────────────────────────────────────────────────────────────────────

class MemoryViewModel(
    private val apiService         : ApiService,
    private val preferencesManager : PreferencesManager,
    private val localStore         : MemoryLocalStorage = MemoryLocalStorage()
) {
    var uiState by mutableStateOf(MemoryUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ── Load ──────────────────────────────────────────────────────────────────

    fun load(babies: List<BabyResponse>, selectedBabyId: String? = null) {
        uiState = uiState.copy(
            babies       = babies,
            selectedBabyId = selectedBabyId ?: babies.firstOrNull()?.babyId,
            isLoading    = true
        )
        scope.launch {
            val allMemories = mutableListOf<MemoryUiItem>()
            // Load memories for ALL babies (not just selected — we filter in UI)
            babies.forEach { baby ->
                when (val result = apiService.getMemoriesByBaby(baby.babyId)) {
                    is ApiResult.Success -> {
                        result.data.forEach { net ->
                            val uiItem = resolveLocalImages(net)
                            allMemories.add(uiItem)
                        }
                    }
                    else -> Unit
                }
            }
            allMemories.sortByDescending { it.memoryDate }
            uiState = uiState.copy(isLoading = false, memories = allMemories)
        }
    }

    /**
     * Resolve local image availability for a memory network response.
     */
    private suspend fun resolveLocalImages(net: MemoryNet): MemoryUiItem {
        val imageCount = net.imageCount ?: 0
        val images = (0 until imageCount).map { i ->
            val key = imageKey(net.memoryId, i)
            val bytes = localStore.loadImage(key)
            MemoryUiImage(
                index       = i,
                localKey    = key,
                caption     = net.captions?.getOrNull(i),
                bytes       = bytes,
                isAvailable = bytes != null
            )
        }
        return MemoryUiItem(
            memoryId         = net.memoryId,
            babyId           = net.babyId,
            babyName         = net.babyName,
            title            = net.title,
            description      = net.description,
            memoryDate       = net.memoryDate,
            ageInMonths      = net.ageInMonths,
            ageInDays        = net.ageInDays,
            images           = images,
            hasMissingImages = images.any { !it.isAvailable } && images.isNotEmpty()
        )
    }

    private fun imageKey(memoryId: String, index: Int) = "memory_${memoryId}_$index"

    // ── Baby selector ─────────────────────────────────────────────────────────

    fun selectBaby(babyId: String?) {
        uiState = uiState.copy(selectedBabyId = babyId)
    }

    // ── View image full-screen ────────────────────────────────────────────────

    fun viewImage(image: MemoryUiImage, memoryTitle: String) {
        uiState = uiState.copy(viewingImage = image, viewingMemoryTitle = memoryTitle)
    }

    fun closeImageViewer() {
        uiState = uiState.copy(viewingImage = null, viewingMemoryTitle = "")
    }

    // ── Add form ──────────────────────────────────────────────────────────────

    fun openAddForm() {
        uiState = uiState.copy(
            showAddForm = true,
            addForm = AddMemoryFormState(
                memoryDate = todayDateString()
            )
        )
    }

    fun closeAddForm() {
        uiState = uiState.copy(showAddForm = false, addForm = AddMemoryFormState())
    }

    fun onTitleChange(value: String) {
        uiState = uiState.copy(
            addForm = uiState.addForm.copy(
                title      = value,
                titleError = if (value.isBlank()) "Title is required" else null
            )
        )
    }

    fun onDescriptionChange(value: String) {
        uiState = uiState.copy(addForm = uiState.addForm.copy(description = value))
    }

    fun onDateChange(value: String) {
        uiState = uiState.copy(
            addForm = uiState.addForm.copy(
                memoryDate = value,
                dateError  = if (value.isBlank()) "Date is required" else null
            )
        )
    }

    fun onImagesSelected(bytesList: List<ByteArray>) {
        val current     = uiState.addForm.selectedBytes.toMutableList()
        val available   = AddMemoryFormState.MAX_IMAGES - current.size
        val toAdd       = bytesList.take(available)
        current.addAll(toAdd)
        uiState = uiState.copy(addForm = uiState.addForm.copy(selectedBytes = current))
    }

    fun removeImage(index: Int) {
        val updated = uiState.addForm.selectedBytes.toMutableList()
        if (index in updated.indices) updated.removeAt(index)
        uiState = uiState.copy(addForm = uiState.addForm.copy(selectedBytes = updated))
    }

    fun onCaptionChange(index: Int, value: String) {
        val captions = uiState.addForm.captions.toMutableList()
        while (captions.size <= index) captions.add("")
        captions[index] = value
        uiState = uiState.copy(addForm = uiState.addForm.copy(captions = captions))
    }

    fun saveMemory() {
        val form   = uiState.addForm
        val babyId = uiState.selectedBabyId
        val userId = preferencesManager.getUserId()

        var titleErr : String? = null
        var dateErr  : String? = null

        if (form.title.isBlank()) titleErr = "Title is required"
        if (form.memoryDate.isBlank()) dateErr = "Date is required"
        if (babyId == null) {
            uiState = uiState.copy(addForm = form.copy(errorMessage = "Please select a child"))
            return
        }

        if (titleErr != null || dateErr != null) {
            uiState = uiState.copy(addForm = form.copy(titleError = titleErr, dateError = dateErr))
            return
        }

        scope.launch {
            uiState = uiState.copy(addForm = form.copy(isLoading = true, errorMessage = null))

            // Create memory on backend (no images sent to backend — stored locally)
            val request = CreateMemoryRequest(
                babyId      = babyId,
                title       = form.title.trim(),
                description = form.description.ifBlank { null },
                memoryDate  = form.memoryDate,
                imageCount  = form.selectedBytes.size,
                captions    = form.captions.ifEmpty { null }
            )

            when (val result = apiService.createMemory(userId ?: "", request)) {
                is ApiResult.Success -> {
                    val memoryId = result.data.memoryId
                    // Save images locally
                    form.selectedBytes.forEachIndexed { i, bytes ->
                        localStore.saveImage(imageKey(memoryId, i), bytes)
                    }
                    uiState = uiState.copy(
                        showAddForm  = false,
                        addForm      = AddMemoryFormState(),
                        actionMessage = "Memory saved! 📸"
                    )
                    // Reload
                    load(uiState.babies, uiState.selectedBabyId)
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        addForm = uiState.addForm.copy(
                            isLoading    = false,
                            errorMessage = result.message
                        )
                    )
                }
                else -> {
                    uiState = uiState.copy(addForm = uiState.addForm.copy(isLoading = false))
                }
            }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun confirmDeleteMemory(memoryId: String) {
        uiState = uiState.copy(deletingMemoryId = memoryId)
    }

    fun cancelDelete() {
        uiState = uiState.copy(deletingMemoryId = null)
    }

    fun deleteMemory(memoryId: String) {
        scope.launch {
            when (apiService.deleteMemory(memoryId)) {
                is ApiResult.Success -> {
                    // Delete local images
                    localStore.deleteImagesByPrefix("memory_$memoryId")
                    val updated = uiState.memories.filter { it.memoryId != memoryId }
                    uiState = uiState.copy(
                        memories         = updated,
                        deletingMemoryId = null,
                        actionMessage    = "Memory deleted"
                    )
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        deletingMemoryId = null,
                        actionMessage    = "Failed to delete memory"
                    )
                }
                else -> uiState = uiState.copy(deletingMemoryId = null)
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun clearActionMessage() { uiState = uiState.copy(actionMessage = null) }
    fun clearError()         { uiState = uiState.copy(errorMessage = null) }

    @OptIn(ExperimentalTime::class)
    private fun todayDateString(): String {
        // Returns YYYY-MM-DD for today using platform clock
        val now = Clock.System.now()
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        return "${now.year}-${now.month.number.toString().padStart(2,'0')}-${now.day.toString().padStart(2,'0')}"
    }

    fun onDestroy() { scope.cancel() }
}
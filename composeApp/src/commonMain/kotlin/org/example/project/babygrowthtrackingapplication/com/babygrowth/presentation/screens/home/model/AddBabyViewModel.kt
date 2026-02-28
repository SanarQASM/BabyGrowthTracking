package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.*
import org.example.project.babygrowthtrackingapplication.platform.ImageUploadService

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class AddBabyUiState(
    // Form fields
    val fullName           : String  = "",
    val dateOfBirth        : String  = "",   // "YYYY-MM-DD"
    // ✅ FIXED: Backend Gender enum is BOY/GIRL (DB: ENUM('boy','girl'))
    // NOT MALE/FEMALE — sending MALE/FEMALE causes a 400 deserialization error.
    val gender             : String  = "BOY",
    val birthWeight        : String  = "",
    val birthHeight        : String  = "",
    val headCircumference  : String  = "",
    val photoUrl           : String? = null,

    // Image picking / upload state
    val selectedImageBytes : ByteArray? = null,
    val isUploadingImage   : Boolean    = false,

    // Async / feedback
    val isLoading          : Boolean = false,
    val errorMessage       : String? = null,
    val successMessage     : String? = null,
    val isSaved            : Boolean = false,

    // Field-level validation errors
    val nameError          : String? = null,
    val dobError           : String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class AddBabyViewModel(
    private val apiService         : ApiService,
    private val preferencesManager : PreferencesManager,
    private val uploadService      : ImageUploadService = ImageUploadService()
) {
    var uiState by mutableStateOf(AddBabyUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ── Field updaters ────────────────────────────────────────────────────────

    fun onFullNameChange(value: String) {
        uiState = uiState.copy(
            fullName  = value,
            nameError = if (value.isBlank()) "Full name is required" else null
        )
    }

    fun onDateOfBirthChange(value: String) {
        uiState = uiState.copy(
            dateOfBirth = value,
            dobError    = if (value.isBlank()) "Date of birth is required" else null
        )
    }

    fun onGenderChange(value: String) {
        // Accept BOY/GIRL only — matches backend Gender enum exactly
        uiState = uiState.copy(gender = value.uppercase())
    }

    fun onBirthWeightChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?\$"))) {
            uiState = uiState.copy(birthWeight = value)
        }
    }

    fun onBirthHeightChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?\$"))) {
            uiState = uiState.copy(birthHeight = value)
        }
    }

    fun onHeadCircumferenceChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?\$"))) {
            uiState = uiState.copy(headCircumference = value)
        }
    }

    fun clearError()   { uiState = uiState.copy(errorMessage   = null) }
    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }

    // ── Image handling ────────────────────────────────────────────────────────

    // NOTE: onImageSelected / retryImageUpload are kept for when the photo
    // upload feature is enabled in a future release. They are not called from
    // the UI right now — the "coming soon" warning dialog blocks the picker.

    fun onImageSelected(imageBytes: ByteArray) {
        uiState = uiState.copy(
            selectedImageBytes = imageBytes,
            isUploadingImage   = true,
            photoUrl           = null
        )
        scope.launch {
            val url = uploadService.uploadBabyPhoto(imageBytes)
            uiState = uiState.copy(
                isUploadingImage = false,
                photoUrl         = url,
                errorMessage     = if (url == null) "Image upload failed — tap to retry" else null
            )
        }
    }

    fun retryImageUpload() {
        uiState.selectedImageBytes?.let { onImageSelected(it) }
    }

    // ── Save / Create ─────────────────────────────────────────────────────────

    fun saveBaby() {
        if (!validate()) return

        val parentId = preferencesManager.getUserId() ?: run {
            uiState = uiState.copy(errorMessage = "Session expired — please log in again")
            return
        }

        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            val request = CreateBabyRequest(
                fullName               = uiState.fullName.trim(),
                dateOfBirth            = uiState.dateOfBirth,
                // ✅ Sends "BOY" or "GIRL" — matches backend Gender enum values
                gender                 = uiState.gender,
                birthWeight            = uiState.birthWeight.toDoubleOrNull(),
                birthHeight            = uiState.birthHeight.toDoubleOrNull(),
                birthHeadCircumference = uiState.headCircumference.toDoubleOrNull(),
                // null is safe — photoUrl is nullable in both BabyCreateRequest
                // (Dtos.kt) and the Baby entity (TEXT column, no @NotNull).
                photoUrl               = uiState.photoUrl
            )

            when (val result = apiService.createBaby(parentId, request)) {
                is ApiResult.Success ->
                    uiState = uiState.copy(
                        isLoading      = false,
                        isSaved        = true,
                        successMessage = "${result.data.fullName} added successfully 🎉"
                    )
                is ApiResult.Error ->
                    uiState = uiState.copy(
                        isLoading    = false,
                        errorMessage = result.message
                    )
                else ->
                    uiState = uiState.copy(isLoading = false)
            }
        }
    }

    /** Reset so the screen can be reused to add another baby. */
    fun resetForm() {
        uiState = AddBabyUiState()
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun validate(): Boolean {
        var valid   = true
        var nameErr : String? = null
        var dobErr  : String? = null

        if (uiState.fullName.isBlank()) {
            nameErr = "Full name is required"
            valid   = false
        }
        if (uiState.dateOfBirth.isBlank()) {
            dobErr = "Date of birth is required"
            valid  = false
        }

        uiState = uiState.copy(nameError = nameErr, dobError = dobErr)
        return valid
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    fun onDestroy() { scope.cancel() }
}
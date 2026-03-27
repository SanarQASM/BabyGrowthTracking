package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.FamilyHistoryNet
import org.example.project.babygrowthtrackingapplication.data.network.FamilyHistoryRequest

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class FamilyHistoryUiState(
    // Form fields (match backend FamilyHistory entity exactly)
    val heredity: String = "",
    val bloodDiseases: String = "",
    val cardiovascularDiseases: String = "",
    val metabolicDiseases: String = "",
    val appendicitis: String = "",
    val tuberculosis: String = "",
    val parkinsonism: String = "",
    val allergies: String = "",
    val others: String = "",

    // Persisted data
    val historyId: String = "",
    val existingHistory: FamilyHistoryNet? = null,

    // UI control
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,         // true = form open for editing
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val showDeleteConfirm: Boolean = false,
    val showNotSetAlert: Boolean = false,

    // Whether baby has family history set
    val isSet: Boolean = false
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class FamilyHistoryViewModel(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) {
    var uiState by mutableStateOf(FamilyHistoryUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ── Load existing family history for a baby ───────────────────────────────

    fun loadFamilyHistory(babyId: String) {
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                when (val result = apiService.getFamilyHistory(babyId)) {
                    is ApiResult.Success -> {
                        val data = result.data
                        if (data != null) {
                            uiState = uiState.copy(
                                isLoading    = false,
                                existingHistory = data,
                                historyId    = data.historyId,
                                isSet        = true,
                                heredity     = data.heredity ?: "",
                                bloodDiseases = data.bloodDiseases ?: "",
                                cardiovascularDiseases = data.cardiovascularDiseases ?: "",
                                metabolicDiseases = data.metabolicDiseases ?: "",
                                appendicitis = data.appendicitis ?: "",
                                tuberculosis = data.tuberculosis ?: "",
                                parkinsonism = data.parkinsonism ?: "",
                                allergies    = data.allergies ?: "",
                                others       = data.others ?: ""
                            )
                        } else {
                            uiState = uiState.copy(isLoading = false, isSet = false, existingHistory = null)
                        }
                    }
                    is ApiResult.Error -> uiState = uiState.copy(isLoading = false, isSet = false)
                    else               -> uiState = uiState.copy(isLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, isSet = false)
            }
        }
    }

    // ── Field updaters ────────────────────────────────────────────────────────

    fun onHeredityChange(v: String)               { uiState = uiState.copy(heredity = v) }
    fun onBloodDiseasesChange(v: String)          { uiState = uiState.copy(bloodDiseases = v) }
    fun onCardiovascularChange(v: String)         { uiState = uiState.copy(cardiovascularDiseases = v) }
    fun onMetabolicChange(v: String)              { uiState = uiState.copy(metabolicDiseases = v) }
    fun onAppendicitisChange(v: String)           { uiState = uiState.copy(appendicitis = v) }
    fun onTuberculosisChange(v: String)           { uiState = uiState.copy(tuberculosis = v) }
    fun onParkinsonismChange(v: String)           { uiState = uiState.copy(parkinsonism = v) }
    fun onAllergiesChange(v: String)              { uiState = uiState.copy(allergies = v) }
    fun onOthersChange(v: String)                 { uiState = uiState.copy(others = v) }

    // ── Open / close edit mode ────────────────────────────────────────────────

    fun startEditing() { uiState = uiState.copy(isEditing = true, errorMessage = null) }
    fun cancelEditing() {
        // Restore original values on cancel
        val d = uiState.existingHistory
        if (d != null) {
            uiState = uiState.copy(
                isEditing    = false,
                heredity     = d.heredity ?: "",
                bloodDiseases = d.bloodDiseases ?: "",
                cardiovascularDiseases = d.cardiovascularDiseases ?: "",
                metabolicDiseases = d.metabolicDiseases ?: "",
                appendicitis = d.appendicitis ?: "",
                tuberculosis = d.tuberculosis ?: "",
                parkinsonism = d.parkinsonism ?: "",
                allergies    = d.allergies ?: "",
                others       = d.others ?: ""
            )
        } else {
            uiState = uiState.copy(isEditing = false)
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun save(babyId: String, onSuccess: () -> Unit = {}) {
        scope.launch {
            uiState = uiState.copy(isSaving = true, errorMessage = null)
            try {
                val request = FamilyHistoryRequest(
                    babyId                 = babyId,
                    heredity               = uiState.heredity.ifBlank { null },
                    bloodDiseases          = uiState.bloodDiseases.ifBlank { null },
                    cardiovascularDiseases = uiState.cardiovascularDiseases.ifBlank { null },
                    metabolicDiseases      = uiState.metabolicDiseases.ifBlank { null },
                    appendicitis           = uiState.appendicitis.ifBlank { null },
                    tuberculosis           = uiState.tuberculosis.ifBlank { null },
                    parkinsonism           = uiState.parkinsonism.ifBlank { null },
                    allergies              = uiState.allergies.ifBlank { null },
                    others                 = uiState.others.ifBlank { null }
                )

                val result = if (uiState.isSet && uiState.historyId.isNotBlank()) {
                    apiService.updateFamilyHistory(uiState.historyId, request)
                } else {
                    apiService.createFamilyHistory(request)
                }

                when (result) {
                    is ApiResult.Success -> {
                        val saved = result.data
                        uiState = uiState.copy(
                            isSaving        = false,
                            isEditing       = false,
                            isSet           = true,
                            existingHistory = saved,
                            historyId       = saved.historyId,
                            successMessage  = "Family history saved successfully"
                        )
                        onSuccess()
                    }
                    is ApiResult.Error -> uiState = uiState.copy(isSaving = false, errorMessage = result.message)
                    else               -> uiState = uiState.copy(isSaving = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isSaving = false, errorMessage = "Save failed: ${e.message}")
            }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun showDeleteConfirm()  { uiState = uiState.copy(showDeleteConfirm = true) }
    fun dismissDeleteConfirm() { uiState = uiState.copy(showDeleteConfirm = false) }

    fun delete(onSuccess: () -> Unit = {}) {
        val historyId = uiState.historyId
        if (historyId.isBlank()) return
        scope.launch {
            uiState = uiState.copy(isSaving = true, showDeleteConfirm = false)
            try {
                when (val result = apiService.deleteFamilyHistory(historyId)) {
                    is ApiResult.Success -> {
                        uiState = FamilyHistoryUiState(
                            successMessage = "Family history deleted",
                            isSet          = false
                        )
                        onSuccess()
                    }
                    is ApiResult.Error -> uiState = uiState.copy(isSaving = false, errorMessage = result.message)
                    else               -> uiState = uiState.copy(isSaving = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isSaving = false, errorMessage = "Delete failed: ${e.message}")
            }
        }
    }

    // ── Alert helpers ─────────────────────────────────────────────────────────

    fun showNotSetAlert()    { uiState = uiState.copy(showNotSetAlert = true) }
    fun dismissNotSetAlert() { uiState = uiState.copy(showNotSetAlert = false) }

    // ── Message clearing ──────────────────────────────────────────────────────

    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }
    fun clearError()   { uiState = uiState.copy(errorMessage = null) }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    fun onDestroy() { scope.cancel() }
}
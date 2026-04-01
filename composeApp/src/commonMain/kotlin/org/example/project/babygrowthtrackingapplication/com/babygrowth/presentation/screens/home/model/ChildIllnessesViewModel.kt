package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.ChildIllnessNet
import org.example.project.babygrowthtrackingapplication.data.network.ChildIllnessRequest

// ─────────────────────────────────────────────────────────────────────────────
// UI Item
// ─────────────────────────────────────────────────────────────────────────────

data class ChildIllnessUiItem(
    val illnessId     : String,
    val illnessName   : String,
    val diagnosisDate : String?,   // always stored as "yyyy-MM-dd" (ISO, language-neutral)
    val notes         : String?,
    val isActive      : Boolean
)

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class ChildIllnessesUiState(
    val illnesses             : List<ChildIllnessUiItem> = emptyList(),

    // Form fields
    val formIllnessName        : String  = "",
    /**
     * Epoch-day (Long) so we never parse locale-specific strings in the VM.
     * null means "no date chosen".
     * -1L is a sentinel meaning "clear the date" — handled in onDateSelected().
     */
    val formDiagnosisDateEpoch : Long?   = null,
    val formNotes              : String  = "",
    val formIsActive           : Boolean = true,
    val formSubmitted          : Boolean = false,

    val editingIllnessId       : String? = null,

    // Dialogs
    val showAddEditDialog      : Boolean = false,
    val showDeleteConfirm      : Boolean = false,
    val showDatePicker         : Boolean = false,

    val pendingDeleteId        : String? = null,

    val isLoading              : Boolean = false,
    val isSaving               : Boolean = false,
    val successMessage         : String? = null,
    val errorMessage           : String? = null
) {
    val hasRecords : Boolean get() = illnesses.isNotEmpty()
    val activeCount: Int     get() = illnesses.count { it.isActive }

    val formDiagnosisDateIso: String?
        get() = formDiagnosisDateEpoch?.let {
            val d = LocalDate.fromEpochDays(it.toInt())
            "${d.year}-${d.month.number.toString().padStart(2, '0')}-${d.day.toString().padStart(2, '0')}"
        }

}

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class ChildIllnessesViewModel(
    private val apiService         : ApiService,
    private val preferencesManager : PreferencesManager
) {
    var uiState by mutableStateOf(ChildIllnessesUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ── Load ──────────────────────────────────────────────────────────────────

    fun loadIllnesses(babyId: String) {
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                when (val result = apiService.getChildIllnesses(babyId)) {
                    is ApiResult.Success -> uiState = uiState.copy(
                        isLoading = false,
                        illnesses = result.data.map { it.toUiItem() }
                    )
                    is ApiResult.Error -> uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    else -> uiState = uiState.copy(isLoading = false)
                }
            } catch (e: Exception) {
                // Error message key: child_illnesses_error_load
                // The screen resolves the string from resources using the key + e.message
                uiState = uiState.copy(
                    isLoading    = false,
                    errorMessage = "ERR_LOAD:${e.message}"   // screen maps this → stringResource
                )
            }
        }
    }

    // ── Dialog openers ────────────────────────────────────────────────────────

    fun startAdding() {
        uiState = uiState.copy(
            showAddEditDialog      = true,
            editingIllnessId       = null,
            formIllnessName        = "",
            formDiagnosisDateEpoch = null,
            formNotes              = "",
            formIsActive           = true,
            formSubmitted          = false,
            errorMessage           = null
        )
    }

    fun startEditing(illness: ChildIllnessUiItem) {
        val epochDay = illness.diagnosisDate?.let {
            try { LocalDate.parse(it).toEpochDays().toLong() } catch (_: Exception) { null }
        }
        uiState = uiState.copy(
            showAddEditDialog      = true,
            editingIllnessId       = illness.illnessId,
            formIllnessName        = illness.illnessName,
            formDiagnosisDateEpoch = epochDay,
            formNotes              = illness.notes ?: "",
            formIsActive           = illness.isActive,
            formSubmitted          = false,
            errorMessage           = null
        )
    }

    fun dismissAddEditDialog() {
        uiState = uiState.copy(showAddEditDialog = false, formSubmitted = false)
    }

    // ── Date picker control ───────────────────────────────────────────────────

    fun openDatePicker()    { uiState = uiState.copy(showDatePicker = true) }
    fun dismissDatePicker() { uiState = uiState.copy(showDatePicker = false) }

    /**
     * Called when the user confirms a date in the picker.
     * Pass -1L to clear the date (the "×" clear button in the dialog uses this).
     */
    fun onDateSelected(epochDay: Long) {
        uiState = uiState.copy(
            formDiagnosisDateEpoch = if (epochDay == -1L) null else epochDay,
            showDatePicker         = false
        )
    }

    // ── Form field updaters ───────────────────────────────────────────────────

    fun onIllnessNameChange(v: String) { uiState = uiState.copy(formIllnessName = v) }
    fun onNotesChange(v: String)       { uiState = uiState.copy(formNotes = v) }
    fun onIsActiveChange(v: Boolean)   { uiState = uiState.copy(formIsActive = v) }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun saveIllness(babyId: String) {
        uiState = uiState.copy(formSubmitted = true)
        if (uiState.formIllnessName.isBlank()) return

        scope.launch {
            uiState = uiState.copy(isSaving = true, errorMessage = null)
            try {
                val request = ChildIllnessRequest(
                    babyId        = babyId,
                    illnessName   = uiState.formIllnessName.trim(),
                    diagnosisDate = uiState.formDiagnosisDateIso,  // "yyyy-MM-dd" or null — always ISO
                    notes         = uiState.formNotes.ifBlank { null },
                    isActive      = uiState.formIsActive
                )

                val editingId = uiState.editingIllnessId
                val result = if (editingId != null)
                    apiService.updateChildIllness(editingId, request)
                else
                    apiService.createChildIllness(request)

                when (result) {
                    is ApiResult.Success -> {
                        val refreshResult = apiService.getChildIllnesses(babyId)
                        val updated = if (refreshResult is ApiResult.Success)
                            refreshResult.data.map { it.toUiItem() }
                        else uiState.illnesses

                        uiState = uiState.copy(
                            isSaving         = false,
                            showAddEditDialog = false,
                            illnesses        = updated,
                            // Keys: child_illnesses_success_updated / child_illnesses_success_added
                            successMessage   = if (editingId != null)
                                "MSG_UPDATED" else "MSG_ADDED"
                        )
                    }
                    is ApiResult.Error -> uiState = uiState.copy(
                        isSaving = false, errorMessage = result.message
                    )
                    else -> uiState = uiState.copy(isSaving = false)
                }
            } catch (e: Exception) {
                // Key: child_illnesses_error_save
                uiState = uiState.copy(
                    isSaving     = false,
                    errorMessage = "ERR_SAVE:${e.message}"
                )
            }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun showDeleteConfirm(illnessId: String) {
        uiState = uiState.copy(showDeleteConfirm = true, pendingDeleteId = illnessId)
    }

    fun dismissDeleteConfirm() {
        uiState = uiState.copy(showDeleteConfirm = false, pendingDeleteId = null)
    }

    fun deleteIllness() {
        val id = uiState.pendingDeleteId ?: return
        scope.launch {
            uiState = uiState.copy(isSaving = true, showDeleteConfirm = false)
            try {
                when (val result = apiService.deleteChildIllness(id)) {
                    is ApiResult.Success -> uiState = uiState.copy(
                        isSaving        = false,
                        pendingDeleteId = null,
                        illnesses       = uiState.illnesses.filter { it.illnessId != id },
                        successMessage  = "MSG_DELETED"   // Key: child_illnesses_success_deleted
                    )
                    is ApiResult.Error -> uiState = uiState.copy(
                        isSaving = false, errorMessage = result.message
                    )
                    else -> uiState = uiState.copy(isSaving = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSaving     = false,
                    errorMessage = "ERR_DELETE:${e.message}"   // Key: child_illnesses_error_delete
                )
            }
        }
    }

    // ── Toggle active ─────────────────────────────────────────────────────────

    fun toggleActive(illness: ChildIllnessUiItem) {
        scope.launch {
            try {
                if (illness.isActive) {
                    when (val r = apiService.deactivateChildIllness(illness.illnessId)) {
                        is ApiResult.Success -> uiState = uiState.copy(
                            illnesses = uiState.illnesses.map {
                                if (it.illnessId == illness.illnessId) it.copy(isActive = false) else it
                            },
                            successMessage = "MSG_RESOLVED"   // Key: child_illnesses_success_resolved
                        )
                        is ApiResult.Error -> uiState = uiState.copy(errorMessage = r.message)
                        else -> Unit
                    }
                } else {
                    val req = ChildIllnessRequest(
                        babyId        = "",
                        illnessName   = illness.illnessName,
                        diagnosisDate = illness.diagnosisDate,   // already ISO — safe
                        notes         = illness.notes,
                        isActive      = true
                    )
                    when (val r = apiService.updateChildIllness(illness.illnessId, req)) {
                        is ApiResult.Success -> uiState = uiState.copy(
                            illnesses = uiState.illnesses.map {
                                if (it.illnessId == illness.illnessId) it.copy(isActive = true) else it
                            },
                            successMessage = "MSG_ACTIVATED"   // Key: child_illnesses_success_activated
                        )
                        is ApiResult.Error -> uiState = uiState.copy(errorMessage = r.message)
                        else -> Unit
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = "ERR_UPDATE:${e.message}"   // Key: child_illnesses_error_update
                )
            }
        }
    }

    // ── Misc ──────────────────────────────────────────────────────────────────

    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }
    fun clearError()   { uiState = uiState.copy(errorMessage = null) }
    fun onDestroy()    { scope.cancel() }

    private fun ChildIllnessNet.toUiItem() = ChildIllnessUiItem(
        illnessId     = illnessId,
        illnessName   = illnessName,
        diagnosisDate = diagnosisDate,   // stored as received from API (ISO)
        notes         = notes,
        isActive      = isActive
    )
}
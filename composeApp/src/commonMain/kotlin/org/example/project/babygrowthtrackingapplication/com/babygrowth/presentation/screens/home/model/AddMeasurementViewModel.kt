package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.CreateGrowthRecordRequest
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class AddMeasurementUiState(
    val weight            : String  = "",
    val height            : String  = "",
    val headCircumference : String  = "",
    val measurementDate   : String  = "",   // "YYYY-MM-DD", pre-filled to today
    val isLoading         : Boolean = false,
    val errorMessage      : String? = null,
    val isSaved           : Boolean = false
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalTime::class)
class AddMeasurementViewModel(
    private val apiService         : ApiService,
    private val preferencesManager : PreferencesManager
) {
    var uiState by mutableStateOf(
        AddMeasurementUiState(
            measurementDate = run {
                val now = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
            }
        )
    )
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ── Input handlers ────────────────────────────────────────────────────────

    fun onWeightChange(v: String) {
        if (v.isEmpty() || v.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?\$")))
            uiState = uiState.copy(weight = v, errorMessage = null)
    }

    fun onHeightChange(v: String) {
        if (v.isEmpty() || v.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?\$")))
            uiState = uiState.copy(height = v, errorMessage = null)
    }

    fun onHeadChange(v: String) {
        if (v.isEmpty() || v.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?\$")))
            uiState = uiState.copy(headCircumference = v, errorMessage = null)
    }

    fun onDateChange(v: String) {
        uiState = uiState.copy(measurementDate = v)
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun saveMeasurement(babyId: String) {
        // ✅ Guard: prevent duplicate submissions while a request is already in flight
        if (uiState.isLoading) return

        if (uiState.weight.isBlank() &&
            uiState.height.isBlank() &&
            uiState.headCircumference.isBlank()
        ) {
            uiState = uiState.copy(errorMessage = "Please enter at least one measurement")
            return
        }

        val userId = preferencesManager.getUserId() ?: run {
            uiState = uiState.copy(errorMessage = "Session expired — please log in again")
            return
        }

        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            val request = CreateGrowthRecordRequest(
                babyId            = babyId,
                measurementDate   = uiState.measurementDate,
                weight            = uiState.weight.toDoubleOrNull(),
                height            = uiState.height.toDoubleOrNull(),
                headCircumference = uiState.headCircumference.toDoubleOrNull()
            )

            when (val result = apiService.createGrowthRecord(userId, request)) {
                is ApiResult.Success -> uiState = uiState.copy(isLoading = false, isSaved = true)
                is ApiResult.Error   -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                else                 -> uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun onDestroy() {
        scope.cancel()
    }
}
// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/team/TeamRequestsViewModel.kt
// Additional ViewModel mixin — handles incoming bench requests for team vaccination

package org.example.project.babygrowthtrackingapplication.team

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.BenchRequestNet
import org.example.project.babygrowthtrackingapplication.data.network.BenchRequestStatusUi
import org.example.project.babygrowthtrackingapplication.data.network.BenchRequestUi
import org.example.project.babygrowthtrackingapplication.data.network.toUi

// ─────────────────────────────────────────────────────────────────────────────
// TeamRequestsUiState — incoming join requests from parents
// ─────────────────────────────────────────────────────────────────────────────

data class TeamRequestsUiState(
    val pendingRequests     : List<BenchRequestUi> = emptyList(),
    val allRequests         : List<BenchRequestUi> = emptyList(),
    val requestsLoading     : Boolean              = false,
    val reviewSubmitting    : Boolean              = false,
    val selectedRequest     : BenchRequestUi?      = null,
    val showRejectDialog    : Boolean              = false,
    val rejectReason        : String               = "",
    val errorMessage        : String?              = null,
    val successMessage      : String?              = null
)

// ─────────────────────────────────────────────────────────────────────────────
// TeamRequestsViewModel
// ─────────────────────────────────────────────────────────────────────────────

class TeamRequestsViewModel(
    private val apiService : ApiService,
    private val getBenchId : () -> String
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var uiState by mutableStateOf(TeamRequestsUiState())
        private set

    fun loadRequests() {
        val benchId = getBenchId()
        if (benchId.isBlank()) return
        scope.launch {
            uiState = uiState.copy(requestsLoading = true)
            try {
                // Load pending first for the badge count
                val pending = apiService.getPendingRequestsForBench(benchId)
                val all     = apiService.getAllRequestsForBench(benchId)
                uiState = uiState.copy(
                    pendingRequests = (pending as? ApiResult.Success)?.data?.map { it.toUi() } ?: emptyList(),
                    allRequests     = (all     as? ApiResult.Success)?.data?.map { it.toUi() } ?: emptyList(),
                    requestsLoading = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(requestsLoading = false, errorMessage = e.message)
            }
        }
    }

    fun selectRequest(request: BenchRequestUi) {
        uiState = uiState.copy(selectedRequest = request)
    }

    fun acceptRequest(requestId: String) {
        scope.launch {
            uiState = uiState.copy(reviewSubmitting = true)
            try {
                val result = apiService.reviewBenchRequest(requestId, "accept")
                when (result) {
                    is ApiResult.Success -> {
                        uiState = uiState.copy(
                            reviewSubmitting = false,
                            selectedRequest  = null,
                            successMessage   = "request_accepted"
                        )
                        loadRequests()
                    }
                    is ApiResult.Error -> uiState = uiState.copy(
                        reviewSubmitting = false,
                        errorMessage = result.message
                    )
                    else -> uiState = uiState.copy(reviewSubmitting = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(reviewSubmitting = false, errorMessage = e.message)
            }
        }
    }

    fun openRejectDialog(request: BenchRequestUi) {
        uiState = uiState.copy(selectedRequest = request, showRejectDialog = true, rejectReason = "")
    }

    fun dismissRejectDialog() {
        uiState = uiState.copy(showRejectDialog = false, rejectReason = "")
    }

    fun onRejectReasonChange(reason: String) {
        uiState = uiState.copy(rejectReason = reason)
    }

    fun confirmReject() {
        val requestId = uiState.selectedRequest?.requestId ?: return
        if (uiState.rejectReason.isBlank()) {
            uiState = uiState.copy(errorMessage = "reject_reason_required")
            return
        }
        scope.launch {
            uiState = uiState.copy(reviewSubmitting = true)
            try {
                val result = apiService.reviewBenchRequest(requestId, "reject", uiState.rejectReason)
                when (result) {
                    is ApiResult.Success -> {
                        uiState = uiState.copy(
                            reviewSubmitting = false,
                            showRejectDialog = false,
                            selectedRequest  = null,
                            rejectReason     = "",
                            successMessage   = "request_rejected"
                        )
                        loadRequests()
                    }
                    is ApiResult.Error -> uiState = uiState.copy(
                        reviewSubmitting = false,
                        errorMessage = result.message
                    )
                    else -> uiState = uiState.copy(reviewSubmitting = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(reviewSubmitting = false, errorMessage = e.message)
            }
        }
    }

    fun clearError()   { uiState = uiState.copy(errorMessage = null) }
    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }
    fun onDestroy()    { scope.cancel() }
}
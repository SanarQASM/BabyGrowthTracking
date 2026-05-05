package org.example.project.babygrowthtrackingapplication.team

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.BenchRequestNet
import org.example.project.babygrowthtrackingapplication.data.network.BenchRequestStatusUi
import org.example.project.babygrowthtrackingapplication.data.network.BenchRequestUi
import org.example.project.babygrowthtrackingapplication.data.network.toUi

data class TeamRequestsUiState(
    val pendingRequests  : List<BenchRequestUi> = emptyList(),
    val allRequests      : List<BenchRequestUi> = emptyList(),
    val requestsLoading  : Boolean              = false,
    val reviewSubmitting : Boolean              = false,
    val selectedRequest  : BenchRequestUi?      = null,
    val showRejectDialog : Boolean              = false,
    val rejectReason     : String               = "",
    val errorMessage     : String?              = null,
    val successMessage   : String?              = null
)

class TeamRequestsViewModel(
    private val apiService : ApiService,
    // FIX: getBenchId is a lambda that reads viewModel.uiState.benchId at call time.
    // Previously called with `getBenchId = { state.benchId }` which captured the
    // state snapshot at composition — returning "" when benchId hadn't loaded yet.
    private val getBenchId : () -> String
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var uiState by mutableStateOf(TeamRequestsUiState())
        private set

    fun loadRequests() {
        val benchId = getBenchId()
        if (benchId.isBlank()) return

        scope.launch {
            uiState = uiState.copy(requestsLoading = true, errorMessage = null)

            // FIX: was using (pending as? ApiResult.Success)?.data which silently
            // returns emptyList() on error, hiding the failure from the user.
            // Now both branches are handled explicitly.
            val pendingResult = apiService.getPendingRequestsForBench(benchId)
            val allResult     = apiService.getAllRequestsForBench(benchId)

            // Collect any error from either call
            val error = when {
                pendingResult is ApiResult.Error -> pendingResult.message
                allResult     is ApiResult.Error -> allResult.message
                else                             -> null
            }

            uiState = uiState.copy(
                pendingRequests = if (pendingResult is ApiResult.Success)
                    pendingResult.data.map { it.toUi() }
                else
                    emptyList(),
                allRequests     = if (allResult is ApiResult.Success)
                    allResult.data.map { it.toUi() }
                else
                    emptyList(),
                requestsLoading = false,
                errorMessage    = error
            )
        }
    }

    fun selectRequest(request: BenchRequestUi) {
        uiState = uiState.copy(selectedRequest = request)
    }

    fun acceptRequest(requestId: String) {
        scope.launch {
            uiState = uiState.copy(reviewSubmitting = true, errorMessage = null)
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
                    errorMessage     = result.message.ifBlank { "Failed to accept request" }
                )
                else -> uiState = uiState.copy(reviewSubmitting = false)
            }
        }
    }

    fun openRejectDialog(request: BenchRequestUi) {
        uiState = uiState.copy(
            selectedRequest  = request,
            showRejectDialog = true,
            rejectReason     = ""
        )
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
            uiState = uiState.copy(reviewSubmitting = true, errorMessage = null)
            val result = apiService.reviewBenchRequest(
                requestId    = requestId,
                action       = "reject",
                rejectReason = uiState.rejectReason
            )
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
                    errorMessage     = result.message.ifBlank { "Failed to reject request" }
                )
                else -> uiState = uiState.copy(reviewSubmitting = false)
            }
        }
    }

    fun clearError()   { uiState = uiState.copy(errorMessage = null) }
    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }

    // FIX: onDestroy is now properly called from TeamVaccinationScreen via
    // DisposableEffect — coroutine scope is cancelled when composable leaves.
    fun onDestroy()    { scope.cancel() }
}
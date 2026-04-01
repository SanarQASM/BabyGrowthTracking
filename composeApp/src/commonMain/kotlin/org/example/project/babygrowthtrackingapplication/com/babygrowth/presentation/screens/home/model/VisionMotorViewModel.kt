package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.VisionMotorNet

// ─────────────────────────────────────────────────────────────────────────────
// Milestone months supported by this screen
// ─────────────────────────────────────────────────────────────────────────────

val VISION_MOTOR_MILESTONE_MONTHS = listOf(1, 3, 6, 9, 12)

// ─────────────────────────────────────────────────────────────────────────────
// Per-month record state
// ─────────────────────────────────────────────────────────────────────────────

data class VisionMotorMonthState(
    val checkMonth : Int,
    val recordId   : String  = "",
    val checkDate  : String? = null,
    val notes      : String? = null,
    // Month 1
    val m1HeadMovesFollowsLight: Boolean? = null,
    val m1TracksPeopleObjects  : Boolean? = null,
    val m1FollowsFlashlight    : Boolean? = null,
    // Month 3
    val m3Head180Tracking      : Boolean? = null,
    val m3AttentiveFaceTracking: Boolean? = null,
    val m3WatchesOwnHands      : Boolean? = null,
    val m3RecognizesMother     : Boolean? = null,
    val m3HandsOpenReflex      : Boolean? = null,
    // Month 6
    val m6EyesHeadFullRange      : Boolean? = null,
    val m6FollowsPersonAcrossRoom: Boolean? = null,
    val m6SmilesAtMirror         : Boolean? = null,
    val m6ReachesForDroppedObject: Boolean? = null,
    val m6TransfersObjects       : Boolean? = null,
    // Month 9
    val m9KeenVisualAttention  : Boolean? = null,
    val m9PincerGrasp          : Boolean? = null,
    val m9ReachesDesiredObjects: Boolean? = null,
    val m9AttentionSpan        : Boolean? = null,
    // Month 12
    val m12NeatPincerGrasp          : Boolean? = null,
    val m12PlaysWithToys            : Boolean? = null,
    val m12ReleasesObjects          : Boolean? = null,
    val m12RecognizesFamiliarPeople : Boolean? = null,
    val m12GetsAttentionByTugging   : Boolean? = null,
)

fun VisionMotorNet.toMonthState() = VisionMotorMonthState(
    checkMonth = checkMonth, recordId = recordId, checkDate = checkDate, notes = notes,
    m1HeadMovesFollowsLight = m1HeadMovesFollowsLight,
    m1TracksPeopleObjects   = m1TracksPeopleObjects,
    m1FollowsFlashlight     = m1FollowsFlashlight,
    m3Head180Tracking       = m3Head180Tracking,
    m3AttentiveFaceTracking = m3AttentiveFaceTracking,
    m3WatchesOwnHands       = m3WatchesOwnHands,
    m3RecognizesMother      = m3RecognizesMother,
    m3HandsOpenReflex       = m3HandsOpenReflex,
    m6EyesHeadFullRange       = m6EyesHeadFullRange,
    m6FollowsPersonAcrossRoom = m6FollowsPersonAcrossRoom,
    m6SmilesAtMirror          = m6SmilesAtMirror,
    m6ReachesForDroppedObject = m6ReachesForDroppedObject,
    m6TransfersObjects        = m6TransfersObjects,
    m9KeenVisualAttention   = m9KeenVisualAttention,
    m9PincerGrasp           = m9PincerGrasp,
    m9ReachesDesiredObjects = m9ReachesDesiredObjects,
    m9AttentionSpan         = m9AttentionSpan,
    m12NeatPincerGrasp          = m12NeatPincerGrasp,
    m12PlaysWithToys            = m12PlaysWithToys,
    m12ReleasesObjects          = m12ReleasesObjects,
    m12RecognizesFamiliarPeople = m12RecognizesFamiliarPeople,
    m12GetsAttentionByTugging   = m12GetsAttentionByTugging,
)

fun VisionMotorMonthState.toNet(babyId: String) = VisionMotorNet(
    recordId = recordId, babyId = babyId, checkMonth = checkMonth,
    checkDate = checkDate, notes = notes,
    m1HeadMovesFollowsLight = m1HeadMovesFollowsLight,
    m1TracksPeopleObjects   = m1TracksPeopleObjects,
    m1FollowsFlashlight     = m1FollowsFlashlight,
    m3Head180Tracking       = m3Head180Tracking,
    m3AttentiveFaceTracking = m3AttentiveFaceTracking,
    m3WatchesOwnHands       = m3WatchesOwnHands,
    m3RecognizesMother      = m3RecognizesMother,
    m3HandsOpenReflex       = m3HandsOpenReflex,
    m6EyesHeadFullRange       = m6EyesHeadFullRange,
    m6FollowsPersonAcrossRoom = m6FollowsPersonAcrossRoom,
    m6SmilesAtMirror          = m6SmilesAtMirror,
    m6ReachesForDroppedObject = m6ReachesForDroppedObject,
    m6TransfersObjects        = m6TransfersObjects,
    m9KeenVisualAttention   = m9KeenVisualAttention,
    m9PincerGrasp           = m9PincerGrasp,
    m9ReachesDesiredObjects = m9ReachesDesiredObjects,
    m9AttentionSpan         = m9AttentionSpan,
    m12NeatPincerGrasp          = m12NeatPincerGrasp,
    m12PlaysWithToys            = m12PlaysWithToys,
    m12ReleasesObjects          = m12ReleasesObjects,
    m12RecognizesFamiliarPeople = m12RecognizesFamiliarPeople,
    m12GetsAttentionByTugging   = m12GetsAttentionByTugging,
)

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class VisionMotorUiState(
    /** Map of checkMonth → MonthState (only months with saved data) */
    val savedRecords   : Map<Int, VisionMotorMonthState> = emptyMap(),
    /** Month currently being edited, null = overview */
    val editingMonth   : Int? = null,
    /** Working copy of the month being edited */
    val editingState   : VisionMotorMonthState? = null,
    val isLoading      : Boolean = false,
    val isSaving       : Boolean = false,
    val successMessage : String? = null,
    val errorMessage   : String? = null,
    /** Baby's current age in months — controls which sections are enabled */
    val babyAgeMonths  : Int = 0,
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class VisionMotorViewModel(
    private val apiService        : ApiService,
    private val preferencesManager: PreferencesManager,
) {
    var uiState by mutableStateOf(VisionMotorUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun load(babyId: String, babyAgeMonths: Int) {
        scope.launch {
            uiState = uiState.copy(isLoading = true, babyAgeMonths = babyAgeMonths)
            try {
                when (val result = apiService.getVisionMotorRecords(babyId)) {
                    is ApiResult.Success -> {
                        val map = result.data.associateBy { it.checkMonth }
                            .mapValues { it.value.toMonthState() }
                        uiState = uiState.copy(isLoading = false, savedRecords = map)
                    }
                    is ApiResult.Error -> uiState = uiState.copy(isLoading = false,
                        errorMessage = result.message)
                    else -> uiState = uiState.copy(isLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    /** Open edit panel for a specific milestone month */
    fun startEditing(month: Int) {
        val existing = uiState.savedRecords[month] ?: VisionMotorMonthState(checkMonth = month)
        uiState = uiState.copy(editingMonth = month, editingState = existing)
    }

    fun cancelEditing() {
        uiState = uiState.copy(editingMonth = null, editingState = null)
    }

    /** Update a single boolean field in the editing copy */
    fun updateField(updater: (VisionMotorMonthState) -> VisionMotorMonthState) {
        val current = uiState.editingState ?: return
        uiState = uiState.copy(editingState = updater(current))
    }

    fun save(babyId: String) {
        val state = uiState.editingState ?: return
        scope.launch {
            uiState = uiState.copy(isSaving = true, errorMessage = null)
            try {
                when (val result = apiService.saveVisionMotorRecord(state.toNet(babyId))) {
                    is ApiResult.Success -> {
                        val updated = uiState.savedRecords.toMutableMap()
                        updated[state.checkMonth] = result.data.toMonthState()
                        uiState = uiState.copy(
                            isSaving       = false,
                            editingMonth   = null,
                            editingState   = null,
                            savedRecords   = updated,
                            successMessage = "MSG_SAVED"
                        )
                    }
                    is ApiResult.Error -> uiState = uiState.copy(isSaving = false,
                        errorMessage = result.message)
                    else -> uiState = uiState.copy(isSaving = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isSaving = false, errorMessage = e.message)
            }
        }
    }

    /** Returns true if the given milestone month is unlocked for this baby */
    fun isMonthEnabled(month: Int): Boolean = uiState.babyAgeMonths >= month

    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }
    fun clearError()   { uiState = uiState.copy(errorMessage = null) }
    fun onDestroy()    { scope.cancel() }
}
// composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/com/babygrowth/presentation/screens/home/model/PreCheckInvestigationViewModel.kt

package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.InvestigationStatusNet
import org.example.project.babygrowthtrackingapplication.data.network.PreCheckInvestigationNet
import org.example.project.babygrowthtrackingapplication.data.network.PreCheckInvestigationRequest
import kotlin.time.ExperimentalTime

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class PreCheckInvestigationUiState(
    // Check date
    val checkDate              : String                 = "",   // ISO "yyyy-MM-dd"
    val checkDateEpoch         : Long?                  = null, // for DatePicker

    // Investigation fields — all three-state
    val jaundice               : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val shortnessOfBreath      : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val turningBlue            : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val marbleHeart            : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val inflammationOfLiver    : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val inflammationOfSpleen   : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val hernia                 : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val hydroceleOfEar         : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val hipJointDislocation    : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val musclesNormal          : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val reactionsNormal        : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val nucleusNormal          : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val genitalsNormal         : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val eyeNormal              : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val redReflex              : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val reactionToSound        : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val others                 : String                 = "",

    // Persistence
    val investigationId        : String                 = "",
    val existingInvestigation  : PreCheckInvestigationNet? = null,

    // UI control
    val isLoading              : Boolean                = false,
    val isSaving               : Boolean                = false,
    val isEditing              : Boolean                = false,
    val showDeleteConfirm      : Boolean                = false,
    val showNotSetAlert        : Boolean                = false,
    val showDatePicker         : Boolean                = false,
    val isSet                  : Boolean                = false,
    val successMessage         : String?                = null,
    val errorMessage           : String?                = null
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class PreCheckInvestigationViewModel(
    private val apiService         : ApiService,
    @Suppress("UNUSED_PARAMETER")
    private val preferencesManager : PreferencesManager
) {
    var uiState by mutableStateOf(PreCheckInvestigationUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ── Load ──────────────────────────────────────────────────────────────────

    fun load(babyId: String) {
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                when (val result = apiService.getPreCheckInvestigationByBaby(babyId)) {
                    is ApiResult.Success -> {
                        val data = result.data
                        if (data != null) {
                            uiState = uiState.copy(
                                isLoading              = false,
                                isSet                  = true,
                                existingInvestigation  = data,
                                investigationId        = data.investigationId,
                                checkDate              = data.checkDate,
                                jaundice               = data.jaundice,
                                shortnessOfBreath      = data.shortnessOfBreath,
                                turningBlue            = data.turningBlue,
                                marbleHeart            = data.marbleHeart,
                                inflammationOfLiver    = data.inflammationOfLiver,
                                inflammationOfSpleen   = data.inflammationOfSpleen,
                                hernia                 = data.hernia,
                                hydroceleOfEar         = data.hydroceleOfEar,
                                hipJointDislocation    = data.hipJointDislocation,
                                musclesNormal          = data.musclesNormal,
                                reactionsNormal        = data.reactionsNormal,
                                nucleusNormal          = data.nucleusNormal,
                                genitalsNormal         = data.genitalsNormal,
                                eyeNormal              = data.eyeNormal,
                                redReflex              = data.redReflex,
                                reactionToSound        = data.reactionToSound,
                                others                 = data.others ?: ""
                            )
                        } else {
                            uiState = uiState.copy(isLoading = false, isSet = false)
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

    // ── Edit mode ─────────────────────────────────────────────────────────────

    @OptIn(ExperimentalTime::class)
    fun startEditing() {
        // Pre-fill check date with today if empty
        val date = uiState.checkDate.ifBlank {
            kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        }
        uiState = uiState.copy(isEditing = true, errorMessage = null, checkDate = date)
    }

    fun cancelEditing() {
        val d = uiState.existingInvestigation
        if (d != null) {
            uiState = uiState.copy(
                isEditing              = false,
                checkDate              = d.checkDate,
                jaundice               = d.jaundice,
                shortnessOfBreath      = d.shortnessOfBreath,
                turningBlue            = d.turningBlue,
                marbleHeart            = d.marbleHeart,
                inflammationOfLiver    = d.inflammationOfLiver,
                inflammationOfSpleen   = d.inflammationOfSpleen,
                hernia                 = d.hernia,
                hydroceleOfEar         = d.hydroceleOfEar,
                hipJointDislocation    = d.hipJointDislocation,
                musclesNormal          = d.musclesNormal,
                reactionsNormal        = d.reactionsNormal,
                nucleusNormal          = d.nucleusNormal,
                genitalsNormal         = d.genitalsNormal,
                eyeNormal              = d.eyeNormal,
                redReflex              = d.redReflex,
                reactionToSound        = d.reactionToSound,
                others                 = d.others ?: ""
            )
        } else {
            uiState = uiState.copy(isEditing = false)
        }
    }

    // ── Date picker ───────────────────────────────────────────────────────────

    fun openDatePicker()    { uiState = uiState.copy(showDatePicker = true) }
    fun dismissDatePicker() { uiState = uiState.copy(showDatePicker = false) }

    fun onDateSelected(epochDay: Long) {
        if (epochDay == -1L) {
            uiState = uiState.copy(checkDateEpoch = null, checkDate = "", showDatePicker = false)
            return
        }
        val d = kotlinx.datetime.LocalDate.fromEpochDays(epochDay.toInt())
        val iso = "${d.year}-${d.monthNumber.toString().padStart(2, '0')}-${d.dayOfMonth.toString().padStart(2, '0')}"
        uiState = uiState.copy(checkDateEpoch = epochDay, checkDate = iso, showDatePicker = false)
    }

    // ── Field updaters ────────────────────────────────────────────────────────

    fun onJaundiceChange(v: InvestigationStatusNet)            { uiState = uiState.copy(jaundice = v) }
    fun onShortnessOfBreathChange(v: InvestigationStatusNet)   { uiState = uiState.copy(shortnessOfBreath = v) }
    fun onTurningBlueChange(v: InvestigationStatusNet)         { uiState = uiState.copy(turningBlue = v) }
    fun onMarbleHeartChange(v: InvestigationStatusNet)         { uiState = uiState.copy(marbleHeart = v) }
    fun onInflammationOfLiverChange(v: InvestigationStatusNet) { uiState = uiState.copy(inflammationOfLiver = v) }
    fun onInflammationOfSpleenChange(v: InvestigationStatusNet){ uiState = uiState.copy(inflammationOfSpleen = v) }
    fun onHerniaChange(v: InvestigationStatusNet)              { uiState = uiState.copy(hernia = v) }
    fun onHydroceleOfEarChange(v: InvestigationStatusNet)      { uiState = uiState.copy(hydroceleOfEar = v) }
    fun onHipJointDislocationChange(v: InvestigationStatusNet) { uiState = uiState.copy(hipJointDislocation = v) }
    fun onMusclesNormalChange(v: InvestigationStatusNet)       { uiState = uiState.copy(musclesNormal = v) }
    fun onReactionsNormalChange(v: InvestigationStatusNet)     { uiState = uiState.copy(reactionsNormal = v) }
    fun onNucleusNormalChange(v: InvestigationStatusNet)       { uiState = uiState.copy(nucleusNormal = v) }
    fun onGenitalsNormalChange(v: InvestigationStatusNet)      { uiState = uiState.copy(genitalsNormal = v) }
    fun onEyeNormalChange(v: InvestigationStatusNet)           { uiState = uiState.copy(eyeNormal = v) }
    fun onRedReflexChange(v: InvestigationStatusNet)           { uiState = uiState.copy(redReflex = v) }
    fun onReactionToSoundChange(v: InvestigationStatusNet)     { uiState = uiState.copy(reactionToSound = v) }
    fun onOthersChange(v: String)                              { uiState = uiState.copy(others = v) }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun save(babyId: String) {
        if (uiState.checkDate.isBlank()) {
            uiState = uiState.copy(errorMessage = "ERR_DATE")
            return
        }
        scope.launch {
            uiState = uiState.copy(isSaving = true, errorMessage = null)
            try {
                val request = PreCheckInvestigationRequest(
                    babyId               = babyId,
                    checkDate            = uiState.checkDate,
                    jaundice             = uiState.jaundice,
                    shortnessOfBreath    = uiState.shortnessOfBreath,
                    turningBlue          = uiState.turningBlue,
                    marbleHeart          = uiState.marbleHeart,
                    inflammationOfLiver  = uiState.inflammationOfLiver,
                    inflammationOfSpleen = uiState.inflammationOfSpleen,
                    hernia               = uiState.hernia,
                    hydroceleOfEar       = uiState.hydroceleOfEar,
                    hipJointDislocation  = uiState.hipJointDislocation,
                    musclesNormal        = uiState.musclesNormal,
                    reactionsNormal      = uiState.reactionsNormal,
                    nucleusNormal        = uiState.nucleusNormal,
                    genitalsNormal       = uiState.genitalsNormal,
                    eyeNormal            = uiState.eyeNormal,
                    redReflex            = uiState.redReflex,
                    reactionToSound      = uiState.reactionToSound,
                    others               = uiState.others.ifBlank { null }
                )
                val result = if (uiState.isSet && uiState.investigationId.isNotBlank())
                    apiService.updatePreCheckInvestigation(uiState.investigationId, request)
                else
                    apiService.createPreCheckInvestigation(request)

                when (result) {
                    is ApiResult.Success -> {
                        val saved = result.data
                        uiState = uiState.copy(
                            isSaving              = false,
                            isEditing             = false,
                            isSet                 = true,
                            existingInvestigation = saved,
                            investigationId       = saved.investigationId,
                            successMessage        = "MSG_SAVED"
                        )
                    }
                    is ApiResult.Error -> uiState = uiState.copy(isSaving = false, errorMessage = result.message)
                    else               -> uiState = uiState.copy(isSaving = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isSaving = false, errorMessage = "ERR_SAVE:${e.message}")
            }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun showDeleteConfirm()    { uiState = uiState.copy(showDeleteConfirm = true) }
    fun dismissDeleteConfirm() { uiState = uiState.copy(showDeleteConfirm = false) }

    fun delete() {
        val id = uiState.investigationId.ifBlank { return }
        scope.launch {
            uiState = uiState.copy(isSaving = true, showDeleteConfirm = false)
            try {
                when (val result = apiService.deletePreCheckInvestigation(id)) {
                    is ApiResult.Success -> uiState = PreCheckInvestigationUiState(
                        isSet = false, successMessage = "MSG_DELETED"
                    )
                    is ApiResult.Error   -> uiState = uiState.copy(isSaving = false, errorMessage = result.message)
                    else                 -> uiState = uiState.copy(isSaving = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isSaving = false, errorMessage = "ERR_DELETE:${e.message}")
            }
        }
    }

    // ── Alert helpers ─────────────────────────────────────────────────────────

    fun showNotSetAlert()    { uiState = uiState.copy(showNotSetAlert = true) }
    fun dismissNotSetAlert() { uiState = uiState.copy(showNotSetAlert = false) }

    // ── Message helpers ───────────────────────────────────────────────────────

    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }
    fun clearError()   { uiState = uiState.copy(errorMessage = null) }
    fun onDestroy()    { scope.cancel() }
}
package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.HearingSpeechNet

val HEARING_SPEECH_MILESTONE_MONTHS = listOf(1, 3, 6, 9, 12)

data class HearingSpeechMonthState(
    val checkMonth : Int,
    val recordId   : String  = "",
    val checkDate  : String? = null,
    val notes      : String? = null,
    // Month 1
    val m1StartlesFixatesAttentive: Boolean? = null,
    val m1TurnsToSoundBrief       : Boolean? = null,
    val m1CriesHungerDiscomfort   : Boolean? = null,
    val m1PrefersVoicesOverSounds : Boolean? = null,
    // Month 3
    val m3CalmWithLoudSound    : Boolean? = null,
    val m3CalmsWithMothersVoice: Boolean? = null,
    val m3LocalizesSoundSource : Boolean? = null,
    val m3VocalDuringFeeding   : Boolean? = null,
    val m3RespondsToNearbySound: Boolean? = null,
    // Month 6
    val m6LocatesMothersVoice   : Boolean? = null,
    val m6VocalizesSoundsBabbles: Boolean? = null,
    val m6SmilesImitateSpeech   : Boolean? = null,
    // Month 9
    val m9AwareOfDailySounds        : Boolean? = null,
    val m9AttemptsReciprocalTalking : Boolean? = null,
    val m9CallsForAttention         : Boolean? = null,
    val m9ReduplicatedBabble        : Boolean? = null,
    val m9RespondsToSimpleQuestions : Boolean? = null,
    // Month 12
    val m12RespondsToOwnName        : Boolean? = null,
    val m12MeaningfulWords          : Boolean? = null,
    val m12UnderstandsSimpleCommands: Boolean? = null,
    val m12GivesTakesOnRequest      : Boolean? = null,
)

fun HearingSpeechNet.toMonthState() = HearingSpeechMonthState(
    checkMonth = checkMonth, recordId = recordId, checkDate = checkDate, notes = notes,
    m1StartlesFixatesAttentive = m1StartlesFixatesAttentive,
    m1TurnsToSoundBrief        = m1TurnsToSoundBrief,
    m1CriesHungerDiscomfort    = m1CriesHungerDiscomfort,
    m1PrefersVoicesOverSounds  = m1PrefersVoicesOverSounds,
    m3CalmWithLoudSound     = m3CalmWithLoudSound,
    m3CalmsWithMothersVoice = m3CalmsWithMothersVoice,
    m3LocalizesSoundSource  = m3LocalizesSoundSource,
    m3VocalDuringFeeding    = m3VocalDuringFeeding,
    m3RespondsToNearbySound = m3RespondsToNearbySound,
    m6LocatesMothersVoice   = m6LocatesMothersVoice,
    m6VocalizesSoundsBabbles = m6VocalizesSoundsBabbles,
    m6SmilesImitateSpeech   = m6SmilesImitateSpeech,
    m9AwareOfDailySounds          = m9AwareOfDailySounds,
    m9AttemptsReciprocalTalking   = m9AttemptsReciprocalTalking,
    m9CallsForAttention           = m9CallsForAttention,
    m9ReduplicatedBabble          = m9ReduplicatedBabble,
    m9RespondsToSimpleQuestions   = m9RespondsToSimpleQuestions,
    m12RespondsToOwnName         = m12RespondsToOwnName,
    m12MeaningfulWords           = m12MeaningfulWords,
    m12UnderstandsSimpleCommands = m12UnderstandsSimpleCommands,
    m12GivesTakesOnRequest       = m12GivesTakesOnRequest,
)

fun HearingSpeechMonthState.toNet(babyId: String) = HearingSpeechNet(
    recordId = recordId, babyId = babyId, checkMonth = checkMonth,
    checkDate = checkDate, notes = notes,
    m1StartlesFixatesAttentive = m1StartlesFixatesAttentive,
    m1TurnsToSoundBrief        = m1TurnsToSoundBrief,
    m1CriesHungerDiscomfort    = m1CriesHungerDiscomfort,
    m1PrefersVoicesOverSounds  = m1PrefersVoicesOverSounds,
    m3CalmWithLoudSound     = m3CalmWithLoudSound,
    m3CalmsWithMothersVoice = m3CalmsWithMothersVoice,
    m3LocalizesSoundSource  = m3LocalizesSoundSource,
    m3VocalDuringFeeding    = m3VocalDuringFeeding,
    m3RespondsToNearbySound = m3RespondsToNearbySound,
    m6LocatesMothersVoice   = m6LocatesMothersVoice,
    m6VocalizesSoundsBabbles = m6VocalizesSoundsBabbles,
    m6SmilesImitateSpeech   = m6SmilesImitateSpeech,
    m9AwareOfDailySounds          = m9AwareOfDailySounds,
    m9AttemptsReciprocalTalking   = m9AttemptsReciprocalTalking,
    m9CallsForAttention           = m9CallsForAttention,
    m9ReduplicatedBabble          = m9ReduplicatedBabble,
    m9RespondsToSimpleQuestions   = m9RespondsToSimpleQuestions,
    m12RespondsToOwnName         = m12RespondsToOwnName,
    m12MeaningfulWords           = m12MeaningfulWords,
    m12UnderstandsSimpleCommands = m12UnderstandsSimpleCommands,
    m12GivesTakesOnRequest       = m12GivesTakesOnRequest,
)

data class HearingSpeechUiState(
    val savedRecords  : Map<Int, HearingSpeechMonthState> = emptyMap(),
    val editingMonth  : Int? = null,
    val editingState  : HearingSpeechMonthState? = null,
    val isLoading     : Boolean = false,
    val isSaving      : Boolean = false,
    val successMessage: String? = null,
    val errorMessage  : String? = null,
    val babyAgeMonths : Int = 0,
)

class HearingSpeechViewModel(
    private val apiService        : ApiService,
    private val preferencesManager: PreferencesManager,
) {
    var uiState by mutableStateOf(HearingSpeechUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun load(babyId: String, babyAgeMonths: Int) {
        scope.launch {
            uiState = uiState.copy(isLoading = true, babyAgeMonths = babyAgeMonths)
            try {
                when (val result = apiService.getHearingSpeechRecords(babyId)) {
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

    fun startEditing(month: Int) {
        val existing = uiState.savedRecords[month] ?: HearingSpeechMonthState(checkMonth = month)
        uiState = uiState.copy(editingMonth = month, editingState = existing)
    }

    fun cancelEditing() {
        uiState = uiState.copy(editingMonth = null, editingState = null)
    }

    fun updateField(updater: (HearingSpeechMonthState) -> HearingSpeechMonthState) {
        val current = uiState.editingState ?: return
        uiState = uiState.copy(editingState = updater(current))
    }

    fun save(babyId: String) {
        val state = uiState.editingState ?: return
        scope.launch {
            uiState = uiState.copy(isSaving = true, errorMessage = null)
            try {
                when (val result = apiService.saveHearingSpeechRecord(state.toNet(babyId))) {
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

    fun isMonthEnabled(month: Int): Boolean = uiState.babyAgeMonths >= month

    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }
    fun clearError()   { uiState = uiState.copy(errorMessage = null) }
    fun onDestroy()    { scope.cancel() }
}
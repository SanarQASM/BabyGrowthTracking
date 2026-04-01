package org.example.project.babygrowthtrackingapplication.data.network

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// VisionMotorNet  — Screen 1: بینین + جووڵە (Seeing + Moving)
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class VisionMotorNet(
    val recordId   : String  = "",
    val babyId     : String,
    val checkMonth : Int,
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
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// HearingSpeechNet  — Screen 2: بیستن + ئاغاوتن (Hearing + Talking)
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class HearingSpeechNet(
    val recordId   : String  = "",
    val babyId     : String,
    val checkMonth : Int,
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
    val createdAt: String? = null,
    val updatedAt: String? = null
)
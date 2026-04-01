package com.example.backend_side.controllers

import com.example.backend_side.ResourceNotFoundException
import com.example.backend_side.ApiResponse
import com.example.backend_side.entity.ChildDevelopmentHearingSpeech
import com.example.backend_side.repositories.BabyRepository
import com.example.backend_side.repositories.ChildDevelopmentHearingSpeechRepository
import com.example.backend_side.repositories.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

// ─────────────────────────────────────────────────────────────────────────────
// ChildDevelopmentHearingSpeechController
// Path: /v1/child-development/hearing-speech
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/v1/child-development/hearing-speech")
@CrossOrigin(origins = ["*"])
class ChildDevelopmentHearingSpeechController(
    private val repo    : ChildDevelopmentHearingSpeechRepository,
    private val babyRepo: BabyRepository,
    private val userRepo: UserRepository
) {

    @GetMapping("/baby/{babyId}")
    fun getAllForBaby(@PathVariable babyId: String): ResponseEntity<ApiResponse<List<HearingSpeechResponse>>> {
        val list = repo.findByBaby_BabyIdOrderByCheckMonthAsc(babyId).map { it.toResponse() }
        return ResponseEntity.ok(ApiResponse(true, "Records retrieved", list))
    }

    @GetMapping("/baby/{babyId}/month/{month}")
    fun getByMonth(
        @PathVariable babyId: String,
        @PathVariable month : Int
    ): ResponseEntity<ApiResponse<HearingSpeechResponse?>> {
        val record = repo.findByBaby_BabyIdAndCheckMonth(babyId, month).map { it.toResponse() }.orElse(null)
        return ResponseEntity.ok(ApiResponse(true, "Record retrieved", record))
    }

    @PostMapping
    fun create(@RequestBody req: HearingSpeechRequest): ResponseEntity<ApiResponse<HearingSpeechResponse>> {
        val baby = babyRepo.findById(req.babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found: ${req.babyId}") }

        val existing = repo.findByBaby_BabyIdAndCheckMonth(req.babyId, req.checkMonth).orElse(null)
        val entity = if (existing != null) {
            applyRequest(existing, req)
        } else {
            applyRequest(
                ChildDevelopmentHearingSpeech(recordId = UUID.randomUUID().toString(), baby = baby),
                req
            )
        }
        val saved = repo.save(entity)
        val status = if (existing == null) HttpStatus.CREATED else HttpStatus.OK
        return ResponseEntity.status(status).body(ApiResponse(true, "Saved", saved.toResponse()))
    }

    @PutMapping("/{recordId}")
    fun update(
        @PathVariable recordId: String,
        @RequestBody  req     : HearingSpeechRequest
    ): ResponseEntity<ApiResponse<HearingSpeechResponse>> {
        val entity = repo.findById(recordId)
            .orElseThrow { ResourceNotFoundException("Record not found: $recordId") }
        val saved = repo.save(applyRequest(entity, req))
        return ResponseEntity.ok(ApiResponse(true, "Updated", saved.toResponse()))
    }

    @DeleteMapping("/{recordId}")
    fun delete(@PathVariable recordId: String): ResponseEntity<ApiResponse<Nothing>> {
        if (!repo.existsById(recordId)) throw ResourceNotFoundException("Record not found: $recordId")
        repo.deleteById(recordId)
        return ResponseEntity.ok(ApiResponse(true, "Deleted"))
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun applyRequest(e: ChildDevelopmentHearingSpeech, r: HearingSpeechRequest): ChildDevelopmentHearingSpeech {
        e.checkMonth = r.checkMonth
        e.checkDate  = r.checkDate?.let { LocalDate.parse(it) }
        e.notes      = r.notes
        // Month 1
        e.m1StartlesFixatesAttentive = r.m1StartlesFixatesAttentive
        e.m1TurnsToSoundBrief        = r.m1TurnsToSoundBrief
        e.m1CriesHungerDiscomfort    = r.m1CriesHungerDiscomfort
        e.m1PrefersVoicesOverSounds  = r.m1PrefersVoicesOverSounds
        // Month 3
        e.m3CalmWithLoudSound      = r.m3CalmWithLoudSound
        e.m3CalmsWithMothersVoice  = r.m3CalmsWithMothersVoice
        e.m3LocalizesSoundSource   = r.m3LocalizesSoundSource
        e.m3VocalDuringFeeding     = r.m3VocalDuringFeeding
        e.m3RespondsToNearbySound  = r.m3RespondsToNearbySound
        // Month 6
        e.m6LocatesMothersVoice   = r.m6LocatesMothersVoice
        e.m6VocalizesSoundsBabbles = r.m6VocalizesSoundsBabbles
        e.m6SmilesImitateSpeech    = r.m6SmilesImitateSpeech
        // Month 9
        e.m9AwareOfDailySounds          = r.m9AwareOfDailySounds
        e.m9AttemptsReciprocalTalking   = r.m9AttemptsReciprocalTalking
        e.m9CallsForAttention           = r.m9CallsForAttention
        e.m9ReduplicatedBabble          = r.m9ReduplicatedBabble
        e.m9RespondsToSimpleQuestions   = r.m9RespondsToSimpleQuestions
        // Month 12
        e.m12RespondsToOwnName          = r.m12RespondsToOwnName
        e.m12MeaningfulWords            = r.m12MeaningfulWords
        e.m12UnderstandsSimpleCommands  = r.m12UnderstandsSimpleCommands
        e.m12GivesTakesOnRequest        = r.m12GivesTakesOnRequest
        return e
    }

    private fun ChildDevelopmentHearingSpeech.toResponse() = HearingSpeechResponse(
        recordId   = recordId,
        babyId     = baby?.babyId ?: "",
        checkMonth = checkMonth,
        checkDate  = checkDate?.toString(),
        notes      = notes,
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
        createdAt = createdAt?.toString(),
        updatedAt = updatedAt?.toString()
    )
}

// ── DTOs ──────────────────────────────────────────────────────────────────────

data class HearingSpeechRequest(
    val babyId     : String,
    val checkMonth : Int,
    val checkDate  : String? = null,
    val notes      : String? = null,
    val m1StartlesFixatesAttentive: Boolean? = null,
    val m1TurnsToSoundBrief       : Boolean? = null,
    val m1CriesHungerDiscomfort   : Boolean? = null,
    val m1PrefersVoicesOverSounds : Boolean? = null,
    val m3CalmWithLoudSound    : Boolean? = null,
    val m3CalmsWithMothersVoice: Boolean? = null,
    val m3LocalizesSoundSource : Boolean? = null,
    val m3VocalDuringFeeding   : Boolean? = null,
    val m3RespondsToNearbySound: Boolean? = null,
    val m6LocatesMothersVoice  : Boolean? = null,
    val m6VocalizesSoundsBabbles: Boolean? = null,
    val m6SmilesImitateSpeech  : Boolean? = null,
    val m9AwareOfDailySounds         : Boolean? = null,
    val m9AttemptsReciprocalTalking  : Boolean? = null,
    val m9CallsForAttention          : Boolean? = null,
    val m9ReduplicatedBabble         : Boolean? = null,
    val m9RespondsToSimpleQuestions  : Boolean? = null,
    val m12RespondsToOwnName        : Boolean? = null,
    val m12MeaningfulWords          : Boolean? = null,
    val m12UnderstandsSimpleCommands: Boolean? = null,
    val m12GivesTakesOnRequest      : Boolean? = null,
)

data class HearingSpeechResponse(
    val recordId   : String,
    val babyId     : String,
    val checkMonth : Int,
    val checkDate  : String? = null,
    val notes      : String? = null,
    val m1StartlesFixatesAttentive: Boolean? = null,
    val m1TurnsToSoundBrief       : Boolean? = null,
    val m1CriesHungerDiscomfort   : Boolean? = null,
    val m1PrefersVoicesOverSounds : Boolean? = null,
    val m3CalmWithLoudSound    : Boolean? = null,
    val m3CalmsWithMothersVoice: Boolean? = null,
    val m3LocalizesSoundSource : Boolean? = null,
    val m3VocalDuringFeeding   : Boolean? = null,
    val m3RespondsToNearbySound: Boolean? = null,
    val m6LocatesMothersVoice  : Boolean? = null,
    val m6VocalizesSoundsBabbles: Boolean? = null,
    val m6SmilesImitateSpeech  : Boolean? = null,
    val m9AwareOfDailySounds        : Boolean? = null,
    val m9AttemptsReciprocalTalking : Boolean? = null,
    val m9CallsForAttention         : Boolean? = null,
    val m9ReduplicatedBabble        : Boolean? = null,
    val m9RespondsToSimpleQuestions : Boolean? = null,
    val m12RespondsToOwnName        : Boolean? = null,
    val m12MeaningfulWords          : Boolean? = null,
    val m12UnderstandsSimpleCommands: Boolean? = null,
    val m12GivesTakesOnRequest      : Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
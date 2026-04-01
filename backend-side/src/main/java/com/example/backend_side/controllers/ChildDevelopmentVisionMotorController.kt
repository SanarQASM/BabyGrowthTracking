package com.example.backend_side.controllers

import com.example.backend_side.ResourceNotFoundException
import com.example.backend_side.ApiResponse
import com.example.backend_side.entity.ChildDevelopmentVisionMotor
import com.example.backend_side.repositories.BabyRepository
import com.example.backend_side.repositories.ChildDevelopmentVisionMotorRepository
import com.example.backend_side.repositories.UserRepository
import kotlinx.serialization.Serializable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

// ─────────────────────────────────────────────────────────────────────────────
// ChildDevelopmentVisionMotorController
// Path: /v1/child-development/vision-motor
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/v1/child-development/vision-motor")
@CrossOrigin(origins = ["*"])
class ChildDevelopmentVisionMotorController(
    private val repo      : ChildDevelopmentVisionMotorRepository,
    private val babyRepo  : BabyRepository,
    private val userRepo  : UserRepository
) {

    @GetMapping("/baby/{babyId}")
    fun getAllForBaby(@PathVariable babyId: String): ResponseEntity<ApiResponse<List<VisionMotorResponse>>> {
        val list = repo.findByBaby_BabyIdOrderByCheckMonthAsc(babyId).map { it.toResponse() }
        return ResponseEntity.ok(ApiResponse(true, "Records retrieved", list))
    }

    @GetMapping("/baby/{babyId}/month/{month}")
    fun getByMonth(
        @PathVariable babyId: String,
        @PathVariable month : Int
    ): ResponseEntity<ApiResponse<VisionMotorResponse?>> {
        val record = repo.findByBaby_BabyIdAndCheckMonth(babyId, month).map { it.toResponse() }.orElse(null)
        return ResponseEntity.ok(ApiResponse(true, "Record retrieved", record))
    }

    @PostMapping
    fun create(@RequestBody req: VisionMotorRequest): ResponseEntity<ApiResponse<VisionMotorResponse>> {
        val baby = babyRepo.findById(req.babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found: ${req.babyId}") }

        // Upsert: if record already exists for this month, update it
        val existing = repo.findByBaby_BabyIdAndCheckMonth(req.babyId, req.checkMonth).orElse(null)
        val entity = if (existing != null) {
            applyRequest(existing, req)
        } else {
            applyRequest(
                ChildDevelopmentVisionMotor(recordId = UUID.randomUUID().toString(), baby = baby),
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
        @RequestBody  req     : VisionMotorRequest
    ): ResponseEntity<ApiResponse<VisionMotorResponse>> {
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

    private fun applyRequest(e: ChildDevelopmentVisionMotor, r: VisionMotorRequest): ChildDevelopmentVisionMotor {
        e.checkMonth = r.checkMonth
        e.checkDate  = r.checkDate?.let { LocalDate.parse(it) }
        e.notes      = r.notes

        // Month 1
        e.m1HeadMovesFollowsLight = r.m1HeadMovesFollowsLight
        e.m1TracksPeopleObjects   = r.m1TracksPeopleObjects
        e.m1FollowsFlashlight     = r.m1FollowsFlashlight
        // Month 3
        e.m3Head180Tracking       = r.m3Head180Tracking
        e.m3AttentiveFaceTracking = r.m3AttentiveFaceTracking
        e.m3WatchesOwnHands       = r.m3WatchesOwnHands
        e.m3RecognizesMother      = r.m3RecognizesMother
        e.m3HandsOpenReflex       = r.m3HandsOpenReflex
        // Month 6
        e.m6EyesHeadFullRange       = r.m6EyesHeadFullRange
        e.m6FollowsPersonAcrossRoom = r.m6FollowsPersonAcrossRoom
        e.m6SmilesAtMirror          = r.m6SmilesAtMirror
        e.m6ReachesForDroppedObject = r.m6ReachesForDroppedObject
        e.m6TransfersObjects        = r.m6TransfersObjects
        // Month 9
        e.m9KeenVisualAttention   = r.m9KeenVisualAttention
        e.m9PincerGrasp           = r.m9PincerGrasp
        e.m9ReachesDesiredObjects = r.m9ReachesDesiredObjects
        e.m9AttentionSpan         = r.m9AttentionSpan
        // Month 12
        e.m12NeatPincerGrasp         = r.m12NeatPincerGrasp
        e.m12PlaysWithToys           = r.m12PlaysWithToys
        e.m12ReleasesObjects         = r.m12ReleasesObjects
        e.m12RecognizesFamiliarPeople = r.m12RecognizesFamiliarPeople
        e.m12GetsAttentionByTugging  = r.m12GetsAttentionByTugging

        return e
    }

    private fun ChildDevelopmentVisionMotor.toResponse() = VisionMotorResponse(
        recordId   = recordId,
        babyId     = baby?.babyId ?: "",
        checkMonth = checkMonth,
        checkDate  = checkDate?.toString(),
        notes      = notes,
        // Month 1
        m1HeadMovesFollowsLight = m1HeadMovesFollowsLight,
        m1TracksPeopleObjects   = m1TracksPeopleObjects,
        m1FollowsFlashlight     = m1FollowsFlashlight,
        // Month 3
        m3Head180Tracking       = m3Head180Tracking,
        m3AttentiveFaceTracking = m3AttentiveFaceTracking,
        m3WatchesOwnHands       = m3WatchesOwnHands,
        m3RecognizesMother      = m3RecognizesMother,
        m3HandsOpenReflex       = m3HandsOpenReflex,
        // Month 6
        m6EyesHeadFullRange       = m6EyesHeadFullRange,
        m6FollowsPersonAcrossRoom = m6FollowsPersonAcrossRoom,
        m6SmilesAtMirror          = m6SmilesAtMirror,
        m6ReachesForDroppedObject = m6ReachesForDroppedObject,
        m6TransfersObjects        = m6TransfersObjects,
        // Month 9
        m9KeenVisualAttention   = m9KeenVisualAttention,
        m9PincerGrasp           = m9PincerGrasp,
        m9ReachesDesiredObjects = m9ReachesDesiredObjects,
        m9AttentionSpan         = m9AttentionSpan,
        // Month 12
        m12NeatPincerGrasp           = m12NeatPincerGrasp,
        m12PlaysWithToys             = m12PlaysWithToys,
        m12ReleasesObjects           = m12ReleasesObjects,
        m12RecognizesFamiliarPeople  = m12RecognizesFamiliarPeople,
        m12GetsAttentionByTugging    = m12GetsAttentionByTugging,
        createdAt = createdAt?.toString(),
        updatedAt = updatedAt?.toString()
    )
}

// ── DTOs ──────────────────────────────────────────────────────────────────────

data class VisionMotorRequest(
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
)

data class VisionMotorResponse(
    val recordId   : String,
    val babyId     : String,
    val checkMonth : Int,
    val checkDate  : String? = null,
    val notes      : String? = null,
    val m1HeadMovesFollowsLight: Boolean? = null,
    val m1TracksPeopleObjects  : Boolean? = null,
    val m1FollowsFlashlight    : Boolean? = null,
    val m3Head180Tracking      : Boolean? = null,
    val m3AttentiveFaceTracking: Boolean? = null,
    val m3WatchesOwnHands      : Boolean? = null,
    val m3RecognizesMother     : Boolean? = null,
    val m3HandsOpenReflex      : Boolean? = null,
    val m6EyesHeadFullRange      : Boolean? = null,
    val m6FollowsPersonAcrossRoom: Boolean? = null,
    val m6SmilesAtMirror         : Boolean? = null,
    val m6ReachesForDroppedObject: Boolean? = null,
    val m6TransfersObjects       : Boolean? = null,
    val m9KeenVisualAttention  : Boolean? = null,
    val m9PincerGrasp          : Boolean? = null,
    val m9ReachesDesiredObjects: Boolean? = null,
    val m9AttentionSpan        : Boolean? = null,
    val m12NeatPincerGrasp          : Boolean? = null,
    val m12PlaysWithToys            : Boolean? = null,
    val m12ReleasesObjects          : Boolean? = null,
    val m12RecognizesFamiliarPeople : Boolean? = null,
    val m12GetsAttentionByTugging   : Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
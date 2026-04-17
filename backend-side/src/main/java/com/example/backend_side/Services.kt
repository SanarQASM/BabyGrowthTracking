package com.example.backend_side

import com.example.backend_side.repositories.*
import com.example.backend_side.entity.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.util.UUID

private val logger = KotlinLogging.logger {}

// ============================================================
// USER SERVICE
// ============================================================

interface UserService {
    fun createUser(request: UserCreateRequest): UserResponse
    fun getUserById(userId: String): UserResponse
    fun updateUser(userId: String, request: UserUpdateRequest): UserResponse
    fun deleteUser(userId: String)
    fun getAllUsers(pageable: Pageable): Page<UserResponse>
    fun getUserByEmail(email: String): UserResponse
}

@Service
@Transactional
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    override fun createUser(request: UserCreateRequest): UserResponse {
        logger.info { "Creating new user with email: ${request.email}" }

        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("User with email ${request.email} already exists")
        }

        val user = User(
            userId = UUID.randomUUID().toString(),
            fullName = request.fullName,
            email = request.email,
            password = request.password?.let { passwordEncoder.encode(it) },
            phone = request.phone,
            address = request.address,
            city = request.city,
            profileImageUrl = request.profileImageUrl,
            role = request.role
        )

        val savedUser = userRepository.save(user)
        logger.info { "User created successfully with ID: ${savedUser.userId}" }

        return savedUser.toResponse()
    }

    override fun getUserById(userId: String): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with ID: $userId") }
        return user.toResponse()
    }

    override fun updateUser(userId: String, request: UserUpdateRequest): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with ID: $userId") }

        request.fullName?.let { user.fullName = it }
        request.phone?.let { user.phone = it }
        request.address?.let { user.address = it }
        request.city?.let { user.city = it }
        request.profileImageUrl?.let { user.profileImageUrl = it }

        val updatedUser = userRepository.save(user)
        logger.info { "User updated successfully: ${updatedUser.userId}" }

        return updatedUser.toResponse()
    }

    override fun deleteUser(userId: String) {
        if (!userRepository.existsById(userId)) {
            throw ResourceNotFoundException("User not found with ID: $userId")
        }
        userRepository.deleteById(userId)
        logger.info { "User deleted: $userId" }
    }

    override fun getAllUsers(pageable: Pageable): Page<UserResponse> {
        return userRepository.findAll(pageable).map { it.toResponse() }
    }

    override fun getUserByEmail(email: String): UserResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("User not found with email: $email") }
        return user.toResponse()
    }

    private fun User.toResponse() = UserResponse(
        userId = userId,
        fullName = fullName,
        email = email,
        phone = phone,
        address = address,
        city = city,
        profileImageUrl = profileImageUrl,
        role = role,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// ============================================================
// BABY SERVICE
// ============================================================

interface BabyService {
    fun createBaby(parentUserId: String, request: BabyCreateRequest): BabyResponse
    fun getBabyById(babyId: String): BabyResponse
    fun updateBaby(babyId: String, request: BabyUpdateRequest): BabyResponse
    fun deleteBaby(babyId: String)
    fun getBabiesByParent(parentUserId: String): List<BabyResponse>
    fun getAllBabies(pageable: Pageable): Page<BabyResponse>
}

@Service
@Transactional
class BabyServiceImpl(
    private val babyRepository: BabyRepository,
    private val userRepository: UserRepository
) : BabyService {

    override fun createBaby(parentUserId: String, request: BabyCreateRequest): BabyResponse {
        logger.info { "Creating new baby for parent: $parentUserId" }

        val parent = userRepository.findById(parentUserId)
            .orElseThrow { ResourceNotFoundException("Parent not found with ID: $parentUserId") }

        val baby = Baby(
            babyId = UUID.randomUUID().toString(),
            parentUser = parent,
            fullName = request.fullName,
            dateOfBirth = java.time.LocalDate.parse(request.dateOfBirth),  // String -> LocalDate
            gender = enumValueOf<com.example.backend_side.entity.Gender>(request.gender.uppercase()), // String -> Gender
            birthWeight = request.birthWeight?.let { java.math.BigDecimal.valueOf(it) },
            birthHeight = request.birthHeight?.let { java.math.BigDecimal.valueOf(it) },
            birthHeadCircumference = request.birthHeadCircumference?.let { java.math.BigDecimal.valueOf(it) },
            photoUrl = request.photoUrl
        )

        val savedBaby = babyRepository.save(baby)
        logger.info { "Baby created successfully with ID: ${savedBaby.babyId}" }

        return savedBaby.toResponse()
    }

    override fun getBabyById(babyId: String): BabyResponse {
        val baby = babyRepository.findById(babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found with ID: $babyId") }
        return baby.toResponse()
    }

    override fun updateBaby(babyId: String, request: BabyUpdateRequest): BabyResponse {
        val baby = babyRepository.findById(babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found with ID: $babyId") }

        request.fullName?.let { baby.fullName = it }
        request.photoUrl?.let { baby.photoUrl = it }
        request.isActive?.let { baby.isActive = it }

        val updatedBaby = babyRepository.save(baby)
        logger.info { "Baby updated successfully: ${updatedBaby.babyId}" }

        return updatedBaby.toResponse()
    }

    override fun deleteBaby(babyId: String) {
        if (!babyRepository.existsById(babyId)) {
            throw ResourceNotFoundException("Baby not found with ID: $babyId")
        }
        babyRepository.deleteById(babyId)
        logger.info { "Baby deleted: $babyId" }
    }

    override fun getBabiesByParent(parentUserId: String): List<BabyResponse> {
        return babyRepository.findByParentUser_UserId(parentUserId).map { it.toResponse() }
    }

    override fun getAllBabies(pageable: Pageable): Page<BabyResponse> {
        return babyRepository.findAll(pageable).map { it.toResponse() }
    }

    private fun Baby.toResponse() = BabyResponse(
        babyId = babyId,
        parentUserId = parentUser?.userId ?: "",
        parentName = parentUser?.fullName ?: "",
        fullName = fullName,
        dateOfBirth = dateOfBirth.toString(),                   // LocalDate -> "YYYY-MM-DD"
        gender = gender.name,                                   // Gender enum -> "BOY"/"GIRL"
        birthWeight = birthWeight?.toDouble(),                  // BigDecimal -> Double
        birthHeight = birthHeight?.toDouble(),                  // BigDecimal -> Double
        birthHeadCircumference = birthHeadCircumference?.toDouble(), // BigDecimal -> Double
        photoUrl = photoUrl,
        ageInMonths = getAgeInMonths(),
        ageInDays = getAgeInDays(),
        isActive = isActive,
        createdAt = createdAt?.toString(),                      // LocalDateTime -> ISO String
        updatedAt = updatedAt?.toString()                       // LocalDateTime -> ISO String
    )
}

// ============================================================
// GROWTH RECORD SERVICE
// ============================================================

interface GrowthRecordService {
    fun createGrowthRecord(measuredBy: String, request: GrowthRecordCreateRequest): GrowthRecordResponse
    fun getGrowthRecordById(recordId: String): GrowthRecordResponse
    fun getGrowthRecordsByBaby(babyId: String): List<GrowthRecordResponse>
    fun getLatestGrowthRecord(babyId: String): GrowthRecordResponse?
    fun deleteGrowthRecord(recordId: String)
}
@Service
@Transactional
class GrowthRecordServiceImpl(
    private val growthRecordRepository: GrowthRecordRepository,
    private val babyRepository        : BabyRepository,
    private val userRepository        : UserRepository
) : GrowthRecordService {

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    //
    // FIX: Two changes to fix the parent/team color mix-up in the chart:
    //
    //  1. measuredByName = null   when the measurer IS the baby's parent.
    //     measuredByName = name   when the measurer is an external/team member.
    //
    //  2. isTeamMeasurement = false  when parent adds the record.
    //     isTeamMeasurement = true   when a team/external member adds the record.
    //
    // The client (ChartsTabContent.kt) uses GrowthRecordResponse.addedByTeam
    // which checks isTeamMeasurement first, then falls back to measuredByName.
    // ─────────────────────────────────────────────────────────────────────────

    override fun createGrowthRecord(
        measuredBy: String,
        request: GrowthRecordCreateRequest
    ): GrowthRecordResponse {
        val baby = babyRepository.findById(request.babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found: ${request.babyId}") }

        val measurer = userRepository.findById(measuredBy).orElse(null)

        // FIX: Is the person adding this record the baby's own parent?
        val isParentMeasurement = (baby.parentUser?.userId == measuredBy)

        val record = GrowthRecord(
            recordId                    = UUID.randomUUID().toString(),
            baby                        = baby,
            measurementDate             = request.measurementDate,
            ageInMonths                 = calculateAgeInMonths(baby.dateOfBirth, request.measurementDate),
            ageInDays                   = calculateAgeInDays(baby.dateOfBirth, request.measurementDate),
            weight                      = request.weight,
            height                      = request.height,
            headCircumference           = request.headCircumference,
            weightPercentile            = request.weightPercentile,
            heightPercentile            = request.heightPercentile,
            headCircumferencePercentile = request.headCircumferencePercentile,
            measuredBy                  = measurer
        )

        val saved = growthRecordRepository.save(record)
        logger.info { "Growth record saved: ${saved.recordId} | parent=$isParentMeasurement | measurer=$measuredBy" }

        return saved.toResponse(isParentMeasurement = isParentMeasurement)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ — single record
    // ─────────────────────────────────────────────────────────────────────────

    override fun getGrowthRecordById(recordId: String): GrowthRecordResponse {
        val record = growthRecordRepository.findById(recordId)
            .orElseThrow { ResourceNotFoundException("Growth record not found: $recordId") }
        val isParent =record.baby?.parentUser?.userId != null &&
                record.baby?.parentUser?.userId == record.measuredBy?.userId
        return record.toResponse(isParentMeasurement = isParent)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ — all records for a baby
    // Each record's measuredBy is compared to baby.parent.userId individually.
    // ─────────────────────────────────────────────────────────────────────────

    override fun getGrowthRecordsByBaby(babyId: String): List<GrowthRecordResponse> {
        val records = growthRecordRepository.findByBaby_BabyIdOrderByMeasurementDateDesc(babyId)
        return records.map { record ->
            val isParent = record.baby?.parentUser?.userId != null &&
                    record.baby?.parentUser?.userId == record.measuredBy?.userId
            record.toResponse(isParentMeasurement = isParent)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ — latest record for a baby
    // ─────────────────────────────────────────────────────────────────────────

    override fun getLatestGrowthRecord(babyId: String): GrowthRecordResponse? {
        val record = growthRecordRepository
            .findTopByBabyBabyIdOrderByMeasurementDateDesc(babyId)
            .orElse(null) ?: return null
        val isParent = record.baby?.parentUser?.userId != null &&
                record.baby?.parentUser?.userId == record.measuredBy?.userId
        return record.toResponse(isParentMeasurement = isParent)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    override fun deleteGrowthRecord(recordId: String) {
        if (!growthRecordRepository.existsById(recordId)) {
            throw ResourceNotFoundException("Growth record not found: $recordId")
        }
        growthRecordRepository.deleteById(recordId)
        logger.info { "Growth record deleted: $recordId" }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAPPER — entity → response DTO
    //
    // isParentMeasurement controls two fields:
    //   measuredByName    : null  if parent | measurer's full name if team
    //   isTeamMeasurement : false if parent | true if team
    // ─────────────────────────────────────────────────────────────────────────

    private fun GrowthRecord.toResponse(isParentMeasurement: Boolean): GrowthRecordResponse {
        return GrowthRecordResponse(
            recordId                    = recordId,
            babyId                      = baby?.babyId ?: "",
            babyName                    = baby?.fullName ?: "",
            measurementDate             = measurementDate,
            ageInMonths                 = ageInMonths,
            ageInDays                   = ageInDays,
            weight                      = weight,
            height                      = height,
            headCircumference           = headCircumference,
            weightPercentile            = weightPercentile,
            heightPercentile            = heightPercentile,
            headCircumferencePercentile = headCircumferencePercentile,
            // FIX: null name + false flag for parent, name + true flag for team
            measuredByName              = if (isParentMeasurement) null else measuredBy?.fullName,
            isTeamMeasurement           = !isParentMeasurement,
            createdAt                   = createdAt?.toString(),
            updatedAt                   = updatedAt?.toString()
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private fun calculateAgeInMonths(dateOfBirth: LocalDate, measurementDate: LocalDate): Int {
        var months = (measurementDate.year - dateOfBirth.year) * 12 +
                (measurementDate.monthValue - dateOfBirth.monthValue)
        if (measurementDate.dayOfMonth < dateOfBirth.dayOfMonth) months--
        return months.coerceAtLeast(0)
    }

    private fun calculateAgeInDays(dateOfBirth: LocalDate, measurementDate: LocalDate): Int {
        return (measurementDate.toEpochDay() - dateOfBirth.toEpochDay()).toInt().coerceAtLeast(0)
    }
}
// ============================================================
// VACCINATION SERVICE
// ============================================================

interface VaccinationService {
    fun createVaccination(request: VaccinationCreateRequest): VaccinationResponse
    fun getVaccinationById(vaccinationId: String): VaccinationResponse
    fun updateVaccination(vaccinationId: String, request: VaccinationUpdateRequest): VaccinationResponse
    fun getVaccinationsByBaby(babyId: String): List<VaccinationResponse>
    fun getUpcomingVaccinations(babyId: String): List<VaccinationResponse>
    fun markAsCompleted(vaccinationId: String, administeredBy: String, administeredDate: LocalDate): VaccinationResponse
}

@Service
@Transactional
class VaccinationServiceImpl(
    private val vaccinationRepository: VaccinationRepository,
    private val babyRepository: BabyRepository,
    private val vaccineTypeRepository: VaccineTypeRepository,
    private val userRepository: UserRepository
) : VaccinationService {

    override fun createVaccination(request: VaccinationCreateRequest): VaccinationResponse {
        logger.info { "Creating vaccination for baby: ${request.babyId}" }

        val baby = babyRepository.findById(request.babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found with ID: ${request.babyId}") }

        val vaccineType = vaccineTypeRepository.findById(request.vaccineId)
            .orElseThrow { ResourceNotFoundException("Vaccine type not found with ID: ${request.vaccineId}") }

        val vaccination = Vaccination(
            vaccinationId = UUID.randomUUID().toString(),
            baby = baby,
            vaccineType = vaccineType,
            scheduledDate = request.scheduledDate,
            location = request.location,
            notes = request.notes
        )

        val savedVaccination = vaccinationRepository.save(vaccination)
        logger.info { "Vaccination created successfully with ID: ${savedVaccination.vaccinationId}" }

        return savedVaccination.toResponse()
    }

    override fun getVaccinationById(vaccinationId: String): VaccinationResponse {
        val vaccination = vaccinationRepository.findById(vaccinationId)
            .orElseThrow { ResourceNotFoundException("Vaccination not found with ID: $vaccinationId") }
        return vaccination.toResponse()
    }

    override fun updateVaccination(vaccinationId: String, request: VaccinationUpdateRequest): VaccinationResponse {
        val vaccination = vaccinationRepository.findById(vaccinationId)
            .orElseThrow { ResourceNotFoundException("Vaccination not found with ID: $vaccinationId") }

        request.administeredDate?.let { vaccination.administeredDate = it }
        request.status?.let { vaccination.status = it }
        request.certificateUrl?.let { vaccination.certificateUrl = it }
        request.batchNumber?.let { vaccination.batchNumber = it }
        request.location?.let { vaccination.location = it }
        request.notes?.let { vaccination.notes = it }

        val updatedVaccination = vaccinationRepository.save(vaccination)
        logger.info { "Vaccination updated successfully: ${updatedVaccination.vaccinationId}" }

        return updatedVaccination.toResponse()
    }

    override fun getVaccinationsByBaby(babyId: String): List<VaccinationResponse> {
        return vaccinationRepository.findByBaby_BabyId(babyId).map { it.toResponse() }
    }

    override fun getUpcomingVaccinations(babyId: String): List<VaccinationResponse> {
        return vaccinationRepository.findByBaby_BabyIdAndStatus(babyId, VaccinationStatus.SCHEDULED)
            .map { it.toResponse() }
    }

    override fun markAsCompleted(vaccinationId: String, administeredBy: String, administeredDate: LocalDate): VaccinationResponse {
        val vaccination = vaccinationRepository.findById(vaccinationId)
            .orElseThrow { ResourceNotFoundException("Vaccination not found with ID: $vaccinationId") }

        val user = userRepository.findById(administeredBy)
            .orElseThrow { ResourceNotFoundException("User not found with ID: $administeredBy") }

        vaccination.apply {
            status = VaccinationStatus.COMPLETED
            this.administeredDate = administeredDate
            this.administeredBy = user
        }

        val updatedVaccination = vaccinationRepository.save(vaccination)
        logger.info { "Vaccination marked as completed: $vaccinationId" }

        return updatedVaccination.toResponse()
    }

    private fun Vaccination.toResponse() = VaccinationResponse(
        vaccinationId = vaccinationId,
        babyId = baby?.babyId ?: "",
        babyName = baby?.fullName ?: "",
        vaccineId = vaccineType?.vaccineId ?: 0,
        vaccineName = vaccineType?.vaccineName ?: "",
        recommendedAgeMonths = vaccineType?.recommendedAgeMonths ?: 0,
        // ✅ doseNumber is now Byte in VaccineType — no conversion needed
        doseNumber = vaccineType?.doseNumber ?: 1,
        scheduledDate = scheduledDate,
        administeredDate = administeredDate,
        status = status,
        administeredByName = administeredBy?.fullName,
        certificateUrl = certificateUrl,
        batchNumber = batchNumber,
        location = location,
        notes = notes,
        createdAt = createdAt
    )
}

// ============================================================
// MEMORY SERVICE
// ============================================================

interface MemoryService {
    fun createMemory(parentUserId: String, request: MemoryCreateRequest): MemoryResponse
    fun getMemoryById(memoryId: String): MemoryResponse
    fun getMemoriesByBaby(babyId: String): List<MemoryResponse>
    fun deleteMemory(memoryId: String)
}

@Service
@Transactional
class MemoryServiceImpl(
    private val memoryRepository      : MemoryRepository,
    private val memoryImageRepository : MemoryImageRepository,
    private val babyRepository        : BabyRepository,
    private val userRepository        : UserRepository
) : MemoryService {

    override fun createMemory(parentUserId: String, request: MemoryCreateRequest): MemoryResponse {
        logger.info { "Creating memory for baby: ${request.babyId}" }

        // ── 1. Resolve Baby entity from the String ID ─────────────────────────
        //    This is what the old controller was missing — it never looked up
        //    the Baby, so memory.baby stayed null and baby_id was null in the DB.
        val baby = babyRepository.findById(request.babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found with ID: ${request.babyId}") }

        // ── 2. Resolve Parent User entity ─────────────────────────────────────
        val parent = userRepository.findById(parentUserId)
            .orElseThrow { ResourceNotFoundException("Parent not found with ID: $parentUserId") }

        // ── 3. Compute age at memory date ─────────────────────────────────────
        val ageInMonths = Period.between(baby.dateOfBirth, request.memoryDate).toTotalMonths().toInt()
        val ageInDays   = ChronoUnit.DAYS.between(baby.dateOfBirth, request.memoryDate).toInt()

        // ── 4. Build and persist the Memory entity ────────────────────────────
        val memory = Memory(
            memoryId     = UUID.randomUUID().toString(),
            baby         = baby,          // ← entity reference, not a String
            parentUser   = parent,        // ← entity reference, not a String
            title        = request.title,
            description  = request.description,
            memoryDate   = request.memoryDate,
            ageInMonths  = ageInMonths,
            ageInDays    = ageInDays,
            // ── NEW: store image metadata sent by the client ──────────────────
            imageCount   = request.imageCount,
            captionsJson = encodeCaptions(request.captions)
        )

        val savedMemory = memoryRepository.save(memory)
        logger.info { "Memory created successfully with ID: ${savedMemory.memoryId}" }

        // ── 5. Save MemoryImage records if imageUrls were supplied ────────────
        //    (kept for backward compat — imageUrls may be empty for local-only mode)
        request.imageUrls.forEachIndexed { index, imageUrl ->
            val memoryImage = MemoryImage(
                memory    = savedMemory,
                imageUrl  = imageUrl,
                caption   = request.imageCaptions.getOrNull(index),
                sortOrder = index
            )
            savedMemory.images.add(memoryImage)
        }

        val finalMemory = memoryRepository.save(savedMemory)
        return finalMemory.toResponse()
    }

    override fun getMemoryById(memoryId: String): MemoryResponse {
        val memory = memoryRepository.findById(memoryId)
            .orElseThrow { ResourceNotFoundException("Memory not found with ID: $memoryId") }
        return memory.toResponse()
    }

    override fun getMemoriesByBaby(babyId: String): List<MemoryResponse> =
        memoryRepository.findByBaby_BabyIdOrderByMemoryDateDesc(babyId)
            .map { it.toResponse() }

    override fun deleteMemory(memoryId: String) {
        if (!memoryRepository.existsById(memoryId)) {
            throw ResourceNotFoundException("Memory not found with ID: $memoryId")
        }
        memoryRepository.deleteById(memoryId)
        logger.info { "Memory deleted: $memoryId" }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Encode a list of caption strings to a simple JSON array string. */
    private fun encodeCaptions(captions: List<String?>?): String? {
        if (captions.isNullOrEmpty()) return null
        val escaped = captions.map { c ->
            "\"${c?.replace("\"", "\\\"") ?: ""}\""
        }
        return "[${escaped.joinToString(",")}]"
    }

    /** Map Memory entity → MemoryResponse DTO. */
    private fun Memory.toResponse() = MemoryResponse(
        memoryId    = memoryId,
        babyId      = baby?.babyId      ?: "",
        babyName    = baby?.fullName     ?: "",
        parentName  = parentUser?.fullName ?: "",
        title       = title,
        description = description,
        memoryDate  = memoryDate,
        ageInMonths = ageInMonths,
        ageInDays   = ageInDays,
        images      = images.map { img ->
            MemoryImageResponse(
                imageId   = img.imageId ?: 0,
                imageUrl  = img.imageUrl,
                caption   = img.caption,
                sortOrder = img.sortOrder
            )
        },
        createdAt = createdAt
    )
}
// ============================================================
// APPOINTMENT SERVICE
// ============================================================

interface AppointmentService {
    fun createAppointment(request: AppointmentCreateRequest): AppointmentResponse
    fun getAppointmentById(appointmentId: String): AppointmentResponse
    fun updateAppointment(appointmentId: String, request: AppointmentUpdateRequest): AppointmentResponse
    fun getAppointmentsByBaby(babyId: String): List<AppointmentResponse>
    fun getUpcomingAppointments(babyId: String): List<AppointmentResponse>
    fun deleteAppointment(appointmentId: String)
}

@Service
@Transactional
class AppointmentServiceImpl(
    private val appointmentRepository: AppointmentRepository,
    private val babyRepository: BabyRepository
) : AppointmentService {

    override fun createAppointment(request: AppointmentCreateRequest): AppointmentResponse {
        logger.info { "Creating appointment for baby: ${request.babyId}" }

        val baby = babyRepository.findById(request.babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found with ID: ${request.babyId}") }

        val appointment = Appointment(
            appointmentId = UUID.randomUUID().toString(),
            baby = baby,
            appointmentType = request.appointmentType,
            scheduledDate = request.scheduledDate,
            scheduledTime = request.scheduledTime?.let { java.time.LocalTime.parse(it) },
            durationMinutes = request.durationMinutes,
            doctorName = request.doctorName,
            location = request.location,
            notes = request.notes
        )

        val savedAppointment = appointmentRepository.save(appointment)
        logger.info { "Appointment created successfully with ID: ${savedAppointment.appointmentId}" }

        return savedAppointment.toResponse()
    }

    override fun getAppointmentById(appointmentId: String): AppointmentResponse {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { ResourceNotFoundException("Appointment not found with ID: $appointmentId") }
        return appointment.toResponse()
    }

    override fun updateAppointment(appointmentId: String, request: AppointmentUpdateRequest): AppointmentResponse {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { ResourceNotFoundException("Appointment not found with ID: $appointmentId") }

        request.scheduledDate?.let { appointment.scheduledDate = it }
        request.scheduledTime?.let { appointment.scheduledTime = java.time.LocalTime.parse(it) }
        request.status?.let { appointment.status = it }
        request.doctorName?.let { appointment.doctorName = it }
        request.location?.let { appointment.location = it }
        request.notes?.let { appointment.notes = it }

        val updatedAppointment = appointmentRepository.save(appointment)
        logger.info { "Appointment updated successfully: ${updatedAppointment.appointmentId}" }

        return updatedAppointment.toResponse()
    }

    override fun getAppointmentsByBaby(babyId: String): List<AppointmentResponse> {
        return appointmentRepository.findByBaby_BabyIdOrderByScheduledDateAsc(babyId)
            .map { it.toResponse() }
    }

    override fun getUpcomingAppointments(babyId: String): List<AppointmentResponse> {
        return appointmentRepository.findUpcomingAppointmentsForBaby(babyId)
            .map { it.toResponse() }
    }

    override fun deleteAppointment(appointmentId: String) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw ResourceNotFoundException("Appointment not found with ID: $appointmentId")
        }
        appointmentRepository.deleteById(appointmentId)
        logger.info { "Appointment deleted: $appointmentId" }
    }

    private fun Appointment.toResponse() = AppointmentResponse(
        appointmentId = appointmentId,
        babyId = baby?.babyId ?: "",
        babyName = baby?.fullName ?: "",
        appointmentType = appointmentType ?: AppointmentType.CHECKUP,
        scheduledDate = scheduledDate,
        scheduledTime = scheduledTime?.toString(),
        durationMinutes = durationMinutes,
        status = status,
        doctorName = doctorName,
        location = location,
        notes = notes,
        reminderSent = reminderSent,
        createdAt = createdAt
    )
}
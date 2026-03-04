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
    private val babyRepository: BabyRepository,
    private val userRepository: UserRepository
) : GrowthRecordService {

    override fun createGrowthRecord(measuredBy: String, request: GrowthRecordCreateRequest): GrowthRecordResponse {
        logger.info { "Creating growth record for baby: ${request.babyId}" }

        val baby = babyRepository.findById(request.babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found with ID: ${request.babyId}") }

        val measurer = userRepository.findById(measuredBy)
            .orElseThrow { ResourceNotFoundException("User not found with ID: $measuredBy") }

        val ageInMonths = Period.between(baby.dateOfBirth, request.measurementDate).toTotalMonths().toInt()
        val ageInDays = ChronoUnit.DAYS.between(baby.dateOfBirth, request.measurementDate).toInt()

        val growthRecord = GrowthRecord(
            recordId = UUID.randomUUID().toString(),
            baby = baby,
            measurementDate = request.measurementDate,
            ageInMonths = ageInMonths,
            ageInDays = ageInDays,
            weight = request.weight,
            height = request.height,
            headCircumference = request.headCircumference,
            // ✅ percentile fields are Byte? in entity — convert directly from request
            weightPercentile = request.weightPercentile,
            heightPercentile = request.heightPercentile,
            headCircumferencePercentile = request.headCircumferencePercentile,
            measuredBy = measurer
        )

        val savedRecord = growthRecordRepository.save(growthRecord)
        logger.info { "Growth record created successfully with ID: ${savedRecord.recordId}" }

        return savedRecord.toResponse()
    }

    override fun getGrowthRecordById(recordId: String): GrowthRecordResponse {
        val record = growthRecordRepository.findById(recordId)
            .orElseThrow { ResourceNotFoundException("Growth record not found with ID: $recordId") }
        return record.toResponse()
    }

    override fun getGrowthRecordsByBaby(babyId: String): List<GrowthRecordResponse> {
        return growthRecordRepository.findByBaby_BabyIdOrderByMeasurementDateDesc(babyId)
            .map { it.toResponse() }
    }

    override fun getLatestGrowthRecord(babyId: String): GrowthRecordResponse? {
        return growthRecordRepository.findLatestByBabyId(babyId)
            .map { it.toResponse() }
            .orElse(null)
    }

    override fun deleteGrowthRecord(recordId: String) {
        if (!growthRecordRepository.existsById(recordId)) {
            throw ResourceNotFoundException("Growth record not found with ID: $recordId")
        }
        growthRecordRepository.deleteById(recordId)
        logger.info { "Growth record deleted: $recordId" }
    }

    private fun GrowthRecord.toResponse() = GrowthRecordResponse(
        recordId = recordId,
        babyId = baby?.babyId ?: "",
        babyName = baby?.fullName ?: "",
        measurementDate = measurementDate,
        ageInMonths = ageInMonths,
        ageInDays = ageInDays,
        weight = weight,
        height = height,
        headCircumference = headCircumference,
        // ✅ already Byte? — no conversion needed
        weightPercentile = weightPercentile,
        heightPercentile = heightPercentile,
        headCircumferencePercentile = headCircumferencePercentile,
        measuredByName = measuredBy?.fullName,
        notes = null,
        createdAt = createdAt.toString()                   // ← LocalDateTime? → String?
    )
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
    private val memoryRepository: MemoryRepository,
    private val memoryImageRepository: MemoryImageRepository,
    private val babyRepository: BabyRepository,
    private val userRepository: UserRepository
) : MemoryService {

    override fun createMemory(parentUserId: String, request: MemoryCreateRequest): MemoryResponse {
        logger.info { "Creating memory for baby: ${request.babyId}" }

        val baby = babyRepository.findById(request.babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found with ID: ${request.babyId}") }

        val parent = userRepository.findById(parentUserId)
            .orElseThrow { ResourceNotFoundException("Parent not found with ID: $parentUserId") }

        val ageInMonths = Period.between(baby.dateOfBirth, request.memoryDate).toTotalMonths().toInt()
        val ageInDays = ChronoUnit.DAYS.between(baby.dateOfBirth, request.memoryDate).toInt()

        val memory = Memory(
            memoryId = UUID.randomUUID().toString(),
            baby = baby,
            parentUser = parent,
            title = request.title,
            description = request.description,
            memoryDate = request.memoryDate,
            ageInMonths = ageInMonths,
            ageInDays = ageInDays
        )

        val savedMemory = memoryRepository.save(memory)

        // Add images if provided
        request.imageUrls.forEachIndexed { index, imageUrl ->
            val memoryImage = MemoryImage(
                memory = savedMemory,
                imageUrl = imageUrl,
                caption = request.imageCaptions.getOrNull(index),
                sortOrder = index
            )
            savedMemory.images.add(memoryImage)
        }

        val finalMemory = memoryRepository.save(savedMemory)
        logger.info { "Memory created successfully with ID: ${finalMemory.memoryId}" }

        return finalMemory.toResponse()
    }

    override fun getMemoryById(memoryId: String): MemoryResponse {
        val memory = memoryRepository.findById(memoryId)
            .orElseThrow { ResourceNotFoundException("Memory not found with ID: $memoryId") }
        return memory.toResponse()
    }

    override fun getMemoriesByBaby(babyId: String): List<MemoryResponse> {
        return memoryRepository.findByBaby_BabyIdOrderByMemoryDateDesc(babyId)
            .map { it.toResponse() }
    }

    override fun deleteMemory(memoryId: String) {
        if (!memoryRepository.existsById(memoryId)) {
            throw ResourceNotFoundException("Memory not found with ID: $memoryId")
        }
        memoryRepository.deleteById(memoryId)
        logger.info { "Memory deleted: $memoryId" }
    }

    private fun Memory.toResponse() = MemoryResponse(
        memoryId = memoryId,
        babyId = baby?.babyId ?: "",
        babyName = baby?.fullName ?: "",
        parentName = parentUser?.fullName ?: "",
        title = title,
        description = description,
        memoryDate = memoryDate,
        ageInMonths = ageInMonths,
        ageInDays = ageInDays,
        images = images.map { img ->
            MemoryImageResponse(
                imageId = img.imageId ?: 0,
                imageUrl = img.imageUrl,
                caption = img.caption,
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
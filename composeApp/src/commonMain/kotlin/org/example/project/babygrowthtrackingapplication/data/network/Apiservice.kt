package org.example.project.babygrowthtrackingapplication.data.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*

// ─────────────────────────────────────────────────────────────────────────────
// API Result wrapper
// ─────────────────────────────────────────────────────────────────────────────

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}

// ─────────────────────────────────────────────────────────────────────────────
// ApiService
// ─────────────────────────────────────────────────────────────────────────────

class ApiService(
    private val getToken: () -> String? = { null }
) {
    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080/api"

        object Endpoints {
            // Auth
            const val AUTH_REGISTER         = "/v1/auth/register"
            const val AUTH_LOGIN            = "/v1/auth/login"
            const val AUTH_GOOGLE           = "/v1/auth/google"
            const val AUTH_FACEBOOK         = "/v1/auth/facebook"
            // Account verification (signup flow)
            const val AUTH_SEND_VERIFY_CODE = "/v1/auth/send-verification"
            const val AUTH_VERIFY_ACCOUNT   = "/v1/auth/verify-account"
            // Forgot password flow (3 steps)
            const val AUTH_FORGOT_PASSWORD  = "/v1/auth/forgot-password"
            const val AUTH_VERIFY_RESET     = "/v1/auth/verify-reset-code"
            const val AUTH_RESET_PASSWORD   = "/v1/auth/reset-password"

            const val USERS = "/v1/users"
            fun user(id: String) = "$USERS/$id"

            const val BABIES = "/v1/babies"
            fun baby(id: String)                 = "$BABIES/$id"
            fun babyStatus(id: String)           = "$BABIES/$id/status"
            fun babiesByParent(parentId: String) = "$BABIES/parent/$parentId"

            const val GROWTH_RECORDS = "/v1/growth-records"
            fun growthRecord(id: String)           = "$GROWTH_RECORDS/$id"
            fun babyGrowthRecords(babyId: String)  = "$GROWTH_RECORDS/baby/$babyId"
            fun latestGrowthRecord(babyId: String) = "$GROWTH_RECORDS/baby/$babyId/latest"

            const val VACCINATIONS = "/v1/vaccinations"
            fun vaccination(id: String)              = "$VACCINATIONS/$id"
            fun babyVaccinations(babyId: String)     = "$VACCINATIONS/baby/$babyId"
            fun upcomingVaccinations(babyId: String) = "$VACCINATIONS/baby/$babyId/upcoming"

            const val FAMILY_HISTORY = "/v1/family-history"
            fun familyHistoryByBaby(babyId: String) = "$FAMILY_HISTORY/baby/$babyId"

            const val CHILD_ILLNESSES = "/v1/child-illnesses"
            fun childIllnessesByBaby(babyId: String) = "$CHILD_ILLNESSES/baby/$babyId"

            const val PRE_CHECK_INVESTIGATIONS = "/v1/pre-check-investigations"
            fun preCheckInvestigationsByBaby(babyId: String) = "$PRE_CHECK_INVESTIGATIONS/baby/$babyId"

            const val HEALTH = "/v1/health"

            // Benches
            const val BENCHES = "/v1/benches"
            const val BENCH_ASSIGNMENTS = "/v1/bench-assignments"
            fun activeAssignment(babyId: String)  = "$BENCH_ASSIGNMENTS/baby/$babyId/active"
            fun changeBench(babyId: String)        = "$BENCH_ASSIGNMENTS/baby/$babyId/change-bench"

            // Vaccination schedules
            const val VACCINATION_SCHEDULES = "/v1/vaccination-schedules"
            fun babyVaccinationSchedule(babyId: String) = "$VACCINATION_SCHEDULES/baby/$babyId"

            // Health issues
            const val HEALTH_ISSUES = "/v1/health-issues"
            fun healthIssuesByBaby(babyId: String) = "$HEALTH_ISSUES/baby/$babyId"
            fun resolveHealthIssue(issueId: String) = "$HEALTH_ISSUES/$issueId/resolve"

            // Appointments
            const val APPOINTMENTS = "/v1/appointments"
            fun appointmentsByBaby(babyId: String)   = "$APPOINTMENTS/baby/$babyId"
            fun cancelAppointment(appointmentId: String) = "$APPOINTMENTS/$appointmentId/cancel"
        }
    }

    internal val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true })
        }
        install(Logging) { logger = Logger.DEFAULT; level = LogLevel.BODY }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis  = 30_000
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            getToken()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun register(request: RegisterRequest): ApiResult<AuthResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_REGISTER}") { setBody(request) } }

    suspend fun login(request: LoginRequest): ApiResult<AuthResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_LOGIN}") { setBody(request) } }

    suspend fun loginWithGoogle(request: GoogleAuthRequest): ApiResult<AuthResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_GOOGLE}") { setBody(request) } }

    suspend fun loginWithFacebook(request: FacebookAuthRequest): ApiResult<AuthResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_FACEBOOK}") { setBody(request) } }

    // ── Account Verification ──────────────────────────────────────────────────

    suspend fun sendVerificationCode(request: SendVerificationCodeRequest): ApiResult<VerificationResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_SEND_VERIFY_CODE}") { setBody(request) } }

    suspend fun verifyAccount(request: VerifyAccountRequest): ApiResult<VerificationResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_VERIFY_ACCOUNT}") { setBody(request) } }

    // ── Forgot Password (3 steps) ─────────────────────────────────────────────

    suspend fun forgotPassword(request: ForgotPasswordRequest): ApiResult<VerificationResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_FORGOT_PASSWORD}") { setBody(request) } }

    suspend fun verifyResetCode(request: VerifyResetCodeRequest): ApiResult<VerificationResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_VERIFY_RESET}") { setBody(request) } }

    suspend fun resetPassword(request: ResetPasswordRequest): ApiResult<VerificationResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_RESET_PASSWORD}") { setBody(request) } }

    // ── Users ─────────────────────────────────────────────────────────────────

    suspend fun getUser(userId: String): ApiResult<UserResponse> =
        makeRequest { client.get("$BASE_URL${Endpoints.user(userId)}") }

    suspend fun updateUser(userId: String, request: UpdateUserRequest): ApiResult<UserResponse> =
        makeRequest { client.put("$BASE_URL${Endpoints.user(userId)}") { setBody(request) } }

    suspend fun getAllUsers(page: Int = 0, size: Int = 10): ApiResult<PageResponse<UserResponse>> =
        makeRequest { client.get("$BASE_URL${Endpoints.USERS}") { parameter("page", page); parameter("size", size) } }

    // ── Babies ────────────────────────────────────────────────────────────────

    suspend fun createBaby(parentUserId: String, request: CreateBabyRequest): ApiResult<BabyResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.BABIES}") { header("X-User-Id", parentUserId); setBody(request) } }

    suspend fun getBaby(babyId: String): ApiResult<BabyResponse> =
        makeRequest { client.get("$BASE_URL${Endpoints.baby(babyId)}") }

    suspend fun getBabiesByParent(parentUserId: String): ApiResult<List<BabyResponse>> =
        makeRequest { client.get("$BASE_URL${Endpoints.babiesByParent(parentUserId)}") }

    suspend fun updateBaby(babyId: String, request: UpdateBabyRequest): ApiResult<BabyResponse> =
        makeRequest { client.put("$BASE_URL${Endpoints.baby(babyId)}") { setBody(request) } }

    suspend fun updateBabyStatus(babyId: String, status: String): ApiResult<BabyResponse> =
        makeRequest { client.patch("$BASE_URL${Endpoints.babyStatus(babyId)}") { setBody(ArchiveBabyRequest(status)) } }

    suspend fun deleteBaby(babyId: String): ApiResult<Unit> =
        makeRequest { client.delete("$BASE_URL${Endpoints.baby(babyId)}") }

    // ── Growth Records ────────────────────────────────────────────────────────

    suspend fun createGrowthRecord(userId: String, request: CreateGrowthRecordRequest): ApiResult<GrowthRecordResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.GROWTH_RECORDS}") { header("X-User-Id", userId); setBody(request) } }

    suspend fun getGrowthRecords(babyId: String): ApiResult<List<GrowthRecordResponse>> =
        makeRequest { client.get("$BASE_URL${Endpoints.babyGrowthRecords(babyId)}") }

    suspend fun getLatestGrowthRecord(babyId: String): ApiResult<GrowthRecordResponse> =
        makeRequest { client.get("$BASE_URL${Endpoints.latestGrowthRecord(babyId)}") }

    suspend fun deleteGrowthRecord(recordId: String): ApiResult<Unit> =
        makeRequest { client.delete("$BASE_URL${Endpoints.growthRecord(recordId)}") }

    // ── Vaccinations ──────────────────────────────────────────────────────────

    suspend fun createVaccination(request: CreateVaccinationRequest): ApiResult<VaccinationResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.VACCINATIONS}") { setBody(request) } }

    suspend fun getVaccinations(babyId: String): ApiResult<List<VaccinationResponse>> =
        makeRequest { client.get("$BASE_URL${Endpoints.babyVaccinations(babyId)}") }

    suspend fun getUpcomingVaccinations(babyId: String): ApiResult<List<VaccinationResponse>> =
        makeRequest { client.get("$BASE_URL${Endpoints.upcomingVaccinations(babyId)}") }

    // ── Benches ───────────────────────────────────────────────────────────────

    suspend fun getAllBenches(): ApiResult<List<VaccinationBenchUi>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.BENCHES}")
            resp.body<ApiListResponse<VaccinationBenchNet>>().data.map { it.toUi() }
        }

    suspend fun assignBench(babyId: String, benchId: String): ApiResult<BabyBenchAssignmentUi> =
        safeCall {
            val resp = client.post("$BASE_URL${Endpoints.BENCH_ASSIGNMENTS}") {
                setBody(mapOf("babyId" to babyId, "benchId" to benchId))
            }
            resp.body<ApiSingleResponse<BabyBenchAssignmentNet>>().data!!.toUi()
        }

    suspend fun changeBench(babyId: String, benchId: String): ApiResult<BabyBenchAssignmentUi> =
        safeCall {
            val resp = client.put("$BASE_URL${Endpoints.changeBench(babyId)}") {
                setBody(mapOf("babyId" to babyId, "benchId" to benchId))
            }
            resp.body<ApiSingleResponse<BabyBenchAssignmentNet>>().data!!.toUi()
        }

    suspend fun getActiveAssignment(babyId: String): ApiResult<BabyBenchAssignmentUi?> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.activeAssignment(babyId)}")
            resp.body<ApiSingleResponse<BabyBenchAssignmentNet>>().data?.toUi()
        }

    // ── Vaccination Schedules ─────────────────────────────────────────────────

    suspend fun getScheduleForBaby(babyId: String): ApiResult<List<VaccinationScheduleUi>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.babyVaccinationSchedule(babyId)}")
            resp.body<ApiListResponse<VaccinationScheduleNet>>().data.map { it.toUi() }
        }

    // ── Health Issues ─────────────────────────────────────────────────────────

    suspend fun getHealthIssuesForBaby(babyId: String): ApiResult<List<HealthIssueUi>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.healthIssuesByBaby(babyId)}")
            resp.body<ApiListResponse<HealthIssueNet>>().data.map { it.toUi() }
        }

    suspend fun createHealthIssue(
        babyId: String,
        title: String,
        description: String?,
        severity: String?,
        issueDate: String
    ): ApiResult<HealthIssueUi> =
        safeCall {
            val body = buildMap<String, String?> {
                put("babyId", babyId)
                put("title", title)
                description?.let { put("description", it) }
                severity?.let { put("severity", it) }
                put("issueDate", issueDate)
            }
            val resp = client.post("$BASE_URL${Endpoints.HEALTH_ISSUES}") { setBody(body) }
            resp.body<ApiSingleResponse<HealthIssueNet>>().data!!.toUi()
        }

    suspend fun resolveHealthIssue(issueId: String): ApiResult<Unit> =
        safeCall {
            client.patch("$BASE_URL${Endpoints.resolveHealthIssue(issueId)}")
        }

    // ── Appointments ──────────────────────────────────────────────────────────

    suspend fun getAppointmentsForBaby(babyId: String): ApiResult<List<AppointmentUi>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.appointmentsByBaby(babyId)}")
            resp.body<ApiListResponse<AppointmentNet>>().data.map { it.toUi() }
        }

    suspend fun createAppointment(
        babyId: String,
        type: String,
        date: String,
        time: String?,
        doctorName: String?,
        location: String?,
        notes: String?
    ): ApiResult<AppointmentUi> =
        safeCall {
            val body = buildMap<String, String?> {
                put("babyId", babyId)
                put("appointmentType", type)
                put("scheduledDate", date)
                time?.let { put("scheduledTime", it) }
                doctorName?.let { put("doctorName", it) }
                location?.let { put("location", it) }
                notes?.let { put("notes", it) }
            }
            val resp = client.post("$BASE_URL${Endpoints.APPOINTMENTS}") { setBody(body) }
            resp.body<ApiSingleResponse<AppointmentNet>>().data!!.toUi()
        }

    suspend fun cancelAppointment(appointmentId: String): ApiResult<Unit> =
        safeCall {
            client.patch("$BASE_URL${Endpoints.cancelAppointment(appointmentId)}")
        }

    // ── Health check ──────────────────────────────────────────────────────────

    suspend fun checkHealth(): ApiResult<HealthResponse> =
        makeRequest { client.get("$BASE_URL${Endpoints.HEALTH}") }

    // ── Request helpers ───────────────────────────────────────────────────────

    /** Wraps structured backend responses (AuthApiResponse envelope). */
    private suspend inline fun <reified T> makeRequest(
        crossinline request: suspend () -> HttpResponse
    ): ApiResult<T> = try {
        val response = request()
        when (response.status) {
            HttpStatusCode.OK, HttpStatusCode.Created -> {
                val apiResponse = response.body<AuthApiResponse<T>>()
                if (apiResponse.success && apiResponse.data != null)
                    ApiResult.Success(apiResponse.data)
                else
                    ApiResult.Error(apiResponse.message ?: "Unknown error")
            }
            HttpStatusCode.BadRequest   -> ApiResult.Error(response.body<AuthApiResponse<T>>().message ?: "Bad request")
            HttpStatusCode.Unauthorized -> ApiResult.Error(response.body<AuthApiResponse<T>>().message ?: "Invalid credentials", 401)
            HttpStatusCode.NotFound     -> ApiResult.Error("Resource not found", 404)
            HttpStatusCode.Conflict     -> ApiResult.Error("Email already registered", 409)
            else -> ApiResult.Error("Request failed: ${response.status.description}", response.status.value)
        }
    } catch (e: HttpRequestTimeoutException) {
        ApiResult.Error("Connection timeout. Check your internet.")
    } catch (e: Exception) {
        e.printStackTrace()
        ApiResult.Error("Network error: ${e.message ?: "Unknown error"}")
    }

    /** Lightweight wrapper for raw calls that map their own response bodies. */
    private suspend fun <T> safeCall(block: suspend () -> T): ApiResult<T> = try {
        ApiResult.Success(block())
    } catch (e: HttpRequestTimeoutException) {
        ApiResult.Error("Connection timeout. Check your internet.")
    } catch (e: Exception) {
        e.printStackTrace()
        ApiResult.Error("Network error: ${e.message ?: "Unknown error"}")
    }

    fun close() { client.close() }
}

// =============================================================================
// Generic response wrappers
// =============================================================================

@Serializable
data class ApiListResponse<T>(
    val success: Boolean,
    val message: String = "",
    val data: List<T>
)

@Serializable
data class ApiSingleResponse<T>(
    val success: Boolean,
    val message: String = "",
    val data: T? = null
)

// =============================================================================
// DTOs — field names must match backend exactly
// =============================================================================

// ── Auth ──────────────────────────────────────────────────────────────────────

@Serializable data class RegisterRequest(
    val fullName: String, val email: String, val password: String,
    val phone: String? = null, val city: String? = null,
    val address: String? = null, val profileImageUrl: String? = null
)

@Serializable data class LoginRequest(
    val emailOrPhone: String,
    val password: String
)

@Serializable data class GoogleAuthRequest(
    val idToken: String, val email: String,
    val displayName: String, val photoUrl: String? = null
)

@Serializable data class FacebookAuthRequest(
    val accessToken: String, val userId: String,
    val email: String, val name: String, val photoUrl: String? = null
)

// ── Account Verification ──────────────────────────────────────────────────────

@Serializable data class SendVerificationCodeRequest(val recipient: String, val method: String)
@Serializable data class VerifyAccountRequest(val code: String, val method: String)

// ── Forgot Password ───────────────────────────────────────────────────────────

@Serializable data class ForgotPasswordRequest(val email: String)
@Serializable data class VerifyResetCodeRequest(val email: String, val code: String)
@Serializable data class ResetPasswordRequest(val email: String, val code: String, val newPassword: String)

// ── Responses ─────────────────────────────────────────────────────────────────

@Serializable data class VerificationResponse(val message: String, val success: Boolean)

@Serializable data class AuthResponse(val token: String, val user: UserResponse)

@Serializable data class AuthApiResponse<T>(
    val success: Boolean, val message: String? = null, val data: T? = null,
    val errors: List<String>? = null,
    val timestamp: String? = null
)

@Serializable data class BackendApiResponse<T>(
    val success: Boolean, val message: String? = null,
    val data: T? = null, val errors: List<String>? = null
)

@Serializable data class HealthResponse(
    val status: String, val timestamp: String? = null,
    val service: String? = null, val version: String? = null
)

@Serializable data class PageResponse<T>(
    val content: List<T>, val totalElements: Long,
    val totalPages: Int, val size: Int, val number: Int
)

// ── Users ─────────────────────────────────────────────────────────────────────

@Serializable data class UpdateUserRequest(
    val fullName: String? = null, val phone: String? = null,
    val city: String? = null, val address: String? = null,
    val profileImageUrl: String? = null
)

@Serializable data class UserResponse(
    val userId: String, val fullName: String, val email: String,
    val phone: String? = null, val city: String? = null,
    val address: String? = null, val profileImageUrl: String? = null,
    val role: String, val isActive: Boolean = false,
    val createdAt: String? = null, val updatedAt: String? = null
)

// ── Babies ────────────────────────────────────────────────────────────────────

@Serializable data class CreateBabyRequest(
    val fullName: String, val dateOfBirth: String, val gender: String,
    val birthWeight: Double? = null, val birthHeight: Double? = null,
    val birthHeadCircumference: Double? = null, val photoUrl: String? = null
)

@Serializable data class UpdateBabyRequest(val fullName: String? = null, val photoUrl: String? = null)
@Serializable data class ArchiveBabyRequest(val status: String)

@Serializable data class BabyResponse(
    val babyId: String, val parentUserId: String, val parentName: String,
    val fullName: String, val dateOfBirth: String, val gender: String,
    val birthWeight: Double? = null, val birthHeight: Double? = null,
    val birthHeadCircumference: Double? = null, val ageInMonths: Int,
    val ageInDays: Long, val photoUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// ── Growth Records ────────────────────────────────────────────────────────────

@Serializable data class CreateGrowthRecordRequest(
    val babyId: String, val measurementDate: String,
    val weight: Double? = null, val height: Double? = null,
    val headCircumference: Double? = null, val weightPercentile: Int? = null,
    val heightPercentile: Int? = null, val headCircumferencePercentile: Int? = null
)

@Serializable data class GrowthRecordResponse(
    val recordId: String,
    val babyId: String,
    val babyName: String,
    val measurementDate: String,
    val ageInMonths: Int,
    val ageInDays: Int?    = null,
    val weight: Double?    = null,
    val height: Double?    = null,
    val headCircumference: Double? = null,
    val weightPercentile: Int?     = null,
    val heightPercentile: Int?     = null,
    val headCircumferencePercentile: Int? = null,
    val measuredByName: String? = null,
    val notes: String?     = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// ── Vaccinations ──────────────────────────────────────────────────────────────

@Serializable data class CreateVaccinationRequest(
    val babyId: String, val vaccineId: Int, val scheduledDate: String,
    val location: String? = null, val notes: String? = null
)

@Serializable data class VaccinationResponse(
    val vaccinationId: String, val babyId: String, val babyName: String,
    val vaccineId: Int, val vaccineName: String, val scheduledDate: String,
    val administeredDate: String? = null, val status: String,
    val location: String? = null, val notes: String? = null, val createdAt: String
)

// ── Benches ───────────────────────────────────────────────────────────────────

@Serializable data class VaccinationBenchNet(
    val benchId: String,
    val nameEn: String,
    val nameAr: String,
    val governorate: String,
    val district: String,
    val addressEn: String? = null,
    val addressAr: String? = null,
    val latitude: Double,
    val longitude: Double,
    val phone: String? = null,
    val workingDays: List<String> = emptyList(),
    val workingHoursStart: String = "08:00",
    val workingHoursEnd: String = "14:00",
    val vaccinationDays: List<String> = emptyList(),
    val type: String = "",
    val vaccinesAvailable: List<String> = emptyList(),
    val isActive: Boolean = true
)

@Serializable data class BabyBenchAssignmentNet(
    val assignmentId: String,
    val babyId: String,
    val babyName: String,
    val benchId: String,
    val benchNameEn: String,
    val benchNameAr: String,
    val governorate: String,
    val isActive: Boolean
)

// ── Vaccination Schedules ─────────────────────────────────────────────────────

@Serializable data class VaccinationScheduleNet(
    val scheduleId: String,
    val babyId: String,
    val babyName: String,
    val benchId: String,
    val benchNameEn: String,
    val benchNameAr: String,
    val vaccineId: Int,
    val vaccineName: String,
    val doseNumber: Int,
    val recommendedAgeMonths: Int,
    val idealDate: String,
    val scheduledDate: String,
    val shiftReason: String = "NONE",
    val shiftDays: Int = 0,
    val status: String,
    val completedDate: String? = null,
    val completedByName: String? = null,
    val isVisibleToParent: Boolean = true,
    val isVisibleToTeam: Boolean = true
)

// ── Health Issues ─────────────────────────────────────────────────────────────

@Serializable data class HealthIssueNet(
    val issueId: String,
    val babyId: String,
    val title: String,
    val description: String? = null,
    val issueDate: String,
    val severity: String? = null,
    val isResolved: Boolean = false,
    val resolutionDate: String? = null,
    val resolvedNotes: String? = null
)

// ── Appointments ──────────────────────────────────────────────────────────────

@Serializable data class AppointmentNet(
    val appointmentId: String,
    val babyId: String,
    val babyName: String,
    val appointmentType: String,
    val scheduledDate: String,
    val scheduledTime: String? = null,
    val durationMinutes: Int = 30,
    val status: String,
    val doctorName: String? = null,
    val location: String? = null,
    val notes: String? = null
)

// =============================================================================
// Net → UI mappers
// =============================================================================

fun VaccinationBenchNet.toUi() = VaccinationBenchUi(
    benchId = benchId, nameEn = nameEn, nameAr = nameAr,
    governorate = governorate, district = district,
    addressEn = addressEn, addressAr = addressAr,
    latitude = latitude, longitude = longitude,
    phone = phone, workingDays = workingDays,
    workingHoursStart = workingHoursStart, workingHoursEnd = workingHoursEnd,
    vaccinationDays = vaccinationDays, type = type,
    vaccinesAvailable = vaccinesAvailable, isActive = isActive
)

fun BabyBenchAssignmentNet.toUi() = BabyBenchAssignmentUi(
    assignmentId = assignmentId, babyId = babyId, babyName = babyName,
    benchId = benchId, benchNameEn = benchNameEn, benchNameAr = benchNameAr,
    governorate = governorate, isActive = isActive
)

fun VaccinationScheduleNet.toUi() = VaccinationScheduleUi(
    scheduleId = scheduleId, babyId = babyId, vaccineId = vaccineId,
    vaccineName = vaccineName, doseNumber = doseNumber,
    recommendedAgeMonths = recommendedAgeMonths, idealDate = idealDate,
    scheduledDate = scheduledDate, shiftReason = shiftReason, shiftDays = shiftDays,
    status = status, completedDate = completedDate, benchNameEn = benchNameEn,
    isVisibleToParent = isVisibleToParent
)

fun HealthIssueNet.toUi() = HealthIssueUi(
    issueId = issueId, babyId = babyId, title = title, description = description,
    issueDate = issueDate, severity = severity, isResolved = isResolved,
    resolutionDate = resolutionDate, resolvedNotes = resolvedNotes
)

fun AppointmentNet.toUi() = AppointmentUi(
    appointmentId = appointmentId, babyId = babyId, babyName = babyName,
    appointmentType = appointmentType, scheduledDate = scheduledDate,
    scheduledTime = scheduledTime, durationMinutes = durationMinutes,
    status = status, doctorName = doctorName, location = location, notes = notes
)
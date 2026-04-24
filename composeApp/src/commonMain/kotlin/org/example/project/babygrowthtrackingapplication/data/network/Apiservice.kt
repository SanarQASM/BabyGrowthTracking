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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.babygrowthtrackingapplication.admin.CreateBenchFormRequest
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
//        private const val BASE_URL = "http://10.0.2.2:8080/api"
//      private const val BASE_URL = "http://localhost:8080/api"
      internal const val BASE_URL = "http://172.20.10.7:8080/api"

        object Endpoints {

            // ── Auth ──────────────────────────────────────────────────────────
            const val AUTH_REGISTER              = "/v1/auth/register"
            const val AUTH_PRE_REGISTER          = "/v1/auth/pre-register"
            const val AUTH_VERIFY_SIGNUP_CODE    = "/v1/auth/verify-signup-code"
            const val AUTH_RESEND_SIGNUP_CODE    = "/v1/auth/resend-signup-code"
            const val AUTH_COMPLETE_REGISTRATION = "/v1/auth/complete-registration"
            const val AUTH_LOGIN                 = "/v1/auth/login"
            const val AUTH_GOOGLE                = "/v1/auth/google"
            const val AUTH_FACEBOOK              = "/v1/auth/facebook"
            const val AUTH_SEND_VERIFY_CODE      = "/v1/auth/send-verification"
            const val AUTH_VERIFY_ACCOUNT        = "/v1/auth/verify-account"
            const val AUTH_FORGOT_PASSWORD       = "/v1/auth/forgot-password"
            const val AUTH_VERIFY_RESET          = "/v1/auth/verify-reset-code"
            const val AUTH_RESET_PASSWORD        = "/v1/auth/reset-password"

            // ── Users ─────────────────────────────────────────────────────────
            const val USERS = "/v1/users"
            fun user(id: String)           = "$USERS/$id"
            fun userByRole(role: String)   = "$USERS/by-role/$role"
            fun deactivateUser(id: String) = "$USERS/$id/deactivate"
            fun activateUser(id: String)   = "$USERS/$id/activate"

            // ── Babies ────────────────────────────────────────────────────────
            const val BABIES     = "/v1/babies"
            const val BABIES_ALL = "/v1/babies/all"
            fun baby(id: String)                 = "$BABIES/$id"
            fun babyStatus(id: String)           = "$BABIES/$id/status"
            fun babiesByParent(parentId: String) = "$BABIES/parent/$parentId"
            fun babiesByBench(benchId: String)   = "$BABIES/bench/$benchId"

            // ── Growth Records ────────────────────────────────────────────────
            const val GROWTH_RECORDS = "/v1/growth-records"
            fun growthRecord(id: String)           = "$GROWTH_RECORDS/$id"
            fun babyGrowthRecords(babyId: String)  = "$GROWTH_RECORDS/baby/$babyId"
            fun latestGrowthRecord(babyId: String) = "$GROWTH_RECORDS/baby/$babyId/latest"
            fun updateGrowthRecord(id: String)     = "$GROWTH_RECORDS/$id"

            // ── Vaccinations ──────────────────────────────────────────────────
            const val VACCINATIONS = "/v1/vaccinations"
            fun vaccination(id: String)                = "$VACCINATIONS/$id"
            fun babyVaccinations(babyId: String)       = "$VACCINATIONS/baby/$babyId"
            fun upcomingVaccinations(babyId: String)   = "$VACCINATIONS/baby/$babyId/upcoming"
            fun rescheduleVaccinations(babyId: String) = "$VACCINATION_SCHEDULES/baby/$babyId/reschedule"
            fun markVaccinationDone(id: String)        = "$VACCINATIONS/$id/complete"

            // ── Vaccination Schedules ─────────────────────────────────────────
            const val VACCINATION_SCHEDULES = "/v1/vaccination-schedules"
            fun babyVaccinationSchedule(babyId: String) = "$VACCINATION_SCHEDULES/baby/$babyId"
            fun vaccinationSchedule(id: String)         = "$VACCINATION_SCHEDULES/$id"
            fun updateScheduleStatus(id: String)        = "$VACCINATION_SCHEDULES/$id/status"

            // ── Benches ───────────────────────────────────────────────────────
            const val BENCHES           = "/v1/benches"
            const val BENCH_ASSIGNMENTS = "/v1/bench-assignments"
            fun bench(id: String)                      = "$BENCHES/$id"
            fun deactivateBench(id: String)            = "$BENCHES/$id/deactivate"
            fun activeAssignment(babyId: String)       = "$BENCH_ASSIGNMENTS/baby/$babyId/active"
            fun changeBench(babyId: String)            = "$BENCH_ASSIGNMENTS/baby/$babyId/change-bench"
            fun assignmentsByBench(benchId: String)    = "$BENCH_ASSIGNMENTS/bench/$benchId"

            // ── Bench Requests ────────────────────────────────────────────────
            // POST   /v1/bench-requests                      → send request (parent)
            // GET    /v1/bench-requests/baby/{babyId}/active → active request for baby
            // GET    /v1/bench-requests/bench/{benchId}/pending → pending list (team)
            // GET    /v1/bench-requests/bench/{benchId}        → all requests (team)
            // PATCH  /v1/bench-requests/{requestId}/review    → accept / reject (team)
            // PATCH  /v1/bench-requests/{requestId}/cancel    → cancel (parent)
            const val BENCH_REQUESTS = "/v1/bench-requests"
            fun benchRequestActiveBaby(babyId: String)  = "$BENCH_REQUESTS/baby/$babyId/active"
            fun benchRequestsPendingForBench(benchId: String) = "$BENCH_REQUESTS/bench/$benchId/pending"
            fun benchRequestsAllForBench(benchId: String)     = "$BENCH_REQUESTS/bench/$benchId"
            fun benchRequestReview(requestId: String)   = "$BENCH_REQUESTS/$requestId/review"
            fun benchRequestCancel(requestId: String)   = "$BENCH_REQUESTS/$requestId/cancel"

            // ── Family History ────────────────────────────────────────────────
            const val FAMILY_HISTORY = "/v1/family-history"
            fun familyHistoryByBaby(babyId: String)   = "$FAMILY_HISTORY/baby/$babyId"
            fun familyHistoryById(historyId: String)  = "$FAMILY_HISTORY/$historyId"

            // ── Child Illnesses ───────────────────────────────────────────────
            const val CHILD_ILLNESSES = "/v1/child-illnesses"
            fun childIllnessesByBaby(babyId: String)       = "$CHILD_ILLNESSES/baby/$babyId"
            fun childIllnessById(illnessId: String)        = "$CHILD_ILLNESSES/$illnessId"
            fun childIllnessDeactivate(illnessId: String)  = "$CHILD_ILLNESSES/$illnessId/deactivate"

            // ── Pre-Check Investigations ──────────────────────────────────────
            const val PRE_CHECK_INVESTIGATIONS = "/v1/pre-check-investigations"
            fun preCheckInvestigationsByBaby(babyId: String) = "$PRE_CHECK_INVESTIGATIONS/baby/$babyId"
            fun preCheckInvestigationById(id: String)        = "$PRE_CHECK_INVESTIGATIONS/$id"

            // ── Health Issues ─────────────────────────────────────────────────
            const val HEALTH_ISSUES = "/v1/health-issues"
            fun healthIssuesByBaby(babyId: String)  = "$HEALTH_ISSUES/baby/$babyId"
            fun healthIssueById(issueId: String)    = "$HEALTH_ISSUES/$issueId"
            fun resolveHealthIssue(issueId: String) = "$HEALTH_ISSUES/$issueId/resolve"

            // ── Appointments ──────────────────────────────────────────────────
            const val APPOINTMENTS = "/v1/appointments"
            fun appointmentsByBaby(babyId: String)       = "$APPOINTMENTS/baby/$babyId"
            fun appointmentById(appointmentId: String)   = "$APPOINTMENTS/$appointmentId"
            fun cancelAppointment(appointmentId: String) = "$APPOINTMENTS/$appointmentId/cancel"
            fun confirmAppointment(id: String)           = "$APPOINTMENTS/$id/confirm"

            // ── Child Development ─────────────────────────────────────────────
            const val CHILD_DEV_VISION_MOTOR   = "/v1/child-development/vision-motor"
            const val CHILD_DEV_HEARING_SPEECH = "/v1/child-development/hearing-speech"
            fun childDevVisionMotorByBaby(babyId: String)   = "$CHILD_DEV_VISION_MOTOR/baby/$babyId"
            fun childDevVisionMotorById(id: String)         = "$CHILD_DEV_VISION_MOTOR/$id"
            fun childDevHearingSpeechByBaby(babyId: String) = "$CHILD_DEV_HEARING_SPEECH/baby/$babyId"
            fun childDevHearingSpeechById(id: String)       = "$CHILD_DEV_HEARING_SPEECH/$id"

            // ── Memories ──────────────────────────────────────────────────────
            const val MEMORIES = "/v1/memories"
            fun memoriesByBaby(babyId: String) = "$MEMORIES/baby/$babyId"
            fun memory(id: String)             = "$MEMORIES/$id"

            // ── Health ────────────────────────────────────────────────────────
            const val HEALTH = "/v1/health"

            // ── Notifications ─────────────────────────────────────────────────
            const val NOTIFICATIONS = "/v1/notifications"
            fun notificationsByUser(userId: String)      = "$NOTIFICATIONS/user/$userId"
            fun markNotificationRead(id: String)         = "$NOTIFICATIONS/$id/read"
            fun markAllNotificationsRead(userId: String) = "$NOTIFICATIONS/user/$userId/read-all"
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

    val httpClient get() = client
    val baseUrl = BASE_URL

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun register(request: RegisterRequest): ApiResult<AuthResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_REGISTER}") { setBody(request) } }

    suspend fun preRegister(request: PreRegisterRequest): ApiResult<PreRegisterResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_PRE_REGISTER}") { setBody(request) } }

    suspend fun verifySignupCode(request: VerifySignupCodeRequest): ApiResult<VerificationResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_VERIFY_SIGNUP_CODE}") { setBody(request) } }

    suspend fun resendSignupCode(request: ResendSignupCodeRequest): ApiResult<AuthResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_RESEND_SIGNUP_CODE}") { setBody(request) } }

    suspend fun completeRegistration(request: CompleteRegistrationRequest): ApiResult<AuthResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.AUTH_COMPLETE_REGISTRATION}") { setBody(request) } }

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

    // ── Forgot Password ───────────────────────────────────────────────────────

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
        makeRequest {
            client.get("$BASE_URL${Endpoints.USERS}") {
                parameter("page", page)
                parameter("size", size)
            }
        }

    suspend fun getUsersByRole(role: String): ApiResult<List<UserResponse>> =
        makeRequest { client.get("$BASE_URL${Endpoints.userByRole(role)}") }

    suspend fun deleteUser(userId: String): ApiResult<Unit> =
        makeRequest { client.delete("$BASE_URL${Endpoints.user(userId)}") }

    suspend fun deactivateUser(userId: String): ApiResult<UserResponse> =
        makeRequest { client.patch("$BASE_URL${Endpoints.deactivateUser(userId)}") }

    suspend fun activateUser(userId: String): ApiResult<UserResponse> =
        makeRequest { client.patch("$BASE_URL${Endpoints.activateUser(userId)}") }

    suspend fun createTeamMember(
        fullName : String,
        email    : String,
        password : String,
        phone    : String? = null,
        city     : String? = null,
        address  : String? = null,
    ): ApiResult<UserResponse> =
        makeRequest {
            client.post("$BASE_URL${Endpoints.USERS}") {
                setBody(
                    CreateUserRequest(
                        fullName = fullName,
                        email    = email,
                        password = password,
                        phone    = phone,
                        city     = city,
                        address  = address,
                        role     = "VACCINATION_TEAM"
                    )
                )
            }
        }

    // ── Babies ────────────────────────────────────────────────────────────────

    suspend fun createBaby(parentUserId: String, request: CreateBabyRequest): ApiResult<BabyResponse> =
        makeRequest {
            client.post("$BASE_URL${Endpoints.BABIES}") {
                header("X-User-Id", parentUserId)
                setBody(request)
            }
        }

    suspend fun getBaby(babyId: String): ApiResult<BabyResponse> =
        makeRequest { client.get("$BASE_URL${Endpoints.baby(babyId)}") }

    suspend fun getBabiesByParent(parentUserId: String): ApiResult<List<BabyResponse>> =
        makeRequest { client.get("$BASE_URL${Endpoints.babiesByParent(parentUserId)}") }

    suspend fun getBabiesByBench(benchId: String): ApiResult<List<BabyResponse>> =
        makeRequest { client.get("$BASE_URL${Endpoints.babiesByBench(benchId)}") }

    suspend fun getAllBabies(page: Int = 0, size: Int = 20): ApiResult<PageResponse<BabyResponse>> =
        makeRequest {
            client.get("$BASE_URL${Endpoints.BABIES_ALL}") {
                parameter("page", page)
                parameter("size", size)
            }
        }

    suspend fun updateBaby(babyId: String, request: UpdateBabyRequest): ApiResult<BabyResponse> =
        makeRequest { client.put("$BASE_URL${Endpoints.baby(babyId)}") { setBody(request) } }

    suspend fun updateBabyStatus(babyId: String, status: String): ApiResult<BabyResponse> =
        makeRequest {
            client.patch("$BASE_URL${Endpoints.babyStatus(babyId)}") {
                setBody(ArchiveBabyRequest(status))
            }
        }

    suspend fun deleteBaby(babyId: String): ApiResult<Unit> =
        makeRequest { client.delete("$BASE_URL${Endpoints.baby(babyId)}") }

    // ── Growth Records ────────────────────────────────────────────────────────

    suspend fun createGrowthRecord(
        userId  : String,
        request : CreateGrowthRecordRequest
    ): ApiResult<GrowthRecordResponse> =
        makeRequest {
            client.post("$BASE_URL${Endpoints.GROWTH_RECORDS}") {
                header("X-User-Id", userId)
                setBody(request)
            }
        }

    suspend fun getGrowthRecords(babyId: String): ApiResult<List<GrowthRecordResponse>> =
        makeRequest { client.get("$BASE_URL${Endpoints.babyGrowthRecords(babyId)}") }

    suspend fun getLatestGrowthRecord(babyId: String): ApiResult<GrowthRecordResponse> =
        makeRequest { client.get("$BASE_URL${Endpoints.latestGrowthRecord(babyId)}") }

    suspend fun updateGrowthRecord(
        recordId : String,
        request  : UpdateGrowthRecordRequest
    ): ApiResult<GrowthRecordResponse> =
        makeRequest {
            client.put("$BASE_URL${Endpoints.updateGrowthRecord(recordId)}") { setBody(request) }
        }

    suspend fun deleteGrowthRecord(recordId: String): ApiResult<Unit> =
        makeRequest { client.delete("$BASE_URL${Endpoints.growthRecord(recordId)}") }

    // ── Vaccinations ──────────────────────────────────────────────────────────

    suspend fun createVaccination(request: CreateVaccinationRequest): ApiResult<VaccinationResponse> =
        makeRequest { client.post("$BASE_URL${Endpoints.VACCINATIONS}") { setBody(request) } }

    suspend fun getVaccinations(babyId: String): ApiResult<List<VaccinationResponse>> =
        makeRequest { client.get("$BASE_URL${Endpoints.babyVaccinations(babyId)}") }

    suspend fun getUpcomingVaccinations(babyId: String): ApiResult<List<VaccinationResponse>> =
        makeRequest { client.get("$BASE_URL${Endpoints.upcomingVaccinations(babyId)}") }

    suspend fun markVaccinationDone(
        vaccinationId   : String,
        administeredDate: String,
        notes           : String? = null
    ): ApiResult<VaccinationResponse> =
        makeRequest {
            client.patch("$BASE_URL${Endpoints.markVaccinationDone(vaccinationId)}") {
                setBody(MarkVaccinationDoneRequest(administeredDate = administeredDate, notes = notes))
            }
        }

    suspend fun deleteVaccination(vaccinationId: String): ApiResult<Unit> =
        makeRequest { client.delete("$BASE_URL${Endpoints.vaccination(vaccinationId)}") }

    // ── Vaccination Schedules ─────────────────────────────────────────────────

    suspend fun getScheduleForBaby(babyId: String): ApiResult<List<VaccinationScheduleUi>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.babyVaccinationSchedule(babyId)}")
            resp.body<ApiListResponse<VaccinationScheduleNet>>().data.map { it.toUi() }
        }

    suspend fun rescheduleAllVaccinations(
        babyId            : String,
        shiftReason       : String,
        notes             : String?  = null,
        rescheduleOverdue : Boolean  = true
    ): ApiResult<RescheduleResultUi> = safeCall {
        val request = RescheduleRequest(
            shiftReason       = shiftReason,
            rescheduleOverdue = rescheduleOverdue,
            notes             = notes
        )
        val resp = client.post(
            "$BASE_URL${Endpoints.rescheduleVaccinations(babyId)}"
        ) { setBody(request) }
        resp.body<ApiSingleResponse<RescheduleResponseNet>>().data!!.toUi()
    }

    suspend fun updateVaccinationScheduleStatus(
        scheduleId : String,
        status     : String,
        notes      : String? = null
    ): ApiResult<VaccinationScheduleNet> =
        safeCall {
            val resp = client.patch("$BASE_URL${Endpoints.updateScheduleStatus(scheduleId)}") {
                setBody(UpdateScheduleStatusRequest(status = status, notes = notes))
            }
            resp.body<ApiSingleResponse<VaccinationScheduleNet>>().data!!
        }

    // ── Benches ───────────────────────────────────────────────────────────────

    suspend fun getAllBenches(): ApiResult<List<VaccinationBenchUi>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.BENCHES}")
            resp.body<ApiListResponse<VaccinationBenchNet>>().data.map { it.toUi() }
        }

    suspend fun getBench(benchId: String): ApiResult<VaccinationBenchUi> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.bench(benchId)}")
            resp.body<ApiSingleResponse<VaccinationBenchNet>>().data!!.toUi()
        }

    /**
     * Admin: create a new vaccination bench.
     * POST /v1/benches
     */
    suspend fun createBench(request: CreateBenchFormRequest): ApiResult<VaccinationBenchUi> =
        safeCall {
            val body = mapOf(
                "nameEn"           to request.nameEn,
                "nameAr"           to request.nameAr,
                "governorate"      to request.governorate,
                "district"         to request.district,
                "addressEn"        to request.addressEn,
                "latitude"         to request.latitude,
                "longitude"        to request.longitude,
                "phone"            to request.phone,
                "workingDays"      to request.workingDays,
                "vaccinationDays"  to request.vaccinationDays,
                "workingHoursStart" to request.workingHoursStart,
                "workingHoursEnd"   to request.workingHoursEnd,
                "vaccinesAvailable" to request.vaccinesAvailable
            )
            val resp = client.post("$BASE_URL${Endpoints.BENCHES}") { setBody(body) }
            resp.body<ApiSingleResponse<VaccinationBenchNet>>().data!!.toUi()
        }

    /**
     * Admin: deactivate (soft-delete) a bench.
     * PATCH /v1/benches/{id}/deactivate
     */
    suspend fun deactivateBench(benchId: String): ApiResult<Unit> =
        safeCall {
            client.patch("$BASE_URL${Endpoints.deactivateBench(benchId)}")
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

    suspend fun getAssignmentsByBench(benchId: String): ApiResult<List<BabyBenchAssignmentUi>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.assignmentsByBench(benchId)}")
            resp.body<ApiListResponse<BabyBenchAssignmentNet>>().data.map { it.toUi() }
        }

    // ── Bench Requests ────────────────────────────────────────────────────────

    /**
     * Parent sends a join request for their baby to a specific bench.
     * POST /v1/bench-requests
     * Body: { babyId, benchId, notes? }
     */
    suspend fun sendBenchRequest(
        babyId  : String,
        benchId : String,
        notes   : String? = null
    ): ApiResult<BenchRequestNet> =
        safeCall {
            val body = buildMap<String, String?> {
                put("babyId",  babyId)
                put("benchId", benchId)
                notes?.let { put("notes", it) }
            }
            val resp = client.post("$BASE_URL${Endpoints.BENCH_REQUESTS}") { setBody(body) }
            resp.body<ApiSingleResponse<BenchRequestNet>>().data!!
        }

    /**
     * Get the active (PENDING or ACCEPTED) bench request for a baby.
     * GET /v1/bench-requests/baby/{babyId}/active
     * Returns null when no active request exists (404 is swallowed gracefully).
     */
    suspend fun getActiveBenchRequest(babyId: String): ApiResult<BenchRequestNet?> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.benchRequestActiveBaby(babyId)}")
            try {
                resp.body<ApiSingleResponse<BenchRequestNet>>().data
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Get all PENDING requests for a bench (used by the team's Requests tab).
     * GET /v1/bench-requests/bench/{benchId}/pending
     */
    suspend fun getPendingRequestsForBench(benchId: String): ApiResult<List<BenchRequestNet>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.benchRequestsPendingForBench(benchId)}")
            resp.body<ApiListResponse<BenchRequestNet>>().data
        }

    /**
     * Get all requests (pending + history) for a bench.
     * GET /v1/bench-requests/bench/{benchId}
     */
    suspend fun getAllRequestsForBench(benchId: String): ApiResult<List<BenchRequestNet>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.benchRequestsAllForBench(benchId)}")
            resp.body<ApiListResponse<BenchRequestNet>>().data
        }

    /**
     * Team member accepts or rejects a request.
     * PATCH /v1/bench-requests/{requestId}/review
     * Body: { action: "accept" | "reject", rejectReason? }
     */
    suspend fun reviewBenchRequest(
        requestId    : String,
        action       : String,          // "accept" or "reject"
        rejectReason : String? = null
    ): ApiResult<BenchRequestNet> =
        safeCall {
            val body = buildMap<String, String?> {
                put("action", action)
                rejectReason?.let { put("rejectReason", it) }
            }
            val resp = client.patch("$BASE_URL${Endpoints.benchRequestReview(requestId)}") {
                setBody(body)
            }
            resp.body<ApiSingleResponse<BenchRequestNet>>().data!!
        }

    /**
     * Parent cancels a pending request before it is reviewed.
     * PATCH /v1/bench-requests/{requestId}/cancel
     */
    suspend fun cancelBenchRequest(requestId: String): ApiResult<BenchRequestNet> =
        safeCall {
            val resp = client.patch("$BASE_URL${Endpoints.benchRequestCancel(requestId)}")
            resp.body<ApiSingleResponse<BenchRequestNet>>().data!!
        }

    // ── Health Issues ─────────────────────────────────────────────────────────

    suspend fun getHealthIssuesForBaby(babyId: String): ApiResult<List<HealthIssueUi>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.healthIssuesByBaby(babyId)}")
            resp.body<ApiListResponse<HealthIssueNet>>().data.map { it.toUi() }
        }

    suspend fun createHealthIssue(
        babyId      : String,
        title       : String,
        description : String?,
        severity    : String?,
        issueDate   : String
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

    suspend fun updateHealthIssue(
        issueId     : String,
        title       : String,
        description : String?,
        severity    : String?,
        issueDate   : String
    ): ApiResult<HealthIssueUi> =
        safeCall {
            val body = buildMap<String, String?> {
                put("title", title)
                description?.let { put("description", it) }
                severity?.let { put("severity", it) }
                put("issueDate", issueDate)
            }
            val resp = client.put("$BASE_URL${Endpoints.healthIssueById(issueId)}") { setBody(body) }
            resp.body<ApiSingleResponse<HealthIssueNet>>().data!!.toUi()
        }

    suspend fun resolveHealthIssue(issueId: String): ApiResult<Unit> =
        safeCall { client.patch("$BASE_URL${Endpoints.resolveHealthIssue(issueId)}") }

    suspend fun deleteHealthIssue(issueId: String): ApiResult<Unit> =
        safeCall { client.delete("$BASE_URL${Endpoints.healthIssueById(issueId)}") }

    // ── Appointments ──────────────────────────────────────────────────────────

    suspend fun getAppointmentsForBaby(babyId: String): ApiResult<List<AppointmentUi>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.appointmentsByBaby(babyId)}")
            resp.body<ApiListResponse<AppointmentNet>>().data.map { it.toUi() }
        }

    suspend fun createAppointment(
        babyId     : String,
        type       : String,
        date       : String,
        time       : String?,
        doctorName : String?,
        location   : String?,
        notes      : String?
    ): ApiResult<AppointmentUi> =
        safeCall {
            val body = buildMap<String, String?> {
                put("babyId", babyId)
                put("appointmentType", type)
                put("scheduledDate", date)
                time?.let       { put("scheduledTime", it) }
                doctorName?.let { put("doctorName", it) }
                location?.let   { put("location", it) }
                notes?.let      { put("notes", it) }
            }
            val resp = client.post("$BASE_URL${Endpoints.APPOINTMENTS}") { setBody(body) }
            resp.body<ApiSingleResponse<AppointmentNet>>().data!!.toUi()
        }

    suspend fun updateAppointment(
        appointmentId : String,
        type          : String,
        date          : String,
        time          : String?,
        doctorName    : String?,
        location      : String?,
        notes         : String?
    ): ApiResult<AppointmentUi> =
        safeCall {
            val body = buildMap<String, String?> {
                put("appointmentType", type)
                put("scheduledDate", date)
                time?.let       { put("scheduledTime", it) }
                doctorName?.let { put("doctorName", it) }
                location?.let   { put("location", it) }
                notes?.let      { put("notes", it) }
            }
            val resp = client.put("$BASE_URL${Endpoints.appointmentById(appointmentId)}") { setBody(body) }
            resp.body<ApiSingleResponse<AppointmentNet>>().data!!.toUi()
        }

    suspend fun cancelAppointment(appointmentId: String): ApiResult<Unit> =
        safeCall { client.patch("$BASE_URL${Endpoints.cancelAppointment(appointmentId)}") }

    suspend fun confirmAppointment(appointmentId: String): ApiResult<AppointmentUi> =
        safeCall {
            val resp = client.patch("$BASE_URL${Endpoints.confirmAppointment(appointmentId)}")
            resp.body<ApiSingleResponse<AppointmentNet>>().data!!.toUi()
        }

    suspend fun deleteAppointment(appointmentId: String): ApiResult<Unit> =
        safeCall { client.delete("$BASE_URL${Endpoints.appointmentById(appointmentId)}") }

    // ── Health check ──────────────────────────────────────────────────────────

    suspend fun checkHealth(): ApiResult<HealthResponse> =
        makeRequest { client.get("$BASE_URL${Endpoints.HEALTH}") }

    // ── Family History ────────────────────────────────────────────────────────

    suspend fun getFamilyHistory(babyId: String): ApiResult<FamilyHistoryNet?> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.familyHistoryByBaby(babyId)}")
            try { resp.body<ApiSingleResponse<FamilyHistoryNet>>().data }
            catch (e: Exception) { null }
        }

    suspend fun createFamilyHistory(request: FamilyHistoryRequest): ApiResult<FamilyHistoryNet> =
        safeCall {
            val resp = client.post("$BASE_URL${Endpoints.FAMILY_HISTORY}") { setBody(request) }
            resp.body<ApiSingleResponse<FamilyHistoryNet>>().data!!
        }

    suspend fun updateFamilyHistory(historyId: String, request: FamilyHistoryRequest): ApiResult<FamilyHistoryNet> =
        safeCall {
            val resp = client.put("$BASE_URL${Endpoints.familyHistoryById(historyId)}") { setBody(request) }
            resp.body<ApiSingleResponse<FamilyHistoryNet>>().data!!
        }

    suspend fun deleteFamilyHistory(historyId: String): ApiResult<Unit> =
        safeCall { client.delete("$BASE_URL${Endpoints.familyHistoryById(historyId)}") }

    // ── Child Illnesses ───────────────────────────────────────────────────────

    suspend fun getChildIllnesses(babyId: String): ApiResult<List<ChildIllnessNet>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.childIllnessesByBaby(babyId)}")
            resp.body<List<ChildIllnessNet>>()
        }

    suspend fun createChildIllness(request: ChildIllnessRequest): ApiResult<ChildIllnessNet> =
        safeCall {
            val resp = client.post("$BASE_URL${Endpoints.CHILD_ILLNESSES}") { setBody(request) }
            resp.body<ApiSingleResponse<ChildIllnessNet>>().data!!
        }

    suspend fun updateChildIllness(illnessId: String, request: ChildIllnessRequest): ApiResult<ChildIllnessNet> =
        safeCall {
            val resp = client.put("$BASE_URL${Endpoints.childIllnessById(illnessId)}") { setBody(request) }
            resp.body<ApiSingleResponse<ChildIllnessNet>>().data!!
        }

    suspend fun deactivateChildIllness(illnessId: String): ApiResult<ChildIllnessNet> =
        safeCall {
            val resp = client.patch("$BASE_URL${Endpoints.childIllnessDeactivate(illnessId)}")
            resp.body<ApiSingleResponse<ChildIllnessNet>>().data!!
        }

    suspend fun deleteChildIllness(illnessId: String): ApiResult<Unit> =
        safeCall { client.delete("$BASE_URL${Endpoints.childIllnessById(illnessId)}") }

    // ── Pre-Check Investigations ──────────────────────────────────────────────

    suspend fun getPreCheckInvestigationByBaby(babyId: String): ApiResult<PreCheckInvestigationNet?> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.preCheckInvestigationsByBaby(babyId)}")
            try { resp.body<ApiSingleResponse<PreCheckInvestigationNet>>().data }
            catch (e: Exception) { null }
        }

    suspend fun createPreCheckInvestigation(request: PreCheckInvestigationRequest): ApiResult<PreCheckInvestigationNet> =
        safeCall {
            val resp = client.post("$BASE_URL${Endpoints.PRE_CHECK_INVESTIGATIONS}") { setBody(request) }
            resp.body<ApiSingleResponse<PreCheckInvestigationNet>>().data!!
        }

    suspend fun updatePreCheckInvestigation(
        id      : String,
        request : PreCheckInvestigationRequest
    ): ApiResult<PreCheckInvestigationNet> =
        safeCall {
            val resp = client.put("$BASE_URL${Endpoints.preCheckInvestigationById(id)}") { setBody(request) }
            resp.body<ApiSingleResponse<PreCheckInvestigationNet>>().data!!
        }

    suspend fun deletePreCheckInvestigation(id: String): ApiResult<Unit> =
        safeCall { client.delete("$BASE_URL${Endpoints.preCheckInvestigationById(id)}") }

    // ── Child Development ─────────────────────────────────────────────────────

    suspend fun getVisionMotorRecords(babyId: String): ApiResult<List<VisionMotorNet>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.childDevVisionMotorByBaby(babyId)}")
            resp.body<ApiListResponse<VisionMotorNet>>().data
        }

    suspend fun saveVisionMotorRecord(request: VisionMotorNet): ApiResult<VisionMotorNet> =
        safeCall {
            val resp = client.post("$BASE_URL${Endpoints.CHILD_DEV_VISION_MOTOR}") { setBody(request) }
            resp.body<ApiSingleResponse<VisionMotorNet>>().data!!
        }

    suspend fun updateVisionMotorRecord(id: String, request: VisionMotorNet): ApiResult<VisionMotorNet> =
        safeCall {
            val resp = client.put("$BASE_URL${Endpoints.childDevVisionMotorById(id)}") { setBody(request) }
            resp.body<ApiSingleResponse<VisionMotorNet>>().data!!
        }

    suspend fun deleteVisionMotorRecord(id: String): ApiResult<Unit> =
        safeCall { client.delete("$BASE_URL${Endpoints.childDevVisionMotorById(id)}") }

    suspend fun getHearingSpeechRecords(babyId: String): ApiResult<List<HearingSpeechNet>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.childDevHearingSpeechByBaby(babyId)}")
            resp.body<ApiListResponse<HearingSpeechNet>>().data
        }

    suspend fun saveHearingSpeechRecord(request: HearingSpeechNet): ApiResult<HearingSpeechNet> =
        safeCall {
            val resp = client.post("$BASE_URL${Endpoints.CHILD_DEV_HEARING_SPEECH}") { setBody(request) }
            resp.body<ApiSingleResponse<HearingSpeechNet>>().data!!
        }

    suspend fun updateHearingSpeechRecord(id: String, request: HearingSpeechNet): ApiResult<HearingSpeechNet> =
        safeCall {
            val resp = client.put("$BASE_URL${Endpoints.childDevHearingSpeechById(id)}") { setBody(request) }
            resp.body<ApiSingleResponse<HearingSpeechNet>>().data!!
        }

    suspend fun deleteHearingSpeechRecord(id: String): ApiResult<Unit> =
        safeCall { client.delete("$BASE_URL${Endpoints.childDevHearingSpeechById(id)}") }

    // ── Memories ──────────────────────────────────────────────────────────────

    suspend fun getMemoriesByBaby(babyId: String): ApiResult<List<MemoryNet>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.memoriesByBaby(babyId)}")
            resp.body<ApiListResponse<MemoryNet>>().data
        }

    suspend fun createMemory(userId: String, request: CreateMemoryRequest): ApiResult<MemoryNet> =
        safeCall {
            val resp = client.post("$BASE_URL${Endpoints.MEMORIES}") {
                if (userId.isNotBlank()) header("X-User-Id", userId)
                setBody(request)
            }
            resp.body<ApiSingleResponse<MemoryNet>>().data!!
        }

    suspend fun updateMemory(memoryId: String, request: UpdateMemoryRequest): ApiResult<MemoryNet> =
        safeCall {
            val resp = client.put("$BASE_URL${Endpoints.memory(memoryId)}") { setBody(request) }
            resp.body<ApiSingleResponse<MemoryNet>>().data!!
        }

    suspend fun deleteMemory(memoryId: String): ApiResult<Unit> =
        safeCall { client.delete("$BASE_URL${Endpoints.memory(memoryId)}") }

    // ── Notifications ─────────────────────────────────────────────────────────

    suspend fun getNotificationsForUser(userId: String): ApiResult<List<NotificationNet>> =
        safeCall {
            val resp = client.get("$BASE_URL${Endpoints.notificationsByUser(userId)}")
            resp.body<ApiListResponse<NotificationNet>>().data
        }

    suspend fun markNotificationRead(notificationId: String): ApiResult<Unit> =
        safeCall { client.patch("$BASE_URL${Endpoints.markNotificationRead(notificationId)}") }

    suspend fun markAllNotificationsRead(userId: String): ApiResult<Unit> =
        safeCall { client.patch("$BASE_URL${Endpoints.markAllNotificationsRead(userId)}") }

    // ── Request helpers ───────────────────────────────────────────────────────

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
            HttpStatusCode.NoContent -> {
                @Suppress("UNCHECKED_CAST")
                ApiResult.Success(Unit as T)
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

    internal suspend fun <T> safeCall(block: suspend () -> T): ApiResult<T> = try {
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
    val success : Boolean,
    val message : String = "",
    val data    : List<T>
)

@Serializable
data class ApiSingleResponse<T>(
    val success : Boolean,
    val message : String = "",
    val data    : T? = null
)

// =============================================================================
// DTOs
// =============================================================================

// ── Auth ──────────────────────────────────────────────────────────────────────

@Serializable data class RegisterRequest(
    val fullName        : String,
    val email           : String,
    val password        : String,
    val phone           : String? = null,
    val city            : String? = null,
    val address         : String? = null,
    val profileImageUrl : String? = null
)

@Serializable data class PreRegisterRequest(
    val fullName : String,
    val email    : String,
    val password : String,
    val phone    : String? = null,
    val city     : String? = null,
    val address  : String? = null
)

@Serializable data class VerifySignupCodeRequest(val email: String, val code: String)

@Serializable data class ResendSignupCodeRequest(val email: String)

@Serializable data class CompleteRegistrationRequest(val email: String)

@Serializable data class LoginRequest(
    val emailOrPhone : String,
    val password     : String
)

@Serializable data class GoogleAuthRequest(
    val idToken     : String,
    val email       : String,
    val displayName : String,
    val photoUrl    : String? = null
)

@Serializable data class FacebookAuthRequest(
    val accessToken : String,
    val userId      : String,
    val email       : String,
    val name        : String,
    val photoUrl    : String? = null
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
    val success   : Boolean,
    val message   : String?       = null,
    val data      : T?            = null,
    val errors    : List<String>? = null,
    val timestamp : String?       = null
)

@Serializable data class BackendApiResponse<T>(
    val success : Boolean,
    val message : String?       = null,
    val data    : T?            = null,
    val errors  : List<String>? = null
)

@Serializable data class HealthResponse(
    val status    : String,
    val timestamp : String? = null,
    val service   : String? = null,
    val version   : String? = null
)

@Serializable
data class PageResponse<T>(
    @SerialName("content")       val content       : List<T>,
    @SerialName("totalElements") val totalElements : Long,
    @SerialName("totalPages")    val totalPages    : Int,
    @SerialName("pageSize")      val pageSize      : Int,
    @SerialName("pageNumber")    val pageNumber    : Int,
    @SerialName("isLast")        val isLast        : Boolean = false
)

// ── Users ─────────────────────────────────────────────────────────────────────

@Serializable data class CreateUserRequest(
    val fullName        : String,
    val email           : String,
    val password        : String,
    val phone           : String? = null,
    val city            : String? = null,
    val address         : String? = null,
    val profileImageUrl : String? = null,
    val role            : String  = "PARENT"
)

@Serializable data class UpdateUserRequest(
    val fullName        : String? = null,
    val phone           : String? = null,
    val city            : String? = null,
    val address         : String? = null,
    val profileImageUrl : String? = null
)

@Serializable data class UserResponse(
    val userId          : String,
    val fullName        : String,
    val email           : String,
    val phone           : String? = null,
    val city            : String? = null,
    val address         : String? = null,
    val profileImageUrl : String? = null,
    val role            : String,
    val isActive        : Boolean = false,
    val createdAt       : String? = null,
    val updatedAt       : String? = null
)

@Serializable data class PreRegisterResponse(
    val email    : String,
    val codeSent : Boolean
)

// ── Babies ────────────────────────────────────────────────────────────────────

@Serializable data class CreateBabyRequest(
    val fullName               : String,
    val dateOfBirth            : String,
    val gender                 : String,
    val birthWeight            : Double? = null,
    val birthHeight            : Double? = null,
    val birthHeadCircumference : Double? = null,
    val photoUrl               : String? = null
)

@Serializable data class UpdateBabyRequest(
    val fullName : String? = null,
    val photoUrl : String? = null
)

@Serializable data class ArchiveBabyRequest(val status: String)

@Serializable data class BabyResponse(
    val babyId                 : String,
    val parentUserId           : String,
    val parentName             : String,
    val fullName               : String,
    val dateOfBirth            : String,
    val gender                 : String,
    val birthWeight            : Double? = null,
    val birthHeight            : Double? = null,
    val birthHeadCircumference : Double? = null,
    val ageInMonths            : Int,
    val ageInDays              : Long,
    val photoUrl               : String? = null,
    val isActive               : Boolean = true,
    val createdAt              : String? = null,
    val updatedAt              : String? = null
)

// ── Growth Records ────────────────────────────────────────────────────────────

@Serializable data class CreateGrowthRecordRequest(
    val babyId                      : String,
    val measurementDate             : String,
    val weight                      : Double? = null,
    val height                      : Double? = null,
    val headCircumference           : Double? = null,
    val weightPercentile            : Int?    = null,
    val heightPercentile            : Int?    = null,
    val headCircumferencePercentile : Int?    = null
)

@Serializable data class UpdateGrowthRecordRequest(
    val measurementDate             : String? = null,
    val weight                      : Double? = null,
    val height                      : Double? = null,
    val headCircumference           : Double? = null,
    val weightPercentile            : Int?    = null,
    val heightPercentile            : Int?    = null,
    val headCircumferencePercentile : Int?    = null
)

@Serializable
data class GrowthRecordResponse(
    val recordId                    : String,
    val babyId                      : String,
    val babyName                    : String,
    val measurementDate             : String,
    val ageInMonths                 : Int,
    val ageInDays                   : Int?    = null,
    val weight                      : Double? = null,
    val height                      : Double? = null,
    val headCircumference           : Double? = null,
    val weightPercentile            : Int?    = null,
    val heightPercentile            : Int?    = null,
    val headCircumferencePercentile : Int?    = null,
    val measuredByName              : String? = null,
    @SerialName("isTeamMeasurement")
    val isTeamMeasurement           : Boolean = false,
    val createdAt                   : String? = null,
    val updatedAt                   : String? = null
) {
    val addedByTeam: Boolean
        get() = isTeamMeasurement || !measuredByName.isNullOrBlank()
}

// ── Vaccinations ──────────────────────────────────────────────────────────────

@Serializable data class CreateVaccinationRequest(
    val babyId        : String,
    val vaccineId     : Int,
    val scheduledDate : String,
    val location      : String? = null,
    val notes         : String? = null
)

@Serializable data class MarkVaccinationDoneRequest(
    val administeredDate : String,
    val notes            : String? = null
)

@Serializable data class VaccinationResponse(
    val vaccinationId    : String,
    val babyId           : String,
    val babyName         : String,
    val vaccineId        : Int,
    val vaccineName      : String,
    val scheduledDate    : String,
    val administeredDate : String? = null,
    val status           : String,
    val location         : String? = null,
    val notes            : String? = null,
    val createdAt        : String
)

@Serializable data class RescheduleRequest(
    val shiftReason       : String,
    val rescheduleOverdue : Boolean,
    val notes             : String? = null
)

@Serializable data class UpdateScheduleStatusRequest(
    val status : String,
    val notes  : String? = null
)

// ── Benches ───────────────────────────────────────────────────────────────────

@Serializable data class VaccinationBenchNet(
    val benchId           : String,
    val nameEn            : String,
    val nameAr            : String,
    val governorate       : String,
    val district          : String,
    val addressEn         : String?      = null,
    val addressAr         : String?      = null,
    val latitude          : Double,
    val longitude         : Double,
    val phone             : String?      = null,
    val workingDays       : List<String> = emptyList(),
    val workingHoursStart : String       = "08:00",
    val workingHoursEnd   : String       = "14:00",
    val vaccinationDays   : List<String> = emptyList(),
    val type              : String       = "",
    val vaccinesAvailable : List<String> = emptyList(),
    val isActive          : Boolean      = true
)

@Serializable data class BabyBenchAssignmentNet(
    val assignmentId : String,
    val babyId       : String,
    val babyName     : String,
    val benchId      : String,
    val benchNameEn  : String,
    val benchNameAr  : String,
    val governorate  : String,
    val isActive     : Boolean = true
)

// ── Vaccination Schedules ─────────────────────────────────────────────────────

@Serializable data class VaccinationScheduleNet(
    val scheduleId           : String,
    val babyId               : String,
    val babyName             : String,
    val benchId              : String,
    val benchNameEn          : String,
    val benchNameAr          : String,
    val vaccineId            : Int,
    val vaccineName          : String,
    val vaccineNameAr        : String? = null,
    val vaccineNameKu        : String? = null,
    val vaccineNameCkb       : String? = null,
    val description          : String? = null,
    val descriptionAr        : String? = null,
    val descriptionKu        : String? = null,
    val descriptionCkb       : String? = null,
    val doseNumber           : Int,
    val recommendedAgeMonths : Int,
    val idealDate            : String,
    val scheduledDate        : String,
    val shiftReason          : String  = "NONE",
    val shiftDays            : Int     = 0,
    val status               : String,
    val completedDate        : String? = null,
    val completedByName      : String? = null,
    val isVisibleToParent    : Boolean = true,
    val isVisibleToTeam      : Boolean = true,
    val createdAt            : String? = null,
    val updatedAt            : String? = null
)

// ── Health Issues ─────────────────────────────────────────────────────────────

@Serializable data class HealthIssueNet(
    val issueId        : String,
    val babyId         : String,
    val title          : String,
    val description    : String? = null,
    val issueDate      : String,
    val severity       : String? = null,
    val isResolved     : Boolean = false,
    val resolutionDate : String? = null,
    val resolvedNotes  : String? = null
)

// ── Family History ────────────────────────────────────────────────────────────

@Serializable data class FamilyHistoryRequest(
    val babyId                 : String,
    val heredity               : String? = null,
    val bloodDiseases          : String? = null,
    val cardiovascularDiseases : String? = null,
    val metabolicDiseases      : String? = null,
    val appendicitis           : String? = null,
    val tuberculosis           : String? = null,
    val parkinsonism           : String? = null,
    val allergies              : String? = null,
    val others                 : String? = null
)

@Serializable data class FamilyHistoryNet(
    val historyId              : String  = "",
    val babyId                 : String  = "",
    val heredity               : String? = null,
    val bloodDiseases          : String? = null,
    val cardiovascularDiseases : String? = null,
    val metabolicDiseases      : String? = null,
    val appendicitis           : String? = null,
    val tuberculosis           : String? = null,
    val parkinsonism           : String? = null,
    val allergies              : String? = null,
    val others                 : String? = null
)

// ── Child Illnesses ───────────────────────────────────────────────────────────

@Serializable data class ChildIllnessNet(
    val illnessId     : String,
    val babyId        : String,
    val illnessName   : String,
    val diagnosisDate : String?  = null,
    val notes         : String?  = null,
    val isActive      : Boolean  = true,
    val createdAt     : String?  = null,
    val updatedAt     : String?  = null
)

@Serializable data class ChildIllnessRequest(
    val babyId        : String,
    val illnessName   : String,
    val diagnosisDate : String?  = null,
    val notes         : String?  = null,
    val isActive      : Boolean  = true
)

// ── Pre-Check Investigations ──────────────────────────────────────────────────

@Serializable
enum class InvestigationStatusNet {
    yes, no, not_known
}

@Serializable
data class PreCheckInvestigationNet(
    val investigationId      : String                 = "",
    val babyId               : String                 = "",
    val checkDate            : String                 = "",
    val jaundice             : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val shortnessOfBreath    : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val turningBlue          : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val marbleHeart          : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val inflammationOfLiver  : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val inflammationOfSpleen : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val hernia               : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val hydroceleOfEar       : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val hipJointDislocation  : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val musclesNormal        : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val reactionsNormal      : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val nucleusNormal        : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val genitalsNormal       : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val eyeNormal            : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val redReflex            : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val reactionToSound      : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val others               : String?                = null,
    val createdAt            : String?                = null,
    val updatedAt            : String?                = null
)

@Serializable
data class PreCheckInvestigationRequest(
    val babyId               : String,
    val checkDate            : String,
    val jaundice             : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val shortnessOfBreath    : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val turningBlue          : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val marbleHeart          : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val inflammationOfLiver  : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val inflammationOfSpleen : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val hernia               : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val hydroceleOfEar       : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val hipJointDislocation  : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val musclesNormal        : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val reactionsNormal      : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val nucleusNormal        : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val genitalsNormal       : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val eyeNormal            : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val redReflex            : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val reactionToSound      : InvestigationStatusNet = InvestigationStatusNet.not_known,
    val others               : String?                = null
)

// ── Appointments ──────────────────────────────────────────────────────────────

@Serializable data class AppointmentNet(
    val appointmentId   : String,
    val babyId          : String,
    val babyName        : String,
    val appointmentType : String,
    val scheduledDate   : String,
    val scheduledTime   : String? = null,
    val durationMinutes : Int     = 30,
    val status          : String,
    val doctorName      : String? = null,
    val location        : String? = null,
    val notes           : String? = null
)

// ── Memories ──────────────────────────────────────────────────────────────────

@Serializable data class MemoryNet(
    val memoryId    : String,
    val babyId      : String,
    val babyName    : String,
    val title       : String,
    val description : String?       = null,
    val memoryDate  : String,
    val imageCount  : Int?          = null,
    val captions    : List<String>? = null,
    val ageInMonths : Int?          = null,
    val ageInDays   : Int?          = null,
    val createdAt   : String?       = null,
    val updatedAt   : String?       = null
)

@Serializable data class CreateMemoryRequest(
    val babyId      : String,
    val title       : String,
    val description : String?       = null,
    val memoryDate  : String,
    val imageCount  : Int           = 0,
    val captions    : List<String>? = null
)

@Serializable data class UpdateMemoryRequest(
    val title       : String?       = null,
    val description : String?       = null,
    val memoryDate  : String?       = null,
    val imageCount  : Int?          = null,
    val captions    : List<String>? = null
)

// ── Notifications ─────────────────────────────────────────────────────────────

@Serializable data class NotificationNet(
    val notificationId : String,
    val userId         : String,
    val title          : String,
    val body           : String,
    val type           : String? = null,
    val referenceId    : String? = null,
    val isRead         : Boolean = false,
    val createdAt      : String? = null
)

// =============================================================================
// Net → UI mappers
// =============================================================================

fun VaccinationBenchNet.toUi() = VaccinationBenchUi(
    benchId           = benchId,
    nameEn            = nameEn,
    nameAr            = nameAr,
    governorate       = governorate,
    district          = district,
    addressEn         = addressEn,
    addressAr         = addressAr,
    latitude          = latitude,
    longitude         = longitude,
    phone             = phone,
    workingDays       = workingDays,
    workingHoursStart = workingHoursStart,
    workingHoursEnd   = workingHoursEnd,
    vaccinationDays   = vaccinationDays,
    type              = type,
    vaccinesAvailable = vaccinesAvailable,
    isActive          = isActive
)

fun BabyBenchAssignmentNet.toUi() = BabyBenchAssignmentUi(
    assignmentId = assignmentId,
    babyId       = babyId,
    babyName     = babyName,
    benchId      = benchId,
    benchNameEn  = benchNameEn,
    benchNameAr  = benchNameAr,
    governorate  = governorate,
    isActive     = isActive
)

fun VaccinationScheduleNet.toUi() = VaccinationScheduleUi(
    scheduleId           = scheduleId,
    babyId               = babyId,
    vaccineId            = vaccineId,
    vaccineName          = vaccineName,
    vaccineNameAr        = vaccineNameAr,
    vaccineNameKu        = vaccineNameKu,
    vaccineNameCkb       = vaccineNameCkb,
    description          = description,
    descriptionAr        = descriptionAr,
    descriptionKu        = descriptionKu,
    descriptionCkb       = descriptionCkb,
    doseNumber           = doseNumber,
    recommendedAgeMonths = recommendedAgeMonths,
    idealDate            = idealDate,
    scheduledDate        = scheduledDate,
    shiftReason          = shiftReason,
    shiftDays            = shiftDays,
    status               = status,
    completedDate        = completedDate,
    benchNameEn          = benchNameEn,
    isVisibleToParent    = isVisibleToParent
)

fun HealthIssueNet.toUi() = HealthIssueUi(
    issueId        = issueId,
    babyId         = babyId,
    title          = title,
    description    = description,
    issueDate      = issueDate,
    severity       = severity,
    isResolved     = isResolved,
    resolutionDate = resolutionDate,
    resolvedNotes  = resolvedNotes
)

fun AppointmentNet.toUi() = AppointmentUi(
    appointmentId   = appointmentId,
    babyId          = babyId,
    babyName        = babyName,
    appointmentType = appointmentType,
    scheduledDate   = scheduledDate,
    scheduledTime   = scheduledTime,
    durationMinutes = durationMinutes,
    status          = status,
    doctorName      = doctorName,
    location        = location,
    notes           = notes
)
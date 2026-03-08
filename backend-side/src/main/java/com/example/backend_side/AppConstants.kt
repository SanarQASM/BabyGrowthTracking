package com.example.backend_side

// ─────────────────────────────────────────────────────────────────────────────
// AppConstants — NEW FILE
//
// Centralises all magic numbers and hardcoded string literals previously
// scattered across Services.kt, Controllers, GlobalExceptionHandler, and
// NotificationService.
//
// USAGE:
//   import com.example.backend_side.AppConstants
//   throw ResourceNotFoundException(AppConstants.Messages.BABY_NOT_FOUND)
// ─────────────────────────────────────────────────────────────────────────────

object AppConstants {

    // =========================================================================
    // ★ API / Server config — WAS: scattered string literals in Openapiconfig.kt
    // =========================================================================
    object Api {
        const val VERSION           = "1.0.0"
        const val TITLE             = "Baby Growth Tracking API"
        const val DESCRIPTION_BRIEF = "API for Managing Baby Growth, Vaccinations, and Health Records"
        const val CONTACT_NAME      = "Baby Growth Support"
        const val CONTACT_EMAIL     = "support@babygrowth.com"

        const val SERVER_LOCAL      = "http://localhost:8080/api"
        const val SERVER_EMULATOR   = "http://10.0.2.2:8080/api"
        const val SERVER_PRODUCTION = "https://api.babygrowth.com"
        const val SERVER_LOCAL_DESC = "Local Development"
        const val SERVER_EMU_DESC   = "Android Emulator"
        const val SERVER_PROD_DESC  = "Production"

        const val SECURITY_SCHEME   = "bearerAuth"
        const val BEARER_FORMAT     = "JWT"
    }

    // =========================================================================
    // ★ JWT / Auth — WAS: magic numbers in Securityconfig.kt / JwtUtil
    // =========================================================================
    object Auth {
        // WAS: 86400000L (24 hours in ms) hardcoded in JwtUtil token generation
        const val TOKEN_EXPIRY_MS         = 86_400_000L    // 24 hours
        const val REFRESH_TOKEN_EXPIRY_MS = 604_800_000L   // 7 days
        // WAS: "10 minutes" string hardcoded in email HTML templates
        const val OTP_VALID_MINUTES       = 10
        const val OTP_VALID_MINUTES_LABEL = "10 minutes"   // used in email copy
        // WAS: 6 hardcoded as OTP length in code generation logic
        const val OTP_LENGTH              = 6
        // WAS: "Bearer " prefix used in multiple filter classes
        const val BEARER_PREFIX           = "Bearer "
        const val HEADER_AUTHORIZATION    = "Authorization"
        const val HEADER_USER_ID          = "X-User-Id"
    }

    // =========================================================================
    // ★ Success messages — WAS: inline string literals in controllers
    // =========================================================================
    object Messages {
        // Baby
        const val BABY_CREATED    = "Baby created successfully"
        const val BABY_UPDATED    = "Baby updated successfully"
        const val BABY_RETRIEVED  = "Baby retrieved"
        const val BABY_DELETED    = "Baby deleted successfully"
        const val BABY_NOT_FOUND  = "Baby not found"

        // User / Auth
        const val USER_REGISTERED        = "User registered successfully"
        const val USER_NOT_FOUND         = "User not found"
        const val EMAIL_ALREADY_USED     = "Email already registered"
        const val INVALID_CREDENTIALS    = "Invalid email or password"
        const val PASSWORD_RESET_OK      = "Password reset successfully"
        const val ACCOUNT_VERIFIED       = "Account verified successfully"
        const val VERIFICATION_SENT      = "Verification code sent"
        const val RESET_CODE_SENT        = "Reset code sent to your email"
        const val RESET_CODE_INVALID     = "Invalid or expired reset code"

        // Growth records
        const val GROWTH_RECORD_CREATED  = "Growth record created"
        const val GROWTH_RECORD_DELETED  = "Growth record deleted"
        const val GROWTH_RECORD_UPDATED  = "Growth record updated"

        // Vaccination
        const val VACCINATION_UPDATED    = "Vaccination updated"
        const val VACCINATION_CREATED    = "Vaccination scheduled"

        // Memory
        const val MEMORY_CREATED         = "Memory created"
        const val MEMORY_DELETED         = "Memory deleted"

        // Appointment
        const val APPOINTMENT_CREATED    = "Appointment created"
        const val APPOINTMENT_CANCELLED  = "Appointment cancelled"

        // Generic / fallback — WAS in GlobalExceptionHandler
        const val RESOURCE_NOT_FOUND      = "Resource not found"
        const val RESOURCE_ALREADY_EXISTS = "Resource already exists"
        const val UNAUTHORIZED            = "Unauthorized"
        const val FORBIDDEN               = "Forbidden"
        const val BAD_REQUEST             = "Bad request"
        const val VALIDATION_FAILED       = "Validation failed"
        const val UNEXPECTED_ERROR        = "An unexpected error occurred"
        const val INTERNAL_SERVER_ERROR   = "Internal server error"
        const val INVALID_ARGUMENT        = "Invalid argument"
        const val UNKNOWN_FIELD           = "unknown"
    }

    // =========================================================================
    // ★ Notification / Email copy — WAS: inline HTML strings in NotificationService
    // =========================================================================
    object Email {
        const val SENDER_NAME            = "BabyGrowth"
        const val SENDER_NO_REPLY        = "Do not reply to this email."
        const val COPYRIGHT              = "© 2025 BabyGrowth"
        const val SUPPORT_EMAIL          = "support@babygrowth.com"
        const val VERIFICATION_SUBJECT   = "Verify your BabyGrowth account"
        const val RESET_SUBJECT          = "Reset your BabyGrowth password"
        const val WELCOME_GOOGLE_SUBJECT = "Welcome to BabyGrowth 🍼"
        // WAS: "10 minutes" hardcoded in 3 separate HTML template strings
        const val CODE_VALIDITY_LABEL    = "10 minutes"
        // WAS: "BabyGrowth: Your verification code is $code. Valid for 10 minutes."
        const val SMS_TEMPLATE_PREFIX    = "BabyGrowth: Your verification code is "
        const val SMS_TEMPLATE_SUFFIX    = ". Valid for ${AppConstants.Email.CODE_VALIDITY_LABEL}. Do not share this code."
    }

    // =========================================================================
    // ★ Pagination defaults — WAS: PageRequest.of(0, 20) magic numbers
    // =========================================================================
    object Pagination {
        const val DEFAULT_PAGE      = 0
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE     = 100
    }

    // =========================================================================
    // ★ Validation constraints — WAS: inline @Size / @Min / @Max annotation values
    // =========================================================================
    object Validation {
        const val NAME_MAX_LENGTH      = 100
        const val EMAIL_MAX_LENGTH     = 255
        const val PASSWORD_MIN_LENGTH  = 8
        const val PASSWORD_MAX_LENGTH  = 128
        const val NOTES_MAX_LENGTH     = 1000
        // WAS: 0.5 / 150.0 as Double literals in weight/height range checks
        const val WEIGHT_KG_MIN        = 0.1
        const val WEIGHT_KG_MAX        = 300.0
        const val HEIGHT_CM_MIN        = 10.0
        const val HEIGHT_CM_MAX        = 300.0
        const val HEAD_CIRC_CM_MIN     = 20.0
        const val HEAD_CIRC_CM_MAX     = 100.0
    }
}
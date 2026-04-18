package com.example.backend_side

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

// Custom Exceptions
class ResourceNotFoundException(message: String)    : RuntimeException(message)
class ResourceAlreadyExistsException(message: String) : RuntimeException(message)
class UnauthorizedException(message: String)         : RuntimeException(message)
class ForbiddenException(message: String)            : RuntimeException(message)
class BadRequestException(message: String)           : RuntimeException(message)

/**
 * Global Exception Handler
 *
 * REFACTORED:
 *  All fallback message strings replaced with AppConstants.Messages references.
 *  ┌────────────────────────────────────────────────────────────────────────┐
 *  │ WAS                            │ NOW                                    │
 *  ├────────────────────────────────────────────────────────────────────────┤
 *  │ "Resource not found"           │ AppConstants.Messages.RESOURCE_NOT_FOUND      │
 *  │ "Resource already exists"      │ AppConstants.Messages.RESOURCE_ALREADY_EXISTS │
 *  │ "Unauthorized"                 │ AppConstants.Messages.UNAUTHORIZED            │
 *  │ "Forbidden"                    │ AppConstants.Messages.FORBIDDEN               │
 *  │ "Bad request"                  │ AppConstants.Messages.BAD_REQUEST             │
 *  │ "Validation failed"            │ AppConstants.Messages.VALIDATION_FAILED       │
 *  │ "Invalid argument"             │ AppConstants.Messages.INVALID_ARGUMENT        │
 *  │ "An unexpected error occurred" │ AppConstants.Messages.UNEXPECTED_ERROR        │
 *  │ "Internal server error"        │ AppConstants.Messages.INTERNAL_SERVER_ERROR   │
 *  │ "unknown"  (field fallback)    │ AppConstants.Messages.UNKNOWN_FIELD           │
 *  │ "Validation failed" (per-field)│ AppConstants.Messages.VALIDATION_FAILED       │
 *  └────────────────────────────────────────────────────────────────────────┘
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: ResourceNotFoundException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error { "Resource not found: ${ex.message}" }
        // WAS: message = ex.message ?: "Resource not found"
        val response = ApiResponse<Nothing>(
            success   = false,
            message   = ex.message ?: AppConstants.Messages.RESOURCE_NOT_FOUND,
            errors    = listOf(ex.message ?: AppConstants.Messages.RESOURCE_NOT_FOUND),
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(ResourceAlreadyExistsException::class)
    fun handleResourceAlreadyExistsException(
        ex: ResourceAlreadyExistsException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error { "Resource already exists: ${ex.message}" }
        // WAS: message = ex.message ?: "Resource already exists"
        val response = ApiResponse<Nothing>(
            success   = false,
            message   = ex.message ?: AppConstants.Messages.RESOURCE_ALREADY_EXISTS,
            errors    = listOf(ex.message ?: AppConstants.Messages.RESOURCE_ALREADY_EXISTS),
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(
        ex: UnauthorizedException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error { "Unauthorized: ${ex.message}" }
        // WAS: message = ex.message ?: "Unauthorized"
        val response = ApiResponse<Nothing>(
            success   = false,
            message   = ex.message ?: AppConstants.Messages.UNAUTHORIZED,
            errors    = listOf(ex.message ?: AppConstants.Messages.UNAUTHORIZED),
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(
        ex: ForbiddenException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error { "Forbidden: ${ex.message}" }
        // WAS: message = ex.message ?: "Forbidden"
        val response = ApiResponse<Nothing>(
            success   = false,
            message   = ex.message ?: AppConstants.Messages.FORBIDDEN,
            errors    = listOf(ex.message ?: AppConstants.Messages.FORBIDDEN),
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(
        ex: BadRequestException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error { "Bad request: ${ex.message}" }
        // WAS: message = ex.message ?: "Bad request"
        val response = ApiResponse<Nothing>(
            success   = false,
            message   = ex.message ?: AppConstants.Messages.BAD_REQUEST,
            errors    = listOf(ex.message ?: AppConstants.Messages.BAD_REQUEST),
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error { "Validation error: ${ex.message}" }
        val errors = ex.bindingResult.allErrors.map { error ->
            // WAS: (error as? FieldError)?.field ?: "unknown"
            val fieldName    = (error as? FieldError)?.field ?: AppConstants.Messages.UNKNOWN_FIELD
            // WAS: error.defaultMessage ?: "Validation failed"
            val errorMessage = error.defaultMessage ?: AppConstants.Messages.VALIDATION_FAILED
            "$fieldName: $errorMessage"
        }
        // WAS: message = "Validation failed"
        val response = ApiResponse<Nothing>(
            success   = false,
            message   = AppConstants.Messages.VALIDATION_FAILED,
            errors    = errors,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error { "Illegal argument: ${ex.message}" }
        // WAS: message = ex.message ?: "Invalid argument"
        val response = ApiResponse<Nothing>(
            success   = false,
            message   = ex.message ?: AppConstants.Messages.INVALID_ARGUMENT,
            errors    = listOf(ex.message ?: AppConstants.Messages.INVALID_ARGUMENT),
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error(ex) { "Unexpected error: ${ex.message}" }
        // WAS: message = "An unexpected error occurred"
        val response = ApiResponse<Nothing>(
            success   = false,
            message   = AppConstants.Messages.UNEXPECTED_ERROR,
            // WAS: ex.message ?: "Internal server error"
            errors    = listOf(ex.message ?: AppConstants.Messages.INTERNAL_SERVER_ERROR),
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(
        ex: org.springframework.web.HttpRequestMethodNotSupportedException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error { "Method not supported: ${ex.method} — supported: ${ex.supportedMethods?.toList()} — path: ${(request as? org.springframework.web.context.request.ServletWebRequest)?.request?.requestURI}" }
        val response = ApiResponse<Nothing>(
            success = false,
            message = "Method ${ex.method} not supported for this endpoint",
            errors  = listOf("Supported methods: ${ex.supportedMethods?.toList()}"),
            timestamp = java.time.LocalDateTime.now()
        )
        return ResponseEntity.status(org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED).body(response)
    }
}
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
class ResourceNotFoundException(message: String) : RuntimeException(message)
class ResourceAlreadyExistsException(message: String) : RuntimeException(message)
class UnauthorizedException(message: String) : RuntimeException(message)
class ForbiddenException(message: String) : RuntimeException(message)
class BadRequestException(message: String) : RuntimeException(message)

// Global Exception Handler
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: ResourceNotFoundException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error { "Resource not found: ${ex.message}" }

        val response = ApiResponse<Nothing>(
            success = false,
            message = ex.message ?: "Resource not found",
            errors = listOf(ex.message ?: "Resource not found"),
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

        val response = ApiResponse<Nothing>(
            success = false,
            message = ex.message ?: "Resource already exists",
            errors = listOf(ex.message ?: "Resource already exists"),
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

        val response = ApiResponse<Nothing>(
            success = false,
            message = ex.message ?: "Unauthorized",
            errors = listOf(ex.message ?: "Unauthorized"),
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

        val response = ApiResponse<Nothing>(
            success = false,
            message = ex.message ?: "Forbidden",
            errors = listOf(ex.message ?: "Forbidden"),
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

        val response = ApiResponse<Nothing>(
            success = false,
            message = ex.message ?: "Bad request",
            errors = listOf(ex.message ?: "Bad request"),
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
            val fieldName = (error as? FieldError)?.field ?: "unknown"
            val errorMessage = error.defaultMessage ?: "Validation failed"
            "$fieldName: $errorMessage"
        }

        val response = ApiResponse<Nothing>(
            success = false,
            message = "Validation failed",
            errors = errors,
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

        val response = ApiResponse<Nothing>(
            success = false,
            message = ex.message ?: "Invalid argument",
            errors = listOf(ex.message ?: "Invalid argument"),
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

        val response = ApiResponse<Nothing>(
            success = false,
            message = "An unexpected error occurred",
            errors = listOf(ex.message ?: "Internal server error"),
            timestamp = LocalDateTime.now()
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
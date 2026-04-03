package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models

/**
 * Cross-platform validation utilities
 * Works on Android, iOS, Desktop, and Web
 */
object ValidationUtils {

    /**
     * Validates email format using regex
     * More permissive than strict RFC 5322 but covers most common email formats
     *
     * @param email The email string to validate
     * @return true if email format is valid, false otherwise
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return emailRegex.matches(email)
    }

    /**
     * Validates phone number format
     * Accepts various formats: (123) 456-7890, 123-456-7890, 1234567890, +1 123 456 7890
     *
     * @param phone The phone string to validate
     * @return true if phone format is valid, false otherwise
     */
    fun isValidPhone(phone: String): Boolean {
        if (phone.isBlank()) return false

        // Remove all non-digit characters for validation
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")

        // Accept phone numbers with 10-15 digits (covers most international formats)
        return digitsOnly.length in 10..15
    }

    /**
     * Validates password strength
     *
     * @param password The password to validate
     * @param minLength Minimum password length (default: 6)
     * @return Pair of (isValid, errorMessage)
     */
    fun validatePassword(password: String, minLength: Int = 6): Pair<Boolean, String?> {
        return when {
            password.isBlank() -> false to "Password cannot be empty"
            password.length < minLength -> false to "Password must be at least $minLength characters"
            else -> true to null
        }
    }

    /**
     * Validates that two passwords match
     *
     * @param password The original password
     * @param confirmPassword The confirmation password
     * @return Pair of (isValid, errorMessage)
     */
    fun validatePasswordMatch(password: String, confirmPassword: String): Pair<Boolean, String?> {
        return when {
            confirmPassword.isBlank() -> false to "Please confirm your password"
            password != confirmPassword -> false to "Passwords do not match"
            else -> true to null
        }
    }

    /**
     * Validates full name
     *
     * @param fullName The full name to validate
     * @return Pair of (isValid, errorMessage)
     */
    fun validateFullName(fullName: String): Pair<Boolean, String?> {
        return when {
            fullName.isBlank() -> false to "Please enter your full name"
            fullName.trim().length < 2 -> false to "Name must be at least 2 characters"
            !fullName.trim().contains(" ") -> false to "Please enter your full name (first and last)"
            else -> true to null
        }
    }
}
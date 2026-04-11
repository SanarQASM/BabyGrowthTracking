package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import com.russhwolf.settings.Settings
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * PreferencesManager - Cross-platform data persistence
 *
 * Handles all app preferences including:
 * - Onboarding state
 * - User authentication state
 * - Account verification status
 * - User profile data
 * - Login credentials (optional save with "Remember Me")
 * - Language preferences
 * - Authentication tokens (with optional expiry support)
 * - Gender theme preferences
 *
 * Uses multiplatform-settings library for cross-platform storage:
 * - Android: SharedPreferences
 * - iOS: UserDefaults
 * - Desktop: Properties file
 * - Web: LocalStorage
 */
class PreferencesManager(private val settings: Settings) {

    companion object {
        // Onboarding
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"

        // Authentication State
        private const val KEY_USER_LOGGED_IN   = "user_logged_in"
        private const val KEY_IS_LOGGED_IN     = "is_logged_in"       // raw key used by repository
        private const val KEY_ACCOUNT_VERIFIED = "account_verified"

        // User Profile
        private const val KEY_USER_ID            = "user_id"
        private const val KEY_USER_EMAIL         = "user_email"
        private const val KEY_USER_NAME          = "user_name"
        private const val KEY_USER_PHONE         = "user_phone"
        private const val KEY_USER_PROFILE_IMAGE = "user_profile_image"

        // Authentication Token
        private const val KEY_AUTH_TOKEN   = "auth_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"

        // Saved Login Credentials (optional — controlled by "Remember Me" checkbox)
        private const val KEY_SAVED_EMAIL_OR_PHONE  = "saved_email_or_phone"
        private const val KEY_SAVED_EMAIL           = "saved_email"        // legacy plain-email key
        private const val KEY_SAVED_PASSWORD        = "saved_password"
        private const val KEY_SAVE_PASSWORD_ENABLED = "save_password_enabled"

        // Gender Theme
        private const val KEY_GENDER_THEME          = "gender_theme"
        private const val KEY_BABY_GENDER_IS_FEMALE = "baby_gender_is_female"
        // Navigation State — add inside companion object
        private const val KEY_LAST_SCREEN    = "last_screen"
        private const val KEY_LAST_TAB       = "last_tab"
        private const val KEY_LAST_SCREEN_TS = "last_screen_timestamp"
        private const val NAV_STATE_TTL_MS   = 30 * 60 * 1000L // 30 minutes
    }

    private val languageManager = LanguageManager(settings)

    /** Expose Settings instance for extension functions */
    fun getSettings(): Settings = settings

    // ============================================================================
    // ONBOARDING
    // ============================================================================

    fun isOnboardingComplete(): Boolean = settings.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    fun setOnboardingComplete(isComplete: Boolean) {
        settings.putBoolean(KEY_ONBOARDING_COMPLETE, isComplete)
    }

    // ============================================================================
    // AUTHENTICATION STATE
    // ============================================================================

    fun isUserLoggedIn(): Boolean = settings.getBoolean(KEY_USER_LOGGED_IN, false)

    /**
     * Sets BOTH the legacy "user_logged_in" key AND the "is_logged_in" key so
     * that AccountRepository.isLoggedIn() and any legacy checks both work.
     *
     * FIX: This is the single authoritative place that controls the auto-login
     * flag. AccountRepository.saveUserSession() now calls this with the correct
     * value based on rememberMe — so it is never accidentally set to true when
     * the user did not check "Save Password".
     */
    fun setUserLoggedIn(isLoggedIn: Boolean) {
        settings.putBoolean(KEY_USER_LOGGED_IN, isLoggedIn)
        settings.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)   // keep both keys in sync
    }

    // ============================================================================
    // ACCOUNT VERIFICATION
    // ============================================================================

    fun isAccountVerified(): Boolean = settings.getBoolean(KEY_ACCOUNT_VERIFIED, false)

    fun setAccountVerified(verified: Boolean) {
        settings.putBoolean(KEY_ACCOUNT_VERIFIED, verified)
    }

    fun clearAccountVerification() {
        settings.remove(KEY_ACCOUNT_VERIFIED)
    }

    // ============================================================================
    // USER PROFILE DATA
    // ============================================================================

    fun saveUserId(userId: String)             { settings.putString(KEY_USER_ID, userId) }
    fun getUserId(): String?                   = settings.getStringOrNull(KEY_USER_ID)

    fun saveUserEmail(email: String)           { settings.putString(KEY_USER_EMAIL, email) }
    fun getUserEmail(): String?                = settings.getStringOrNull(KEY_USER_EMAIL)

    fun saveUserName(name: String)             { settings.putString(KEY_USER_NAME, name) }
    fun getUserName(): String?                 = settings.getStringOrNull(KEY_USER_NAME)

    fun saveUserPhone(phone: String)           { settings.putString(KEY_USER_PHONE, phone) }
    fun getUserPhone(): String?                = settings.getStringOrNull(KEY_USER_PHONE)

    fun saveUserProfileImage(imageUrl: String) { settings.putString(KEY_USER_PROFILE_IMAGE, imageUrl) }
    fun getUserProfileImage(): String?         = settings.getStringOrNull(KEY_USER_PROFILE_IMAGE)

    // ============================================================================
    // AUTHENTICATION TOKEN (JWT)
    // ============================================================================

    fun saveAuthToken(token: String) {
        settings.putString(KEY_AUTH_TOKEN, token)
    }

    fun getAuthToken(): String? = settings.getStringOrNull(KEY_AUTH_TOKEN)

    fun clearAuthToken() {
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_TOKEN_EXPIRY)
    }

    fun saveTokenExpiry(expiryTimestamp: Long) {
        settings.putLong(KEY_TOKEN_EXPIRY, expiryTimestamp)
    }

    fun getTokenExpiry(): Long = settings.getLong(KEY_TOKEN_EXPIRY, 0L)

    /**
     * Returns true when:
     *  - a token exists AND is not blank, AND
     *  - either no expiry was set (backend doesn't provide one → treat as valid),
     *    or the stored expiry is in the future.
     */
    @OptIn(ExperimentalTime::class)
    fun isTokenValid(): Boolean {
        val token = getAuthToken() ?: return false
        if (token.isBlank()) return false
        val expiry = getTokenExpiry()
        return if (expiry > 0L) {
            Clock.System.now().toEpochMilliseconds() < expiry
        } else {
            true  // token present, no expiry stored → consider valid
        }
    }

    // ============================================================================
    // SAVED LOGIN CREDENTIALS  ("Remember Me" / "Save Password" feature)
    // ============================================================================

    /**
     * Saves email/phone + password AND sets the save_password_enabled flag to true.
     *
     * Only called by AccountRepository.login() when rememberMe = true.
     * When this flag is true, the next launch will:
     *   1. Find is_logged_in = true  (set by saveUserSession with rememberMe=true)
     *   2. Find a valid token
     *   3. Find save_password_enabled = true
     *   → All three gates pass → user goes straight to Home.
     *
     * SECURITY NOTE: For production, consider:
     * - Android: EncryptedSharedPreferences / Jetpack Security
     * - iOS: Keychain Services
     * - Desktop: Encrypted file storage
     * - Web: Token-based auth only (avoid storing passwords)
     */
    fun saveLoginCredentials(emailOrPhone: String, password: String) {
        settings.putString(KEY_SAVED_EMAIL_OR_PHONE, emailOrPhone)
        settings.putString(KEY_SAVED_EMAIL, emailOrPhone)   // keep legacy key in sync
        settings.putString(KEY_SAVED_PASSWORD, password)
        settings.putBoolean(KEY_SAVE_PASSWORD_ENABLED, true)
    }

    /**
     * Returns the saved email/phone ONLY if "Save Password" was enabled.
     * Falls back to the legacy "saved_email" key for backwards compatibility.
     */
    fun getSavedEmailOrPhone(): String? {
        if (!settings.getBoolean(KEY_SAVE_PASSWORD_ENABLED, false)) return null
        return settings.getStringOrNull(KEY_SAVED_EMAIL_OR_PHONE)
            ?: settings.getStringOrNull(KEY_SAVED_EMAIL)
    }

    /**
     * Returns saved password ONLY if "Save Password" was enabled, otherwise null.
     */
    fun getSavedPassword(): String? {
        if (!settings.getBoolean(KEY_SAVE_PASSWORD_ENABLED, false)) return null
        return settings.getStringOrNull(KEY_SAVED_PASSWORD)
    }

    /**
     * Wipes all saved credential data and sets save_password_enabled = false.
     *
     * FIX: After this call, isSavePasswordEnabled() returns false, which causes
     * AccountRepository.isLoggedIn() to return false on the next launch, forcing
     * the user back to the Login screen — even if is_logged_in was somehow left
     * as true from a previous session.
     */
    fun clearLoginCredentials() {
        settings.remove(KEY_SAVED_EMAIL_OR_PHONE)
        settings.remove(KEY_SAVED_EMAIL)
        settings.remove(KEY_SAVED_PASSWORD)
        settings.putBoolean(KEY_SAVE_PASSWORD_ENABLED, false)
    }

    /**
     * Returns true ONLY when the user explicitly checked "Save Password" at login.
     * This is used as the third gate in AccountRepository.isLoggedIn().
     */
    fun isSavePasswordEnabled(): Boolean =
        settings.getBoolean(KEY_SAVE_PASSWORD_ENABLED, false)

    // ============================================================================
    // GENDER THEME PREFERENCES
    // ============================================================================

    fun saveGenderTheme(theme: GenderTheme) {
        settings.putString(KEY_GENDER_THEME, theme.name)
    }

    fun getGenderTheme(): GenderTheme {
        val themeName = settings.getStringOrNull(KEY_GENDER_THEME) ?: return GenderTheme.NEUTRAL
        return try {
            GenderTheme.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            GenderTheme.NEUTRAL
        }
    }

    fun saveBabyGender(isFemale: Boolean) {
        settings.putBoolean(KEY_BABY_GENDER_IS_FEMALE, isFemale)
        saveGenderTheme(if (isFemale) GenderTheme.GIRL else GenderTheme.BOY)
    }

    fun getBabyGender(): Boolean? {
        return if (settings.hasKey(KEY_BABY_GENDER_IS_FEMALE)) {
            settings.getBoolean(KEY_BABY_GENDER_IS_FEMALE, false)
        } else null
    }

    fun isGenderThemeSet(): Boolean = settings.getStringOrNull(KEY_GENDER_THEME) != null

    fun clearGenderTheme() {
        settings.remove(KEY_GENDER_THEME)
        settings.remove(KEY_BABY_GENDER_IS_FEMALE)
    }
// ============================================================================
// NAVIGATION STATE PERSISTENCE
// ============================================================================

    @OptIn(ExperimentalTime::class)
    fun saveLastScreen(screenName: String, tabName: String) {
        settings.putString(KEY_LAST_SCREEN, screenName)
        settings.putString(KEY_LAST_TAB, tabName)
        settings.putLong(KEY_LAST_SCREEN_TS, Clock.System.now().toEpochMilliseconds())
    }

    @OptIn(ExperimentalTime::class)
    fun getLastScreen(): String? {
        val ts = settings.getLong(KEY_LAST_SCREEN_TS, 0L)
        if (ts == 0L) return null
        val age = Clock.System.now().toEpochMilliseconds() - ts
        return if (age < NAV_STATE_TTL_MS) settings.getStringOrNull(KEY_LAST_SCREEN) else null
    }

    fun getLastTab(): String = settings.getString(KEY_LAST_TAB, "HOME")

    fun clearLastScreen() {
        settings.remove(KEY_LAST_SCREEN)
        settings.remove(KEY_LAST_TAB)
        settings.remove(KEY_LAST_SCREEN_TS)
    }

    // ============================================================================
    // GENERIC GETTERS / SETTERS
    // ============================================================================

    fun putString(key: String, value: String)                  { settings.putString(key, value) }
    fun getString(key: String, defaultValue: String = "")      = settings.getString(key, defaultValue)
    fun getStringOrNull(key: String): String?                  = settings.getStringOrNull(key)

    fun putInt(key: String, value: Int)                        { settings.putInt(key, value) }
    fun getInt(key: String, defaultValue: Int = 0)             = settings.getInt(key, defaultValue)

    fun putLong(key: String, value: Long)                      { settings.putLong(key, value) }
    fun getLong(key: String, defaultValue: Long = 0L)          = settings.getLong(key, defaultValue)

    fun putBoolean(key: String, value: Boolean)                { settings.putBoolean(key, value) }
    fun getBoolean(key: String, defaultValue: Boolean = false) = settings.getBoolean(key, defaultValue)

    fun remove(key: String) { settings.remove(key) }

    // ============================================================================
    // LOGOUT & CLEANUP
    // ============================================================================

    /**
     * Standard logout: clears session data and token.
     * Intentionally preserves saved credentials (email pre-fill) and gender theme.
     */
    fun logout() {
        setUserLoggedIn(false)   // clears both "user_logged_in" and "is_logged_in"
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_EMAIL)
        settings.remove(KEY_USER_NAME)
        settings.remove(KEY_USER_PHONE)
        settings.remove(KEY_USER_PROFILE_IMAGE)
        clearAuthToken()
        clearAccountVerification()
        // Saved credentials and gender theme are intentionally preserved
    }

    /**
     * Full logout: also wipes saved login credentials.
     * Gender theme is still preserved across sessions.
     */
    fun logoutAndClearCredentials() {
        logout()
        clearLoginCredentials()
    }

    /** Nuclear option: wipes everything (use for account deletion / testing). */
    fun clearAllData() {
        settings.clear()
    }

    // ============================================================================
    // LANGUAGE PREFERENCES
    // ============================================================================

    fun getCurrentLanguage(): Language = languageManager.getCurrentLanguage()

    fun setLanguage(language: Language) { languageManager.setLanguage(language) }
}

/** Helper extension to check if a key exists in Settings. */
private fun Settings.hasKey(key: String): Boolean = this.keys.contains(key)
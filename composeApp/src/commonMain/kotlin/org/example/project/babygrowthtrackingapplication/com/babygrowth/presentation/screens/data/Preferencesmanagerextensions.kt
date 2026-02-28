package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

/**
 * Extension functions for PreferencesManager — login credential helpers.
 *
 * These are thin delegating wrappers kept for backwards-compatibility and
 * call-site convenience.  All real logic lives inside PreferencesManager
 * so there is a single source of truth.
 *
 * Cross-platform storage backend:
 * - Android  : SharedPreferences  (use EncryptedSharedPreferences in production)
 * - iOS      : UserDefaults       (use Keychain Services in production)
 * - Desktop  : Properties file    (use encrypted storage in production)
 * - Web      : LocalStorage       (prefer token-only auth; avoid storing passwords)
 */

/**
 * Save email/phone + password together.
 * Equivalent to calling [PreferencesManager.saveLoginCredentials] directly.
 */
fun PreferencesManager.saveCredentials(emailOrPhone: String, password: String) {
    saveLoginCredentials(emailOrPhone, password)
}

/**
 * Returns the saved email/phone when "Save Password" is enabled, otherwise null.
 * Checks the primary key first, then falls back to the legacy "saved_email" key.
 */
fun PreferencesManager.fetchSavedEmailOrPhone(): String? = getSavedEmailOrPhone()

/**
 * Returns the saved password when "Save Password" is enabled, otherwise null.
 */
fun PreferencesManager.fetchSavedPassword(): String? = getSavedPassword()

/**
 * Wipes all saved credential data and resets the feature flag to false.
 */
fun PreferencesManager.wipeLoginCredentials() {
    clearLoginCredentials()
}

/**
 * Returns true when the user previously checked "Save Password" at login.
 */
fun PreferencesManager.isRememberMeEnabled(): Boolean = isSavePasswordEnabled()
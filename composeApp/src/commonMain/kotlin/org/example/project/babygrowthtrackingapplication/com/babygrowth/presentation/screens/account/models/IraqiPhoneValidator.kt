// composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/com/babygrowth/presentation/screens/account/models/IraqiPhoneValidator.kt
package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models

/**
 * Validates and normalises Iraqi mobile phone numbers.
 *
 * Rules (Bug 2):
 *  • A number starting with 0 must be exactly 11 digits.
 *    e.g. 07507669716
 *  • A number starting with 964 must be exactly 13 digits.
 *    e.g. 9647507669716
 *  • 0 and 964 are interchangeable prefixes (964 replaces the leading 0).
 *  • If the user removes the leading 0, 964 is prepended automatically
 *    (handled by [normalise]).
 *  • The local part (after the prefix) must belong to a known Iraqi
 *    Mobile Network Code (MNC):
 *      Zain    : 780, 781, 782, 783
 *      Asiacell: 770, 771, 772, 773
 *      Korek   : 750, 751
 *
 * Usage:
 *   val error = IraqiPhoneValidator.validate("07507669716")  // null = valid
 *   val normalised = IraqiPhoneValidator.normalise("7507669716")
 *   // → "9647507669716"  (no leading 0 → 964 prefix added)
 */
object IraqiPhoneValidator {

    // Known Iraqi mobile network codes (first 3 digits of the 10-digit local part)
    private val VALID_MNCS = setOf(
        "780", "781", "782", "783",   // Zain
        "770", "771", "772", "773",   // Asiacell
        "750", "751"                  // Korek
    )

    /**
     * Normalises the raw input before storing it in state.
     *
     * Transformations:
     *  1. Strip all whitespace, dashes, parentheses.
     *  2. If the number has no leading 0 and no 964 prefix but starts with a
     *     valid MNC (e.g. "7507669716"), prepend "964".
     *  3. If it starts with "00964", replace with "964".
     *
     * The function does NOT validate the number — call [validate] separately.
     */
    fun normalise(raw: String): String {
        val digits = raw.filter { it.isDigit() }

        return when {
            // 00964XXXXXXXXXX → 964XXXXXXXXXX
            digits.startsWith("00964") -> "964" + digits.drop(5)

            // Already has the international prefix
            digits.startsWith("964") -> digits

            // Has the local 0-prefix (most common user input)
            digits.startsWith("0") -> digits

            // No prefix at all (user typed the MNC directly, e.g. "750…")
            // → prepend 964 as per the rule "if 0 is removed, use 964"
            digits.isNotEmpty() -> "964$digits"

            else -> raw
        }
    }

    /**
     * Returns `null` when valid, or a non-null error key/message when invalid.
     *
     * @param phone A normalised phone string (output of [normalise]).
     */
    fun validate(phone: String): String? {
        val digits = phone.filter { it.isDigit() }

        return when {
            digits.isBlank() ->
                "phone_error_required"

            digits.startsWith("0") && digits.length != 11 ->
                "phone_error_local_11_digits"   // 0 + 10 local digits

            digits.startsWith("964") && digits.length != 13 ->
                "phone_error_intl_13_digits"    // 964 + 10 local digits

            !digits.startsWith("0") && !digits.startsWith("964") ->
                "phone_error_invalid_prefix"

            !isValidMnc(digits) ->
                "phone_error_invalid_operator"

            else -> null
        }
    }

    /**
     * Returns `true` when the number belongs to a known Iraqi operator.
     * Extracts the 3-digit MNC from either format.
     */
    private fun isValidMnc(digits: String): Boolean {
        val mnc = when {
            digits.startsWith("964") -> digits.drop(3).take(3)  // 964[MNC]XXXXXXX
            digits.startsWith("0")   -> digits.drop(1).take(3)  // 0[MNC]XXXXXXX
            else                     -> return false
        }
        return mnc in VALID_MNCS
    }

    /**
     * Converts a number from one format to the other.
     * 07507669716 ↔ 9647507669716
     */
    fun toLocalFormat(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return when {
            digits.startsWith("964") -> "0" + digits.drop(3)
            digits.startsWith("0")   -> digits
            else                     -> phone
        }
    }

    fun toInternationalFormat(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return when {
            digits.startsWith("0")   -> "964" + digits.drop(1)
            digits.startsWith("964") -> digits
            else                     -> phone
        }
    }
}
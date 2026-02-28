package com.example.backend_side

import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class NotificationService(
    private val mailSender  : JavaMailSender,
    @Value("\${twilio.phone-number}")   private val twilioPhone  : String,
    @Value("\${app.mail.from-address}") private val fromAddress  : String,
    @Value("\${app.mail.from-name}")    private val fromName     : String
) {

    // ─── Verification email (signup flow) ─────────────────────────────────────

    fun sendVerificationEmail(toEmail: String, userName: String, code: String) {
        logger.info { "Sending verification email to $toEmail" }
        try {
            val message = mailSender.createMimeMessage()
            val helper  = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(fromAddress, fromName)
            helper.setTo(toEmail)
            helper.setSubject("Your BabyGrowth verification code")
            helper.setText(buildVerificationEmailHtml(userName, code), true)
            mailSender.send(message)
            logger.info { "Verification email sent to $toEmail" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send verification email to $toEmail" }
            throw RuntimeException("Failed to send verification email: ${e.message}")
        }
    }

    // ─── Verification SMS ─────────────────────────────────────────────────────

    fun sendVerificationSms(toPhone: String, code: String) {
        logger.info { "Sending verification SMS to $toPhone" }
        try {
            val normalised = if (toPhone.startsWith("+")) toPhone else "+$toPhone"
            Message.creator(PhoneNumber(normalised), PhoneNumber(twilioPhone), buildSmsText(code)).create()
            logger.info { "Verification SMS sent to $normalised" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send SMS to $toPhone" }
            throw RuntimeException("Failed to send verification SMS: ${e.message}")
        }
    }

    // ─── Google welcome email ─────────────────────────────────────────────────

    fun sendGoogleWelcomeEmail(toEmail: String, userName: String) {
        logger.info { "Sending Google welcome email to $toEmail" }
        try {
            val message = mailSender.createMimeMessage()
            val helper  = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(fromAddress, fromName)
            helper.setTo(toEmail)
            helper.setSubject("Welcome to BabyGrowth! 👶")
            helper.setText(buildGoogleWelcomeEmailHtml(userName), true)
            mailSender.send(message)
            logger.info { "Google welcome email sent to $toEmail" }
        } catch (e: Exception) {
            // Non-fatal — welcome email failure should not block login
            logger.error(e) { "Failed to send Google welcome email to $toEmail (non-fatal)" }
        }
    }

    // ─── NEW: Password reset email ────────────────────────────────────────────
    // Called from AuthController.forgotPassword() — Step 1 of the reset flow.
    // Uses a distinct subject and red/orange styling so it's clearly different
    // from the green signup verification email.

    fun sendPasswordResetEmail(toEmail: String, userName: String, code: String) {
        logger.info { "Sending password reset email to $toEmail" }
        try {
            val message = mailSender.createMimeMessage()
            val helper  = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(fromAddress, fromName)
            helper.setTo(toEmail)
            helper.setSubject("Reset your BabyGrowth password")
            helper.setText(buildPasswordResetEmailHtml(userName, code), true)
            mailSender.send(message)
            logger.info { "Password reset email sent to $toEmail" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send password reset email to $toEmail" }
            throw RuntimeException("Failed to send password reset email: ${e.message}")
        }
    }

    // =========================================================================
    // HTML builders
    // =========================================================================

    private fun buildVerificationEmailHtml(userName: String, code: String): String {
        val formattedCode = "${code.take(3)} ${code.takeLast(3)}"
        return """
            <!DOCTYPE html><html lang="en">
            <head><meta charset="UTF-8"/><meta name="viewport" content="width=device-width,initial-scale=1.0"/></head>
            <body style="margin:0;padding:0;background-color:#f9fafb;font-family:Arial,Helvetica,sans-serif;">
              <table width="100%" cellpadding="0" cellspacing="0" border="0" style="background-color:#f9fafb;padding:40px 0;">
                <tr><td align="center">
                  <table width="560" cellpadding="0" cellspacing="0" border="0"
                         style="background:#fff;border-radius:16px;box-shadow:0 2px 8px rgba(0,0,0,0.08);overflow:hidden;max-width:560px;width:100%;">
                    <tr><td style="background:linear-gradient(135deg,#f472b6,#ec4899);padding:32px 40px;text-align:center;">
                      <p style="margin:0;font-size:28px;">👶</p>
                      <h1 style="margin:8px 0 0;color:#fff;font-size:22px;font-weight:700;">BabyGrowth</h1>
                    </td></tr>
                    <tr><td style="padding:40px 40px 32px;">
                      <h2 style="margin:0 0 12px;color:#1f2937;font-size:20px;font-weight:700;">Verify your account</h2>
                      <p style="margin:0 0 24px;color:#6b7280;font-size:15px;line-height:1.6;">
                        Hi ${userName.split(" ").first()},<br/><br/>
                        Welcome to BabyGrowth! Use the code below to verify your account. Valid for <strong>10 minutes</strong>.
                      </p>
                      <table width="100%" cellpadding="0" cellspacing="0" border="0">
                        <tr><td align="center" style="padding:0 0 28px;">
                          <div style="display:inline-block;background:#fdf2f8;border:2px solid #f9a8d4;border-radius:12px;padding:20px 48px;">
                            <span style="font-size:36px;font-weight:700;letter-spacing:8px;color:#ec4899;font-family:'Courier New',monospace;">$formattedCode</span>
                          </div>
                        </td></tr>
                      </table>
                      <p style="margin:0;color:#9ca3af;font-size:13px;border-top:1px solid #f3f4f6;padding-top:24px;">
                        If you didn't create a BabyGrowth account, you can safely ignore this email.
                      </p>
                    </td></tr>
                    <tr><td style="background:#f9fafb;padding:20px 40px;text-align:center;border-top:1px solid #f3f4f6;">
                      <p style="margin:0;color:#9ca3af;font-size:12px;">© 2025 BabyGrowth · Do not reply to this email.</p>
                    </td></tr>
                  </table>
                </td></tr>
              </table>
            </body></html>
        """.trimIndent()
    }

    // ─── NEW: Password reset email HTML ───────────────────────────────────────
    // Orange/amber header to visually distinguish it from the pink verification email.

    private fun buildPasswordResetEmailHtml(userName: String, code: String): String {
        val formattedCode = "${code.take(3)} ${code.takeLast(3)}"
        return """
            <!DOCTYPE html><html lang="en">
            <head><meta charset="UTF-8"/><meta name="viewport" content="width=device-width,initial-scale=1.0"/></head>
            <body style="margin:0;padding:0;background-color:#f9fafb;font-family:Arial,Helvetica,sans-serif;">
              <table width="100%" cellpadding="0" cellspacing="0" border="0" style="background-color:#f9fafb;padding:40px 0;">
                <tr><td align="center">
                  <table width="560" cellpadding="0" cellspacing="0" border="0"
                         style="background:#fff;border-radius:16px;box-shadow:0 2px 8px rgba(0,0,0,0.08);overflow:hidden;max-width:560px;width:100%;">

                    <!-- Orange header — visually distinct from verification email -->
                    <tr><td style="background:linear-gradient(135deg,#fb923c,#f97316);padding:32px 40px;text-align:center;">
                      <p style="margin:0;font-size:28px;">🔐</p>
                      <h1 style="margin:8px 0 0;color:#fff;font-size:22px;font-weight:700;">Password Reset</h1>
                      <p style="margin:6px 0 0;color:#fed7aa;font-size:14px;">BabyGrowth</p>
                    </td></tr>

                    <tr><td style="padding:40px 40px 32px;">
                      <h2 style="margin:0 0 12px;color:#1f2937;font-size:20px;font-weight:700;">Reset your password</h2>
                      <p style="margin:0 0 24px;color:#6b7280;font-size:15px;line-height:1.6;">
                        Hi ${userName.split(" ").first()},<br/><br/>
                        We received a request to reset the password for your BabyGrowth account.
                        Use the code below to continue. It is valid for <strong>10 minutes</strong>.
                      </p>

                      <!-- Code box — amber styling -->
                      <table width="100%" cellpadding="0" cellspacing="0" border="0">
                        <tr><td align="center" style="padding:0 0 28px;">
                          <div style="display:inline-block;background:#fff7ed;border:2px solid #fdba74;border-radius:12px;padding:20px 48px;">
                            <span style="font-size:36px;font-weight:700;letter-spacing:8px;color:#f97316;font-family:'Courier New',monospace;">$formattedCode</span>
                          </div>
                        </td></tr>
                      </table>

                      <!-- Security warning -->
                      <table width="100%" cellpadding="0" cellspacing="0" border="0"
                             style="background:#fffbeb;border:1px solid #fde68a;border-radius:10px;margin:0 0 24px;">
                        <tr><td style="padding:16px 20px;">
                          <p style="margin:0;color:#92400e;font-size:13px;line-height:1.6;">
                            ⚠️ <strong>Didn't request this?</strong> If you did not ask to reset your password,
                            please ignore this email. Your account remains secure and no changes have been made.
                          </p>
                        </td></tr>
                      </table>

                      <p style="margin:0;color:#9ca3af;font-size:13px;border-top:1px solid #f3f4f6;padding-top:24px;">
                        This code will expire in 10 minutes and can only be used once.
                      </p>
                    </td></tr>

                    <tr><td style="background:#f9fafb;padding:20px 40px;text-align:center;border-top:1px solid #f3f4f6;">
                      <p style="margin:0;color:#9ca3af;font-size:12px;">© 2025 BabyGrowth · Do not reply to this email.</p>
                    </td></tr>
                  </table>
                </td></tr>
              </table>
            </body></html>
        """.trimIndent()
    }

    private fun buildGoogleWelcomeEmailHtml(userName: String): String {
        val firstName = userName.split(" ").first()
        return """
            <!DOCTYPE html><html lang="en">
            <head><meta charset="UTF-8"/></head>
            <body style="margin:0;padding:0;background:#f9fafb;font-family:Arial,sans-serif;">
              <table width="100%" cellpadding="0" cellspacing="0" border="0" style="padding:40px 0;">
                <tr><td align="center">
                  <table width="560" cellpadding="0" cellspacing="0" border="0"
                         style="background:#fff;border-radius:16px;box-shadow:0 2px 8px rgba(0,0,0,0.08);overflow:hidden;max-width:560px;">
                    <tr><td style="background:linear-gradient(135deg,#f472b6,#ec4899);padding:40px;text-align:center;">
                      <p style="margin:0;font-size:48px;">👶</p>
                      <h1 style="margin:12px 0 0;color:#fff;font-size:26px;font-weight:700;">Welcome to BabyGrowth!</h1>
                      <p style="margin:8px 0 0;color:#fce7f3;font-size:15px;">Your baby's growth journey starts here</p>
                    </td></tr>
                    <tr><td style="padding:40px;">
                      <p style="margin:0 0 20px;color:#1f2937;font-size:18px;font-weight:700;">Hi $firstName! 👋</p>
                      <p style="margin:0 0 20px;color:#6b7280;font-size:15px;line-height:1.7;">
                        Thank you for joining <strong>BabyGrowth</strong> with your Google account.
                        We're excited to be part of your parenting journey!
                      </p>
                      <table width="100%" cellpadding="0" cellspacing="0" border="0" style="background:#fdf2f8;border-radius:12px;margin:0 0 24px;">
                        <tr><td style="padding:24px;">
                          <p style="margin:0 0 12px;color:#ec4899;font-size:15px;font-weight:700;">🌸 What you can do:</p>
                          <p style="margin:4px 0;color:#4b5563;font-size:14px;">📏 Track growth milestones</p>
                          <p style="margin:4px 0;color:#4b5563;font-size:14px;">💉 Manage vaccination schedules</p>
                          <p style="margin:4px 0;color:#4b5563;font-size:14px;">📅 Schedule doctor appointments</p>
                          <p style="margin:4px 0;color:#4b5563;font-size:14px;">🏥 Keep a full health history</p>
                        </td></tr>
                      </table>
                      <table width="100%" cellpadding="0" cellspacing="0" border="0"
                             style="background:#fffbeb;border:1px solid #fde68a;border-radius:12px;">
                        <tr><td style="padding:20px 24px;">
                          <p style="margin:0 0 8px;color:#92400e;font-size:14px;font-weight:700;">🔔 Security Notice</p>
                          <p style="margin:0;color:#92400e;font-size:13px;line-height:1.6;">
                            A new account was created using your Google account (<strong>$userName</strong>).
                            If this wasn't you, contact <a href="mailto:support@babygrowth.com" style="color:#ec4899;">support@babygrowth.com</a>.
                          </p>
                        </td></tr>
                      </table>
                    </td></tr>
                    <tr><td style="background:#f9fafb;padding:20px 40px;text-align:center;border-top:1px solid #f3f4f6;">
                      <p style="margin:0;color:#9ca3af;font-size:12px;">© 2025 BabyGrowth · Do not reply.</p>
                    </td></tr>
                  </table>
                </td></tr>
              </table>
            </body></html>
        """.trimIndent()
    }

    private fun buildSmsText(code: String): String =
        "BabyGrowth: Your verification code is $code. Valid for 10 minutes. Do not share this code."
}
package com.example.backend_side

import com.twilio.Twilio
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import jakarta.annotation.PostConstruct

private val logger = KotlinLogging.logger {}

/**
 * Initialises the Twilio SDK once when Spring Boot starts.
 *
 * Twilio.init() is a static call that only needs to happen once per JVM lifetime.
 * Putting it here in a @Configuration class with @PostConstruct guarantees it runs
 * before any controller or service tries to send an SMS.
 */
@Configuration
class TwilioConfig(
    @Value("\${twilio.account-sid}") val accountSid  : String,
    @Value("\${twilio.auth-token}")  val authToken   : String,
    @Value("\${twilio.phone-number}") val phoneNumber : String
) {
    @PostConstruct
    fun init() {
        Twilio.init(accountSid, authToken)
        logger.info { "Twilio SDK initialised — sending from $phoneNumber" }
    }
}
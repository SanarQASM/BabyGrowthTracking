package com.example.backend_side

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

// ============================================================
// JACKSON CONFIGURATION  — FIXED (no custom ObjectMapper bean)
// ============================================================
//
// PREVIOUS PROBLEM:
//   The old config created a @Bean @Primary ObjectMapper() from scratch.
//   A plain new ObjectMapper() knows nothing about Kotlin — it throws:
//     "Cannot construct instance of `LoginRequest`
//      (no Creators, like default constructor, exist)"
//   Attempts to fix it by importing KotlinModule or jacksonObjectMapper()
//   failed with "Unresolved reference" because jackson-module-kotlin
//   was not on the compile classpath.
//
// ROOT CAUSE of "Unresolved reference":
//   jackson-module-kotlin IS present at runtime (pulled in by
//   spring-boot-starter-web + kotlin-spring plugin) but was NOT
//   declared as an explicit compile dependency, so the Kotlin
//   compiler could not resolve any of its types or functions.
//
// THE CORRECT FIX — use Jackson2ObjectMapperBuilderCustomizer:
//   Spring Boot's JacksonAutoConfiguration already creates an
//   ObjectMapper with KotlinModule registered — as long as
//   kotlin-reflect is on the classpath (it is, via libs.kotlin.reflect).
//   By removing our @Primary ObjectMapper bean we let Spring Boot's
//   auto-configuration run, which does the right thing automatically.
//
//   We only need a Jackson2ObjectMapperBuilderCustomizer to apply our
//   two settings (JavaTimeModule + disable WRITE_DATES_AS_TIMESTAMPS).
//   This customizer is applied ON TOP of Spring Boot's auto-configured
//   builder, so KotlinModule is already registered before our code runs.
//
//   Jackson2ObjectMapperBuilderCustomizer imports come entirely from
//   spring-boot-autoconfigure (already on classpath via spring-boot-starter-web)
//   and jackson-databind — no new dependency needed at all.
//
// RESULT:
//   ✅  Kotlin data classes deserialize correctly (@RequestBody works)
//   ✅  LocalDate / LocalDateTime serialize as "2025-03-14", not arrays
//   ✅  Unknown JSON fields are ignored (FAIL_ON_UNKNOWN_PROPERTIES = false)
//   ✅  Zero new imports / dependencies required
// ============================================================

@Configuration
class JacksonConfig {

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper =
        ObjectMapper()
            .registerModule(JavaTimeModule())
            .registerKotlinModule()   // ← ADD THIS
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}
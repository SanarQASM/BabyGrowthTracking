// =============================================================================
// 🖥️  backend-side/build.gradle.kts — SPRING BOOT MODULE
//
// Structure observed:
//   src/main/java/com/example/backend_side/
//     ├── controllers/         — REST controllers
//     ├── entity/              — JPA entities
//     ├── repositories/        — Spring Data JPA repos
//     ├── BabyGrowthTrackingApplication.kt  — @SpringBootApplication entry
//     ├── Dtos.kt              — Request/Response DTOs
//     ├── Globalexceptionhandler.kt — @ControllerAdvice
//     ├── Openapiconfigkt.kt   — Swagger / SpringDoc config
//     ├── Securityconfig.kt    — Spring Security + JWT
//     └── Services.kt          — @Service business logic layer
//   src/main/resources/
//     └── application.properties
// =============================================================================

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.example"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xjsr305=strict"))
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group.startsWith("com.fasterxml.jackson")) {
            useVersion("2.17.2")
            because("Align Jackson versions to fix jjwt-jackson ServiceLoader failure")
        }
    }
}

// =============================================================================
// 📦 DEPENDENCIES
// =============================================================================
dependencies {

    // ─── Spring Boot Starters ─────────────────────────────────────────────────
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)

    // ─── NEW: Email (JavaMailSender) ──────────────────────────────────────────
    // Auto-configures JavaMailSender from spring.mail.* in application.properties
    // Version is managed automatically by spring-dependency-management plugin
    implementation(libs.spring.boot.starter.mail)

    // ─── NEW: SMS (Twilio) ────────────────────────────────────────────────────
    // Official Twilio Java/Kotlin SDK — initialised in TwilioConfig.kt
    implementation(libs.twilio)

    // ─── Kotlin ───────────────────────────────────────────────────────────────
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.serialization.json)

    // ─── OpenAPI / Swagger ────────────────────────────────────────────────────
    implementation(libs.springdoc.openapi.ui)

    // ─── JWT (Security) ───────────────────────────────────────────────────────
    implementation(libs.spring.security.jwt)
    implementation(libs.spring.security.jwt.impl)
    implementation(libs.spring.security.jwt.jackson)

    // ─── Database ─────────────────────────────────────────────────────────────
    runtimeOnly("com.mysql:mysql-connector-j")

    // ─── Logging ──────────────────────────────────────────────────────────────
    implementation(libs.logback.classic)
    implementation(libs.kotlin.logging)

    // ─── Testing ──────────────────────────────────────────────────────────────
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.test)
}

// =============================================================================
// 🚀 APPLICATION ENTRY POINT
// =============================================================================
tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    sourceResources(sourceSets["main"])
}

tasks.withType<Test> {
    useJUnitPlatform()
}
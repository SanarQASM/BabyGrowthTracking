// =============================================================================
// 🏗️  build.gradle.kts — ROOT BUILD FILE
// This file ONLY declares plugins. It does NOT apply them here.
// Each submodule applies what it needs in its own build.gradle.kts.
// =============================================================================

plugins {
    // ─── Kotlin ───────────────────────────────────────────────────────────────
    alias(libs.plugins.kotlin.multiplatform)    apply false
    alias(libs.plugins.kotlin.android)          apply false
    alias(libs.plugins.kotlin.jvm)              apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization)    apply false
    // Required for Spring Boot — makes classes open for Spring proxies
    alias(libs.plugins.kotlin.spring)           apply false
    alias(libs.plugins.kotlin.jpa)              apply false

    // ─── Compose Multiplatform ────────────────────────────────────────────────
    alias(libs.plugins.compose.multiplatform)   apply false

    // ─── Android ──────────────────────────────────────────────────────────────
    alias(libs.plugins.android.application)     apply false
    alias(libs.plugins.android.library)         apply false

    // ─── Firebase / Google Services ───────────────────────────────────────────
    // Applied here with apply false; composeApp applies it directly
    alias(libs.plugins.google.services)         apply false

    // ─── Spring Boot (Backend) ────────────────────────────────────────────────
    alias(libs.plugins.spring.boot)             apply false
    alias(libs.plugins.spring.dependency.management) apply false
}
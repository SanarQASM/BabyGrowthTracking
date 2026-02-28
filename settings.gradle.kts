// =============================================================================
// ⚙️  settings.gradle.kts — ROOT SETTINGS
// Baby Growth Tracking Application
// =============================================================================

rootProject.name = "BabyGrowthTrackingApplication"

// ─── Plugin & Dependency Repositories ────────────────────────────────────────
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // All dependency repositories live here, not in individual build files
    repositories {
        google()
        mavenCentral()
        // ⚠️ REQUIRED for org.jetbrains.androidx.navigation:navigation-compose
        // and other JetBrains multiplatform AndroidX ports
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        // ⚠️ REQUIRED — JetBrains AndroidX artifacts live here
        // (navigation-compose, lifecycle, savedstate, etc.)
        maven("https://androidx.dev/storage/compose-compiler/repository")
        maven("https://plugins.gradle.org/m2/")
    }
}

// ─── Modules ──────────────────────────────────────────────────────────────────
// Every folder in the project that has its own build.gradle.kts goes here
include(":composeApp")   // 📱 Multiplatform UI — Android, iOS, Desktop, JS, WasmJS
include(":backend-side") // 🖥️  Spring Boot REST API
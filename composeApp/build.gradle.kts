// =============================================================================
// 📱 composeApp/build.gradle.kts — COMPOSE MULTIPLATFORM MODULE
// =============================================================================

@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

kotlin {
    // ✅ Let Kotlin handle the default hierarchy automatically
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm("desktop") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }

    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.wasm.js"
            }
        }
        binaries.executable()
    }

    sourceSets {

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.navigation.compose)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.compottie)
            implementation(libs.compottie.resources)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)

            implementation(libs.napier)
            implementation(libs.uuid)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            // ✅ THE FIX — apply the Firebase BOM as a platform dependency FIRST.
            //    This tells Gradle to use the BOM to resolve the version for every
            //    firebase-* entry that has no explicit version in the catalog.
            //    Without this line, firebase-storage (and any other no-version
            //    Firebase lib) is "Unresolved reference" at compile time.
            implementation(project.dependencies.platform(libs.firebase.bom))

            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
            implementation(compose.preview)
            implementation(libs.androidx.lifecycle.process)

            // Firebase — versions all come from the BOM above
            implementation(libs.firebase.auth)
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.storage)
            implementation(libs.firebase.messaging)

            // ✅ Needed for .await() on Firebase Task objects
            implementation(libs.kotlinx.coroutines.play.services)

            implementation(libs.play.services.auth)
            implementation(libs.credentials.core)
            implementation(libs.credentials.play.services)
            implementation(libs.googleid)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.play.services.maps)
            implementation(libs.maps.compose)
        }

        // ✅ iOS - no manual dependsOn, applyDefaultHierarchyTemplate handles it
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.androidx.datastore.preferences.core)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.cio)
                implementation(libs.androidx.datastore.preferences.core)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.slf4j.simple)
            }
        }

        // ✅ webMain - shared between jsMain and wasmJsMain
        val webMain by creating {
            dependsOn(commonMain.get())
            resources.srcDirs("src/webMain/resources")
        }

        jsMain.get().apply {
            dependsOn(webMain)
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        wasmJsMain.get().apply {
            dependsOn(webMain)
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }
}

android {
    namespace = "org.example.project.babygrowthtrackingapplication"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.example.project.babygrowthtrackingapplication"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.desktop {
    application {
        mainClass = "org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "BabyGrowthTracking"
            packageVersion = "1.0.0"
        }
    }
}

// ✅ Safety net for any remaining duplicate resource files
tasks.withType<Copy>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
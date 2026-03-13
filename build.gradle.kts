// FitLog - Project-level build configuration
// This file defines plugins used across all modules. We use the `apply false`
// pattern so that individual modules can opt-in to the plugins they need.
// This avoids applying plugins globally where they are not needed.

plugins {
    // Android application plugin — only applied at the app module level
    id("com.android.application") version "8.2.0" apply false

    // Kotlin Android plugin — provides Kotlin compilation support for Android
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false

    // KSP (Kotlin Symbol Processing) — used by Room for annotation processing
    // KSP is preferred over KAPT for better build performance
    id("com.google.devtools.ksp") version "1.9.21-1.0.16" apply false
}

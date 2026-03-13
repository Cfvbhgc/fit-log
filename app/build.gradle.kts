// FitLog - App Module Build Configuration
// This is the main (and only) module of the FitLog application.
// We use ViewBinding instead of Jetpack Compose for our XML-based UI approach,
// which pairs naturally with the MVP (Model-View-Presenter) architecture pattern.
//
// TODO: Consider splitting into feature modules if the app grows significantly
// (e.g., :feature:workout, :feature:progress, :feature:library)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // KSP is used by Room for compile-time annotation processing
    // It generates DAO implementations and database schema validation
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.fitlog.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fitlog.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // TODO: Add testInstrumentationRunner for UI tests
        // testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Room schema export — useful for migration testing and debugging
        // The exported JSON schema files document the database structure at each version
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            // TODO: Enable minification once ProGuard rules are configured
            // for Room, MPAndroidChart, and other reflection-dependent libraries
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Java 17 is the minimum for Room 2.6+ with KSP
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // ViewBinding generates type-safe binding classes for each XML layout,
    // eliminating the need for findViewById() calls and reducing NPE risk.
    // This is essential for our MVP pattern where Views bind to XML layouts.
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX Core — Kotlin extensions for Android framework APIs
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Material Design Components — themed widgets like MaterialCardView,
    // BottomNavigationView, FloatingActionButton, TextInputLayout, etc.
    implementation(libs.material)

    // Layout libraries
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.fragment.ktx)

    // Room Database — our local persistence layer
    // room-runtime: Core Room library with annotations and runtime support
    // room-ktx: Adds Kotlin coroutine support for DAO methods (suspend functions, Flow)
    // room-compiler: KSP processor that generates DAO implementations at compile time
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Kotlin Coroutines — structured concurrency for database operations,
    // network calls, and other async work. The Android artifact adds
    // Dispatchers.Main and lifecycle-aware coroutine scopes.
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // MPAndroidChart — renders line charts for exercise weight progression
    // We use this for the Progress screen to visualize training data over time
    implementation(libs.mpandroidchart)

    // Lifecycle — provides lifecycle-aware components and coroutine scopes
    implementation(libs.androidx.lifecycle.runtime.ktx)
}

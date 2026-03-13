// FitLog - Fitness Diary Application
// Settings file configures the project structure and plugin repositories.
// We use the standard Gradle plugin management approach with Google's Maven
// and Maven Central as our primary artifact sources.

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // FAIL_ON_PROJECT_REPOS ensures all dependencies come from these centralized
    // repositories, preventing accidental use of project-level repos.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // MPAndroidChart is hosted on JitPack
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "FitLog"
include(":app")

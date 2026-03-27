package com.fitlog.app

import android.app.Application
import com.fitlog.app.data.local.FitLogDatabase
import com.fitlog.app.data.repository.ExerciseRepositoryImpl
import com.fitlog.app.data.repository.WorkoutRepositoryImpl

/**
 * FitLogApp — Custom Application class for the FitLog fitness diary.
 *
 * This class serves as the dependency root for our simple manual DI approach.
 * We lazily initialize the Room database and repository instances here so they
 * are available throughout the app's lifecycle as singletons.
 *
 * Design Decision: We chose manual dependency injection over Dagger/Hilt to keep
 * the project lightweight and easy to understand. For a production app with more
 * modules, migrating to Hilt would be recommended for better scalability.
 *
 * TODO: Consider migrating to Hilt for proper DI if the project grows
 * TODO: Add Timber for better logging in debug builds
 * TODO: Initialize crash reporting (e.g., Firebase Crashlytics) here
 */
class FitLogApp : Application() {

    /**
     * Lazily initialized Room database instance.
     * Using `lazy` ensures the database is only created when first accessed,
     * not at application startup — improving cold start time.
     */
    val database: FitLogDatabase by lazy {
        FitLogDatabase.getInstance(this)
    }

    /**
     * Repository for workout-related data operations.
     * The repository pattern abstracts the data source from the presenters,
     * making it easy to swap Room for a remote API in the future.
     */
    val workoutRepository: WorkoutRepositoryImpl by lazy {
        WorkoutRepositoryImpl(
            workoutDao = database.workoutDao(),
            exerciseDao = database.exerciseDao()
        )
    }

    /**
     * Repository for exercise-related data operations.
     * Handles exercise CRUD, set management, and progress tracking queries.
     */
    val exerciseRepository: ExerciseRepositoryImpl by lazy {
        ExerciseRepositoryImpl(
            exerciseDao = database.exerciseDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        // TODO: Initialize logging framework (e.g., Timber.plant(DebugTree()))
        // TODO: Set up WorkManager for scheduled workout reminders
        // TODO: Pre-populate exercise library templates on first launch
    }
}

package com.fitlog.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * FitLogDatabase — Room database definition for the FitLog application.
 *
 * This is the central database class that Room uses to generate the SQLite
 * implementation. It declares all entities (tables) and provides access to DAOs.
 *
 * We use the Singleton pattern (via companion object) to ensure only one database
 * instance exists throughout the app lifecycle. Multiple instances could cause
 * data corruption and increased memory usage.
 *
 * Database Schema:
 * - workouts: Parent table for workout sessions
 * - exercises: Child of workouts, linked via workoutId foreign key
 * - exercise_sets: Child of exercises, linked via exerciseId foreign key
 *
 * TODO: Implement database migrations when schema changes (version 2+)
 * TODO: Add a pre-populated exercise template table with common exercises
 * TODO: Add database backup/export functionality using Room's write-ahead logging
 */
@Database(
    entities = [
        WorkoutEntity::class,
        ExerciseEntity::class,
        ExerciseSetEntity::class
    ],
    version = 1,
    // Export schema to JSON for migration testing and version history
    exportSchema = true
)
abstract class FitLogDatabase : RoomDatabase() {

    /** DAO for workout-related queries */
    abstract fun workoutDao(): WorkoutDao

    /** DAO for exercise and set queries */
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        // Volatile ensures the instance is immediately visible to all threads
        @Volatile
        private var INSTANCE: FitLogDatabase? = null

        /**
         * Get the singleton database instance, creating it if necessary.
         *
         * The double-checked locking pattern (check -> synchronize -> check again)
         * prevents multiple threads from creating duplicate instances while
         * avoiding the performance cost of synchronization on every call.
         */
        fun getInstance(context: Context): FitLogDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        /**
         * Build the Room database with configuration.
         *
         * fallbackToDestructiveMigration() is used during development so schema
         * changes don't crash the app. For production, proper Migration objects
         * should be provided to preserve user data across updates.
         *
         * The onCreate callback pre-populates the database with sample exercise
         * templates so the exercise library isn't empty on first launch.
         */
        private fun buildDatabase(context: Context): FitLogDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                FitLogDatabase::class.java,
                "fitlog_database"
            )
                // TODO: Replace with proper migrations before production release
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
        }
    }

    /**
     * Callback that runs when the database is first created.
     * We use this to pre-populate some sample data so the app doesn't
     * feel empty on first launch.
     *
     * TODO: Move pre-population data to a JSON asset file for easier maintenance
     */
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-populate with a sample workout to demonstrate the app's features.
            // This runs on a background coroutine to avoid blocking the main thread.
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    prepopulateDatabase(database)
                }
            }
        }

        /**
         * Creates a sample workout with exercises and sets to onboard new users.
         * This gives them an immediate sense of how the app organizes data.
         */
        private suspend fun prepopulateDatabase(database: FitLogDatabase) {
            val workoutDao = database.workoutDao()
            val exerciseDao = database.exerciseDao()

            // Create a sample strength workout
            val workoutId = workoutDao.insertWorkout(
                WorkoutEntity(
                    date = System.currentTimeMillis(),
                    type = "strength",
                    notes = "Welcome to FitLog! This is a sample workout.",
                    durationMinutes = 60
                )
            )

            // Add a sample exercise: Bench Press
            val benchPressId = exerciseDao.insertExercise(
                ExerciseEntity(
                    workoutId = workoutId,
                    name = "Bench Press",
                    notes = "Flat barbell bench press"
                )
            )

            // Add sample sets for Bench Press
            exerciseDao.insertExerciseSet(
                ExerciseSetEntity(exerciseId = benchPressId, setNumber = 1, reps = 10, weight = 60.0)
            )
            exerciseDao.insertExerciseSet(
                ExerciseSetEntity(exerciseId = benchPressId, setNumber = 2, reps = 8, weight = 70.0)
            )
            exerciseDao.insertExerciseSet(
                ExerciseSetEntity(exerciseId = benchPressId, setNumber = 3, reps = 6, weight = 80.0)
            )

            // Add another sample exercise: Squat
            val squatId = exerciseDao.insertExercise(
                ExerciseEntity(
                    workoutId = workoutId,
                    name = "Squat",
                    notes = "Barbell back squat"
                )
            )

            exerciseDao.insertExerciseSet(
                ExerciseSetEntity(exerciseId = squatId, setNumber = 1, reps = 10, weight = 80.0)
            )
            exerciseDao.insertExerciseSet(
                ExerciseSetEntity(exerciseId = squatId, setNumber = 2, reps = 8, weight = 90.0)
            )
            exerciseDao.insertExerciseSet(
                ExerciseSetEntity(exerciseId = squatId, setNumber = 3, reps = 6, weight = 100.0)
            )
        }
    }
}

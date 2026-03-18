package com.fitlog.app.data.repository

import com.fitlog.app.domain.model.Exercise
import com.fitlog.app.domain.model.ExerciseProgress
import com.fitlog.app.domain.model.ExerciseSet
import kotlinx.coroutines.flow.Flow

/**
 * ExerciseRepository — Interface for exercise and set data operations.
 *
 * Handles individual exercise CRUD, set management, and the specialized
 * queries needed for the exercise library and progress chart features.
 *
 * Separated from WorkoutRepository because exercises have their own
 * distinct use cases (library browsing, progress tracking) that don't
 * always involve the parent workout context.
 *
 * TODO: Add bulk insert for copying exercises from templates
 * TODO: Add support for exercise tags/categories
 */
interface ExerciseRepository {

    /** Observe exercises for a specific workout */
    fun getExercisesForWorkout(workoutId: Long): Flow<List<Exercise>>

    /** Get a single exercise by ID */
    suspend fun getExerciseById(exerciseId: Long): Exercise?

    /** Create a new exercise in a workout and return its ID */
    suspend fun createExercise(exercise: Exercise): Long

    /** Delete an exercise (cascades to its sets) */
    suspend fun deleteExercise(exercise: Exercise)

    /** Add a set to an exercise and return its ID */
    suspend fun addSet(exerciseSet: ExerciseSet): Long

    /** Delete a specific set */
    suspend fun deleteSet(exerciseSet: ExerciseSet)

    /** Observe sets for a specific exercise */
    fun getSetsForExercise(exerciseId: Long): Flow<List<ExerciseSet>>

    /** Observe all distinct exercise names (the exercise library) */
    fun getAllExerciseNames(): Flow<List<String>>

    /** Search exercise names by partial match */
    fun searchExerciseNames(query: String): Flow<List<String>>

    /** Get weight progression data for a specific exercise (for charts) */
    suspend fun getWeightProgression(exerciseName: String): List<ExerciseProgress>

    /** Get how many times an exercise has been performed */
    suspend fun getExerciseCount(exerciseName: String): Int
}

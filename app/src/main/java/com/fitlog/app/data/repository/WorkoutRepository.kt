package com.fitlog.app.data.repository

import com.fitlog.app.domain.model.Exercise
import com.fitlog.app.domain.model.Workout
import com.fitlog.app.domain.model.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

/**
 * WorkoutRepository — Interface defining the contract for workout data operations.
 *
 * This interface abstracts the data source from the presenter layer, following
 * the Repository pattern. Presenters depend on this interface, not the concrete
 * implementation, which means:
 *
 * 1. We can easily swap the data source (e.g., add a remote API) without changing presenters
 * 2. We can create mock implementations for unit testing presenters
 * 3. The presenter doesn't need to know about Room, SQLite, or any storage details
 *
 * Design Decision: We return Flow for list operations (reactive updates) and use
 * suspend functions for one-shot operations (insert, update, delete). This matches
 * the common Android pattern where lists observe changes but mutations are fire-and-forget.
 *
 * TODO: Add error handling strategy (sealed class Result<T> wrapper)
 * TODO: Add offline-first sync support if a backend API is added
 */
interface WorkoutRepository {

    /** Observe all workouts, ordered by date descending (newest first) */
    fun getAllWorkouts(): Flow<List<Workout>>

    /** Observe workouts filtered by type */
    fun getWorkoutsByType(type: String): Flow<List<Workout>>

    /** Observe workouts within a date range */
    fun getWorkoutsBetweenDates(startDate: Long, endDate: Long): Flow<List<Workout>>

    /** Get a single workout by ID, with all its exercises and sets loaded */
    suspend fun getWorkoutWithExercises(workoutId: Long): WorkoutWithExercises?

    /** Create a new workout and return its generated ID */
    suspend fun createWorkout(workout: Workout): Long

    /** Update an existing workout */
    suspend fun updateWorkout(workout: Workout)

    /** Delete a workout (cascades to exercises and sets) */
    suspend fun deleteWorkout(workout: Workout)

    /** Get total workout count for statistics */
    suspend fun getWorkoutCount(): Int
}

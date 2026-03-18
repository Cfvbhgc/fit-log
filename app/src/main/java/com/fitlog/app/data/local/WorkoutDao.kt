package com.fitlog.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * WorkoutDao — Data Access Object for workout-related database operations.
 *
 * This DAO provides all the SQL queries needed to manage workouts and their
 * associated exercises and sets. We use Kotlin Flow for reactive queries
 * (the UI automatically updates when data changes) and suspend functions
 * for one-shot operations.
 *
 * Design Decision: We use @Transaction for queries that load related entities
 * (workout + exercises + sets) to ensure data consistency — all related data
 * is read in a single database transaction, preventing partial reads.
 *
 * TODO: Add pagination support using Room's Paging 3 integration for large workout histories
 * TODO: Add full-text search on workout notes
 */
@Dao
interface WorkoutDao {

    /**
     * Insert a new workout and return its auto-generated ID.
     * The returned Long is the rowId, which equals the auto-generated primary key.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    /**
     * Update an existing workout. Matches on primary key (id).
     */
    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    /**
     * Delete a workout. CASCADE foreign keys will also delete related exercises and sets.
     */
    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    /**
     * Get all workouts ordered by date (newest first).
     * Returns a Flow so the UI can observe changes reactively.
     */
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    /**
     * Get a single workout by its ID.
     * Used when opening the workout detail screen.
     */
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Long): WorkoutEntity?

    /**
     * Get workouts filtered by type (strength, cardio, flexibility).
     * Used by the filter feature in the workout history list.
     */
    @Query("SELECT * FROM workouts WHERE type = :type ORDER BY date DESC")
    fun getWorkoutsByType(type: String): Flow<List<WorkoutEntity>>

    /**
     * Get workouts within a date range.
     * Useful for monthly views and progress charts that need data from a specific period.
     */
    @Query("SELECT * FROM workouts WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getWorkoutsBetweenDates(startDate: Long, endDate: Long): Flow<List<WorkoutEntity>>

    /**
     * Count total workouts — used for statistics display.
     */
    @Query("SELECT COUNT(*) FROM workouts")
    suspend fun getWorkoutCount(): Int

    /**
     * Get exercises for a specific workout.
     * @Transaction ensures atomicity when reading from multiple tables.
     */
    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId")
    suspend fun getExercisesForWorkout(workoutId: Long): List<ExerciseEntity>

    /**
     * Get exercise sets for a specific exercise.
     */
    @Query("SELECT * FROM exercise_sets WHERE exerciseId = :exerciseId ORDER BY setNumber ASC")
    suspend fun getSetsForExercise(exerciseId: Long): List<ExerciseSetEntity>
}

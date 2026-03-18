package com.fitlog.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * ExerciseDao — Data Access Object for exercise and exercise set operations.
 *
 * This DAO handles CRUD for exercises and their sets, as well as specialized
 * queries for the exercise library and progress tracking features.
 *
 * The "exercise library" is derived from DISTINCT exercise names used across
 * all workouts — the user builds their library organically by logging exercises.
 *
 * Design Decision: We keep exercise library logic in the same DAO rather than
 * creating a separate "template" table. This approach is simpler and means the
 * library automatically grows as users create workouts. A dedicated template
 * table could be added later for pre-populated exercise definitions.
 *
 * TODO: Create a separate ExerciseTemplateEntity for curated exercise definitions
 *       with descriptions, muscle groups, and instructional images
 * TODO: Add support for supersets (linking two exercises together)
 */
@Dao
interface ExerciseDao {

    /**
     * Insert a new exercise and return its auto-generated ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    /**
     * Update an existing exercise.
     */
    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    /**
     * Delete an exercise. CASCADE will remove associated sets.
     */
    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    /**
     * Get all exercises for a given workout, ordered by ID (insertion order).
     * Returns a Flow so the workout detail screen updates reactively when
     * exercises are added or removed.
     */
    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId ORDER BY id ASC")
    fun getExercisesForWorkout(workoutId: Long): Flow<List<ExerciseEntity>>

    /**
     * Get a single exercise by ID.
     */
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: Long): ExerciseEntity?

    /**
     * Insert a new exercise set.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseSet(exerciseSet: ExerciseSetEntity): Long

    /**
     * Update an existing set (e.g., correcting reps or weight).
     */
    @Update
    suspend fun updateExerciseSet(exerciseSet: ExerciseSetEntity)

    /**
     * Delete a specific set.
     */
    @Delete
    suspend fun deleteExerciseSet(exerciseSet: ExerciseSetEntity)

    /**
     * Get all sets for an exercise, ordered by set number.
     * Returns a Flow for reactive UI updates.
     */
    @Query("SELECT * FROM exercise_sets WHERE exerciseId = :exerciseId ORDER BY setNumber ASC")
    fun getSetsForExercise(exerciseId: Long): Flow<List<ExerciseSetEntity>>

    /**
     * Get all sets for an exercise as a one-shot query (non-Flow).
     * Used in presenter logic where we need the current snapshot, not ongoing observation.
     */
    @Query("SELECT * FROM exercise_sets WHERE exerciseId = :exerciseId ORDER BY setNumber ASC")
    suspend fun getSetsForExerciseSnapshot(exerciseId: Long): List<ExerciseSetEntity>

    /**
     * Get all distinct exercise names across all workouts.
     * This forms the "exercise library" — a list of exercises the user has ever performed.
     * Useful for autocomplete when adding exercises to new workouts.
     */
    @Query("SELECT DISTINCT name FROM exercises ORDER BY name ASC")
    fun getAllExerciseNames(): Flow<List<String>>

    /**
     * Search exercise names by partial match (case-insensitive).
     * Used by the search feature in the exercise library screen.
     * The LIKE operator with wildcards enables substring matching.
     */
    @Query("SELECT DISTINCT name FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchExerciseNames(query: String): Flow<List<String>>

    /**
     * Get the weight progression for a specific exercise over time.
     * Returns the maximum weight used per workout date, which shows
     * the user's strength progression for that exercise.
     *
     * This query joins exercises -> exercise_sets -> workouts to get dates,
     * then groups by workout to find the max weight per session.
     * The result is ordered chronologically for chart display.
     */
    @Query("""
        SELECT w.date AS date, MAX(es.weight) AS maxWeight
        FROM exercises e
        INNER JOIN exercise_sets es ON es.exerciseId = e.id
        INNER JOIN workouts w ON w.id = e.workoutId
        WHERE e.name = :exerciseName
        GROUP BY w.id
        ORDER BY w.date ASC
    """)
    suspend fun getWeightProgressionForExercise(exerciseName: String): List<ExerciseProgressData>

    /**
     * Count how many times an exercise has been performed across all workouts.
     * Useful for the exercise library to show frequency/popularity.
     */
    @Query("SELECT COUNT(*) FROM exercises WHERE name = :exerciseName")
    suspend fun getExerciseCount(exerciseName: String): Int
}

/**
 * Data class for the weight progression query result.
 * Room maps the query column aliases (date, maxWeight) to these fields.
 *
 * This is a "database view" class — it doesn't correspond to a table but rather
 * to the result shape of a specific query.
 */
data class ExerciseProgressData(
    val date: Long,
    val maxWeight: Double
)

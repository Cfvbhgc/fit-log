package com.fitlog.app.data.repository

import com.fitlog.app.data.local.ExerciseDao
import com.fitlog.app.domain.model.Exercise
import com.fitlog.app.domain.model.ExerciseProgress
import com.fitlog.app.domain.model.ExerciseSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * ExerciseRepositoryImpl — Concrete implementation of ExerciseRepository backed by Room.
 *
 * This repository handles exercise-specific data operations including:
 * - Exercise CRUD within workouts
 * - Set management (add, remove)
 * - Exercise library queries (distinct names across all workouts)
 * - Progress tracking (weight progression per exercise over time)
 *
 * Design Decision: We load exercise sets eagerly when fetching exercises
 * (not lazy-loaded) because the UI almost always needs them for display.
 * This simplifies the presenter logic at the cost of slightly larger queries.
 *
 * TODO: Add local caching for exercise library to avoid repeated DISTINCT queries
 * TODO: Add support for exercise templates with default set/rep schemes
 */
class ExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    /**
     * Observe exercises for a workout.
     * Each emission triggers a refresh that also loads the latest sets
     * for each exercise via a nested query.
     *
     * Note: This uses a snapshot query for sets within the Flow mapping,
     * which means set changes won't trigger an emission unless the exercise
     * itself changes. For real-time set updates, observe sets separately.
     */
    override fun getExercisesForWorkout(workoutId: Long): Flow<List<Exercise>> {
        return exerciseDao.getExercisesForWorkout(workoutId).map { entities ->
            entities.map { entity ->
                val sets = exerciseDao.getSetsForExerciseSnapshot(entity.id)
                entity.toDomainModel(sets = sets.map { it.toDomainModel() })
            }
        }
    }

    /**
     * Get a single exercise with its sets.
     */
    override suspend fun getExerciseById(exerciseId: Long): Exercise? {
        val entity = exerciseDao.getExerciseById(exerciseId) ?: return null
        val sets = exerciseDao.getSetsForExerciseSnapshot(exerciseId)
        return entity.toDomainModel(sets = sets.map { it.toDomainModel() })
    }

    /**
     * Create a new exercise under a workout.
     * Returns the generated ID for immediate use (e.g., adding sets).
     */
    override suspend fun createExercise(exercise: Exercise): Long {
        return exerciseDao.insertExercise(exercise.toEntity())
    }

    /**
     * Delete an exercise. CASCADE handles set cleanup.
     */
    override suspend fun deleteExercise(exercise: Exercise) {
        exerciseDao.deleteExercise(exercise.toEntity())
    }

    /**
     * Add a set to an exercise. Returns the generated set ID.
     */
    override suspend fun addSet(exerciseSet: ExerciseSet): Long {
        return exerciseDao.insertExerciseSet(exerciseSet.toEntity())
    }

    /**
     * Delete a specific set from an exercise.
     */
    override suspend fun deleteSet(exerciseSet: ExerciseSet) {
        exerciseDao.deleteExerciseSet(exerciseSet.toEntity())
    }

    /**
     * Observe sets for an exercise. Used by the workout detail screen
     * to show real-time updates when sets are added or removed.
     */
    override fun getSetsForExercise(exerciseId: Long): Flow<List<ExerciseSet>> {
        return exerciseDao.getSetsForExercise(exerciseId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Get all distinct exercise names used across all workouts.
     * This forms the user's personal "exercise library" — it grows
     * organically as they log new exercises.
     */
    override fun getAllExerciseNames(): Flow<List<String>> {
        return exerciseDao.getAllExerciseNames()
    }

    /**
     * Search exercise names with partial matching.
     * Used by the SearchView in the exercise library fragment.
     */
    override fun searchExerciseNames(query: String): Flow<List<String>> {
        return exerciseDao.searchExerciseNames(query)
    }

    /**
     * Get weight progression data points for charting.
     * Maps the raw database result to the domain ExerciseProgress model.
     */
    override suspend fun getWeightProgression(exerciseName: String): List<ExerciseProgress> {
        return exerciseDao.getWeightProgressionForExercise(exerciseName).map {
            ExerciseProgress(date = it.date, maxWeight = it.maxWeight)
        }
    }

    /**
     * Get how many times a specific exercise has been performed.
     * Displayed in the exercise library to show exercise popularity.
     */
    override suspend fun getExerciseCount(exerciseName: String): Int {
        return exerciseDao.getExerciseCount(exerciseName)
    }
}

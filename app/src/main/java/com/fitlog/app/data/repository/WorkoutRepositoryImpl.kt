package com.fitlog.app.data.repository

import com.fitlog.app.data.local.ExerciseDao
import com.fitlog.app.data.local.ExerciseEntity
import com.fitlog.app.data.local.ExerciseSetEntity
import com.fitlog.app.data.local.WorkoutDao
import com.fitlog.app.data.local.WorkoutEntity
import com.fitlog.app.domain.model.Exercise
import com.fitlog.app.domain.model.ExerciseSet
import com.fitlog.app.domain.model.Workout
import com.fitlog.app.domain.model.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * WorkoutRepositoryImpl — Concrete implementation of WorkoutRepository backed by Room.
 *
 * This class handles the mapping between domain models (Workout, Exercise, ExerciseSet)
 * and database entities (WorkoutEntity, ExerciseEntity, ExerciseSetEntity). It serves
 * as the single source of truth for workout data in the application.
 *
 * Mapping Strategy: We use extension functions (defined at the bottom of this file)
 * to convert between entities and domain models. This keeps the mapping logic
 * co-located with the repository and avoids polluting the domain models.
 *
 * Design Decision: The repository takes DAO references through constructor injection
 * rather than the database itself. This follows the Dependency Inversion Principle
 * and makes it easier to mock DAOs in tests.
 *
 * TODO: Add caching layer for frequently accessed workouts
 * TODO: Wrap operations in try-catch and return Result<T> for error handling
 */
class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao
) : WorkoutRepository {

    /**
     * Observe all workouts as domain models.
     * The Flow.map transforms each emission from entity list to domain model list.
     */
    override fun getAllWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAllWorkouts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Observe workouts filtered by type (strength, cardio, flexibility).
     */
    override fun getWorkoutsByType(type: String): Flow<List<Workout>> {
        return workoutDao.getWorkoutsByType(type).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Observe workouts within a specific date range.
     * Used by the progress chart to show monthly data.
     */
    override fun getWorkoutsBetweenDates(startDate: Long, endDate: Long): Flow<List<Workout>> {
        return workoutDao.getWorkoutsBetweenDates(startDate, endDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Load a complete workout with all exercises and their sets.
     * This performs multiple queries to assemble the full workout hierarchy:
     * 1. Load the workout entity
     * 2. Load all exercises for the workout
     * 3. For each exercise, load all its sets
     *
     * TODO: Consider using Room's @Relation annotation for automatic loading
     * TODO: Add @Transaction annotation support for atomic multi-table reads
     */
    override suspend fun getWorkoutWithExercises(workoutId: Long): WorkoutWithExercises? {
        val workoutEntity = workoutDao.getWorkoutById(workoutId) ?: return null

        // Load exercises and their sets in a nested fashion
        val exerciseEntities = workoutDao.getExercisesForWorkout(workoutId)
        val exercises = exerciseEntities.map { exerciseEntity ->
            val setEntities = workoutDao.getSetsForExercise(exerciseEntity.id)
            exerciseEntity.toDomainModel(
                sets = setEntities.map { it.toDomainModel() }
            )
        }

        return WorkoutWithExercises(
            workout = workoutEntity.toDomainModel(),
            exercises = exercises
        )
    }

    /**
     * Create a new workout. Returns the auto-generated ID for navigation purposes
     * (e.g., navigating to the newly created workout's detail screen).
     */
    override suspend fun createWorkout(workout: Workout): Long {
        return workoutDao.insertWorkout(workout.toEntity())
    }

    /**
     * Update an existing workout's metadata (date, type, notes, duration).
     */
    override suspend fun updateWorkout(workout: Workout) {
        workoutDao.updateWorkout(workout.toEntity())
    }

    /**
     * Delete a workout. Room's CASCADE foreign keys handle cleanup of
     * associated exercises and sets automatically.
     */
    override suspend fun deleteWorkout(workout: Workout) {
        workoutDao.deleteWorkout(workout.toEntity())
    }

    /**
     * Get the total number of workouts in the database.
     * Used for statistics display on the main screen.
     */
    override suspend fun getWorkoutCount(): Int {
        return workoutDao.getWorkoutCount()
    }
}

// =============================================================================
// Entity <-> Domain Model Mapping Extensions
// =============================================================================
// These extension functions convert between database entities and domain models.
// Keeping them here (rather than in the entity or model classes) ensures that
// neither layer has a dependency on the other.

/** Convert WorkoutEntity (database) -> Workout (domain) */
fun WorkoutEntity.toDomainModel() = Workout(
    id = id,
    date = date,
    type = type,
    notes = notes,
    durationMinutes = durationMinutes
)

/** Convert Workout (domain) -> WorkoutEntity (database) */
fun Workout.toEntity() = WorkoutEntity(
    id = id,
    date = date,
    type = type,
    notes = notes,
    durationMinutes = durationMinutes
)

/** Convert ExerciseEntity (database) -> Exercise (domain) with pre-loaded sets */
fun ExerciseEntity.toDomainModel(sets: List<ExerciseSet> = emptyList()) = Exercise(
    id = id,
    workoutId = workoutId,
    name = name,
    notes = notes,
    sets = sets
)

/** Convert Exercise (domain) -> ExerciseEntity (database) */
fun Exercise.toEntity() = ExerciseEntity(
    id = id,
    workoutId = workoutId,
    name = name,
    notes = notes
)

/** Convert ExerciseSetEntity (database) -> ExerciseSet (domain) */
fun ExerciseSetEntity.toDomainModel() = ExerciseSet(
    id = id,
    exerciseId = exerciseId,
    setNumber = setNumber,
    reps = reps,
    weight = weight
)

/** Convert ExerciseSet (domain) -> ExerciseSetEntity (database) */
fun ExerciseSet.toEntity() = ExerciseSetEntity(
    id = id,
    exerciseId = exerciseId,
    setNumber = setNumber,
    reps = reps,
    weight = weight
)

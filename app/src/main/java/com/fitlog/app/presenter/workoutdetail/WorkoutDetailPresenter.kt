package com.fitlog.app.presenter.workoutdetail

import com.fitlog.app.data.repository.ExerciseRepository
import com.fitlog.app.data.repository.WorkoutRepository
import com.fitlog.app.domain.model.Exercise
import com.fitlog.app.domain.model.ExerciseSet
import com.fitlog.app.domain.model.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * WorkoutDetailPresenter — Manages the Workout Detail screen logic.
 *
 * This presenter coordinates between two repositories (Workout + Exercise)
 * to load the full workout hierarchy and handle mutations (add/delete exercises
 * and sets). It observes exercise changes via Flow so the UI updates
 * automatically when exercises or sets are modified.
 *
 * Loading Strategy:
 * 1. Initial load: Fetch the full WorkoutWithExercises from WorkoutRepository
 * 2. Ongoing observation: Collect the exercise Flow from ExerciseRepository
 *    for reactive updates to the exercise list
 *
 * Design Decision: We keep a local reference to the current workoutId and
 * workout rather than passing them through every method. This simplifies
 * the API but means the presenter is stateful — it must be loadWorkoutDetail()
 * before any other operations.
 *
 * TODO: Add optimistic UI updates (show changes before server confirmation)
 * TODO: Add undo support for delete operations
 * TODO: Add set number auto-increment when adding sets
 */
class WorkoutDetailPresenter(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : WorkoutDetailContract.Presenter {

    private var view: WorkoutDetailContract.View? = null
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Current workout ID — set by loadWorkoutDetail */
    private var workoutId: Long = -1

    /** Current workout reference — needed for delete operations */
    private var currentWorkout: Workout? = null

    override fun attachView(view: WorkoutDetailContract.View) {
        this.view = view
    }

    override fun detachView() {
        presenterScope.cancel()
        this.view = null
    }

    /**
     * Load the full workout detail including all exercises and sets.
     *
     * First does a one-shot load of the complete workout (for the header),
     * then sets up a Flow collection for reactive exercise list updates.
     * This way, adding/deleting exercises automatically refreshes the list.
     */
    override fun loadWorkoutDetail(workoutId: Long) {
        this.workoutId = workoutId
        view?.showLoading()

        presenterScope.launch {
            try {
                // One-shot load of the full workout with exercises
                val workoutWithExercises = workoutRepository.getWorkoutWithExercises(workoutId)

                if (workoutWithExercises == null) {
                    view?.hideLoading()
                    view?.showError("Workout not found.")
                    view?.closeScreen()
                    return@launch
                }

                currentWorkout = workoutWithExercises.workout
                view?.hideLoading()
                view?.showWorkoutDetail(workoutWithExercises)

                // Start observing exercise changes for reactive updates
                observeExercises()

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("Failed to load workout: ${e.message}")
            }
        }
    }

    /**
     * Observe exercise list changes via Flow.
     * This keeps the UI in sync when exercises/sets are added or removed.
     */
    private fun observeExercises() {
        presenterScope.launch {
            exerciseRepository.getExercisesForWorkout(workoutId)
                .catch { e ->
                    view?.showError("Failed to observe exercises: ${e.message}")
                }
                .collect { exercises ->
                    view?.showExercises(exercises)
                }
        }
    }

    /**
     * Add a new exercise to the current workout.
     * The exercise name is validated to prevent empty entries.
     * After insertion, the Flow observer automatically updates the list.
     */
    override fun addExercise(name: String, notes: String) {
        if (name.isBlank()) {
            view?.showError("Exercise name cannot be empty.")
            return
        }

        presenterScope.launch {
            try {
                val exercise = Exercise(
                    workoutId = workoutId,
                    name = name.trim(),
                    notes = notes.trim()
                )
                exerciseRepository.createExercise(exercise)
                // No manual UI update needed — Flow observer handles it
            } catch (e: Exception) {
                view?.showError("Failed to add exercise: ${e.message}")
            }
        }
    }

    /**
     * Delete an exercise from the workout.
     * CASCADE foreign key ensures associated sets are also deleted.
     */
    override fun deleteExercise(exercise: Exercise) {
        presenterScope.launch {
            try {
                exerciseRepository.deleteExercise(exercise)
                // Flow observer handles UI update
            } catch (e: Exception) {
                view?.showError("Failed to delete exercise: ${e.message}")
            }
        }
    }

    /**
     * Add a new set to an exercise.
     *
     * The set number is auto-calculated based on existing sets for the exercise.
     * We query the current sets, find the max set number, and increment by 1.
     * This ensures set numbers are always sequential even after deletions.
     */
    override fun addSet(exerciseId: Long, reps: Int, weight: Double) {
        if (reps <= 0) {
            view?.showError("Reps must be greater than zero.")
            return
        }

        if (weight < 0) {
            view?.showError("Weight cannot be negative.")
            return
        }

        presenterScope.launch {
            try {
                // Determine the next set number by checking existing sets
                val exercise = exerciseRepository.getExerciseById(exerciseId)
                val nextSetNumber = (exercise?.sets?.maxOfOrNull { it.setNumber } ?: 0) + 1

                val newSet = ExerciseSet(
                    exerciseId = exerciseId,
                    setNumber = nextSetNumber,
                    reps = reps,
                    weight = weight
                )

                exerciseRepository.addSet(newSet)
                // Flow observer handles UI update
            } catch (e: Exception) {
                view?.showError("Failed to add set: ${e.message}")
            }
        }
    }

    /**
     * Delete a specific set from an exercise.
     */
    override fun deleteSet(exerciseSet: ExerciseSet) {
        presenterScope.launch {
            try {
                exerciseRepository.deleteSet(exerciseSet)
                // Flow observer handles UI update
            } catch (e: Exception) {
                view?.showError("Failed to delete set: ${e.message}")
            }
        }
    }

    /**
     * Delete the entire workout.
     * After successful deletion, instructs the View to close the screen
     * since there's nothing left to display.
     */
    override fun deleteWorkout() {
        val workout = currentWorkout ?: return

        presenterScope.launch {
            try {
                workoutRepository.deleteWorkout(workout)
                view?.closeScreen()
            } catch (e: Exception) {
                view?.showError("Failed to delete workout: ${e.message}")
            }
        }
    }
}

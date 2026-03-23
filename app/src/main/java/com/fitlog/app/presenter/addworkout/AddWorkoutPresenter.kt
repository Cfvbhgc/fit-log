package com.fitlog.app.presenter.addworkout

import com.fitlog.app.data.repository.WorkoutRepository
import com.fitlog.app.domain.model.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * AddWorkoutPresenter — Manages business logic for creating new workouts.
 *
 * Validates user input, creates the workout via the repository, and tells
 * the View to navigate to the workout detail screen on success.
 *
 * Validation Rules:
 * - Date must be selected (non-zero)
 * - Type must be one of the valid workout types
 * - Duration must be non-negative
 *
 * Design Decision: Validation happens in the Presenter, not the View.
 * The View is "dumb" — it collects raw form data and forwards it here.
 * The Presenter decides whether the data is valid and what to do with it.
 *
 * TODO: Add support for editing existing workouts (receive workoutId in constructor)
 * TODO: Add form state restoration for process death scenarios
 */
class AddWorkoutPresenter(
    private val workoutRepository: WorkoutRepository
) : AddWorkoutContract.Presenter {

    private var view: AddWorkoutContract.View? = null
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun attachView(view: AddWorkoutContract.View) {
        this.view = view
    }

    override fun detachView() {
        presenterScope.cancel()
        this.view = null
    }

    /**
     * Validate input and save a new workout.
     *
     * On success, navigates to the workout detail screen so the user can
     * immediately start adding exercises. This flow (create workout -> add exercises)
     * is the most natural user journey.
     *
     * On validation failure, shows an error message without saving.
     * On database error, shows an error and keeps the form open so the user
     * can retry without losing their input.
     */
    override fun saveWorkout(date: Long, type: String, notes: String, durationMinutes: Int) {
        // Input validation — check each field and provide specific feedback
        if (date <= 0L) {
            view?.showValidationError("Please select a date for the workout.")
            return
        }

        if (type !in Workout.ALL_TYPES) {
            view?.showValidationError("Please select a valid workout type.")
            return
        }

        if (durationMinutes < 0) {
            view?.showValidationError("Duration cannot be negative.")
            return
        }

        // All validations passed — save to database
        view?.showLoading()

        presenterScope.launch {
            try {
                val workout = Workout(
                    date = date,
                    type = type,
                    notes = notes.trim(),
                    durationMinutes = durationMinutes
                )

                // createWorkout returns the auto-generated ID
                val workoutId = workoutRepository.createWorkout(workout)

                view?.hideLoading()
                // Navigate to detail screen so user can add exercises
                view?.navigateToWorkoutDetail(workoutId)

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("Failed to save workout: ${e.message}")
            }
        }
    }
}

package com.fitlog.app.presenter.addworkout

import com.fitlog.app.presenter.BasePresenter
import com.fitlog.app.presenter.BaseView

/**
 * AddWorkoutContract — MVP contract for the Add Workout screen.
 *
 * This screen allows users to create a new workout session by selecting:
 * - Date (via date picker)
 * - Workout type (strength, cardio, or flexibility)
 * - Optional notes
 * - Duration in minutes
 *
 * After saving, the user is navigated to the workout detail screen where
 * they can add exercises and sets to the newly created workout.
 *
 * TODO: Add support for editing existing workouts (reuse this screen)
 * TODO: Add workout templates (pre-filled exercise plans)
 */
interface AddWorkoutContract {

    interface View : BaseView {
        /** Navigate to workout detail after successful creation */
        fun navigateToWorkoutDetail(workoutId: Long)

        /** Close this screen (finish the activity) */
        fun closeScreen()

        /** Show validation error on the form */
        fun showValidationError(message: String)
    }

    interface Presenter : BasePresenter<View> {
        /**
         * Save a new workout with the provided form data.
         *
         * @param date Workout date as epoch milliseconds
         * @param type Workout type string (strength, cardio, flexibility)
         * @param notes Optional user notes
         * @param durationMinutes Duration in minutes (0 if not specified)
         */
        fun saveWorkout(date: Long, type: String, notes: String, durationMinutes: Int)
    }
}

package com.fitlog.app.presenter.workoutdetail

import com.fitlog.app.domain.model.Exercise
import com.fitlog.app.domain.model.ExerciseSet
import com.fitlog.app.domain.model.WorkoutWithExercises
import com.fitlog.app.presenter.BasePresenter
import com.fitlog.app.presenter.BaseView

/**
 * WorkoutDetailContract — MVP contract for the Workout Detail screen.
 *
 * This is one of the most complex screens in the app. It displays a single
 * workout's metadata (date, type, notes) and a nested list of exercises,
 * each with their own sets. Users can add/remove exercises and sets.
 *
 * The View uses a RecyclerView with a nested adapter pattern:
 * - Outer adapter: ExerciseAdapter (one item per exercise)
 * - Inner adapter: ExerciseSetAdapter (one item per set within each exercise)
 *
 * TODO: Add exercise reordering via drag-and-drop
 * TODO: Add set editing (tap to modify reps/weight)
 * TODO: Add workout sharing (export as text/image)
 */
interface WorkoutDetailContract {

    interface View : BaseView {
        /** Display the full workout with exercises and sets */
        fun showWorkoutDetail(workoutWithExercises: WorkoutWithExercises)

        /** Update just the exercise list (after add/delete without full reload) */
        fun showExercises(exercises: List<Exercise>)

        /** Show the dialog to add a new exercise */
        fun showAddExerciseDialog()

        /** Show the dialog to add a new set to an exercise */
        fun showAddSetDialog(exerciseId: Long)

        /** Close the screen (e.g., after deleting the workout) */
        fun closeScreen()
    }

    interface Presenter : BasePresenter<View> {
        /** Load the workout and all its exercises/sets */
        fun loadWorkoutDetail(workoutId: Long)

        /** Add a new exercise to this workout */
        fun addExercise(name: String, notes: String)

        /** Delete an exercise from this workout */
        fun deleteExercise(exercise: Exercise)

        /** Add a new set to an exercise */
        fun addSet(exerciseId: Long, reps: Int, weight: Double)

        /** Delete a specific set */
        fun deleteSet(exerciseSet: ExerciseSet)

        /** Delete the entire workout */
        fun deleteWorkout()
    }
}

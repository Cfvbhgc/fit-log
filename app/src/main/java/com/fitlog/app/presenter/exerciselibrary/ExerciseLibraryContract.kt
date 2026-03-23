package com.fitlog.app.presenter.exerciselibrary

import com.fitlog.app.presenter.BasePresenter
import com.fitlog.app.presenter.BaseView

/**
 * ExerciseLibraryContract — MVP contract for the Exercise Library screen.
 *
 * The exercise library shows all distinct exercises the user has ever performed,
 * along with usage counts. Users can search by name to find specific exercises.
 * Tapping an exercise could navigate to its progress chart in the future.
 *
 * The library is "organic" — it grows automatically as users log workouts.
 * There's no separate step to "add an exercise to the library"; it happens
 * implicitly when they add an exercise to a workout.
 *
 * TODO: Add ability to manually add exercises to the library with descriptions
 * TODO: Add muscle group categorization and filtering
 * TODO: Add exercise details screen with history and best lifts
 */
interface ExerciseLibraryContract {

    /**
     * Data class bundling an exercise name with its usage count.
     * Used for display in the library list — shows how often each exercise
     * has been performed across all workouts.
     */
    data class ExerciseLibraryItem(
        val name: String,
        val timesPerformed: Int
    )

    interface View : BaseView {
        /** Display the list of exercises with their usage counts */
        fun showExercises(exercises: List<ExerciseLibraryItem>)

        /** Show empty state when no exercises have been logged yet */
        fun showEmptyLibrary()

        /** Hide empty state */
        fun hideEmptyLibrary()
    }

    interface Presenter : BasePresenter<View> {
        /** Load all exercises in the library */
        fun loadExercises()

        /** Search exercises by name (partial match) */
        fun searchExercises(query: String)
    }
}

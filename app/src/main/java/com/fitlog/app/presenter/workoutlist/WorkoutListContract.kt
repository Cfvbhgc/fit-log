package com.fitlog.app.presenter.workoutlist

import com.fitlog.app.domain.model.Workout
import com.fitlog.app.presenter.BasePresenter
import com.fitlog.app.presenter.BaseView

/**
 * WorkoutListContract — MVP contract for the Workout List screen.
 *
 * Defines the communication protocol between the WorkoutListFragment (View)
 * and WorkoutListPresenter (Presenter). This contract ensures both sides
 * agree on what actions are available and what data will be displayed.
 *
 * The View interface lists all UI operations the Presenter can trigger.
 * The Presenter interface lists all user actions the View can forward.
 *
 * Design Decision: We nest View and Presenter interfaces inside a contract
 * object/interface rather than declaring them as separate files. This is
 * a common Android MVP convention that keeps related contracts together
 * and makes it easy to see the full communication surface at a glance.
 */
interface WorkoutListContract {

    /**
     * View interface — implemented by WorkoutListFragment.
     * These are all the ways the Presenter can update the UI.
     */
    interface View : BaseView {
        /** Display the list of workouts in the RecyclerView */
        fun showWorkouts(workouts: List<Workout>)

        /** Show empty state when no workouts exist yet */
        fun showEmptyState()

        /** Hide empty state when workouts are available */
        fun hideEmptyState()

        /** Navigate to the workout detail screen */
        fun navigateToWorkoutDetail(workoutId: Long)

        /** Navigate to the add workout screen */
        fun navigateToAddWorkout()
    }

    /**
     * Presenter interface — implemented by WorkoutListPresenter.
     * These are all the user actions the View forwards to the Presenter.
     */
    interface Presenter : BasePresenter<View> {
        /** Load all workouts (called on screen initialization) */
        fun loadWorkouts()

        /** Apply a type filter ("strength", "cardio", "flexibility", or null for all) */
        fun filterByType(type: String?)

        /** User tapped a workout item in the list */
        fun onWorkoutClicked(workout: Workout)

        /** User tapped the FAB to add a new workout */
        fun onAddWorkoutClicked()

        /** User long-pressed a workout to delete it */
        fun onDeleteWorkout(workout: Workout)
    }
}

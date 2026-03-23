package com.fitlog.app.presenter.progress

import com.fitlog.app.domain.model.ExerciseProgress
import com.fitlog.app.presenter.BasePresenter
import com.fitlog.app.presenter.BaseView

/**
 * ProgressContract — MVP contract for the Progress (Charts) screen.
 *
 * This screen shows a line chart of weight progression for a selected exercise.
 * Users pick an exercise from a dropdown (populated with all exercises they've
 * ever performed), and the chart updates to show their max weight per workout
 * session over time.
 *
 * The chart uses MPAndroidChart's LineChart to render the data points,
 * with dates on the X-axis and weight (kg/lbs) on the Y-axis.
 *
 * TODO: Add chart type selector (line, bar, scatter)
 * TODO: Add time range filter (last month, 3 months, 6 months, all time)
 * TODO: Add volume progression chart (total weight x reps per session)
 */
interface ProgressContract {

    interface View : BaseView {
        /** Populate the exercise selector dropdown with available exercise names */
        fun showExerciseNames(names: List<String>)

        /** Display the progress chart with the provided data points */
        fun showProgressChart(data: List<ExerciseProgress>)

        /** Show empty state when no progress data exists for the selected exercise */
        fun showNoProgressData()

        /** Show message when no exercises exist at all */
        fun showNoExercisesMessage()
    }

    interface Presenter : BasePresenter<View> {
        /** Load the list of available exercise names for the dropdown */
        fun loadExerciseNames()

        /** Load and display progress data for the selected exercise */
        fun loadProgressForExercise(exerciseName: String)
    }
}

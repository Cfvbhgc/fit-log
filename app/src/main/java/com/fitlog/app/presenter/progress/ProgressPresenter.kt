package com.fitlog.app.presenter.progress

import com.fitlog.app.data.repository.ExerciseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ProgressPresenter — Manages the Progress (Charts) screen logic.
 *
 * Loads the list of all exercise names for the dropdown selector and fetches
 * weight progression data for the selected exercise to render as a line chart.
 *
 * Data Flow:
 * 1. On attach, load all exercise names for the spinner
 * 2. When user selects an exercise, fetch its weight progression
 * 3. Push the data points to the View for chart rendering
 *
 * Design Decision: We use `first()` on the exercise names Flow to get a one-shot
 * snapshot rather than continuously observing. The progress screen doesn't need
 * real-time updates to the exercise list — the user can refresh by navigating away
 * and back. This keeps the implementation simpler.
 *
 * TODO: Add caching for progress data to avoid re-querying on configuration changes
 * TODO: Add data smoothing options (moving average, trend line)
 */
class ProgressPresenter(
    private val exerciseRepository: ExerciseRepository
) : ProgressContract.Presenter {

    private var view: ProgressContract.View? = null
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun attachView(view: ProgressContract.View) {
        this.view = view
    }

    override fun detachView() {
        presenterScope.cancel()
        this.view = null
    }

    /**
     * Load all distinct exercise names for the dropdown selector.
     * If no exercises exist yet, shows a message encouraging the user
     * to log some workouts first.
     */
    override fun loadExerciseNames() {
        view?.showLoading()

        presenterScope.launch {
            try {
                // Get a snapshot of all exercise names
                val names = exerciseRepository.getAllExerciseNames()
                    .catch { e ->
                        view?.hideLoading()
                        view?.showError("Failed to load exercises: ${e.message}")
                    }
                    .first()

                view?.hideLoading()

                if (names.isEmpty()) {
                    // No exercises logged yet — guide the user
                    view?.showNoExercisesMessage()
                } else {
                    view?.showExerciseNames(names)
                    // Auto-select the first exercise and load its progress
                    loadProgressForExercise(names.first())
                }

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("Failed to load exercise names: ${e.message}")
            }
        }
    }

    /**
     * Load weight progression data for a specific exercise.
     * The data is a list of (date, maxWeight) pairs ordered chronologically.
     *
     * If the exercise has been performed in only one workout, we still show
     * the chart with a single data point — it's still meaningful feedback.
     */
    override fun loadProgressForExercise(exerciseName: String) {
        if (exerciseName.isBlank()) {
            view?.showNoProgressData()
            return
        }

        view?.showLoading()

        presenterScope.launch {
            try {
                val progressData = exerciseRepository.getWeightProgression(exerciseName)

                view?.hideLoading()

                if (progressData.isEmpty()) {
                    // Exercise exists in the library but has no sets with weight data
                    view?.showNoProgressData()
                } else {
                    view?.showProgressChart(progressData)
                }

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("Failed to load progress data: ${e.message}")
            }
        }
    }
}

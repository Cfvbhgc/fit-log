package com.fitlog.app.presenter.workoutlist

import com.fitlog.app.data.repository.WorkoutRepository
import com.fitlog.app.domain.model.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * WorkoutListPresenter — Manages the business logic for the Workout List screen.
 *
 * This presenter observes workout data from the repository and pushes updates
 * to the View. It handles filtering, navigation delegation, and delete operations.
 *
 * Coroutine Strategy: We create a dedicated CoroutineScope with a SupervisorJob
 * that is cancelled in detachView(). SupervisorJob ensures that if one coroutine
 * fails, it doesn't cancel all others (e.g., a failed delete shouldn't stop
 * the workout list observation).
 *
 * Design Decision: The presenter holds a nullable reference to the View.
 * Every View method call is guarded with a null check (via the `view` property)
 * to prevent crashes if the View is destroyed while a coroutine is completing.
 *
 * TODO: Add unit tests with a mock WorkoutRepository
 * TODO: Add undo support for delete operations (Snackbar with undo action)
 */
class WorkoutListPresenter(
    private val workoutRepository: WorkoutRepository
) : WorkoutListContract.Presenter {

    /** Nullable view reference — null when detached to prevent memory leaks */
    private var view: WorkoutListContract.View? = null

    /**
     * Coroutine scope for this presenter.
     * Uses SupervisorJob so individual coroutine failures don't cancel siblings.
     * Uses Dispatchers.Main so we can safely call View methods from coroutines.
     */
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Active Flow collection job — cancelled and relaunched when filter changes */
    private var workoutCollectionJob: Job? = null

    /** Current type filter — null means show all types */
    private var currentFilter: String? = null

    override fun attachView(view: WorkoutListContract.View) {
        this.view = view
    }

    override fun detachView() {
        // Cancel all running coroutines to prevent View calls after destruction
        presenterScope.cancel()
        this.view = null
    }

    /**
     * Load workouts from the repository.
     * Starts a Flow collection that automatically updates the View whenever
     * the underlying data changes (e.g., after adding or deleting a workout).
     */
    override fun loadWorkouts() {
        view?.showLoading()
        collectWorkouts(workoutRepository.getAllWorkouts())
    }

    /**
     * Apply a type filter to the workout list.
     * Cancels any existing collection and starts a new one with the filter applied.
     *
     * @param type The workout type to filter by, or null to show all workouts
     */
    override fun filterByType(type: String?) {
        currentFilter = type
        view?.showLoading()

        val flow = if (type != null) {
            workoutRepository.getWorkoutsByType(type)
        } else {
            workoutRepository.getAllWorkouts()
        }

        collectWorkouts(flow)
    }

    /**
     * Handle workout item click — delegate navigation to the View.
     * The View handles the actual Intent/Fragment transaction because
     * navigation is a UI concern, not a business logic concern.
     */
    override fun onWorkoutClicked(workout: Workout) {
        view?.navigateToWorkoutDetail(workout.id)
    }

    /**
     * Handle FAB click — delegate add-workout navigation to the View.
     */
    override fun onAddWorkoutClicked() {
        view?.navigateToAddWorkout()
    }

    /**
     * Delete a workout from the repository.
     * On success, the active Flow collection will automatically emit
     * an updated list without the deleted workout.
     *
     * TODO: Implement soft delete with undo capability
     */
    override fun onDeleteWorkout(workout: Workout) {
        presenterScope.launch {
            try {
                workoutRepository.deleteWorkout(workout)
                // No need to manually refresh — the Flow will emit the updated list
            } catch (e: Exception) {
                view?.showError("Failed to delete workout: ${e.message}")
            }
        }
    }

    /**
     * Internal helper to start collecting a workout Flow.
     * Cancels any previous collection job to avoid duplicate observers.
     *
     * The catch operator handles any errors during collection (e.g., database
     * corruption) and forwards them to the View as error messages.
     */
    private fun collectWorkouts(flow: Flow<List<Workout>>) {
        // Cancel previous collection before starting a new one
        workoutCollectionJob?.cancel()

        workoutCollectionJob = presenterScope.launch {
            flow.catch { e ->
                view?.hideLoading()
                view?.showError("Failed to load workouts: ${e.message}")
            }.collect { workouts ->
                view?.hideLoading()
                if (workouts.isEmpty()) {
                    view?.showEmptyState()
                } else {
                    view?.hideEmptyState()
                    view?.showWorkouts(workouts)
                }
            }
        }
    }
}

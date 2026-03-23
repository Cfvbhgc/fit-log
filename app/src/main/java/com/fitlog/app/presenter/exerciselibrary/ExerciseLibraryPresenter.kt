package com.fitlog.app.presenter.exerciselibrary

import com.fitlog.app.data.repository.ExerciseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ExerciseLibraryPresenter — Manages the Exercise Library screen logic.
 *
 * Loads all distinct exercise names from the database, enriches each with
 * a usage count (how many times performed), and supports search filtering.
 *
 * Data Flow:
 * 1. Collect exercise names from the repository (reactive via Flow)
 * 2. For each name, query the usage count (one-shot per name)
 * 3. Bundle into ExerciseLibraryItem objects and push to View
 *
 * Design Decision: We fetch usage counts individually for each exercise name
 * rather than a single aggregation query. This is simpler to implement and
 * the exercise library is typically small enough (< 100 items) that the
 * N+1 query pattern is acceptable here.
 *
 * TODO: Optimize with a single aggregation query if performance becomes an issue
 * TODO: Add sorting options (alphabetical, most used, recently added)
 * TODO: Add exercise deletion from the library
 */
class ExerciseLibraryPresenter(
    private val exerciseRepository: ExerciseRepository
) : ExerciseLibraryContract.Presenter {

    private var view: ExerciseLibraryContract.View? = null
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Active collection job — cancelled and relaunched when search query changes */
    private var collectionJob: Job? = null

    override fun attachView(view: ExerciseLibraryContract.View) {
        this.view = view
    }

    override fun detachView() {
        presenterScope.cancel()
        this.view = null
    }

    /**
     * Load all exercises in the library.
     * Starts a Flow collection that automatically updates when new exercises
     * are added to workouts (since the library is derived from workout data).
     */
    override fun loadExercises() {
        view?.showLoading()
        collectExerciseNames(exerciseRepository.getAllExerciseNames())
    }

    /**
     * Search exercises by name.
     * If the query is empty/blank, shows all exercises.
     * Otherwise, filters by partial name match.
     */
    override fun searchExercises(query: String) {
        view?.showLoading()

        val flow = if (query.isBlank()) {
            exerciseRepository.getAllExerciseNames()
        } else {
            exerciseRepository.searchExerciseNames(query.trim())
        }

        collectExerciseNames(flow)
    }

    /**
     * Internal helper to collect exercise names from a Flow and enrich
     * each with its usage count before sending to the View.
     *
     * Cancels any previous collection to avoid stale results from a
     * previous search query appearing after a new search.
     */
    private fun collectExerciseNames(flow: Flow<List<String>>) {
        collectionJob?.cancel()

        collectionJob = presenterScope.launch {
            flow.catch { e ->
                view?.hideLoading()
                view?.showError("Failed to load exercises: ${e.message}")
            }.collect { names ->
                if (names.isEmpty()) {
                    view?.hideLoading()
                    view?.showEmptyLibrary()
                } else {
                    // Enrich each name with its usage count
                    val libraryItems = names.map { name ->
                        val count = try {
                            exerciseRepository.getExerciseCount(name)
                        } catch (e: Exception) {
                            // If count query fails, show 0 rather than failing entirely
                            0
                        }
                        ExerciseLibraryContract.ExerciseLibraryItem(
                            name = name,
                            timesPerformed = count
                        )
                    }

                    view?.hideLoading()
                    view?.hideEmptyLibrary()
                    view?.showExercises(libraryItems)
                }
            }
        }
    }
}

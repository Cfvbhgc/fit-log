package com.fitlog.app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitlog.app.R
import com.fitlog.app.databinding.FragmentWorkoutListBinding
import com.fitlog.app.domain.model.Workout
import com.fitlog.app.presenter.workoutlist.WorkoutListContract
import com.fitlog.app.presenter.workoutlist.WorkoutListPresenter
import com.fitlog.app.ui.activity.AddWorkoutActivity
import com.fitlog.app.ui.activity.WorkoutDetailActivity
import com.fitlog.app.ui.adapter.WorkoutAdapter
import com.fitlog.app.util.fitLogApp
import com.fitlog.app.util.hide
import com.fitlog.app.util.show
import com.fitlog.app.util.showToast

/**
 * WorkoutListFragment — Displays the main list of workout sessions.
 *
 * This is the default/home fragment shown when the app launches. It presents
 * a RecyclerView of workout cards sorted by date (newest first) with:
 * - Type filter spinner at the top
 * - FAB for creating new workouts
 * - Empty state when no workouts match the filter
 * - Long-press to delete workouts
 *
 * Implements WorkoutListContract.View, following the MVP pattern where all
 * display logic is dictated by the Presenter.
 *
 * Design Decision: We use onViewCreated for Presenter attachment (not onResume)
 * because the Presenter's Flow collection handles data freshness. When the
 * fragment becomes visible again (e.g., returning from AddWorkoutActivity),
 * the Flow automatically emits the latest data.
 *
 * TODO: Add pull-to-refresh with SwipeRefreshLayout
 * TODO: Add search functionality for workout notes
 * TODO: Add multi-select for batch delete
 */
class WorkoutListFragment : Fragment(), WorkoutListContract.View {

    companion object {
        const val TAG = "WorkoutListFragment"

        /** Factory method — Android convention for Fragment creation */
        fun newInstance(): WorkoutListFragment = WorkoutListFragment()
    }

    private var _binding: FragmentWorkoutListBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: WorkoutListContract.Presenter
    private lateinit var workoutAdapter: WorkoutAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create the Presenter with the workout repository from the Application class
        presenter = WorkoutListPresenter(requireContext().fitLogApp.workoutRepository)
        presenter.attachView(this)

        setupRecyclerView()
        setupFilterSpinner()
        setupFab()

        // Initial data load — starts a Flow collection that updates the list reactively
        presenter.loadWorkouts()
    }

    override fun onDestroyView() {
        presenter.detachView()
        _binding = null
        super.onDestroyView()
    }

    /**
     * Set up the RecyclerView with the WorkoutAdapter.
     * LinearLayoutManager displays workouts as a vertical list of cards.
     */
    private fun setupRecyclerView() {
        workoutAdapter = WorkoutAdapter(
            onWorkoutClick = { workout ->
                presenter.onWorkoutClicked(workout)
            },
            onWorkoutLongClick = { workout ->
                showDeleteConfirmation(workout)
            }
        )

        binding.recyclerViewWorkouts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workoutAdapter
            // Fixed size optimization — our item heights don't change based on content
            setHasFixedSize(true)
        }
    }

    /**
     * Set up the workout type filter spinner.
     * Options: "All", "Strength", "Cardio", "Flexibility"
     *
     * When the user selects a type, the Presenter filters the workout list accordingly.
     */
    private fun setupFilterSpinner() {
        val filterOptions = listOf("All") + Workout.ALL_TYPES.map { type ->
            type.replaceFirstChar { it.uppercase() }
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filterOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = adapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = if (position == 0) null else Workout.ALL_TYPES[position - 1]
                presenter.filterByType(selectedType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No action needed — filter remains as is
            }
        }
    }

    /**
     * Set up the Floating Action Button for creating new workouts.
     */
    private fun setupFab() {
        binding.fabAddWorkout.setOnClickListener {
            presenter.onAddWorkoutClicked()
        }
    }

    /**
     * Show a confirmation dialog before deleting a workout.
     * Long-press triggers this — we want to prevent accidental deletions.
     */
    private fun showDeleteConfirmation(workout: Workout) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_workout))
            .setMessage(getString(R.string.delete_workout_confirmation))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                presenter.onDeleteWorkout(workout)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    // =========================================================================
    // WorkoutListContract.View implementation
    // =========================================================================

    override fun showWorkouts(workouts: List<Workout>) {
        workoutAdapter.submitList(workouts)
    }

    override fun showEmptyState() {
        binding.textEmptyState.show()
        binding.recyclerViewWorkouts.hide()
    }

    override fun hideEmptyState() {
        binding.textEmptyState.hide()
        binding.recyclerViewWorkouts.show()
    }

    override fun navigateToWorkoutDetail(workoutId: Long) {
        val intent = Intent(requireContext(), WorkoutDetailActivity::class.java).apply {
            putExtra(WorkoutDetailActivity.EXTRA_WORKOUT_ID, workoutId)
        }
        startActivity(intent)
    }

    override fun navigateToAddWorkout() {
        val intent = Intent(requireContext(), AddWorkoutActivity::class.java)
        startActivity(intent)
    }

    override fun showLoading() {
        binding.progressBar.show()
    }

    override fun hideLoading() {
        binding.progressBar.hide()
    }

    override fun showError(message: String) {
        requireContext().showToast(message)
    }
}

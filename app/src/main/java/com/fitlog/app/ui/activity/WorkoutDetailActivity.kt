package com.fitlog.app.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitlog.app.R
import com.fitlog.app.databinding.ActivityWorkoutDetailBinding
import com.fitlog.app.domain.model.Exercise
import com.fitlog.app.domain.model.ExerciseSet
import com.fitlog.app.domain.model.WorkoutWithExercises
import com.fitlog.app.presenter.workoutdetail.WorkoutDetailContract
import com.fitlog.app.presenter.workoutdetail.WorkoutDetailPresenter
import com.fitlog.app.ui.adapter.ExerciseAdapter
import com.fitlog.app.ui.dialog.AddExerciseDialog
import com.fitlog.app.ui.dialog.AddSetDialog
import com.fitlog.app.util.DateUtils
import com.fitlog.app.util.capitalizeFirst
import com.fitlog.app.util.fitLogApp
import com.fitlog.app.util.hide
import com.fitlog.app.util.show
import com.fitlog.app.util.showToast

/**
 * WorkoutDetailActivity — Displays a single workout with all its exercises and sets.
 *
 * This is the most feature-rich screen in the app. It shows:
 * - Workout header: date, type, duration, notes
 * - List of exercises, each with their sets
 * - FAB to add new exercises
 * - "Add Set" button on each exercise
 * - Delete actions for exercises and sets
 *
 * The exercise list uses a nested RecyclerView pattern:
 * - Outer RecyclerView: ExerciseAdapter (one card per exercise)
 * - Inner RecyclerView: ExerciseSetAdapter (one row per set within each exercise)
 *
 * This Activity implements multiple callback interfaces for the dialogs
 * (AddExerciseDialog, AddSetDialog) to receive user input.
 *
 * Design Decision: This is an Activity (not a Fragment) because it needs its own
 * toolbar, options menu, and back stack entry. The user navigates here from
 * either the workout list or the add-workout flow.
 *
 * TODO: Add workout editing (date, type, notes, duration)
 * TODO: Add workout summary statistics (total volume, total sets, etc.)
 * TODO: Add share/export functionality
 * TODO: Add timer integration for rest periods
 */
class WorkoutDetailActivity :
    AppCompatActivity(),
    WorkoutDetailContract.View,
    AddExerciseDialog.OnExerciseAddedListener,
    AddSetDialog.OnSetAddedListener {

    companion object {
        /** Intent extra key for passing the workout ID */
        const val EXTRA_WORKOUT_ID = "extra_workout_id"
    }

    private lateinit var binding: ActivityWorkoutDetailBinding
    private lateinit var presenter: WorkoutDetailContract.Presenter
    private lateinit var exerciseAdapter: ExerciseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.workout_detail)

        // Create the presenter with repository dependencies from the Application class
        presenter = WorkoutDetailPresenter(
            workoutRepository = fitLogApp.workoutRepository,
            exerciseRepository = fitLogApp.exerciseRepository
        )
        presenter.attachView(this)

        setupExerciseRecyclerView()
        setupFab()

        // Load the workout data using the ID passed via Intent
        val workoutId = intent.getLongExtra(EXTRA_WORKOUT_ID, -1L)
        if (workoutId > 0) {
            presenter.loadWorkoutDetail(workoutId)
        } else {
            showError("Invalid workout ID")
            finish()
        }
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * Inflate the options menu with a "Delete Workout" action.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_workout_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_workout -> {
                showDeleteWorkoutConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Set up the RecyclerView for exercises.
     * The ExerciseAdapter handles both exercise display and nested set display.
     */
    private fun setupExerciseRecyclerView() {
        exerciseAdapter = ExerciseAdapter(
            onAddSetClick = { exerciseId ->
                // Show dialog to add a set to this exercise
                presenter.let { showAddSetDialog(exerciseId) }
            },
            onDeleteExerciseClick = { exercise ->
                showDeleteExerciseConfirmation(exercise)
            },
            onDeleteSetClick = { exerciseSet ->
                presenter.deleteSet(exerciseSet)
            }
        )

        binding.recyclerViewExercises.apply {
            layoutManager = LinearLayoutManager(this@WorkoutDetailActivity)
            adapter = exerciseAdapter
        }
    }

    /**
     * Set up the FAB to open the "Add Exercise" dialog.
     */
    private fun setupFab() {
        binding.fabAddExercise.setOnClickListener {
            showAddExerciseDialog()
        }
    }

    /**
     * Show a confirmation dialog before deleting the entire workout.
     * Destructive actions should always have a confirmation step.
     */
    private fun showDeleteWorkoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete this workout? All exercises and sets will be permanently removed.")
            .setPositiveButton("Delete") { _, _ ->
                presenter.deleteWorkout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Show a confirmation dialog before deleting an exercise.
     */
    private fun showDeleteExerciseConfirmation(exercise: Exercise) {
        AlertDialog.Builder(this)
            .setTitle("Delete Exercise")
            .setMessage("Delete \"${exercise.name}\" and all its sets?")
            .setPositiveButton("Delete") { _, _ ->
                presenter.deleteExercise(exercise)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // =========================================================================
    // WorkoutDetailContract.View implementation
    // =========================================================================

    override fun showWorkoutDetail(workoutWithExercises: WorkoutWithExercises) {
        val workout = workoutWithExercises.workout

        // Populate the workout header section
        binding.textDetailDate.text = DateUtils.formatFullDate(workout.date)
        binding.textDetailType.text = workout.type.capitalizeFirst()
        binding.textDetailDuration.text = DateUtils.formatDuration(workout.durationMinutes)

        if (workout.notes.isNotBlank()) {
            binding.textDetailNotes.text = workout.notes
            binding.textDetailNotes.show()
        } else {
            binding.textDetailNotes.hide()
        }

        // Update the exercise list
        exerciseAdapter.submitList(workoutWithExercises.exercises)
    }

    override fun showExercises(exercises: List<Exercise>) {
        exerciseAdapter.submitList(exercises)

        // Show/hide empty state based on exercise count
        if (exercises.isEmpty()) {
            binding.textEmptyExercises.show()
        } else {
            binding.textEmptyExercises.hide()
        }
    }

    override fun showAddExerciseDialog() {
        val dialog = AddExerciseDialog.newInstance()
        dialog.setOnExerciseAddedListener(this)
        dialog.show(supportFragmentManager, AddExerciseDialog.TAG)
    }

    override fun showAddSetDialog(exerciseId: Long) {
        val dialog = AddSetDialog.newInstance(exerciseId)
        dialog.setOnSetAddedListener(this)
        dialog.show(supportFragmentManager, AddSetDialog.TAG)
    }

    override fun closeScreen() {
        showToast("Workout deleted")
        finish()
    }

    override fun showLoading() {
        binding.progressBar.show()
    }

    override fun hideLoading() {
        binding.progressBar.hide()
    }

    override fun showError(message: String) {
        showToast(message)
    }

    // =========================================================================
    // Dialog callback implementations
    // =========================================================================

    /**
     * Called when the user confirms adding a new exercise via AddExerciseDialog.
     */
    override fun onExerciseAdded(name: String, notes: String) {
        presenter.addExercise(name, notes)
    }

    /**
     * Called when the user confirms adding a new set via AddSetDialog.
     */
    override fun onSetAdded(exerciseId: Long, reps: Int, weight: Double) {
        presenter.addSet(exerciseId, reps, weight)
    }
}

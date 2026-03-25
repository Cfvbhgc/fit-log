package com.fitlog.app.ui.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.fitlog.app.R
import com.fitlog.app.databinding.ActivityAddWorkoutBinding
import com.fitlog.app.domain.model.Workout
import com.fitlog.app.presenter.addworkout.AddWorkoutContract
import com.fitlog.app.presenter.addworkout.AddWorkoutPresenter
import com.fitlog.app.util.DateUtils
import com.fitlog.app.util.fitLogApp
import com.fitlog.app.util.showToast
import java.util.Calendar

/**
 * AddWorkoutActivity — Screen for creating a new workout session.
 *
 * Implements the AddWorkoutContract.View interface, acting as the "dumb" View
 * in our MVP pattern. This Activity:
 * - Displays a form (date picker, type spinner, notes, duration)
 * - Collects user input and forwards it to the Presenter
 * - Handles navigation commands from the Presenter
 * - Shows errors/loading states as directed by the Presenter
 *
 * No business logic lives here — validation, data persistence, and decision
 * making all happen in AddWorkoutPresenter.
 *
 * Design Decision: This is a separate Activity (not a Fragment in MainActivity)
 * because the add-workout flow benefits from its own back stack entry and
 * a full-screen form experience. The user can tap Back to cancel.
 *
 * TODO: Add "Edit Workout" mode (receive existing workout ID and pre-fill form)
 * TODO: Add calendar widget instead of simple date picker
 * TODO: Add workout template selection
 */
class AddWorkoutActivity : AppCompatActivity(), AddWorkoutContract.View {

    private lateinit var binding: ActivityAddWorkoutBinding
    private lateinit var presenter: AddWorkoutContract.Presenter

    /** Selected workout date as epoch milliseconds. Zero means no date selected. */
    private var selectedDate: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar with back navigation
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_workout)

        // Create and attach the Presenter
        // Dependencies are obtained from the Application class (manual DI)
        presenter = AddWorkoutPresenter(fitLogApp.workoutRepository)
        presenter.attachView(this)

        setupWorkoutTypeSpinner()
        setupDatePicker()
        setupSaveButton()

        // Default to today's date for convenience
        setDateToToday()
    }

    override fun onDestroy() {
        // Detach the View to prevent memory leaks and stale View calls
        presenter.detachView()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        // Handle toolbar back button — same as system back
        finish()
        return true
    }

    /**
     * Set up the workout type dropdown (Spinner).
     * Populated with the three workout types: Strength, Cardio, Flexibility.
     *
     * We use AutoCompleteTextView with a TextInputLayout for Material Design
     * styling. The dropdown is exposed (not editable) so users must pick
     * from the predefined options.
     */
    private fun setupWorkoutTypeSpinner() {
        val types = Workout.ALL_TYPES.map { type ->
            // Capitalize for display: "strength" -> "Strength"
            type.replaceFirstChar { it.uppercase() }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        binding.spinnerWorkoutType.setAdapter(adapter)

        // Default to "Strength" as the most common workout type
        binding.spinnerWorkoutType.setText(types.first(), false)
    }

    /**
     * Set up the date picker button.
     * Tapping the date field opens a DatePickerDialog. The selected date
     * is stored as epoch milliseconds and displayed in the text field.
     */
    private fun setupDatePicker() {
        binding.editWorkoutDate.setOnClickListener {
            showDatePicker()
        }

        // Also allow tapping the TextInputLayout icon to open the picker
        binding.textInputDate.setEndIconOnClickListener {
            showDatePicker()
        }
    }

    /**
     * Show the Material DatePickerDialog.
     * Pre-selects the currently chosen date, or today if none selected.
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedDate > 0) {
            calendar.timeInMillis = selectedDate
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                    // Set to noon to avoid timezone edge cases with midnight
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                selectedDate = selected.timeInMillis
                binding.editWorkoutDate.setText(DateUtils.formatShortDate(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /**
     * Set the default date to today for user convenience.
     * Most users create workouts for the current day.
     */
    private fun setDateToToday() {
        selectedDate = System.currentTimeMillis()
        binding.editWorkoutDate.setText(DateUtils.formatShortDate(selectedDate))
    }

    /**
     * Set up the save button.
     * Collects all form data and forwards it to the Presenter for validation.
     */
    private fun setupSaveButton() {
        binding.buttonSaveWorkout.setOnClickListener {
            val type = binding.spinnerWorkoutType.text.toString().lowercase()
            val notes = binding.editWorkoutNotes.text.toString()
            val durationText = binding.editWorkoutDuration.text.toString()
            val duration = durationText.toIntOrNull() ?: 0

            // Delegate to presenter — it handles validation and saving
            presenter.saveWorkout(selectedDate, type, notes, duration)
        }
    }

    // =========================================================================
    // AddWorkoutContract.View implementation
    // =========================================================================

    override fun navigateToWorkoutDetail(workoutId: Long) {
        // Navigate to the newly created workout's detail screen
        val intent = Intent(this, WorkoutDetailActivity::class.java).apply {
            putExtra(WorkoutDetailActivity.EXTRA_WORKOUT_ID, workoutId)
        }
        startActivity(intent)
        finish() // Remove the add-workout screen from the back stack
    }

    override fun closeScreen() {
        finish()
    }

    override fun showValidationError(message: String) {
        showToast(message)
    }

    override fun showLoading() {
        binding.buttonSaveWorkout.isEnabled = false
        binding.buttonSaveWorkout.text = getString(R.string.saving)
    }

    override fun hideLoading() {
        binding.buttonSaveWorkout.isEnabled = true
        binding.buttonSaveWorkout.text = getString(R.string.save_workout)
    }

    override fun showError(message: String) {
        showToast(message)
    }
}

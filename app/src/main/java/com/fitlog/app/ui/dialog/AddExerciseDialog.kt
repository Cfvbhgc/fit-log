package com.fitlog.app.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.fitlog.app.databinding.DialogAddExerciseBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * AddExerciseDialog — Dialog for adding a new exercise to a workout.
 *
 * Presents a simple form with an exercise name field and optional notes.
 * The result is communicated back to the parent Activity via the
 * OnExerciseAddedListener callback interface.
 *
 * Design Decision: We use DialogFragment (not a plain AlertDialog) because
 * DialogFragment survives configuration changes (screen rotation) and
 * integrates properly with the Fragment lifecycle. A plain AlertDialog
 * would be dismissed and lost on rotation.
 *
 * TODO: Add autocomplete for exercise name using the exercise library
 * TODO: Add exercise category/muscle group selector
 * TODO: Add quick-add buttons for common exercises
 */
class AddExerciseDialog : DialogFragment() {

    /**
     * Callback interface for communicating the result back to the host.
     * The host Activity implements this interface to receive the new exercise data.
     */
    interface OnExerciseAddedListener {
        fun onExerciseAdded(name: String, notes: String)
    }

    /** Listener reference — set when the dialog is created */
    private var listener: OnExerciseAddedListener? = null

    private var _binding: DialogAddExerciseBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val TAG = "AddExerciseDialog"

        /**
         * Factory method for creating a new instance.
         * Using a factory method is the Android convention for Fragments
         * because the system may recreate them and needs a no-arg constructor.
         */
        fun newInstance(): AddExerciseDialog {
            return AddExerciseDialog()
        }
    }

    /**
     * Set the listener for exercise addition events.
     * Called by the host Activity before showing the dialog.
     */
    fun setOnExerciseAddedListener(listener: OnExerciseAddedListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddExerciseBinding.inflate(LayoutInflater.from(requireContext()))

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Exercise")
            .setView(binding.root)
            .setPositiveButton("Add") { _, _ ->
                val name = binding.editExerciseName.text.toString().trim()
                val notes = binding.editExerciseNotes.text.toString().trim()

                if (name.isNotBlank()) {
                    listener?.onExerciseAdded(name, notes)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.fitlog.app.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.fitlog.app.databinding.DialogAddSetBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * AddSetDialog — Dialog for adding a new set to an exercise.
 *
 * Presents input fields for reps and weight. The exerciseId is passed via
 * the Bundle arguments so the dialog knows which exercise to add the set to.
 *
 * Design Decision: We pass the exerciseId through the Fragment arguments Bundle
 * rather than a constructor parameter. This is required for proper Fragment
 * restoration — Android may recreate Fragments using the no-arg constructor,
 * and only Bundle arguments survive this process.
 *
 * TODO: Add "copy previous set" button to quickly duplicate the last set
 * TODO: Add weight increment/decrement buttons (+2.5kg, -2.5kg)
 * TODO: Add RPE (Rate of Perceived Exertion) slider
 */
class AddSetDialog : DialogFragment() {

    /**
     * Callback interface for communicating the new set data back to the host.
     */
    interface OnSetAddedListener {
        fun onSetAdded(exerciseId: Long, reps: Int, weight: Double)
    }

    private var listener: OnSetAddedListener? = null

    private var _binding: DialogAddSetBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val TAG = "AddSetDialog"
        private const val ARG_EXERCISE_ID = "exercise_id"

        /**
         * Factory method that accepts the exercise ID via arguments Bundle.
         * This ensures the exerciseId survives Fragment recreation.
         */
        fun newInstance(exerciseId: Long): AddSetDialog {
            return AddSetDialog().apply {
                arguments = Bundle().apply {
                    putLong(ARG_EXERCISE_ID, exerciseId)
                }
            }
        }
    }

    /**
     * Set the listener for set addition events.
     */
    fun setOnSetAddedListener(listener: OnSetAddedListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddSetBinding.inflate(LayoutInflater.from(requireContext()))

        val exerciseId = arguments?.getLong(ARG_EXERCISE_ID) ?: -1L

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Set")
            .setView(binding.root)
            .setPositiveButton("Add") { _, _ ->
                // Parse input with defaults for invalid input
                val reps = binding.editReps.text.toString().toIntOrNull() ?: 0
                val weight = binding.editWeight.text.toString().toDoubleOrNull() ?: 0.0

                if (reps > 0 && exerciseId > 0) {
                    listener?.onSetAdded(exerciseId, reps, weight)
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

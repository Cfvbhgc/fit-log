package com.fitlog.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fitlog.app.databinding.ItemExerciseSetBinding
import com.fitlog.app.domain.model.ExerciseSet
import com.fitlog.app.util.formatWeight

/**
 * ExerciseSetAdapter — RecyclerView adapter for displaying sets within an exercise.
 *
 * This adapter is used as a nested adapter inside ExerciseAdapter. Each row shows:
 * - Set number (1, 2, 3...)
 * - Reps performed
 * - Weight used
 * - Delete button
 *
 * The adapter is intentionally simple since sets are displayed in a compact format.
 * Sets are always displayed in order of their setNumber field.
 *
 * Design Decision: We use DiffUtil even for this small list (typically 3-5 items)
 * for consistency with the rest of the app and to get smooth animations when
 * sets are added or removed.
 *
 * TODO: Add tap-to-edit functionality for modifying reps/weight
 * TODO: Add visual indicator for personal records (highest weight for this exercise)
 * TODO: Add rest timer button between sets
 */
class ExerciseSetAdapter(
    /** Called when the delete button is tapped on a set */
    private val onDeleteSetClick: (ExerciseSet) -> Unit
) : RecyclerView.Adapter<ExerciseSetAdapter.SetViewHolder>() {

    /** Current list of sets */
    private var sets: List<ExerciseSet> = emptyList()

    /**
     * Update the sets list using DiffUtil.
     */
    fun submitList(newSets: List<ExerciseSet>) {
        val diffCallback = SetDiffCallback(sets, newSets)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        sets = newSets
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val binding = ItemExerciseSetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.bind(sets[position])
    }

    override fun getItemCount(): Int = sets.size

    /**
     * ViewHolder for a single set row.
     * Displays set number, reps, weight, and a delete action.
     */
    inner class SetViewHolder(
        private val binding: ItemExerciseSetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exerciseSet: ExerciseSet) {
            // Display set number with "Set" prefix for clarity
            binding.textSetNumber.text = "Set ${exerciseSet.setNumber}"

            // Display reps count
            binding.textReps.text = "${exerciseSet.reps} reps"

            // Display weight with smart formatting (no trailing .0 for whole numbers)
            binding.textWeight.text = "${exerciseSet.weight.formatWeight()} kg"

            // Delete button — removes this set from the exercise
            binding.buttonDeleteSet.setOnClickListener {
                onDeleteSetClick(exerciseSet)
            }
        }
    }

    /**
     * DiffUtil callback for exercise sets.
     * Simple comparison since sets are small, immutable data objects.
     */
    class SetDiffCallback(
        private val oldList: List<ExerciseSet>,
        private val newList: List<ExerciseSet>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        /** Same set if the ID matches */
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        /** Full equality check for content changes */
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

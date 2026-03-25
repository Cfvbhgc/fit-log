package com.fitlog.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitlog.app.databinding.ItemExerciseBinding
import com.fitlog.app.domain.model.Exercise
import com.fitlog.app.domain.model.ExerciseSet

/**
 * ExerciseAdapter — RecyclerView adapter for displaying exercises within a workout.
 *
 * This is a "nested adapter" pattern: each exercise item contains its own
 * RecyclerView for displaying sets (using ExerciseSetAdapter). This creates
 * a two-level list structure:
 *
 *   Exercise 1: "Bench Press"
 *     ├── Set 1: 10 reps x 60kg
 *     ├── Set 2: 8 reps x 70kg
 *     └── Set 3: 6 reps x 80kg
 *   Exercise 2: "Squat"
 *     ├── Set 1: 10 reps x 80kg
 *     └── Set 2: 8 reps x 90kg
 *
 * Design Decision: Nested RecyclerView is simpler than a flat list with multiple
 * view types (exercise header + set row). The performance tradeoff is acceptable
 * since a typical workout has 4-8 exercises with 3-5 sets each — a small dataset.
 *
 * TODO: Add drag-and-drop reordering for exercises
 * TODO: Add collapse/expand animation for the sets list
 * TODO: Consider using ConcatAdapter as an alternative to nested RecyclerView
 */
class ExerciseAdapter(
    /** Called when "Add Set" button is tapped on an exercise */
    private val onAddSetClick: (exerciseId: Long) -> Unit,
    /** Called when an exercise delete button is tapped */
    private val onDeleteExerciseClick: (Exercise) -> Unit,
    /** Called when a set delete button is tapped */
    private val onDeleteSetClick: (ExerciseSet) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    /** Current list of exercises displayed */
    private var exercises: List<Exercise> = emptyList()

    /**
     * Update the exercise list using DiffUtil.
     * Since exercises contain nested sets, the DiffUtil check covers both
     * exercise metadata changes and set list changes.
     */
    fun submitList(newExercises: List<Exercise>) {
        val diffCallback = ExerciseDiffCallback(exercises, newExercises)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        exercises = newExercises
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exercises[position])
    }

    override fun getItemCount(): Int = exercises.size

    /**
     * ViewHolder for exercise items.
     * Sets up the nested RecyclerView for sets and handles click events.
     */
    inner class ExerciseViewHolder(
        private val binding: ItemExerciseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /** Nested adapter for exercise sets — created once per ViewHolder */
        private val setAdapter = ExerciseSetAdapter(
            onDeleteSetClick = onDeleteSetClick
        )

        init {
            // Configure the nested RecyclerView for sets
            // Using setHasFixedSize(false) because the set count can change
            binding.recyclerViewSets.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = setAdapter
                // Disable nested scrolling so the parent RecyclerView handles scrolling
                isNestedScrollingEnabled = false
            }
        }

        fun bind(exercise: Exercise) {
            binding.textExerciseName.text = exercise.name

            // Show notes if available
            if (exercise.notes.isNotBlank()) {
                binding.textExerciseNotes.text = exercise.notes
                binding.textExerciseNotes.visibility = android.view.View.VISIBLE
            } else {
                binding.textExerciseNotes.visibility = android.view.View.GONE
            }

            // Update the nested set adapter with this exercise's sets
            setAdapter.submitList(exercise.sets)

            // "Add Set" button — opens a dialog to input reps and weight
            binding.buttonAddSet.setOnClickListener {
                onAddSetClick(exercise.id)
            }

            // Delete exercise button
            binding.buttonDeleteExercise.setOnClickListener {
                onDeleteExerciseClick(exercise)
            }
        }
    }

    /**
     * DiffUtil callback for exercises.
     * Uses both the exercise ID and the full exercise data (including sets)
     * to determine whether items need to be updated.
     */
    class ExerciseDiffCallback(
        private val oldList: List<Exercise>,
        private val newList: List<Exercise>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        /**
         * Full equality check including the sets list.
         * This ensures the ViewHolder is rebound when sets are added/removed.
         */
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

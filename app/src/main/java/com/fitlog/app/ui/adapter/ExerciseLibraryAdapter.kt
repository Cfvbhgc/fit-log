package com.fitlog.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fitlog.app.databinding.ItemExerciseLibraryBinding
import com.fitlog.app.presenter.exerciselibrary.ExerciseLibraryContract

/**
 * ExerciseLibraryAdapter — RecyclerView adapter for the exercise library screen.
 *
 * Displays a list of distinct exercise names along with how many times each
 * exercise has been performed across all workouts. This gives users a quick
 * overview of their exercise repertoire and training frequency.
 *
 * Uses DiffUtil for efficient list updates, consistent with all other adapters
 * in the app.
 *
 * TODO: Add click listener to navigate to progress chart for the selected exercise
 * TODO: Add contextual menu for exercise actions (view history, rename, merge)
 * TODO: Add visual indicators for muscle groups or exercise categories
 */
class ExerciseLibraryAdapter : RecyclerView.Adapter<ExerciseLibraryAdapter.LibraryViewHolder>() {

    /** Current list of exercise library items */
    private var exercises: List<ExerciseLibraryContract.ExerciseLibraryItem> = emptyList()

    /**
     * Update the exercise list using DiffUtil.
     */
    fun submitList(newExercises: List<ExerciseLibraryContract.ExerciseLibraryItem>) {
        val diffCallback = LibraryDiffCallback(exercises, newExercises)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        exercises = newExercises
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val binding = ItemExerciseLibraryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LibraryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        holder.bind(exercises[position])
    }

    override fun getItemCount(): Int = exercises.size

    /**
     * ViewHolder for exercise library items.
     * Shows the exercise name and a count of how many times it has been performed.
     */
    inner class LibraryViewHolder(
        private val binding: ItemExerciseLibraryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExerciseLibraryContract.ExerciseLibraryItem) {
            binding.textExerciseName.text = item.name
            binding.textTimesPerformed.text = "${item.timesPerformed} time${if (item.timesPerformed != 1) "s" else ""} performed"
        }
    }

    /**
     * DiffUtil callback for exercise library items.
     * Compares by exercise name (which is the unique identifier in the library).
     */
    class LibraryDiffCallback(
        private val oldList: List<ExerciseLibraryContract.ExerciseLibraryItem>,
        private val newList: List<ExerciseLibraryContract.ExerciseLibraryItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        /** Same exercise if the name matches (name is the unique key in the library) */
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].name == newList[newItemPosition].name
        }

        /** Full equality check including the times-performed count */
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

package com.fitlog.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fitlog.app.R
import com.fitlog.app.databinding.ItemWorkoutBinding
import com.fitlog.app.domain.model.Workout
import com.fitlog.app.util.DateUtils
import com.fitlog.app.util.capitalizeFirst

/**
 * WorkoutAdapter — RecyclerView adapter for displaying workout sessions.
 *
 * Uses DiffUtil for efficient list updates — instead of calling notifyDataSetChanged()
 * (which rebuilds the entire list), DiffUtil calculates the minimal set of changes
 * needed and applies targeted animations (insert, remove, move, change).
 *
 * Design Decision: We use the "ListAdapter" pattern manually (with DiffUtil.calculateDiff)
 * rather than extending ListAdapter<> because it gives us more control over the
 * diffing process and makes the DiffUtil usage more explicit for learning purposes.
 *
 * Click Handling: The adapter delegates click events to the parent via lambda callbacks
 * rather than holding a reference to the Activity/Fragment. This keeps the adapter
 * decoupled from the navigation layer.
 *
 * TODO: Add swipe-to-delete with ItemTouchHelper
 * TODO: Add item animations (fade in on first appearance)
 * TODO: Add multi-select mode for batch operations
 */
class WorkoutAdapter(
    /** Called when a workout item is tapped — typically navigates to detail */
    private val onWorkoutClick: (Workout) -> Unit,
    /** Called when a workout item is long-pressed — typically shows delete confirmation */
    private val onWorkoutLongClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    /** Current list of workouts displayed in the RecyclerView */
    private var workouts: List<Workout> = emptyList()

    /**
     * Update the workout list using DiffUtil for efficient diffing.
     *
     * DiffUtil compares the old and new lists on a background thread-safe manner,
     * then dispatches the minimal set of change notifications to the adapter.
     * This produces smooth animations and avoids unnecessary rebinding.
     */
    fun submitList(newWorkouts: List<Workout>) {
        val diffCallback = WorkoutDiffCallback(workouts, newWorkouts)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        workouts = newWorkouts
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workouts[position])
    }

    override fun getItemCount(): Int = workouts.size

    /**
     * ViewHolder for workout items.
     * Uses ViewBinding for type-safe view access — no findViewById needed.
     * The bind() method populates all views with workout data.
     */
    inner class WorkoutViewHolder(
        private val binding: ItemWorkoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(workout: Workout) {
            // Format and display workout data
            binding.textWorkoutDate.text = DateUtils.formatShortDate(workout.date)
            binding.textWorkoutType.text = workout.type.capitalizeFirst()
            binding.textWorkoutDuration.text = DateUtils.formatDuration(workout.durationMinutes)

            // Show notes preview if available, truncated for the list view
            binding.textWorkoutNotes.text = if (workout.notes.isNotBlank()) {
                workout.notes
            } else {
                binding.root.context.getString(R.string.no_notes)
            }

            // Set workout type icon/color indicator based on type
            val typeColorRes = when (workout.type) {
                Workout.TYPE_STRENGTH -> R.color.workout_strength
                Workout.TYPE_CARDIO -> R.color.workout_cardio
                Workout.TYPE_FLEXIBILITY -> R.color.workout_flexibility
                else -> R.color.workout_strength
            }
            binding.viewTypeIndicator.setBackgroundResource(typeColorRes)

            // Click listeners — forward events to the parent via callbacks
            binding.root.setOnClickListener {
                onWorkoutClick(workout)
            }

            binding.root.setOnLongClickListener {
                onWorkoutLongClick(workout)
                true // Return true to consume the long click event
            }
        }
    }

    /**
     * DiffUtil.Callback for calculating differences between two workout lists.
     *
     * areItemsTheSame: Checks if two items represent the same workout (by ID).
     * areContentsTheSame: Checks if the workout data has changed (needs rebinding).
     *
     * This distinction is important: if areItemsTheSame is true but areContentsTheSame
     * is false, the ViewHolder is rebound with new data but the item is NOT recreated
     * (no insert/delete animation, just a content change animation).
     */
    class WorkoutDiffCallback(
        private val oldList: List<Workout>,
        private val newList: List<Workout>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        /** Same ID means same logical item, even if contents changed */
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        /** Full equality check to determine if rebinding is needed */
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

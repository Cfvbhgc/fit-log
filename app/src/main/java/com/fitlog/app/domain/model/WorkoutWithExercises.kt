package com.fitlog.app.domain.model

/**
 * WorkoutWithExercises — Composite domain model bundling a workout with its exercises.
 *
 * This model is used on the workout detail screen where we need to display
 * the workout's metadata alongside all its exercises and their sets.
 *
 * Rather than having the View make separate calls for the workout and its
 * exercises, the repository assembles this composite object in a single
 * operation, simplifying the presenter logic.
 *
 * TODO: Add computed properties like totalVolume, totalSets, etc. for summary display
 */
data class WorkoutWithExercises(
    val workout: Workout,
    val exercises: List<Exercise>
) {
    /**
     * Total number of exercises in this workout.
     * Displayed in the workout list item cards.
     */
    val exerciseCount: Int get() = exercises.size

    /**
     * Total number of sets across all exercises.
     * Useful for workout summary statistics.
     */
    val totalSets: Int get() = exercises.sumOf { it.sets.size }

    /**
     * Total volume (weight x reps) across all exercises and sets.
     * A key metric for tracking overall workout intensity.
     */
    val totalVolume: Double get() = exercises.sumOf { exercise ->
        exercise.sets.sumOf { it.volume }
    }
}

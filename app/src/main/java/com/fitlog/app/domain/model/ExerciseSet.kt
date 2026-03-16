package com.fitlog.app.domain.model

/**
 * ExerciseSet — Domain model representing a single set within an exercise.
 *
 * A "set" in weightlifting terms is one group of consecutive repetitions.
 * For example: "3 sets of 10 reps at 60kg" would be three ExerciseSet objects,
 * each with reps=10 and weight=60.0, with setNumbers 1, 2, and 3.
 *
 * Design Decision: Weight is a Double to accommodate fractional plates (1.25kg, 2.5kg).
 * The setNumber provides explicit ordering rather than relying on list position,
 * which is more robust when sets are added, removed, or reordered.
 *
 * TODO: Add support for different set types (warmup, working, drop set, failure)
 * TODO: Add tempo field (e.g., "3-1-2" for eccentric-pause-concentric seconds)
 */
data class ExerciseSet(
    val id: Long = 0,
    val exerciseId: Long,
    val setNumber: Int,
    val reps: Int,
    val weight: Double
) {
    /**
     * Calculates the total volume (weight x reps) for this set.
     * Volume is a key metric for tracking progressive overload.
     */
    val volume: Double get() = weight * reps
}

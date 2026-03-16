package com.fitlog.app.domain.model

/**
 * Exercise — Domain model representing an exercise within a workout.
 *
 * Contains the exercise metadata (name, notes) plus its associated sets.
 * The sets list is included directly in the domain model for convenient
 * access in the UI layer, even though they are stored in a separate table.
 *
 * TODO: Add muscle group categories (chest, back, shoulders, arms, legs, core)
 * TODO: Add equipment type (barbell, dumbbell, machine, cable, bodyweight)
 */
data class Exercise(
    val id: Long = 0,
    val workoutId: Long,
    val name: String,
    val notes: String = "",
    /** Sets associated with this exercise, loaded eagerly for display */
    val sets: List<ExerciseSet> = emptyList()
)

package com.fitlog.app.domain.model

/**
 * Workout — Domain model representing a workout session.
 *
 * This is the "clean" domain representation of a workout, decoupled from the
 * Room entity. Presenters and Views work with domain models, not entities.
 *
 * Design Decision: Separating domain models from database entities follows
 * clean architecture principles. If we add a network layer later, the domain
 * model stays stable even if the database schema or API response changes.
 *
 * TODO: Add WorkoutType enum instead of raw String for type safety
 */
data class Workout(
    val id: Long = 0,
    val date: Long,
    val type: String,
    val notes: String = "",
    val durationMinutes: Int = 0
) {
    companion object {
        /** Workout type constants to avoid magic strings throughout the codebase */
        const val TYPE_STRENGTH = "strength"
        const val TYPE_CARDIO = "cardio"
        const val TYPE_FLEXIBILITY = "flexibility"

        /** All available workout types for UI display (spinner, filter, etc.) */
        val ALL_TYPES = listOf(TYPE_STRENGTH, TYPE_CARDIO, TYPE_FLEXIBILITY)
    }
}

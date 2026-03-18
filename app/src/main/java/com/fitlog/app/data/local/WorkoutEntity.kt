package com.fitlog.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * WorkoutEntity — Room entity representing a single workout session.
 *
 * Each workout has a date, type (strength/cardio/flexibility), optional notes,
 * and a duration in minutes. The workout serves as the parent entity for exercises,
 * which in turn contain sets.
 *
 * Database hierarchy: Workout -> Exercise -> ExerciseSet
 *
 * Design Decision: We store the date as a Long (epoch milliseconds) rather than
 * a String to enable efficient date range queries and sorting. Room's type
 * converters could also handle java.time types, but Long is simpler and more portable.
 *
 * TODO: Add a "completed" boolean flag to distinguish in-progress vs finished workouts
 * TODO: Consider adding a "rating" field (1-5) for subjective workout quality tracking
 */
@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Workout date stored as epoch milliseconds for efficient querying */
    val date: Long,

    /**
     * Workout type — one of "strength", "cardio", or "flexibility".
     * Stored as a plain String to avoid a type converter for a simple enum.
     * TODO: Consider using a Room TypeConverter with a proper Kotlin enum
     */
    val type: String,

    /** Optional user notes about the workout (how they felt, goals, etc.) */
    val notes: String = "",

    /** Workout duration in minutes. Zero means duration was not tracked. */
    val durationMinutes: Int = 0
)

package com.fitlog.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ExerciseSetEntity — Room entity representing a single set within an exercise.
 *
 * Each set records the set number (ordering), number of repetitions, and weight used.
 * This granular data model enables rich progress tracking — we can chart weight
 * progression over time for any given exercise across multiple workouts.
 *
 * Design Decision: Weight is stored as a Double (in kg or lbs, user preference)
 * rather than an Int to accommodate fractional plates (e.g., 2.5kg increments).
 * The setNumber field determines display order within the exercise.
 *
 * Foreign key with CASCADE delete ensures sets are cleaned up when their
 * parent exercise is removed.
 *
 * TODO: Add a "completed" boolean to support planned vs actual sets
 * TODO: Add "rpe" (Rate of Perceived Exertion) field for intensity tracking
 * TODO: Add "restSeconds" field for rest period tracking between sets
 */
@Entity(
    tableName = "exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["exerciseId"])]
)
data class ExerciseSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Reference to the parent exercise — enforced by foreign key constraint */
    val exerciseId: Long,

    /** Set number within the exercise (1-based), used for display ordering */
    val setNumber: Int,

    /** Number of repetitions performed in this set */
    val reps: Int,

    /**
     * Weight used in this set (in user's preferred unit — kg or lbs).
     * Stored as Double to support fractional weights like 2.5kg plates.
     * A value of 0.0 indicates bodyweight or no weight (common for cardio/flexibility).
     */
    val weight: Double
)

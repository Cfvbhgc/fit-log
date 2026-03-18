package com.fitlog.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ExerciseEntity — Room entity representing a single exercise within a workout.
 *
 * An exercise belongs to exactly one workout (via workoutId foreign key) and can
 * have multiple sets (ExerciseSetEntity). The foreign key constraint with CASCADE
 * delete ensures that when a workout is deleted, all its exercises are automatically
 * removed — preventing orphaned records.
 *
 * The index on workoutId improves JOIN performance when loading a workout with
 * all its exercises, which is one of the most frequent queries in the app.
 *
 * TODO: Add a "category" field (e.g., chest, back, legs) for better filtering
 * TODO: Add an "order" field to let users reorder exercises within a workout
 */
@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            // CASCADE ensures exercises are deleted when their parent workout is deleted
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workoutId"])]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Reference to the parent workout — enforced by foreign key constraint */
    val workoutId: Long,

    /** Exercise name (e.g., "Bench Press", "Squat", "Running") */
    val name: String,

    /** Optional notes specific to this exercise instance */
    val notes: String = ""
)

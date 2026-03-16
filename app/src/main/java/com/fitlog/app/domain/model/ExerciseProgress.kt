package com.fitlog.app.domain.model

/**
 * ExerciseProgress — Domain model for chart data representing weight progression.
 *
 * Each instance represents a single data point on the progress chart:
 * the maximum weight lifted for a specific exercise on a given date.
 *
 * The progress chart plots these points chronologically, creating a line chart
 * that visualizes strength gains (or losses) over time. This is one of the
 * most motivating features for users — seeing their progress visually.
 *
 * Design Decision: We track "max weight per session" rather than "average weight"
 * because max weight better represents strength progress (progressive overload).
 * Users typically care about their personal records and peak performance.
 *
 * TODO: Add support for volume-based progress (total weight x reps per session)
 * TODO: Add support for estimated 1RM (one-rep max) calculation
 */
data class ExerciseProgress(
    /** Date of the workout where this weight was achieved (epoch millis) */
    val date: Long,

    /** Maximum weight lifted for the exercise during that workout session */
    val maxWeight: Double
)

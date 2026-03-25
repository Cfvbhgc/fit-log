package com.fitlog.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * DateUtils — Utility functions for date formatting and manipulation.
 *
 * Centralizes all date-related logic to ensure consistent formatting throughout
 * the app. All dates are stored internally as epoch milliseconds (Long), and
 * these utilities handle conversion to human-readable strings for display.
 *
 * Design Decision: We use SimpleDateFormat instead of java.time.format because
 * it's simpler for our use cases. For a production app with timezone-sensitive
 * operations, java.time (available on API 26+ which is our minSdk) would be
 * a better choice.
 *
 * TODO: Add user preference for date format (US vs. European vs. ISO)
 * TODO: Add locale-aware formatting
 * TODO: Migrate to java.time.LocalDate for better API
 */
object DateUtils {

    /** Full date format for workout detail headers: "Monday, January 15, 2024" */
    private val fullDateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    /** Short date format for list items: "Jan 15, 2024" */
    private val shortDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    /** Month-year format for chart X-axis labels: "Jan 2024" */
    private val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    /** Chart date format for individual data points: "Jan 15" */
    private val chartDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    /**
     * Format epoch millis to a full date string.
     * Example: "Monday, January 15, 2024"
     * Used in workout detail screen headers.
     */
    fun formatFullDate(epochMillis: Long): String {
        return fullDateFormat.format(Date(epochMillis))
    }

    /**
     * Format epoch millis to a short date string.
     * Example: "Jan 15, 2024"
     * Used in workout list item cards for compact display.
     */
    fun formatShortDate(epochMillis: Long): String {
        return shortDateFormat.format(Date(epochMillis))
    }

    /**
     * Format epoch millis to month-year string.
     * Example: "Jan 2024"
     * Used for chart axis labels and section headers.
     */
    fun formatMonthYear(epochMillis: Long): String {
        return monthYearFormat.format(Date(epochMillis))
    }

    /**
     * Format epoch millis to a chart-friendly short date.
     * Example: "Jan 15"
     * Used for individual data point labels on the progress chart.
     */
    fun formatChartDate(epochMillis: Long): String {
        return chartDateFormat.format(Date(epochMillis))
    }

    /**
     * Get the start of the current month as epoch millis.
     * Used for the monthly progress chart default date range.
     */
    fun getStartOfCurrentMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Get the end of the current month as epoch millis.
     */
    fun getEndOfCurrentMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Get the start of a month N months ago from today.
     * Used for "last 3 months" or "last 6 months" chart ranges.
     *
     * @param monthsAgo How many months to go back (0 = current month)
     */
    fun getStartOfMonthsAgo(monthsAgo: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -monthsAgo)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Format a duration in minutes to a human-readable string.
     * Examples: "45 min", "1h 30min", "2h"
     */
    fun formatDuration(minutes: Int): String {
        return when {
            minutes <= 0 -> "—"
            minutes < 60 -> "${minutes} min"
            minutes % 60 == 0 -> "${minutes / 60}h"
            else -> "${minutes / 60}h ${minutes % 60}min"
        }
    }
}

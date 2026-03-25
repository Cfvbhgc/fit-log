package com.fitlog.app.util

import android.content.Context
import android.view.View
import android.widget.Toast
import com.fitlog.app.FitLogApp
import com.google.android.material.snackbar.Snackbar

/**
 * Extensions.kt — Kotlin extension functions used throughout the FitLog app.
 *
 * Extension functions let us add methods to existing classes without inheritance.
 * This file contains utility extensions for Android framework classes that
 * reduce boilerplate and improve readability in Activities and Fragments.
 *
 * Design Decision: We keep all extensions in a single file rather than
 * scattering them across the codebase. This makes them easy to discover
 * and prevents duplicate implementations. For a larger project, consider
 * splitting into domain-specific extension files (ViewExtensions, ContextExtensions, etc.)
 *
 * TODO: Add extension functions for input validation (EditText.isNotBlank(), etc.)
 * TODO: Add animation extensions (View.fadeIn(), View.fadeOut())
 */

// =============================================================================
// Context Extensions
// =============================================================================

/**
 * Quick access to the FitLogApp application instance.
 * Avoids casting applicationContext in every Activity/Fragment.
 *
 * Usage: val app = requireContext().fitLogApp
 */
val Context.fitLogApp: FitLogApp
    get() = applicationContext as FitLogApp

/**
 * Show a short Toast message.
 * Extension on Context so it can be called from Activities, Fragments, Services, etc.
 *
 * Usage: context.showToast("Workout saved!")
 */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * Show a long Toast message for more important information.
 *
 * Usage: context.showLongToast("Workout deleted. This cannot be undone.")
 */
fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// =============================================================================
// View Extensions
// =============================================================================

/**
 * Set view visibility to VISIBLE.
 * Reads more naturally than `view.visibility = View.VISIBLE`.
 *
 * Usage: progressBar.show()
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Set view visibility to GONE (removed from layout).
 * Use GONE (not INVISIBLE) to reclaim the view's space in the layout.
 *
 * Usage: progressBar.hide()
 */
fun View.hide() {
    visibility = View.GONE
}

/**
 * Set view visibility to INVISIBLE (hidden but still occupies space).
 * Use this when you need to preserve the layout structure.
 *
 * Usage: placeholder.makeInvisible()
 */
fun View.makeInvisible() {
    visibility = View.INVISIBLE
}

/**
 * Toggle visibility between VISIBLE and GONE based on a condition.
 *
 * Usage: emptyStateView.visibleIf(workouts.isEmpty())
 */
fun View.visibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

/**
 * Show a Snackbar on this view.
 * Snackbars are preferred over Toasts for actions that can be undone.
 *
 * Usage: rootView.showSnackbar("Workout deleted", "Undo") { undoDelete() }
 */
fun View.showSnackbar(
    message: String,
    actionText: String? = null,
    duration: Int = Snackbar.LENGTH_LONG,
    action: (() -> Unit)? = null
) {
    val snackbar = Snackbar.make(this, message, duration)
    if (actionText != null && action != null) {
        snackbar.setAction(actionText) { action() }
    }
    snackbar.show()
}

// =============================================================================
// String Extensions
// =============================================================================

/**
 * Capitalize the first letter of a string.
 * Used for displaying workout types: "strength" -> "Strength"
 *
 * Kotlin's capitalize() is deprecated, so we implement our own version.
 */
fun String.capitalizeFirst(): String {
    return if (isEmpty()) this
    else this[0].uppercase() + substring(1)
}

// =============================================================================
// Number Extensions
// =============================================================================

/**
 * Format a Double as a weight string, removing unnecessary decimal places.
 * Examples: 60.0 -> "60", 67.5 -> "67.5", 100.25 -> "100.25"
 *
 * This prevents awkward displays like "60.0 kg" when the weight is a whole number.
 */
fun Double.formatWeight(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        this.toString()
    }
}

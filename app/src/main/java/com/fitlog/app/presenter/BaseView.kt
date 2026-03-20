package com.fitlog.app.presenter

/**
 * BaseView — Base interface for all MVP View contracts.
 *
 * Every View in our MVP architecture implements this interface (directly or
 * through a more specific contract interface). It provides common UI operations
 * that all screens need: error display and loading state management.
 *
 * In MVP, the View is a passive interface — it only displays data provided by
 * the Presenter and forwards user actions back to the Presenter. The View
 * should contain ZERO business logic.
 *
 * Design Decision: We use a simple interface rather than an abstract class
 * because Activities and Fragments already have their own base classes.
 * Kotlin's interface default implementations give us some flexibility.
 *
 * TODO: Add showMessage(message: String) for non-error feedback (e.g., "Workout saved!")
 * TODO: Add isActive(): Boolean to check if the view is still visible before updating
 */
interface BaseView {

    /**
     * Show a loading indicator (e.g., ProgressBar, shimmer effect).
     * Called by the Presenter when starting an async operation.
     */
    fun showLoading()

    /**
     * Hide the loading indicator.
     * Called by the Presenter when the async operation completes.
     */
    fun hideLoading()

    /**
     * Display an error message to the user.
     * The Presenter translates exceptions into user-friendly messages
     * before calling this method.
     *
     * @param message Human-readable error description
     */
    fun showError(message: String)
}

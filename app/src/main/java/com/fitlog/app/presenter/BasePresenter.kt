package com.fitlog.app.presenter

/**
 * BasePresenter — Base interface for all MVP Presenters.
 *
 * Defines the lifecycle methods that every Presenter must implement to properly
 * manage the View attachment/detachment cycle. This prevents memory leaks and
 * ensures we don't call methods on destroyed Views.
 *
 * MVP Lifecycle:
 * 1. View creates the Presenter
 * 2. View calls attachView(view) in onCreate/onViewCreated
 * 3. Presenter interacts with View through the interface
 * 4. View calls detachView() in onDestroy/onDestroyView
 * 5. Presenter nullifies the View reference, cancels ongoing operations
 *
 * Design Decision: The type parameter V is bounded by BaseView to ensure
 * type safety — you can't accidentally attach the wrong view type.
 *
 * TODO: Add a base implementation class that handles common coroutine scope management
 * TODO: Consider using WeakReference for the view to prevent memory leaks
 */
interface BasePresenter<V : BaseView> {

    /**
     * Attach the View to this Presenter.
     * Called from the View's onCreate (Activity) or onViewCreated (Fragment).
     * After this call, the Presenter can safely call methods on the View.
     */
    fun attachView(view: V)

    /**
     * Detach the View from this Presenter.
     * Called from the View's onDestroy (Activity) or onDestroyView (Fragment).
     * After this call, the Presenter MUST NOT call any methods on the View.
     * This is also the place to cancel ongoing coroutines.
     */
    fun detachView()
}

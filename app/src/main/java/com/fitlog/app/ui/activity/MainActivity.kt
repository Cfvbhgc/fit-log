package com.fitlog.app.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fitlog.app.R
import com.fitlog.app.databinding.ActivityMainBinding
import com.fitlog.app.ui.fragment.ExerciseLibraryFragment
import com.fitlog.app.ui.fragment.ProgressFragment
import com.fitlog.app.ui.fragment.WorkoutListFragment

/**
 * MainActivity — Primary host activity for the FitLog application.
 *
 * Uses BottomNavigationView to switch between three main screens:
 * 1. Workouts — List of workout sessions (default tab)
 * 2. Progress — Charts showing exercise weight progression
 * 3. Library — Browse all exercises the user has performed
 *
 * Fragment Management Strategy: We use show/hide instead of replace to preserve
 * fragment state when switching tabs. This means all three fragments stay in
 * memory, but the user's scroll position and form state are preserved when
 * switching back to a tab. For an app with only 3 lightweight fragments,
 * the memory tradeoff is acceptable.
 *
 * Design Decision: We use manual fragment transactions rather than Navigation
 * Component because our navigation is simple (3 tabs + 2 detail activities)
 * and manual management gives us full control over the fragment lifecycle.
 * Navigation Component would add complexity without significant benefit here.
 *
 * TODO: Add a "Settings" tab or overflow menu for preferences
 * TODO: Add onboarding/tutorial for first-time users
 * TODO: Save and restore the selected tab across process death
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** References to the three main fragments — created once and reused */
    private var workoutListFragment: WorkoutListFragment? = null
    private var progressFragment: ProgressFragment? = null
    private var exerciseLibraryFragment: ExerciseLibraryFragment? = null

    /** Track the currently visible fragment to manage show/hide transitions */
    private var activeFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar as the ActionBar
        setSupportActionBar(binding.toolbar)

        if (savedInstanceState == null) {
            // First launch — create and add all fragments
            setupFragments()
        } else {
            // Restore fragments after configuration change
            restoreFragments()
        }

        setupBottomNavigation()
    }

    /**
     * Create and add all three main fragments to the container.
     * All fragments are added but only the workout list is shown initially.
     * The others are hidden until their tab is selected.
     */
    private fun setupFragments() {
        workoutListFragment = WorkoutListFragment.newInstance()
        progressFragment = ProgressFragment.newInstance()
        exerciseLibraryFragment = ExerciseLibraryFragment.newInstance()

        supportFragmentManager.beginTransaction().apply {
            // Add all fragments to the same container
            add(R.id.fragmentContainer, exerciseLibraryFragment!!, ExerciseLibraryFragment.TAG)
            hide(exerciseLibraryFragment!!)

            add(R.id.fragmentContainer, progressFragment!!, ProgressFragment.TAG)
            hide(progressFragment!!)

            add(R.id.fragmentContainer, workoutListFragment!!, WorkoutListFragment.TAG)
            // WorkoutListFragment is shown by default (no hide call)
        }.commit()

        activeFragment = workoutListFragment
    }

    /**
     * Restore fragment references after configuration change.
     * Fragments survive configuration changes via the FragmentManager,
     * but our local references are lost. We re-find them by their tags.
     */
    private fun restoreFragments() {
        workoutListFragment = supportFragmentManager
            .findFragmentByTag(WorkoutListFragment.TAG) as? WorkoutListFragment
        progressFragment = supportFragmentManager
            .findFragmentByTag(ProgressFragment.TAG) as? ProgressFragment
        exerciseLibraryFragment = supportFragmentManager
            .findFragmentByTag(ExerciseLibraryFragment.TAG) as? ExerciseLibraryFragment

        // Determine which fragment is currently visible
        activeFragment = workoutListFragment
    }

    /**
     * Configure BottomNavigationView to switch between fragments.
     * Uses show/hide transactions for instant tab switching with state preservation.
     */
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val targetFragment: Fragment? = when (item.itemId) {
                R.id.nav_workouts -> {
                    supportActionBar?.title = getString(R.string.title_workouts)
                    workoutListFragment
                }
                R.id.nav_progress -> {
                    supportActionBar?.title = getString(R.string.title_progress)
                    progressFragment
                }
                R.id.nav_library -> {
                    supportActionBar?.title = getString(R.string.title_library)
                    exerciseLibraryFragment
                }
                else -> null
            }

            if (targetFragment != null && targetFragment != activeFragment) {
                supportFragmentManager.beginTransaction().apply {
                    // Hide the currently visible fragment
                    activeFragment?.let { hide(it) }
                    // Show the selected fragment
                    show(targetFragment)
                }.commit()

                activeFragment = targetFragment
            }

            true // Return true to display the selected item
        }

        // Set the default title
        supportActionBar?.title = getString(R.string.title_workouts)
    }
}

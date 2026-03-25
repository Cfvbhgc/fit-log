package com.fitlog.app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitlog.app.databinding.FragmentExerciseLibraryBinding
import com.fitlog.app.presenter.exerciselibrary.ExerciseLibraryContract
import com.fitlog.app.presenter.exerciselibrary.ExerciseLibraryPresenter
import com.fitlog.app.ui.adapter.ExerciseLibraryAdapter
import com.fitlog.app.util.fitLogApp
import com.fitlog.app.util.hide
import com.fitlog.app.util.show
import com.fitlog.app.util.showToast

/**
 * ExerciseLibraryFragment — Browse and search all exercises the user has performed.
 *
 * The "exercise library" is organically built from exercises logged in workouts.
 * Each entry shows the exercise name and how many times it has been performed.
 * A SearchView at the top allows filtering by name.
 *
 * This screen is read-only — exercises are added indirectly by creating workouts.
 * The library provides a quick reference and could serve as a starting point
 * for adding exercises to new workouts (autocomplete source).
 *
 * Design Decision: The library is derived from DISTINCT exercise names rather
 * than a separate template table. This "earn your library" approach is simpler
 * to implement and ensures the library only contains exercises the user actually
 * performs. A future enhancement could add a curated exercise database.
 *
 * TODO: Add tap-to-view-progress (navigate to ProgressFragment with pre-selected exercise)
 * TODO: Add exercise details screen with history, best lifts, and trends
 * TODO: Add ability to manually add exercises with descriptions and muscle groups
 */
class ExerciseLibraryFragment : Fragment(), ExerciseLibraryContract.View {

    companion object {
        const val TAG = "ExerciseLibraryFragment"

        fun newInstance(): ExerciseLibraryFragment = ExerciseLibraryFragment()
    }

    private var _binding: FragmentExerciseLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: ExerciseLibraryContract.Presenter
    private lateinit var libraryAdapter: ExerciseLibraryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter = ExerciseLibraryPresenter(requireContext().fitLogApp.exerciseRepository)
        presenter.attachView(this)

        setupRecyclerView()
        setupSearchView()

        // Load all exercises initially
        presenter.loadExercises()
    }

    override fun onDestroyView() {
        presenter.detachView()
        _binding = null
        super.onDestroyView()
    }

    /**
     * Set up the RecyclerView for the exercise library list.
     */
    private fun setupRecyclerView() {
        libraryAdapter = ExerciseLibraryAdapter()

        binding.recyclerViewLibrary.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = libraryAdapter
            setHasFixedSize(true)
        }
    }

    /**
     * Set up the SearchView for filtering exercises by name.
     *
     * We use setOnQueryTextListener to get real-time search results as
     * the user types (onQueryTextChange), plus handle the search submit
     * action (onQueryTextSubmit).
     *
     * Design Decision: Debouncing the search query would be ideal to avoid
     * excessive database queries while the user is typing rapidly. For now,
     * with a small dataset and local database, the latency is negligible.
     *
     * TODO: Add debounce (300ms) to onQueryTextChange for large libraries
     */
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                presenter.searchExercises(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Search on every character change for real-time results
                presenter.searchExercises(newText ?: "")
                return true
            }
        })
    }

    // =========================================================================
    // ExerciseLibraryContract.View implementation
    // =========================================================================

    override fun showExercises(exercises: List<ExerciseLibraryContract.ExerciseLibraryItem>) {
        libraryAdapter.submitList(exercises)
    }

    override fun showEmptyLibrary() {
        binding.textEmptyLibrary.show()
        binding.recyclerViewLibrary.hide()
    }

    override fun hideEmptyLibrary() {
        binding.textEmptyLibrary.hide()
        binding.recyclerViewLibrary.show()
    }

    override fun showLoading() {
        binding.progressBar.show()
    }

    override fun hideLoading() {
        binding.progressBar.hide()
    }

    override fun showError(message: String) {
        requireContext().showToast(message)
    }
}

package com.fitlog.app.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.fitlog.app.R
import com.fitlog.app.databinding.FragmentProgressBinding
import com.fitlog.app.domain.model.ExerciseProgress
import com.fitlog.app.presenter.progress.ProgressContract
import com.fitlog.app.presenter.progress.ProgressPresenter
import com.fitlog.app.util.DateUtils
import com.fitlog.app.util.fitLogApp
import com.fitlog.app.util.hide
import com.fitlog.app.util.show
import com.fitlog.app.util.showToast
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * ProgressFragment — Displays weight progression charts for exercises.
 *
 * Shows a line chart (via MPAndroidChart) plotting the maximum weight used
 * per workout session over time for a selected exercise. This visual feedback
 * is key for tracking progressive overload — one of the most important
 * principles in strength training.
 *
 * Chart Configuration:
 * - X-axis: Dates (formatted as "Jan 15")
 * - Y-axis: Weight in kg
 * - Data points: Max weight per workout session
 * - Line style: Smooth curve with filled area
 *
 * Design Decision: We use MPAndroidChart for charting because it's the most
 * mature Android charting library with extensive customization options.
 * A Canvas-based custom chart was considered but would require significantly
 * more code for similar functionality.
 *
 * TODO: Add chart type selector (line, bar, scatter)
 * TODO: Add date range filter (last month, 3 months, 6 months, all time)
 * TODO: Add comparison mode (overlay two exercises on the same chart)
 * TODO: Add best lift annotations (markers on personal records)
 */
class ProgressFragment : Fragment(), ProgressContract.View {

    companion object {
        const val TAG = "ProgressFragment"

        fun newInstance(): ProgressFragment = ProgressFragment()
    }

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: ProgressContract.Presenter

    /** Cache of exercise names for the spinner, needed for label mapping */
    private var exerciseNames: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create presenter with exercise repository for progress data
        presenter = ProgressPresenter(requireContext().fitLogApp.exerciseRepository)
        presenter.attachView(this)

        setupChart()
        setupExerciseSpinner()

        // Load exercise names to populate the dropdown
        presenter.loadExerciseNames()
    }

    override fun onDestroyView() {
        presenter.detachView()
        _binding = null
        super.onDestroyView()
    }

    /**
     * Configure the MPAndroidChart LineChart appearance and behavior.
     *
     * We customize:
     * - Colors to match the app's Material Design theme
     * - Axis formatting for dates and weights
     * - Touch interaction (pinch zoom, drag)
     * - Legend and description text
     */
    private fun setupChart() {
        binding.lineChart.apply {
            // General chart settings
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)

            // Background and grid styling
            setBackgroundColor(Color.WHITE)
            setDrawGridBackground(false)

            // X-axis configuration (dates at the bottom)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                textColor = Color.DKGRAY
            }

            // Left Y-axis (weight values)
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                textColor = Color.DKGRAY
                axisMinimum = 0f // Weight starts at 0
            }

            // Hide the right Y-axis (not needed for single dataset)
            axisRight.isEnabled = false

            // Legend configuration
            legend.isEnabled = true
            legend.textColor = Color.DKGRAY

            // Show "No data" message when chart is empty
            setNoDataText(getString(R.string.select_exercise_for_progress))
            setNoDataTextColor(Color.GRAY)
        }
    }

    /**
     * Set up the exercise selector spinner.
     * When the user picks an exercise, the chart updates with its progression data.
     */
    private fun setupExerciseSpinner() {
        binding.spinnerExercise.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position < exerciseNames.size) {
                    val selectedExercise = exerciseNames[position]
                    presenter.loadProgressForExercise(selectedExercise)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No action needed
            }
        }
    }

    // =========================================================================
    // ProgressContract.View implementation
    // =========================================================================

    override fun showExerciseNames(names: List<String>) {
        exerciseNames = names

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            names
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerExercise.adapter = adapter

        // Show the exercise selector
        binding.spinnerExercise.show()
    }

    override fun showProgressChart(data: List<ExerciseProgress>) {
        binding.lineChart.show()
        binding.textNoProgress.hide()

        // Convert domain model to MPAndroidChart Entry objects
        // X-axis: sequential index (we use a custom formatter to show dates)
        // Y-axis: max weight value
        val entries = data.mapIndexed { index, progress ->
            Entry(index.toFloat(), progress.maxWeight.toFloat())
        }

        // Create the dataset with styling
        val dataSet = LineDataSet(entries, "Max Weight (kg)").apply {
            // Line appearance
            color = requireContext().getColor(R.color.colorPrimary)
            lineWidth = 2.5f
            setDrawCircles(true)
            circleRadius = 4f
            setCircleColor(requireContext().getColor(R.color.colorPrimary))

            // Fill area under the line for visual emphasis
            setDrawFilled(true)
            fillColor = requireContext().getColor(R.color.colorPrimaryLight)
            fillAlpha = 50

            // Value labels on data points
            valueTextSize = 10f
            valueTextColor = Color.DKGRAY
            setDrawValues(true)

            // Smooth curve mode for aesthetics
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
        }

        // Custom X-axis formatter to show dates instead of index numbers
        binding.lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index in data.indices) {
                    DateUtils.formatChartDate(data[index].date)
                } else {
                    ""
                }
            }
        }

        // Apply the data to the chart and animate
        binding.lineChart.data = LineData(dataSet)
        binding.lineChart.animateX(500) // 500ms animation for smooth appearance
        binding.lineChart.invalidate() // Force redraw
    }

    override fun showNoProgressData() {
        binding.lineChart.hide()
        binding.textNoProgress.show()
        binding.textNoProgress.text = getString(R.string.no_progress_data)
    }

    override fun showNoExercisesMessage() {
        binding.lineChart.hide()
        binding.spinnerExercise.hide()
        binding.textNoProgress.show()
        binding.textNoProgress.text = getString(R.string.no_exercises_logged)
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

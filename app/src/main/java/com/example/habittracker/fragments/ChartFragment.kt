package com.example.habittracker.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.habittracker.models.MoodEntry
import com.example.habittracker.R
import com.example.habittracker.utils.HabitPrefsManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class ChartFragment : Fragment() {

    private lateinit var lineChart: LineChart
    private lateinit var barChart: BarChart
    private lateinit var tvHabitStats: TextView
    private lateinit var tvMoodStats: TextView
    private lateinit var tvWeeklyAverage: TextView
    private lateinit var tvTotalCompleted: TextView
    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvMoodSummary: TextView
    private lateinit var prefsManager: HabitPrefsManager

    private val moodToScore = mapOf(
        "Excellent" to 7,
        "Great" to 6,
        "Good"  to 5,
        "Neutral" to 4,
        "Poor" to 3,
        "Bad" to 2,
        "Awful" to 1
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chart, container, false)

        prefsManager = HabitPrefsManager(requireContext())

        initViews(view)
        setupCharts()
        calculateStats()

        return view
    }

    private fun initViews(view: View) {
        lineChart = view.findViewById(R.id.lineChart)
        barChart = view.findViewById(R.id.barChart)
        tvHabitStats = view.findViewById(R.id.tvHabitStats)
        tvMoodStats = view.findViewById(R.id.tvMoodStats)
        tvWeeklyAverage = view.findViewById(R.id.tvWeeklyAverage)
        tvTotalCompleted = view.findViewById(R.id.tvTotalCompleted)
        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak)
        tvMoodSummary = view.findViewById(R.id.tvMoodSummary)
    }

    private fun setupCharts() {
        setupHabitCompletionChart()
        setupMoodTrendChart()
    }

    private fun setupHabitCompletionChart() {
        val entries = mutableListOf<BarEntry>()
        val dates = mutableListOf<String>()
        val calendar = Calendar.getInstance()

        // Get last 7 days
        for (i in 6 downTo 0) {
            val tempCal = calendar.clone() as Calendar
            tempCal.add(Calendar.DAY_OF_MONTH, -i)

            val completionPercentage = calculateCompletionForDate(tempCal)
            entries.add(BarEntry(i.toFloat(), completionPercentage))

            val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
            dates.add(dateFormat.format(tempCal.time))
        }

        val dataSet = BarDataSet(entries, "Habit Completion %")
        dataSet.color = Color.parseColor("#2196F3") // Accent color
        dataSet.valueTextColor = Color.parseColor("#0D47A1")
        dataSet.valueTextSize = 10f
        dataSet.setDrawValues(true)

        val barData = BarData(dataSet)
        barData.barWidth = 0.7f

        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBorders(false)
        barChart.legend.isEnabled = true
        barChart.legend.textColor = Color.parseColor("#0D47A1")
        barChart.animateY(1000)

        // X-axis styling
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.parseColor("#546E7A")
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() < dates.size) dates[value.toInt()] else ""
            }
        }

        // Y-axis styling
        barChart.axisLeft.textColor = Color.parseColor("#546E7A")
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.axisMaximum = 100f
        barChart.axisLeft.setDrawGridLines(true)
        barChart.axisLeft.gridColor = Color.parseColor("#E3F2FD")
        barChart.axisRight.isEnabled = false

        barChart.invalidate()
    }

    private fun setupMoodTrendChart() {
        val entries = mutableListOf<Entry>()
        val dates = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val moodEntries = loadMoodEntries()


        for (i in 6 downTo 0) {
            val tempCal = calendar.clone() as Calendar
            tempCal.add(Calendar.DAY_OF_MONTH, -i)

            val dateKey = getDateKey(tempCal)
            val moodEntry = moodEntries[dateKey]
            val score = moodEntry?.let { moodToScore[it.mood] ?: 4 } ?: 4

            entries.add(Entry((6 - i).toFloat(), score.toFloat()))

            val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
            dates.add(dateFormat.format(tempCal.time))
        }

        val dataSet = LineDataSet(entries, "Mood Trend")
        dataSet.color = Color.parseColor("#2196F3")
        dataSet.setCircleColor(Color.parseColor("#2196F3"))
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 5f
        dataSet.setDrawCircleHole(false)
        dataSet.valueTextSize = 10f
        dataSet.valueTextColor = Color.parseColor("#0D47A1")
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#90CAF9")
        dataSet.fillAlpha = 100
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when (value.toInt()) {
                    7 -> "😄"
                    6 -> "😊"
                    5 -> "🙂"
                    4 -> "😐"
                    3 -> "😟"
                    2 -> "😢"
                    1 -> "😫"
                    else -> ""
                }
            }
        }

        val lineData = LineData(dataSet)

        lineChart.data = lineData
        lineChart.description.isEnabled = false
        lineChart.setDrawGridBackground(false)
        lineChart.legend.isEnabled = true
        lineChart.legend.textColor = Color.parseColor("#0D47A1")
        lineChart.animateX(1000)

        // X-axis styling
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.parseColor("#546E7A")
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() < dates.size) dates[value.toInt()] else ""
            }
        }

        // Y-axis styling
        lineChart.axisLeft.textColor = Color.parseColor("#546E7A")
        lineChart.axisLeft.axisMinimum = 0f
        lineChart.axisLeft.axisMaximum = 8f
        lineChart.axisLeft.setDrawGridLines(true)
        lineChart.axisLeft.gridColor = Color.parseColor("#E3F2FD")
        lineChart.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when (value.toInt()) {
                    7 -> "Excellent"
                    6 -> "Great"
                    5 -> "Good"
                    4 -> "Neutral"
                    3 -> "Poor"
                    2 -> "Bad"
                    1 -> "Awful"
                    else -> ""
                }
            }
        }
        lineChart.axisRight.isEnabled = false

        lineChart.invalidate()
    }

    private fun calculateCompletionForDate(date: Calendar): Float {
        val habits = prefsManager.loadHabits()

        // Filter habits that should appear on this date
        val relevantHabits = habits.filter { habit ->
            if (habit.isEveryday) {
                !isDateBeforeToday(date)
            } else {
                habit.customDate?.let { isSameDay(it, date) } ?: false
            }
        }

        if (relevantHabits.isEmpty()) return 0f

        val completedCount = relevantHabits.count { habit ->
            habit.isCompletedOnDate(date)
        }

        return (completedCount.toFloat() / relevantHabits.size.toFloat()) * 100f
    }

    private fun calculateStats() {
        val habits = prefsManager.loadHabits()
        val calendar = Calendar.getInstance()

        // Calculate weekly average
        var totalPercentage = 0f
        var daysWithHabits = 0

        for (i in 0 until 7) {
            val tempCal = calendar.clone() as Calendar
            tempCal.add(Calendar.DAY_OF_MONTH, -i)
            val percentage = calculateCompletionForDate(tempCal)
            if (percentage > 0) {
                totalPercentage += percentage
                daysWithHabits++
            }
        }

        val weeklyAverage = if (daysWithHabits > 0) totalPercentage / daysWithHabits else 0f
        tvWeeklyAverage.text = String.format("%.1f%%", weeklyAverage)

        // Calculate total completed today
        val todayCompleted = habits.count { it.isCompletedOnDate(calendar) }
        val todayTotal = habits.filter { habit ->
            if (habit.isEveryday) true
            else habit.customDate?.let { isSameDay(it, calendar) } ?: false
        }.size
        tvTotalCompleted.text = "$todayCompleted / $todayTotal"

        // Calculate current streak
        var streak = 0
        val checkCal = calendar.clone() as Calendar
        while (true) {
            val completion = calculateCompletionForDate(checkCal)
            if (completion == 100f) {
                streak++
                checkCal.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                break
            }
        }
        tvCurrentStreak.text = "$streak days"


        val moodEntries = loadMoodEntries()
        val recentMoods = mutableListOf<String>()
        for (i in 0 until 7) {
            val tempCal = calendar.clone() as Calendar
            tempCal.add(Calendar.DAY_OF_MONTH, -i)
            val dateKey = getDateKey(tempCal)
            moodEntries[dateKey]?.let { recentMoods.add(it.mood) }
        }

        val moodCounts = recentMoods.groupingBy { it }.eachCount()
        val mostCommon = moodCounts.maxByOrNull { it.value }?.key ?: "No data"
        tvMoodSummary.text = "Most common: $mostCommon"
    }

    private fun loadMoodEntries(): Map<String, MoodEntry> {
        val sharedPreferences = requireContext().getSharedPreferences("MoodTrackerPrefs", 0)
        val json = sharedPreferences.getString("mood_entries", null)
        if (json != null) {
            val type = object : TypeToken<MutableMap<String, MoodEntry>>() {}.type
            return Gson().fromJson(json, type)
        }
        return emptyMap()
    }

    private fun getDateKey(cal: Calendar): String {
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun isDateBeforeToday(cal: Calendar): Boolean {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val calNormalized = cal.clone() as Calendar
        calNormalized.set(Calendar.HOUR_OF_DAY, 0)
        calNormalized.set(Calendar.MINUTE, 0)
        calNormalized.set(Calendar.SECOND, 0)
        calNormalized.set(Calendar.MILLISECOND, 0)

        return calNormalized.before(today)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar) =
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)

}
package com.example.habittracker.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.adapters.CalendarAdapter
import com.example.habittracker.models.MoodEntry
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class MoodFragment : Fragment() {

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var monthYearText: TextView
    private lateinit var fabAddMood: FloatingActionButton
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gson: Gson
    private lateinit var btnPreviousMonth: View
    private lateinit var btnNextMonth: View


    private val calendar = Calendar.getInstance()
    private val moodEntries = mutableMapOf<String, MoodEntry>() // dateKey to MoodEntry

    companion object {
        private const val PREFS_NAME = "MoodTrackerPrefs"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)


        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        gson = Gson()


        loadMoods()

        initViews(view)
        setupCalendar()
        setupFab()

        return view
    }

    private fun initViews(view: View) {
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)
        monthYearText = view.findViewById(R.id.monthYearText)
        fabAddMood = view.findViewById(R.id.fabAddMood)
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)

        setupMonthNavigation()
    }

    private fun setupMonthNavigation() {
        btnPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
    }

    private fun setupCalendar() {
        calendarRecyclerView.layoutManager = GridLayoutManager(context, 7)

        calendarAdapter = CalendarAdapter(
            onDateClick = { dateKey ->

                if (isDateTodayOrPast(dateKey)) {
                    val mood = moodEntries[dateKey]
                    if (mood != null) {
                        showMoodDetail(mood, dateKey)
                    } else {
                        showMoodSelector(dateKey)
                    }
                }
            }
        )

        calendarRecyclerView.adapter = calendarAdapter
        updateCalendar()
    }

    private fun setupFab() {
        fabAddMood.setOnClickListener {
            val today = getDateKey(Calendar.getInstance())
            showMoodSelector(today)
        }
    }

    private fun updateCalendar() {
        val dates = generateCalendarDates()
        val today = getDateKey(Calendar.getInstance())
        calendarAdapter.updateDates(dates, moodEntries, today)

        val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val year = calendar.get(Calendar.YEAR)
        monthYearText.text = "$monthName $year"
    }

    private fun generateCalendarDates(): List<CalendarAdapter.CalendarDate> {
        val dates = mutableListOf<CalendarAdapter.CalendarDate>()

        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)


        for (i in 1 until firstDayOfWeek) {
            dates.add(CalendarAdapter.CalendarDate("", "", false))
        }


        for (day in 1..daysInMonth) {
            tempCal.set(Calendar.DAY_OF_MONTH, day)
            val dateKey = getDateKey(tempCal)
            dates.add(CalendarAdapter.CalendarDate(day.toString(), dateKey, true))
        }

        return dates
    }

    private fun getDateKey(cal: Calendar): String {
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun showMoodSelector(dateKey: String) {
        val dialog = MoodSelectorDialog(
            onMoodSelected = { mood, emoji ->
                showMoodReasonDialog(mood, emoji, dateKey)
            }
        )
        dialog.show(childFragmentManager, "MoodSelector")
    }

    private fun showMoodReasonDialog(mood: String, emoji: String, dateKey: String) {
        val dialog = MoodReasonDialog(
            mood = mood,
            emoji = emoji,
            onSave = { reason ->
                saveMood(dateKey, mood, emoji, reason)
            }
        )
        dialog.show(childFragmentManager, "MoodReason")
    }

    private fun showMoodDetail(moodEntry: MoodEntry, dateKey: String) {
        val dialog = MoodDetailDialog(
            moodEntry = moodEntry,
            onDelete = {
                deleteMood(dateKey)
            }
        )
        dialog.show(childFragmentManager, "MoodDetail")
    }

    private fun saveMood(dateKey: String, mood: String, emoji: String, reason: String) {
        moodEntries[dateKey] = MoodEntry(mood, emoji, reason)
        saveMoods()
        updateCalendar()
    }

    private fun deleteMood(dateKey: String) {
        moodEntries.remove(dateKey)
        saveMoods()
        updateCalendar()
    }

    private fun saveMoods() {
        val json = gson.toJson(moodEntries)
        sharedPreferences.edit().putString(KEY_MOOD_ENTRIES, json).apply()
    }

    private fun loadMoods() {
        val json = sharedPreferences.getString(KEY_MOOD_ENTRIES, null)
        if (json != null) {
            val type = object : TypeToken<MutableMap<String, MoodEntry>>() {}.type
            val loadedEntries: MutableMap<String, MoodEntry> = gson.fromJson(json, type)
            moodEntries.clear()
            moodEntries.putAll(loadedEntries)
        }
    }

    private fun isDateTodayOrPast(dateKey: String): Boolean {
        val parts = dateKey.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()

        val dateToCheck = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return dateToCheck.timeInMillis <= today.timeInMillis
    }
}
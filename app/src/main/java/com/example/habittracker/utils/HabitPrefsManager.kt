package com.example.habittracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.habittracker.models.Habit

class HabitPrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "HabitTrackerPrefs"
        private const val KEY_HABITS = "habits_list"
    }


    fun saveHabits(habits: List<Habit>) {
        val habitsJson = habits.joinToString(";;;") { it.toJsonString() }
        prefs.edit().putString(KEY_HABITS, habitsJson).apply()
    }

    fun loadHabits(): MutableList<Habit> {
        val habitsJson = prefs.getString(KEY_HABITS, "") ?: ""
        if (habitsJson.isEmpty()) return mutableListOf()

        return habitsJson.split(";;;")
            .mapNotNull { Habit.fromJsonString(it) }
            .toMutableList()
    }

    fun clearAllHabits() {
        prefs.edit().remove(KEY_HABITS).apply()
    }
}
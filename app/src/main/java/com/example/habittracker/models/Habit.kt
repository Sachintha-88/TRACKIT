package com.example.habittracker.models

import java.util.*

data class Habit(
    var name: String,
    var emoji: String,
    var description: String,
    var isCompleted: Boolean = false,
    var isEveryday: Boolean = true,
    var customDate: Calendar? = null,
    var completedDates: MutableSet<String> = mutableSetOf() // Store dates in "yyyy-MM-dd" format
) {
    // Helper function to check if habit is completed on a specific date
    fun isCompletedOnDate(date: Calendar): Boolean {
        val dateKey = getDateKey(date)
        return completedDates.contains(dateKey)
    }

    // Helper function to toggle completion for a specific date
    fun toggleCompletionForDate(date: Calendar) {
        val dateKey = getDateKey(date)
        if (completedDates.contains(dateKey)) {
            completedDates.remove(dateKey)
        } else {
            completedDates.add(dateKey)
        }
    }

    // Helper function to set completion for a specific date
    fun setCompletionForDate(date: Calendar, isCompleted: Boolean) {
        val dateKey = getDateKey(date)
        if (isCompleted) {
            completedDates.add(dateKey)
        } else {
            completedDates.remove(dateKey)
        }
    }

    private fun getDateKey(date: Calendar): String {
        return String.format(
            "%04d-%02d-%02d",
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH) + 1,
            date.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Convert to JSON string for SharedPreferences
    fun toJsonString(): String {
        val customDateStr = customDate?.let {
            "${it.get(Calendar.YEAR)}-${it.get(Calendar.MONTH)}-${it.get(Calendar.DAY_OF_MONTH)}"
        } ?: "null"

        val completedDatesStr = completedDates.joinToString(",")

        return "$name|$emoji|$description|$isEveryday|$customDateStr|$completedDatesStr"
    }

    companion object {

        fun fromJsonString(jsonString: String): Habit? {
            return try {
                val parts = jsonString.split("|")
                if (parts.size < 5) return null

                val name = parts[0]
                val emoji = parts[1]
                val description = parts[2]
                val isEveryday = parts[3].toBoolean()
                val customDateStr = parts[4]

                val customDate = if (customDateStr != "null") {
                    val dateParts = customDateStr.split("-")
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, dateParts[0].toInt())
                        set(Calendar.MONTH, dateParts[1].toInt())
                        set(Calendar.DAY_OF_MONTH, dateParts[2].toInt())
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                } else null

                val completedDates = if (parts.size > 5 && parts[5].isNotEmpty()) {
                    parts[5].split(",").toMutableSet()
                } else {
                    mutableSetOf()
                }

                Habit(name, emoji, description, false, isEveryday, customDate, completedDates)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
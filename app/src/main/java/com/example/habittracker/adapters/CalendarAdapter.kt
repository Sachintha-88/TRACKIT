package com.example.habittracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.models.MoodEntry

class CalendarAdapter(
    private val onDateClick: (String) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    data class CalendarDate(
        val day: String,
        val dateKey: String,
        val isCurrentMonth: Boolean
    )

    private var dates = listOf<CalendarDate>()
    private var moodEntries = mapOf<String, MoodEntry>()
    private var todayDateKey = ""

    inner class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayText: TextView = view.findViewById(R.id.dayText)
        val emojiText: TextView = view.findViewById(R.id.emojiText)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val date = dates[position]
                    if (date.isCurrentMonth) {
                        onDateClick(date.dateKey)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = dates[position]

        holder.dayText.text = date.day
        holder.dayText.visibility = if (date.isCurrentMonth) View.VISIBLE else View.INVISIBLE

        // Check if this date is in the future
        val isFutureDate = isFutureDate(date.dateKey)

        // Show mood emoji if exists
        val mood = moodEntries[date.dateKey]
        if (mood != null) {
            holder.emojiText.text = mood.emoji
            holder.emojiText.visibility = View.VISIBLE
        } else {
            holder.emojiText.visibility = View.GONE
        }

        // Styling - dim future dates
        if (isFutureDate && date.isCurrentMonth) {
            holder.dayText.alpha = 0.3f
        } else {
            holder.dayText.alpha = if (date.isCurrentMonth) 1f else 0.3f
        }
    }

    private fun isFutureDate(dateKey: String): Boolean {
        if (dateKey.isEmpty() || todayDateKey.isEmpty()) return false

        try {
            val parts = dateKey.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()

            val todayParts = todayDateKey.split("-")
            val todayYear = todayParts[0].toInt()
            val todayMonth = todayParts[1].toInt()
            val todayDay = todayParts[2].toInt()

            val dateToCheck = year * 10000 + month * 100 + day
            val today = todayYear * 10000 + todayMonth * 100 + todayDay

            return dateToCheck > today
        } catch (e: Exception) {
            return false
        }
    }

    override fun getItemCount() = dates.size

    fun updateDates(newDates: List<CalendarDate>, newMoodEntries: Map<String, MoodEntry>, today: String) {
        dates = newDates
        moodEntries = newMoodEntries
        todayDateKey = today
        notifyDataSetChanged()
    }
}
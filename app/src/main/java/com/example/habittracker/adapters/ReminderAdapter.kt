package com.example.habittracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.models.ReminderTime

class ReminderAdapter(
    private val reminders: List<ReminderTime>,
    private val onDeleteClick: (ReminderTime) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeText: TextView = view.findViewById(R.id.tvReminderTime)
        val deleteButton: ImageButton = view.findViewById(R.id.btnDeleteReminder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.timeText.text = reminder.timeString
        holder.deleteButton.setOnClickListener {
            onDeleteClick(reminder)
        }
    }

    override fun getItemCount() = reminders.size
}
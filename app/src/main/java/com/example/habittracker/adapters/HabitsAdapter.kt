package com.example.habittracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.models.Habit
import java.util.*

class HabitsAdapter(
    private val habits: MutableList<Habit>,
    private var currentDate: Calendar,
    private val onDelete: (Habit, Int) -> Unit,
    private val onEdit: (Habit, Int) -> Unit,
    private val onCheckChanged: (Habit, Boolean) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvHabitEmoji)
        val tvName: TextView = view.findViewById(R.id.tvHabitName)
        val tvDescription: TextView = view.findViewById(R.id.tvHabitDescription)
        val cbComplete: CheckBox = view.findViewById(R.id.cbHabitComplete)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditHabit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteHabit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]


        holder.cbComplete.setOnCheckedChangeListener(null)

        holder.tvEmoji.text = habit.emoji
        holder.tvName.text = habit.name
        holder.tvDescription.text = habit.description
        holder.tvDescription.visibility = if (habit.description.isEmpty()) View.GONE else View.VISIBLE


        holder.cbComplete.isChecked = habit.isCompletedOnDate(currentDate)


        holder.cbComplete.setOnCheckedChangeListener { _, isChecked ->
            onCheckChanged(habit, isChecked)
        }

        holder.btnEdit.setOnClickListener { onEdit(habit, position) }
        holder.btnDelete.setOnClickListener { onDelete(habit, position) }
        holder.itemView.setOnClickListener { onEdit(habit, position) }
    }

    override fun getItemCount(): Int = habits.size

    fun updateData(newList: List<Habit>, newDate: Calendar) {
        currentDate = newDate
        habits.clear()
        habits.addAll(newList)
        notifyDataSetChanged()
    }

    fun getHabitAt(position: Int): Habit = habits[position]
}
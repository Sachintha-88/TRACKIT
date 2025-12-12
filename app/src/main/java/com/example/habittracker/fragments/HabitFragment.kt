package com.example.habittracker.fragments

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.adapters.HabitsAdapter
import com.example.habittracker.models.Habit
import com.example.habittracker.utils.HabitPrefsManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class HabitFragment : Fragment() {

    private lateinit var tvMainDate: TextView
    private lateinit var tvFullDate: TextView
    private lateinit var llDateContainer: LinearLayout
    private lateinit var rvHabits: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var fabAddHabit: FloatingActionButton
    private val habitsList = mutableListOf<Habit>()
    private lateinit var habitsAdapter: HabitsAdapter
    private val calendar = Calendar.getInstance()
    private val today = Calendar.getInstance()
    private lateinit var prefsManager: HabitPrefsManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_habit, container, false)


        prefsManager = HabitPrefsManager(requireContext())

        initViews(view)
        loadHabitsFromPrefs()
        setupRecyclerView()
        setupDateDisplay()
        setupHorizontalDates()
        setupFab()
        updateHabitsForSelectedDate()
        return view
    }

    private fun initViews(view: View) {
        tvMainDate = view.findViewById(R.id.tvMainDate)
        tvFullDate = view.findViewById(R.id.tvFullDate)
        llDateContainer = view.findViewById(R.id.llDateContainer)
        rvHabits = view.findViewById(R.id.rvHabits)
        llEmptyState = view.findViewById(R.id.llEmptyState)
        fabAddHabit = view.findViewById(R.id.fabAddHabit)
    }

    private fun loadHabitsFromPrefs() {
        habitsList.clear()
        habitsList.addAll(prefsManager.loadHabits())
    }

    private fun saveHabitsToPrefs() {
        prefsManager.saveHabits(habitsList)
    }

    private fun setupDateDisplay() {
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val diff = getDaysDifference(calendar, today)

        tvMainDate.text = when (diff) {
            0 -> "Today"
            1 -> "Tomorrow"
            -1 -> "Yesterday"
            else -> SimpleDateFormat("MMMM d", Locale.getDefault()).format(calendar.time)
        }

        tvFullDate.text = dateFormat.format(calendar.time)
    }

    private fun setupHorizontalDates() {
        llDateContainer.removeAllViews()
        val startCal = calendar.clone() as Calendar
        startCal.add(Calendar.DAY_OF_MONTH, -3)

        for (i in 0 until 7) {
            val dateView = layoutInflater.inflate(R.layout.item_date, llDateContainer, false)
            val cvDateItem = dateView.findViewById<CardView>(R.id.cvDateItem)
            val tvDay = dateView.findViewById<TextView>(R.id.tvDateDay)
            val tvMonth = dateView.findViewById<TextView>(R.id.tvDateMonth)

            tvDay.text = SimpleDateFormat("d", Locale.getDefault()).format(startCal.time)
            tvMonth.text = SimpleDateFormat("MMM", Locale.getDefault()).format(startCal.time)

            // Highlight selected
            if (isSameDay(startCal, calendar)) {
                cvDateItem.setCardBackgroundColor(Color.parseColor("#2196F3"))
                tvDay.setTextColor(Color.WHITE)
                tvMonth.setTextColor(Color.WHITE)
            } else {
                cvDateItem.setCardBackgroundColor(Color.WHITE)
                tvDay.setTextColor(Color.parseColor("#0D47A1"))
                tvMonth.setTextColor(Color.parseColor("#546E7A"))
            }

            val clickCal = startCal.clone() as Calendar
            cvDateItem.setOnClickListener {
                calendar.timeInMillis = clickCal.timeInMillis
                setupDateDisplay()
                setupHorizontalDates()
                updateHabitsForSelectedDate()
            }

            llDateContainer.addView(dateView)
            startCal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun setupRecyclerView() {
        habitsAdapter = HabitsAdapter(
            habits = mutableListOf(),
            currentDate = calendar,
            onDelete = { _, pos ->
                deleteHabit(pos)
            },
            onEdit = { habit, pos ->
                showAddHabitDialog(editHabit = habit, editPos = pos)
            },
            onCheckChanged = { habit, isChecked ->
                habit.setCompletionForDate(calendar, isChecked)
                saveHabitsToPrefs()
            }
        )
        rvHabits.layoutManager = LinearLayoutManager(requireContext())
        rvHabits.adapter = habitsAdapter
    }

    private fun setupFab() {
        fabAddHabit.setOnClickListener { showAddHabitDialog() }
    }

    private fun showAddHabitDialog(editHabit: Habit? = null, editPos: Int? = null) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_habit)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val etName = dialog.findViewById<TextInputEditText>(R.id.etHabitName)
        val etEmoji = dialog.findViewById<TextInputEditText>(R.id.etHabitEmoji)
        val etDesc = dialog.findViewById<TextInputEditText>(R.id.etHabitDescription)
        val rbEveryday = dialog.findViewById<RadioButton>(R.id.rbEveryday)
        val rbCustom = dialog.findViewById<RadioButton>(R.id.rbCustom)
        val dpCustom = dialog.findViewById<DatePicker>(R.id.dpCustomDate)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialog.findViewById<Button>(R.id.btnSave)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)


        tvTitle?.text = if (editHabit != null) "Edit Habit" else "Add New Habit"


        val todayCalendar = Calendar.getInstance()
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
        todayCalendar.set(Calendar.MINUTE, 0)
        todayCalendar.set(Calendar.SECOND, 0)
        todayCalendar.set(Calendar.MILLISECOND, 0)
        dpCustom.minDate = todayCalendar.timeInMillis
        dpCustom.visibility = View.GONE


        rbEveryday.isChecked = true


        editHabit?.let {
            etName.setText(it.name)
            etEmoji.setText(it.emoji)
            etDesc.setText(it.description)
            if (it.isEveryday) {
                rbEveryday.isChecked = true
            } else {
                rbCustom.isChecked = true
                dpCustom.visibility = View.VISIBLE
                it.customDate?.let { cal ->
                    dpCustom.updateDate(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                }
            }
        }

        rbEveryday.setOnCheckedChangeListener { _, isChecked ->
            dpCustom.visibility = if (isChecked) View.GONE else View.VISIBLE
        }
        rbCustom.setOnCheckedChangeListener { _, isChecked ->
            dpCustom.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val emoji = etEmoji.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val isEveryday = rbEveryday.isChecked
            val customDateCal = if (!isEveryday) {
                Calendar.getInstance().apply {
                    set(dpCustom.year, dpCustom.month, dpCustom.dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            } else null

            if (name.isEmpty()) {
                etName.error = "Name required"
                return@setOnClickListener
            }
            if (emoji.isEmpty()) {
                etEmoji.error = "Emoji required"
                return@setOnClickListener
            }


            if (!isEveryday && customDateCal != null) {
                val selectedDateOnly = customDateCal.clone() as Calendar
                selectedDateOnly.set(Calendar.HOUR_OF_DAY, 0)
                selectedDateOnly.set(Calendar.MINUTE, 0)
                selectedDateOnly.set(Calendar.SECOND, 0)
                selectedDateOnly.set(Calendar.MILLISECOND, 0)

                if (selectedDateOnly.before(todayCalendar)) {
                    Toast.makeText(
                        requireContext(),
                        "Cannot select past dates",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }

            if (editHabit != null && editPos != null) {

                editHabit.name = name
                editHabit.emoji = emoji
                editHabit.description = desc
                editHabit.isEveryday = isEveryday
                editHabit.customDate = customDateCal
            } else {

                habitsList.add(Habit(name, emoji, desc, false, isEveryday, customDateCal))
            }

            saveHabitsToPrefs() // Save to SharedPreferences
            updateHabitsForSelectedDate()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteHabit(pos: Int) {

        val habitToDelete = habitsAdapter.getHabitAt(pos)
        habitsList.remove(habitToDelete)
        saveHabitsToPrefs()
        updateHabitsForSelectedDate()
    }

    private fun updateHabitsForSelectedDate() {
        val filtered = habitsList.filter { habit ->
            if (habit.isEveryday) {
                // Everyday habits only show from today onwards (not for past dates)
                !isDateBeforeToday(calendar)
            } else {
                habit.customDate?.let { isSameDay(it, calendar) } ?: false
            }
        }

        habitsAdapter.updateData(filtered, calendar)
        llEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        rvHabits.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun isDateBeforeToday(cal: Calendar): Boolean {
        val todayNormalized = today.clone() as Calendar
        todayNormalized.set(Calendar.HOUR_OF_DAY, 0)
        todayNormalized.set(Calendar.MINUTE, 0)
        todayNormalized.set(Calendar.SECOND, 0)
        todayNormalized.set(Calendar.MILLISECOND, 0)

        val calNormalized = cal.clone() as Calendar
        calNormalized.set(Calendar.HOUR_OF_DAY, 0)
        calNormalized.set(Calendar.MINUTE, 0)
        calNormalized.set(Calendar.SECOND, 0)
        calNormalized.set(Calendar.MILLISECOND, 0)

        return calNormalized.before(todayNormalized)
    }

    private fun getDaysDifference(cal1: Calendar, cal2: Calendar): Int {
        val d1 = cal1.clone() as Calendar
        d1.set(Calendar.HOUR_OF_DAY, 0)
        d1.set(Calendar.MINUTE, 0)
        d1.set(Calendar.SECOND, 0)
        d1.set(Calendar.MILLISECOND, 0)
        val d2 = cal2.clone() as Calendar
        d2.set(Calendar.HOUR_OF_DAY, 0)
        d2.set(Calendar.MINUTE, 0)
        d2.set(Calendar.SECOND, 0)
        d2.set(Calendar.MILLISECOND, 0)
        return ((d1.timeInMillis - d2.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar) =
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)

    override fun onPause() {
        super.onPause()
        saveHabitsToPrefs()
    }
}
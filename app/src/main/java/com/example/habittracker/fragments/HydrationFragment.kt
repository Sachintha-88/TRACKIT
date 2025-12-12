package com.example.habittracker.fragments

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.adapters.ReminderAdapter
import com.example.habittracker.models.ReminderTime
import com.example.habittracker.receivers.HydrationReceiver
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class HydrationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var fabAdd: FloatingActionButton
    private val reminderList = mutableListOf<ReminderTime>()

    companion object {
        private const val PREFS_NAME = "HydrationPrefs"
        private const val KEY_REMINDERS = "reminders"
        private const val CHANNEL_ID = "hydration_channel"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hydration, container, false)

        // Initialize views
        val dropIcon = view.findViewById<ImageView>(R.id.ivWaterDrop)
        val titleText = view.findViewById<TextView>(R.id.tvDrinkWater)
        recyclerView = view.findViewById(R.id.rvReminders)
        fabAdd = view.findViewById(R.id.fabAddReminder)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        reminderAdapter = ReminderAdapter(reminderList) { reminder ->
            deleteReminder(reminder)
        }
        recyclerView.adapter = reminderAdapter

        // Load saved reminders
        loadReminders()

        // Create notification channel
        createNotificationChannel()

        // Add button click listener
        fabAdd.setOnClickListener {
            showTimePickerDialog()
        }

        return view
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                addReminder(selectedHour, selectedMinute)
            },
            hour,
            minute,
            false
        ).show()
    }

    private fun addReminder(hour: Int, minute: Int) {
        val timeString = String.format("%02d:%02d", hour, minute)
        val id = System.currentTimeMillis().toInt()

        val reminder = ReminderTime(id, timeString, hour, minute)


        if (reminderList.any { it.timeString == timeString }) {
            Toast.makeText(requireContext(), "This time already exists!", Toast.LENGTH_SHORT).show()
            return
        }

        reminderList.add(reminder)
        reminderList.sortBy { it.hour * 60 + it.minute }
        reminderAdapter.notifyDataSetChanged()

        // Save to SharedPreferences
        saveReminders()

        // Schedule alarm
        scheduleAlarm(reminder)

        Toast.makeText(requireContext(), "Reminder added for $timeString", Toast.LENGTH_SHORT).show()
    }

    private fun deleteReminder(reminder: ReminderTime) {
        reminderList.remove(reminder)
        reminderAdapter.notifyDataSetChanged()

        // Cancel alarm
        cancelAlarm(reminder)

        // Save to SharedPreferences
        saveReminders()

        Toast.makeText(requireContext(), "Reminder deleted", Toast.LENGTH_SHORT).show()
    }

    private fun scheduleAlarm(reminder: ReminderTime) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)

            // If time has passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelAlarm(reminder: ReminderTime) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    private fun saveReminders() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val remindersString = reminderList.joinToString(";") {
            "${it.id},${it.hour},${it.minute}"
        }

        editor.putString(KEY_REMINDERS, remindersString)
        editor.apply()
    }

    private fun loadReminders() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val remindersString = prefs.getString(KEY_REMINDERS, "") ?: ""

        if (remindersString.isNotEmpty()) {
            reminderList.clear()
            remindersString.split(";").forEach { reminderData ->
                val parts = reminderData.split(",")
                if (parts.size == 3) {
                    val id = parts[0].toInt()
                    val hour = parts[1].toInt()
                    val minute = parts[2].toInt()
                    val timeString = String.format("%02d:%02d", hour, minute)
                    val reminder = ReminderTime(id, timeString, hour, minute)
                    reminderList.add(reminder)


                    scheduleAlarm(reminder)
                }
            }
            reminderAdapter.notifyDataSetChanged()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminders"
            val descriptionText = "Daily water drinking reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
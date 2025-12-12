package com.example.habittracker.models

data class ReminderTime(
    val id: Int,
    val timeString: String,
    val hour: Int,
    val minute: Int
)
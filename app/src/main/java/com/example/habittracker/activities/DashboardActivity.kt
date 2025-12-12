package com.example.habittracker.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.habittracker.R
import com.example.habittracker.fragments.HabitFragment
import com.example.habittracker.fragments.MoodFragment
import com.example.habittracker.fragments.HydrationFragment
import com.example.habittracker.fragments.ChartFragment
import com.example.habittracker.fragments.SettingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)


        loadFragment(HabitFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_habit -> loadFragment(HabitFragment())
                R.id.nav_mood -> loadFragment(MoodFragment())
                R.id.nav_hygiene -> loadFragment(HydrationFragment())
                R.id.nav_chart -> loadFragment(ChartFragment())
                R.id.nav_settings -> loadFragment(SettingFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}

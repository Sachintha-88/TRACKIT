package com.example.habittracker.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.habittracker.R
import com.google.android.material.card.MaterialCardView

class MoodSelectorDialog(
    private val onMoodSelected: (String, String) -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_mood_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val moods = listOf(
            Triple("Excellent", "😄", R.id.moodExcellent),
            Triple("Great", "😊", R.id.moodGreat),
            Triple("Good", "🙂", R.id.moodGood),
            Triple("Neutral", "😐", R.id.moodNeutral),
            Triple("Poor", "😟", R.id.moodPoor),
            Triple("Bad", "😢", R.id.moodBad),
            Triple("Awful", "😫", R.id.moodAwful)
        )

        moods.forEach { (mood, emoji, viewId) ->
            view.findViewById<MaterialCardView>(viewId)?.setOnClickListener {
                onMoodSelected(mood, emoji)
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
package com.example.habittracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.habittracker.R
import com.example.habittracker.models.MoodEntry

class MoodDetailDialog(
    private val moodEntry: MoodEntry,
    private val onDelete: () -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_mood_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emojiText = view.findViewById<TextView>(R.id.emojiText)
        val moodNameText = view.findViewById<TextView>(R.id.moodNameText)
        val reasonText = view.findViewById<TextView>(R.id.reasonText)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)
        val closeButton = view.findViewById<Button>(R.id.closeButton)

        emojiText.text = moodEntry.emoji
        moodNameText.text = moodEntry.mood
        reasonText.text = if (moodEntry.reason.isNotEmpty()) {
            moodEntry.reason
        } else {
            "No reason provided"
        }

        deleteButton.setOnClickListener {
            onDelete()
            dismiss()
        }

        closeButton.setOnClickListener {
            dismiss()
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
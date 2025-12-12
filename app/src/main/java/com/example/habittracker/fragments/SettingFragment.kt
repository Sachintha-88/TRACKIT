package com.example.habittracker.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.habittracker.R
import java.io.ByteArrayOutputStream

class SettingFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var usernameInput: EditText
    private lateinit var saveButton: Button
    private lateinit var changePhotoButton: Button

    private lateinit var sharedPrefs: SharedPreferences
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)


        sharedPrefs = requireActivity().getSharedPreferences("HabitTrackerPrefs", Context.MODE_PRIVATE)


        profileImage = view.findViewById(R.id.profileImage)
        usernameInput = view.findViewById(R.id.usernameInput)
        saveButton = view.findViewById(R.id.saveButton)
        changePhotoButton = view.findViewById(R.id.changePhotoButton)


        loadUserData()


        changePhotoButton.setOnClickListener {
            openImagePicker()
        }

        saveButton.setOnClickListener {
            saveUserData()
        }

        return view
    }

    private fun loadUserData() {

        val savedUsername = sharedPrefs.getString("username", "")
        usernameInput.setText(savedUsername)


        val savedImage = sharedPrefs.getString("profile_image", null)
        if (savedImage != null) {
            val bitmap = decodeBase64(savedImage)
            profileImage.setImageBitmap(bitmap)
        }
    }

    private fun saveUserData() {
        val username = usernameInput.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }


        sharedPrefs.edit().putString("username", username).apply()

        Toast.makeText(context, "Settings saved!", Toast.LENGTH_SHORT).show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                profileImage.setImageBitmap(bitmap)

                // Save image to SharedPreferences
                val encodedImage = encodeBase64(bitmap)
                sharedPrefs.edit().putString("profile_image", encodedImage).apply()

                Toast.makeText(context, "Photo updated!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun encodeBase64(bitmap: Bitmap): String {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun decodeBase64(encodedImage: String): Bitmap {
        val decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingFragment()
    }
}
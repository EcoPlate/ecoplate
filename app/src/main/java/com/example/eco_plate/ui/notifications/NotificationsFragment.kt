package com.example.eco_plate.ui.notifications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.eco_plate.databinding.FragmentNotificationsBinding
import java.io.File

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private var pendingBitmap: Bitmap? = null

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bmp = result.data?.extras?.get("data") as? Bitmap
            if (bmp != null) {
                pendingBitmap = bmp
                binding.imageView2.setImageBitmap(bmp)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        requireContext().contentResolver.openInputStream(uri)?.use { stream ->
            val bmp = BitmapFactory.decodeStream(stream)
            if (bmp != null) {
                pendingBitmap = bmp
                binding.imageView2.setImageBitmap(bmp)
            } else {
                Toast.makeText(requireContext(), "Could not load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupUi()
        loadProfile()
        return root
    }

    private fun setupUi() {
        binding.buttonChange.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Profile photo")
                .setItems(arrayOf("Take photo", "Choose from gallery")) { _, which ->
                    when (which) {
                        0 -> ensureCameraPermissionAndOpen()
                        1 -> galleryLauncher.launch("image/*")
                    }
                }
                .show()
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
            Toast.makeText(requireContext(), "Saved!", Toast.LENGTH_SHORT).show()
        }
        binding.btnCancel.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    private fun ensureCameraPermissionAndOpen() {
        val granted = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) openCamera() else requestCameraPermission.launch(android.Manifest.permission.CAMERA)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun saveProfile() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("name", binding.name.text.toString())
        editor.putString("email", binding.email.text.toString())
        editor.putString("phone", binding.phone.text.toString())
        editor.putString("class", binding.etClass.text.toString())
        editor.putString("major", binding.etMajor.text.toString())
        val selectedId = binding.gender.checkedRadioButtonId
        editor.putInt("gender_id", if (selectedId != View.NO_ID) selectedId else View.NO_ID)

        pendingBitmap?.let { bmp ->
            val fileName = "profile.png"
            requireContext().openFileOutput(fileName, Context.MODE_PRIVATE).use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val fullPath = File(requireContext().filesDir, fileName).absolutePath
            editor.putString("photo_path", fullPath)
        }
        editor.apply()
        pendingBitmap = null
    }

    private fun loadProfile() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        binding.name.setText(prefs.getString("name", ""))
        binding.email.setText(prefs.getString("email", ""))
        binding.phone.setText(prefs.getString("phone", ""))
        binding.etClass.setText(prefs.getString("class", ""))
        binding.etMajor.setText(prefs.getString("major", ""))
        val savedGenderId = prefs.getInt("gender_id", View.NO_ID)
        if (savedGenderId != View.NO_ID) binding.gender.check(savedGenderId) else binding.gender.clearCheck()
        val path = prefs.getString("photo_path", null)
        if (!path.isNullOrEmpty()) {
            val f = File(path)
            if (f.exists()) binding.imageView2.setImageBitmap(BitmapFactory.decodeFile(path))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
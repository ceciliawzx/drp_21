package com.android.kotlinmvvmtodolist.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.android.kotlinmvvmtodolist.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraFunc(
    private val fragment: Fragment,
    private val previewID: Int
) {

    private val REQUEST_IMAGE_CAPTURE = 1
    private var currentPhotoPath: String = ""

    private val activity = fragment.requireActivity()
    private val context = fragment.requireContext()

    private var takePictureLauncher: ActivityResultLauncher<Intent>

    init {
        takePictureLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val imageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
                    if (imageBitmap != null) {

                        val view = fragment.requireView()

                        val imageView = view.findViewById<ImageView>(previewID)
                        val newWidth = (imageBitmap.width * 0.3).toInt()
                        val newHeight = (imageBitmap.height * 0.3).toInt()
                        val resizedBitmap =
                            Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, true)
                        imageView.visibility = View.VISIBLE
                        imageView.setImageBitmap(resizedBitmap)

                    } else {
                        Toast.makeText(context, "Failed to capture photo.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(context, "Photo capture cancelled.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Take photo when click camera button
    fun takePhoto(): String {
        // Grant permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_IMAGE_CAPTURE
            )
        } else {
            // Dispatch
            dispatchTakePictureIntent()
        }
        return currentPhotoPath
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity.packageManager)?.also {
                val photoFile: File? = try {
                    // Create temporary file
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e("DispatchTakePicture", "Error creating image file: ${ex.message}")
                    null
                }

                if (photoFile != null) {
                    // Temporary file created successfully
                    val photoURI: Uri = FileProvider.getUriForFile(
                        context,
                        "com.example.android.fileprovider",
                        photoFile
                    )

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    // Close temporary file
                    val outputStream = activity.contentResolver.openOutputStream(photoURI)
                    outputStream?.close()
                    // Get result
                    takePictureLauncher.launch(takePictureIntent)
                }
            }
        }
    }

    // Create temporary image file
    private fun createImageFile(): File? {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? =
            activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        try {
            val imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            currentPhotoPath = imageFile.absolutePath
            return imageFile
        } catch (ex: IOException) {
            Log.e("CreateImageFile", "Error creating temporary file: ${ex.message}")
            return null
        }
    }
}

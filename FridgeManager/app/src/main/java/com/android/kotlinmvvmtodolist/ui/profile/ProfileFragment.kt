package com.android.kotlinmvvmtodolist.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.helper.widget.MotionEffect
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentAddBinding
import com.android.kotlinmvvmtodolist.databinding.FragmentProfileBinding
import com.android.kotlinmvvmtodolist.ui.SignInActivity
import com.android.kotlinmvvmtodolist.ui.add.AddFragmentArgs
import com.android.kotlinmvvmtodolist.ui.add.PreviewDialog
import com.android.kotlinmvvmtodolist.ui.camera.CameraFunc
import com.android.kotlinmvvmtodolist.ui.shopList.ShopListViewModel
import com.android.kotlinmvvmtodolist.ui.task.TaskViewModel
import com.android.kotlinmvvmtodolist.util.NotificationAlert
import com.android.kotlinmvvmtodolist.util.ShopItemWorker
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import com.android.kotlinmvvmtodolist.util.Constants.USER_DATABASE_REFERENCE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var textUsername: TextView
    private lateinit var databaseReference: DatabaseReference
    private val PICK_IMAGE_REQUEST = 1
    private val CAMERA_REQUEST = 2
    private val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var profileImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        textUsername = binding.textUsername
        databaseReference = USER_DATABASE_REFERENCE

        val userRef = databaseReference.child("User").child(currentUserID!!)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userName = dataSnapshot.child("userName").value.toString()
                    textUsername.text = userName
                    Log.d("username", "$userName")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the onCancelled event if needed
            }
        })

        this.profileImage = binding.profileImage

        // 给view绑定数据
        binding.apply {

            databaseReference.child("User").child(currentUserID).child("profileImage").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val encodedImage = dataSnapshot.value.toString()
                    encodedImage?.let {
                        // Convert the string back to a byte array
                        val imageData: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)

                        // Convert the byte array to a bitmap and set it as the profile photo
                        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                        profileImage.setImageBitmap(bitmap)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors
                }
            })

            // Set click listener on the profile image
            profileImage.setOnClickListener {
                // Open the image gallery to select a photo
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
            }


            textUid.text = "uid: $currentUserID"

            btnCopyUid.setOnClickListener{
                val clipboard = getSystemService(requireContext(), ClipboardManager::class.java)
                val clip = android.content.ClipData.newPlainText("UID", currentUserID)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "UID Copied!", Toast.LENGTH_SHORT).show()
            }

            btnLogout.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Confirm Logout")
                builder.setMessage("Are you sure you want to log out?")
                builder.setPositiveButton("AYes") { dialogInterface: DialogInterface, _: Int ->

                    FirebaseAuth.getInstance().signOut()

                    clearLoginCredentials()

                    navigateToLoginActivity()
                    dialogInterface.dismiss()
                }
                builder.setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
        return binding.root
    }

    private fun clearLoginCredentials() {
        val sharedPreferences = requireContext().getSharedPreferences("login_credentials", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(requireContext(), SignInActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            // Get the selected image URI
            val selectedImageUri: Uri? = data.data

            // Convert the selected image to a byte array
            selectedImageUri?.let { imageUri ->
                val inputStream: InputStream? = requireActivity().contentResolver.openInputStream(imageUri)
                val imageData: ByteArray? = inputStream?.readBytes()

                // Convert the byte array to a string
                val encodedImage: String = Base64.encodeToString(imageData, Base64.DEFAULT)

                // Update the profile photo in the Firebase Realtime Database
                updateProfilePhoto(encodedImage)
            }
        }
    }

    private fun updateProfilePhoto(encodedImage: String) {
        // Update the profile photo in the Firebase Realtime Database
        databaseReference.child("User").child(currentUserID!!).child("profileImage").setValue(encodedImage)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Profile photo updated successfully
                    // Convert the string back to a byte array
                    val imageData: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)

                    // Convert the byte array to a bitmap and set it as the profile photo
                    val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    profileImage.setImageBitmap(bitmap)
                } else {
                    // Handle the error
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
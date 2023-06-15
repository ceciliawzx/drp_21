package com.android.kotlinmvvmtodolist.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.databinding.FragmentProfileBinding
import com.android.kotlinmvvmtodolist.ui.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import com.android.kotlinmvvmtodolist.util.Constants.USER_DATABASE_REFERENCE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import android.view.Menu
import android.view.MenuInflater
import androidx.navigation.fragment.navArgs
import com.android.kotlinmvvmtodolist.ui.chat.ConversationFragmentArgs

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var textUsername: TextView
    private val databaseReference = USER_DATABASE_REFERENCE
    private val PICK_IMAGE_REQUEST = 1
    private val CAMERA_REQUEST = 2

    private val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var profileImage: ImageView

    private val args by navArgs<ProfileFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        val userName = args.userName
        val encodedImage = args.profileImage

        // Show userName
        textUsername = binding.textUsername
        textUsername.text = userName

        // Show profileImage
        this.profileImage = binding.profileImage
        val imageData: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
        // Convert the byte array to a bitmap and set it as the profile photo
        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
        profileImage.setImageBitmap(bitmap)

        // Set click listener on the profile image
        profileImage.setOnClickListener {
            // Open the image gallery to select a photo
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        }

        // 给view绑定数据
        binding.apply {
            textUid.text = "uid: $currentUserID"

            btnCopyUid.setOnClickListener {
                val clipboard = getSystemService(requireContext(), ClipboardManager::class.java)
                val clip = android.content.ClipData.newPlainText("UID", currentUserID)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "UID Copied!", Toast.LENGTH_SHORT).show()
            }

            btnLogout.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Confirm Logout")
                builder.setMessage("Are you sure you want to log out?")
                builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, _: Int ->

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

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun clearLoginCredentials() {
        val sharedPreferences =
            requireContext().getSharedPreferences("login_credentials", Context.MODE_PRIVATE)
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
                val inputStream: InputStream? =
                    requireActivity().contentResolver.openInputStream(imageUri)
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
        databaseReference.child("User").child(currentUserID!!).child("profileImage")
            .setValue(encodedImage)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)

        val messageButton = menu.findItem(R.id.profile_message)
        messageButton.setOnMenuItemClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_chatFragment)
            true
        }
    }
}
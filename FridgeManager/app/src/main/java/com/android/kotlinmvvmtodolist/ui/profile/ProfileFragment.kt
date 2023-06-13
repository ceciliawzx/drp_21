package com.android.kotlinmvvmtodolist.ui.profile

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
import androidx.core.content.ContextCompat.getSystemService
import com.android.kotlinmvvmtodolist.util.Constants.USER_DATABASE_REFERENCE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var textUsername: TextView
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val textUsername = binding.textUsername

        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        databaseReference = USER_DATABASE_REFERENCE

        val userRef = databaseReference.child("User").child(currentUserID!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
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

        // 给view绑定数据
        binding.apply {
            textUid.text = "uid: $currentUserID"

            btnCopyUid.setOnClickListener{
                val clipboard = getSystemService(requireContext(), ClipboardManager::class.java)
                val clip = android.content.ClipData.newPlainText("UID", currentUserID)
                clipboard?.setPrimaryClip(clip)
                showCopySuccessDialog()
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

    private fun showCopySuccessDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Copy Successful")
        builder.setMessage("The UID has been copied to the clipboard.")
        builder.setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
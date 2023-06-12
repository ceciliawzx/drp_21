package com.android.kotlinmvvmtodolist.ui.profile

import android.app.AlertDialog
import android.app.DatePickerDialog
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

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // 给view绑定数据
        binding.apply {
            // Limits check
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.android.kotlinmvvmtodolist.ui.shopList

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.helper.widget.MotionEffect.TAG
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.data.local.ShopItemEntry
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentAddBinding
import com.android.kotlinmvvmtodolist.databinding.FragmentAddItemBinding
import com.android.kotlinmvvmtodolist.ui.task.TaskViewModel
import com.android.kotlinmvvmtodolist.util.Notification
import com.android.kotlinmvvmtodolist.util.messageExtra
import com.android.kotlinmvvmtodolist.util.titleExtra
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class AddItemFragment : Fragment() {

    private val viewModel: ShopListViewModel by viewModels()
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddItemBinding.inflate(inflater, container, false)

        val shopListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.priorities)
        )

        // 给view绑定数据
        binding.apply {
            shopListSpinner.adapter = shopListAdapter

            // Limits check
            shopListBtnAdd.setOnClickListener {

                val titleTitle = shopListFoodName.text.toString()
                val type = shopListSpinner.selectedItemPosition

                val shopItemEntry = ShopItemEntry(
                    0,
                    titleTitle,
                    type,
                    System.currentTimeMillis()
                )

                viewModel.insert(shopItemEntry)

                Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_addItemFragment_to_shopListFragment)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

package com.android.kotlinmvvmtodolist.ui.update

import android.app.DatePickerDialog
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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentUpdateBinding
import com.android.kotlinmvvmtodolist.ui.task.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class UpdateFragment : Fragment() {

    private val viewModel: TaskViewModel by viewModels()
    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<UpdateFragmentArgs>()

    private var mDisplayDate: TextView? = null
    private var mDateSetListener: DatePickerDialog.OnDateSetListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        mDisplayDate = binding.root.findViewById(R.id.update_choose_date)
        var expireDate: String = ""

        // adapt results of database to ui, 每一条item
        val myAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.priorities)
        )

        val unitAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.units)
        )

        // 给view绑定数据
        binding.apply {
            updateSpinner.adapter = myAdapter
            updateUnitSpinner.adapter = unitAdapter

            updateFoodName.setText(args.task.title)
            updateFoodAmount.setText(args.task.amount.toString())
            updateUnitSpinner.setSelection(args.task.unit)
            updateChooseDate.text = args.task.expireDate


            updateChooseDate.setOnClickListener {
                val cal = Calendar.getInstance()
                val year = cal[Calendar.YEAR]
                val month = cal[Calendar.MONTH]
                val day = cal[Calendar.DAY_OF_MONTH]
                val dialog = DatePickerDialog(
                    requireContext(),
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    mDateSetListener,
                    year,
                    month,
                    day
                )
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
            }

            mDateSetListener =
                DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                    var month = month
                    month += 1
                    Log.d(MotionEffect.TAG, "onDateSet: yyyy-mm-dd: $year-$month-$day")
                    val date = "$month/$day/$year"
                    expireDate = "$year-$month-$day"
                    mDisplayDate!!.text = date
                }

            // Limits check
            btnUpdate.setOnClickListener {
                if(TextUtils.isEmpty((updateFoodName.text))){
                    Toast.makeText(requireContext(), "Please enter food name!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if(TextUtils.isEmpty((updateFoodAmount.text))){
                    Toast.makeText(requireContext(), "Please enter the amount!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val foodAmountText = updateFoodAmount.text.toString()
                val amountNum = foodAmountText.toIntOrNull()
                if(amountNum == null){
                    Toast.makeText(requireContext(), "Please enter a number!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else if(amountNum <= 0 || amountNum > 10000) {
                    Toast.makeText(requireContext(), "Number out of bound!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val titleTitle = updateFoodName.text.toString()
                val type = updateSpinner.selectedItemPosition
                val unit = updateUnitSpinner.selectedItemPosition
                val amount = updateFoodAmount.text.toString().toInt()

                val taskEntry = TaskEntry(
                    args.task.id,
                    titleTitle,
                    type,
                    System.currentTimeMillis(),
                    expireDate,
                    amount,
                    unit
                )

                viewModel.update(taskEntry)
                Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_updateFragment_to_taskFragment)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
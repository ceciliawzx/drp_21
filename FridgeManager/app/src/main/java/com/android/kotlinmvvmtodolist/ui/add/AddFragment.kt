package com.android.kotlinmvvmtodolist.ui.add

import android.app.DatePickerDialog
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
import androidx.constraintlayout.helper.widget.MotionEffect.TAG
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentAddBinding
import com.android.kotlinmvvmtodolist.ui.task.TaskViewModel
import com.android.kotlinmvvmtodolist.ui.camera.CameraFunc
import com.android.kotlinmvvmtodolist.util.NotificationAlert.calculateDaysLeft
import com.android.kotlinmvvmtodolist.util.NotificationAlert.getNotificationTime
import com.android.kotlinmvvmtodolist.util.NotificationAlert.scheduleNotification
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar


@AndroidEntryPoint
class AddFragment : Fragment() {

    private val viewModel: TaskViewModel by activityViewModels()
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private var mDisplayDate: TextView? = null
    private var mDateSetListener: DatePickerDialog.OnDateSetListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val args = AddFragmentArgs.fromBundle(requireArguments())
        val autofillTitle = args.title
        val autofillType = args.type

        // fragment_add.xml binding
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        mDisplayDate = binding.root.findViewById(R.id.choose_date)
        var expireDate: String = ""
        var dateChosen: Boolean = false

        // Camera
        val cameraUtils =
            CameraFunc(this@AddFragment, R.id.imagePreview)
        var currentPhotoPath: String = ""

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
            spinner.adapter = myAdapter
            unitSpinner.adapter = unitAdapter

            if (autofillType != -1) {
                foodName.setText(autofillTitle)
                spinner.setSelection(autofillType)
            }

            chooseDate.setOnClickListener {
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


            var continuousBuying = false

            buyingSwitch.setOnCheckedChangeListener { _, isChecked ->
                continuousBuying = isChecked
            }

            mDateSetListener =
                DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                    var month = month
                    month += 1
                    Log.d(TAG, "onDateSet: yyyy-mm-dd: $year-$month-$day")
                    val date = "$month/$day/$year"
                    expireDate = "$year-$month-$day"
                    mDisplayDate!!.text = date
                    dateChosen = true
                }

            btnCamera.setOnClickListener {
                currentPhotoPath = cameraUtils.takePhoto()
            }

            imagePreview.setOnClickListener {
                val imageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
                val dialogFragment = PreviewDialog(imageBitmap)
                dialogFragment.show(parentFragmentManager, "ImageDialogFragment")
            }

            // Limits check
            btnAdd.setOnClickListener {
                if (TextUtils.isEmpty((foodName.text)) && currentPhotoPath == "") {
                    Toast.makeText(requireContext(), "Please enter food name or take a photo!", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                if (TextUtils.isEmpty((foodAmount.text))) {
                    Toast.makeText(requireContext(), "Please enter the amount!", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val foodAmountText = foodAmount.text.toString()
                val amountNum = foodAmountText.toIntOrNull()
                if (amountNum == null) {
                    Toast.makeText(requireContext(), "Please enter a number!", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                } else if (amountNum <= 0 || amountNum > 10000) {
                    Toast.makeText(requireContext(), "Number out of bound!", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                if (!dateChosen) {
                    Toast.makeText(requireContext(), "Please Enter Date!", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val days = if (!TextUtils.isEmpty(notifyDays.text)) notifyDays.text.toString() else "1"
                val titleTitle = foodName.text.toString()
                val type = spinner.selectedItemPosition
                val unit = unitSpinner.selectedItemPosition
                val amount: Int = foodAmount.text.toString().toInt()



                // Ensure every notificationID is unique
                val notificationID = viewModel.getNextNotificationID()

                val continuous = if (continuousBuying) 1 else 0

                val taskEntry = TaskEntry(
                    0,
                    titleTitle,
                    type,
                    System.currentTimeMillis(),
                    expireDate,
                    amount,
                    unit,
                    notificationID,
                    continuous,
                    currentPhotoPath,
                    days
                )

                viewModel.insert(taskEntry)

                val notificationTime = getNotificationTime(expireDate, days)
                val daysLeft = calculateDaysLeft(expireDate)
                val title = "$titleTitle expire soon"
                // TODO: notify ? days before expiration
                val message1 = "Your $titleTitle will expire in $daysLeft days!"
                // val message = "Your $titleTitle will expire tomorrow!!!"
                val message = "Your $titleTitle will expire $days days later!!!"
                scheduleNotification(requireContext(), title, message, notificationTime, notificationID)
                Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_SHORT).show()
                if (autofillType == -1) {
                    findNavController().navigate(R.id.action_addFragment_to_taskFragment)
                } else {
                    findNavController().navigate(R.id.shopListFragment)
                }
            }
        }
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

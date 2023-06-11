package com.android.kotlinmvvmtodolist.ui.add

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.data.local.ShopItemEntry
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentAddBinding
import com.android.kotlinmvvmtodolist.ui.MainActivity
import com.android.kotlinmvvmtodolist.ui.task.TaskViewModel
import com.android.kotlinmvvmtodolist.ui.camera.CameraFunc
import com.android.kotlinmvvmtodolist.ui.shopList.ShopListViewModel
import com.android.kotlinmvvmtodolist.util.NotificationAlert.calculateDaysLeft
import com.android.kotlinmvvmtodolist.util.NotificationAlert.getNotificationTime
import com.android.kotlinmvvmtodolist.util.NotificationAlert.scheduleNotification
import com.android.kotlinmvvmtodolist.util.ShopItemWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class AddFragment : Fragment() {

    private val viewModel: TaskViewModel by activityViewModels()
    private val shopListViewModel: ShopListViewModel by activityViewModels()

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
        val autofillContinuous = args.continuous
        val autofillId = args.shopItemEntryId

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
                buyingSwitch.isChecked = true
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

            var continuousBuying = autofillContinuous == 1

            buyingSwitch.setOnCheckedChangeListener { _, isChecked ->
                continuousBuying = isChecked
            }

            mDateSetListener =
                DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                    var month = month
                    month += 1
                    Log.d(TAG, "onDateSet: yyyy-mm-dd: $year-$month-$day")
                    val date = "$month/$day/$year"
                    val monthString = if (month < 10) "0$month" else "$month"
                    val dayString = if (day < 10) "0$day" else "$day"
                    // expireDate format: "yyyy-MM-dd"
                    expireDate = "$year-$monthString-$dayString"
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
                    Toast.makeText(requireContext(), "Please enter item name or take a photo!", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                // default amount = 1
                val foodAmountText: String = if (TextUtils.isEmpty((foodAmount.text))) {
                    "1"
                } else {
                    foodAmount.text.toString()
                }

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
                    Toast.makeText(requireContext(), "Please choose an expiration date!", Toast.LENGTH_SHORT)
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
                val addRequestID = viewModel.getNextAddRequestID()

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
                    days,
                    addRequestID
                )

                viewModel.insert(taskEntry)

                // If continuousBuying switched on, add a new ShopItemEntry to the database in a certain time.
                if (continuousBuying) {
                    scheduleShopItem(taskEntry)
                }

                val notificationTime = getNotificationTime(expireDate, days)
                val daysLeft = calculateDaysLeft(expireDate)
                val title = "$titleTitle expire soon"
                // TODO: notify ? days before expiration
                val message1 = "Your $titleTitle will expire in $daysLeft days!"
                // val message = "Your $titleTitle will expire tomorrow!!!"
                val message = "Your $titleTitle will expire $days days later!!!"
                activity?.let { it1 ->
                    scheduleNotification(requireContext(),
                        it1, title, message, notificationTime, notificationID)
                }
                Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_SHORT).show()
                if (autofillType == -1) {
                    findNavController().navigate(R.id.action_addFragment_to_taskFragment)
                } else {
                    lifecycleScope.launch {
                        val shopItemEntry = shopListViewModel.getItemById(autofillId)
                        if (shopItemEntry != null) {
                            shopItemEntry.bought = 1
                            shopListViewModel.update(shopItemEntry)
                            findNavController().navigate(R.id.action_addFragment_to_shopListFragment)
                        }
                    }
                }
            }
        }
        return binding.root
    }

    private fun scheduleShopItem(taskEntry: TaskEntry) {
        val expireDate = taskEntry.expireDate
        val taskTitle = taskEntry.title
        val taskType = taskEntry.type
        val addID = taskEntry.addRequestId

        ShopItemWorker.scheduleShopItemEntry(
            requireContext(),
            addID,
            expireDate,
            taskTitle,
            taskType,
            shopListViewModel
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

package com.android.kotlinmvvmtodolist.ui.update

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
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
import androidx.navigation.fragment.navArgs
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.data.local.ShopItemEntry
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentUpdateBinding
import com.android.kotlinmvvmtodolist.ui.add.PreviewDialog
import com.android.kotlinmvvmtodolist.ui.camera.CameraFunc
import com.android.kotlinmvvmtodolist.ui.shopList.ShopListViewModel
import com.android.kotlinmvvmtodolist.ui.task.TaskViewModel
import com.android.kotlinmvvmtodolist.util.Notification
import com.android.kotlinmvvmtodolist.util.ShowImage.HORIZONTAL_PREVIEW_SCALE
import com.android.kotlinmvvmtodolist.util.ShowImage.VERTICLE_PREVIEW_SCALE
import com.android.kotlinmvvmtodolist.util.ShowImage.showImage
import com.android.kotlinmvvmtodolist.util.messageExtra
import com.android.kotlinmvvmtodolist.util.titleExtra
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import com.android.kotlinmvvmtodolist.util.NotificationAlert.getNotificationTime
import com.android.kotlinmvvmtodolist.util.NotificationAlert.showAlert
import com.android.kotlinmvvmtodolist.util.ShopItemWorker
import kotlinx.coroutines.launch


@AndroidEntryPoint
class UpdateFragment : Fragment() {

    private val viewModel: TaskViewModel by activityViewModels()
    private val shopListViewModel: ShopListViewModel by activityViewModels()
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
        var expireDate: String = args.task.expireDate

        // Camera
        val cameraUtils = CameraFunc(this@UpdateFragment, R.id.update_imagePreview)
        var currentPhotoPath: String = args.task.imagePath

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

            // Reserve the original information
            updateFoodName.setText(args.task.title)
            updateFoodAmount.setText(args.task.amount.toString())
            updateUnitSpinner.setSelection(args.task.unit)
            updateSpinner.setSelection(args.task.type)
            updateChooseDate.setText(args.task.expireDate)
            updateBuyingSwitch.isChecked = args.task.continuousBuying == 1
            updateNotifyDays.setText(args.task.notifyDaysBefore)

            // Show stored image preview
            showImage(
                binding.updateImagePreview,
                currentPhotoPath,
                HORIZONTAL_PREVIEW_SCALE,
                VERTICLE_PREVIEW_SCALE
            )

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
                    val monthString = if (month < 10) "0$month" else "$month"
                    val dayString = if (day < 10) "0$day" else "$day"
                    expireDate = "$year-$monthString-$dayString"
//                    expireDate = "$year-$month-$day"
                    mDisplayDate!!.text = date
                }

            var continuousBuying = args.task.continuousBuying == 1

            updateBuyingSwitch.setOnCheckedChangeListener { _, isChecked ->
                continuousBuying = isChecked
            }


            updateCamera.setOnClickListener {
                currentPhotoPath = cameraUtils.takePhoto()
            }

            updateImagePreview.setOnClickListener {
                val imageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
                val dialogFragment = PreviewDialog(imageBitmap)
                dialogFragment.show(parentFragmentManager, "ImageDialogFragment")
            }

            // Limits check
            btnUpdate.setOnClickListener {
                if (TextUtils.isEmpty((updateFoodName.text))) {
                    Toast.makeText(requireContext(), "Please enter food name!", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                if (TextUtils.isEmpty((updateFoodAmount.text))) {
                    Toast.makeText(requireContext(), "Please enter the amount!", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val foodAmountText = updateFoodAmount.text.toString()
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


                val days = if (!TextUtils.isEmpty(updateNotifyDays.text)) updateNotifyDays.text.toString() else "1"
                val titleTitle = updateFoodName.text.toString()
                val type = updateSpinner.selectedItemPosition
                val unit = updateUnitSpinner.selectedItemPosition
                val amount = updateFoodAmount.text.toString().toInt()

                val continuous = if (continuousBuying) 1 else 0

                Log.d("Adding", "update: ori id = ${args.task.id}")

                val taskEntry = TaskEntry(
                    args.task.id,
                    titleTitle,
                    type,
                    System.currentTimeMillis(),
                    expireDate,
                    amount,
                    unit,
                    args.task.notificationID,
                    continuous,
                    currentPhotoPath,
                    days,
                    args.task.addRequestId
                )

                viewModel.update(taskEntry)

                // If continuous != original continuous, rescheduleShopItem
                if (continuous != args.task.continuousBuying) {
                    // If origin = 0, update = 1 -> schedule the auto add
                    // Otherwise, cancel the job if not yet added
                    lifecycleScope.launch {
                        Log.d("Adding", "continuous change: addId = ${args.task.addRequestId}, continuous = $continuous")
                        rescheduleShopItem(taskEntry, continuous == 0)
                    }
                }


                // If the expiration date changed, update the notification and shopItemEntry adding
                if (args.task.expireDate != expireDate || args.task.notifyDaysBefore != days) {
                    val notificationTime = getNotificationTime(expireDate, days)
                    val title = "$titleTitle expire soon"
                    // val message = "Your $titleTitle will expire tomorrow!!!"
                    val message = "Your $titleTitle will expire $days days later!!!"
                    rescheduleNotification(expireDate, title, message, notificationTime)
                    // update the shopItemEntry
                    if (continuousBuying) {
                        lifecycleScope.launch {
                            rescheduleShopItem(taskEntry, false)
                        }
                    }
                }

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

    private fun rescheduleNotification(expireDate: String, title: String, message: String, notificationTime: Long) {
        val intent = Intent(requireContext(), Notification::class.java)
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            args.task.notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Cancel the previous notification
        alarmManager.cancel(pendingIntent)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notificationTime,
            pendingIntent
        )
        showAlert(notificationTime, title, message, requireContext())

        // Schedule a new notification
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notificationTime,
            pendingIntent
        )
    }

    // If cancel is true, cancel the request if not yet run
    private suspend fun rescheduleShopItem(taskEntry: TaskEntry, cancel: Boolean) {
        val expireDate = taskEntry.expireDate
        val taskTitle = taskEntry.title
        val taskType = taskEntry.type
        val addID = taskEntry.addRequestId

        Log.d("Adding", "reschedule/cancel: taskId = $id")

        val existedShopItemEntry: ShopItemEntry? = shopListViewModel.getItemByAddRequestId(addID)
        val notAdded = existedShopItemEntry == null

        if (cancel) {
            if (notAdded) {
                ShopItemWorker.cancelRequest(requireContext(), addID)
            }
        } else {
            if (notAdded) {
                ShopItemWorker.scheduleShopItemEntry(
                    requireContext(),
                    addID,
                    expireDate,
                    taskTitle,
                    taskType,
                    shopListViewModel
                )
            }
        }

    }

}
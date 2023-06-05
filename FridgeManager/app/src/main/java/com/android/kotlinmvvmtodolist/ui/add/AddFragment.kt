package com.android.kotlinmvvmtodolist.ui.add

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.core.content.ContextCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.helper.widget.MotionEffect.TAG
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentAddBinding
import com.android.kotlinmvvmtodolist.ui.task.TaskViewModel
import com.android.kotlinmvvmtodolist.util.Notification
import com.android.kotlinmvvmtodolist.util.channelID
import com.android.kotlinmvvmtodolist.util.messageExtra
import com.android.kotlinmvvmtodolist.util.notificationID
import com.android.kotlinmvvmtodolist.util.titleExtra
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import java.sql.Time
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import kotlin.math.min

@AndroidEntryPoint
class AddFragment : Fragment() {

    private val viewModel: TaskViewModel by viewModels()
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private var mDisplayDate: TextView? = null
    private var mDateSetListener: DatePickerDialog.OnDateSetListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // fragment_add.xml binding
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        mDisplayDate = binding.root.findViewById(R.id.choose_date)
        var expireDate: String = ""
        var dateChosen: Boolean = false

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

            // Limits check
            btnAdd.setOnClickListener {
                if(TextUtils.isEmpty((foodName.text))){
                    Toast.makeText(requireContext(), "Please enter food name!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if(TextUtils.isEmpty((foodAmount.text))){
                    Toast.makeText(requireContext(), "Please enter the amount!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val foodAmountText = foodAmount.text.toString()
                val amountNum = foodAmountText.toIntOrNull()
                if(amountNum == null){
                    Toast.makeText(requireContext(), "Please enter a number!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else if(amountNum <= 0 || amountNum > 10000) {
                    Toast.makeText(requireContext(), "Number out of bound!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if(!dateChosen) {
                    Toast.makeText(requireContext(), "Please Enter Date!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val titleTitle = foodName.text.toString()
                val type = spinner.selectedItemPosition
                val unit = unitSpinner.selectedItemPosition
                val amount = foodAmount.text.toString().toInt()

                val notification =
                    createNotification("Exp notification", "An expiration date notification")

                val taskEntry = TaskEntry(
                    0,
                    titleTitle,
                    type,
                    System.currentTimeMillis(),
                    expireDate,
                    amount,
                    unit,
                    notificationID
                )

                viewModel.insert(taskEntry)
                val notificationTime = getNotificationTime(expireDate)
                scheduleNotification(titleTitle, expireDate, notificationTime)
                Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_addFragment_to_taskFragment)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createNotification(name: String, des: String) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = des
        val notificationManager = requireContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleNotification(title: String, message: String, notificationTime: Long) {
        val intent = Intent(requireContext(), Notification::class.java)
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notificationTime,
            pendingIntent
        )
        showAlert(notificationTime, title, message)
    }

    private fun showAlert(time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(requireContext())
        val timeFormat = android.text.format.DateFormat.getTimeFormat(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Notification scheduled")
            .setMessage(
                "Title: " + title
                        + "\nMessage: " + message
                        + "\nAt: " + dateFormat.format(date) + " " + timeFormat.format(date)
            )
            .setPositiveButton("Okay"){_,_ ->}
            .show()
    }

    private fun getNotificationTime(expirationDate: String): Long {
        val times = expirationDate.split('-')
        val year = times[0].toInt()
        val month = times[1].toInt() - 1
        val day = times[2].toInt()

        /* TODO: This is for the sake of the test. When you run the test,
            set the hour and minute to be the time you expect the notification to happen */
        val hour = 20
        val minute = 5

        val calendar: Calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }

}
package com.android.kotlinmvvmtodolist.ui.add

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.helper.widget.MotionEffect.TAG
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlinmvvmtodolist.R
import com.android.kotlinmvvmtodolist.data.local.TaskEntry
import com.android.kotlinmvvmtodolist.databinding.FragmentAddBinding
import com.android.kotlinmvvmtodolist.ui.task.TaskViewModel
import com.android.kotlinmvvmtodolist.util.Notification
import com.android.kotlinmvvmtodolist.util.messageExtra
import com.android.kotlinmvvmtodolist.util.titleExtra
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit



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

            imageButton.setOnClickListener {
                takePhoto(requireView())
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

                // Ensure every notificationID is unique
                val notificationID = viewModel.getNextNotificationID()

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
                val daysLeft = calculateDaysLeft(expireDate)
                val title = "$titleTitle expire soon"
                // TODO: notify ? days before expiration
                val message1 = "Your $titleTitle will expire in $daysLeft days!"
                val message = "Your $titleTitle will expire tomorrow!!!"
                scheduleNotification(title, message, notificationTime, notificationID)
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

    private fun scheduleNotification(title: String, message: String, notificationTime: Long, notificationID: Int) {
        val intent = Intent(requireContext(), Notification::class.java)
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)
        intent.putExtra("notificationId", notificationID)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notificationTime,
            pendingIntent
        )
        showAlert(notificationTime, title, message, requireContext())
    }

    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var currentPhotoPath: String

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Photo capture was successful
                Toast.makeText(requireContext(), "Photo taken and saved.", Toast.LENGTH_SHORT).show()
            }
        }

    fun takePhoto(view: View) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_IMAGE_CAPTURE
            )
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e("DispatchTakePicture", "Error creating image file: ${ex.message}")
                    null
                }

                if (photoFile != null) {
                    // Temporary file created successfully
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.android.fileprovider",
                        photoFile
                    )

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    val outputStream = requireActivity().contentResolver.openOutputStream(photoURI)
                    outputStream?.close()
                    takePictureLauncher.launch(takePictureIntent)
                }
            }
        }
    }

    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        try {
            val imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            currentPhotoPath = imageFile.absolutePath
            return imageFile
        } catch (ex: IOException) {
            Log.e("CreateImageFile", "Error creating temporary file: ${ex.message}")
            return null
        }
    }
}


// Calculate when to notify the expiring item. For now notify one day before
fun getNotificationTime(expirationDate: String): Long {
    val times = expirationDate.split('-')
    val year = times[0].toInt()
    val month = times[1].toInt() - 1
    val day = times[2].toInt() - 1

    /* TODO: This is for the sake of the test. When you run the test,
        set the hour and minute to be the time you expect the notification to happen */
    val hour = 9  // 0 ~ 23
    val minute = 0
    val second = 0

    val calendar: Calendar = Calendar.getInstance()
    calendar.set(year, month, day, hour, minute, second)
    return calendar.timeInMillis
}

// Show an alert to users that a notification has been scheduled
// TODO: maybe delete this later, based on user feedback
fun showAlert(time: Long, title: String, message: String, context: Context) {
    val date = Date(time)
    val dateFormat = DateFormat.getLongDateFormat(context)
    val timeFormat = DateFormat.getTimeFormat(context)
    AlertDialog.Builder(context)
        .setTitle("Notification scheduled")
        .setMessage(
            "Title: " + title
                    + "\nMessage: " + message
                    + "\nAt: " + dateFormat.format(date) + " " + timeFormat.format(date)
        )
        .setPositiveButton("Okay") { _, _ -> }
        .show()
}

// Calculate days left between now and expirationDate
fun calculateDaysLeft(expirationDate: String): Int {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDate = Date()
    val expiryDate = dateFormat.parse(expirationDate)
    val diff = (expiryDate?.time ?: currentDate.time) - currentDate.time
    return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
}

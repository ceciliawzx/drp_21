package com.android.kotlinmvvmtodolist.util

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import com.android.kotlinmvvmtodolist.util.ShowImage.HORIZONTAL_PREVIEW_SCALE
import com.android.kotlinmvvmtodolist.util.ShowImage.VERTICLE_PREVIEW_SCALE
import com.android.kotlinmvvmtodolist.util.ShowImage.showImage
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@SuppressLint("SetTextI18n")
@BindingAdapter("setType")
fun setType(view: TextView, type: Int){
    when(type){
        0 -> {
            view.text = "Meat"
            view.setTextColor(Color.RED)
        }
        1 -> {
            view.text = "Vegetable"
            view.setTextColor(Color.GREEN)
        }
        2 -> {
            view.text = "Egg and Milk"
            view.setTextColor(Color.DKGRAY)
        }
        else -> {
            view.text = "Other"
            view.setTextColor(Color.BLUE)
        }
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("setUnit")
fun setUnit(view: TextView, number: Int){

    when(number){
        0 -> {
            view.text = "Unit(s)"
            view.setTextColor(Color.BLACK)
        }
        1 -> {
            view.text = "g"
            view.setTextColor(Color.BLACK)
        }
        2 -> {
            view.text = "ml"
            view.setTextColor(Color.BLACK)
        }
        else -> {
            view.text = "L"
            view.setTextColor(Color.BLACK)
        }
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("setAmount")
fun setAmount(view: TextView, amount: Int){
    view.text = amount.toString()
    view.setTextColor(Color.BLACK)
}

@SuppressLint("SetTextI18n")
@BindingAdapter("setDate")
//fun setDate(view: TextView, date: String){
//
//    val currentDate = Calendar.getInstance().time.toString()
//    val dateFormat1 = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
//    val parsedDate = dateFormat1.parse(currentDate)
//    // start date in the format yyyy-mm-dd
//    val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(parsedDate)
//
//    val dateFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//
//    val date1 = dateFormat2.parse(date)
//    val date2 = dateFormat2.parse(endDate)
//
//    val differenceInMillis = date1.time - date2.time
//    val differenceInDays = differenceInMillis / (1000 * 60 * 60 * 24)
//
//    if (differenceInDays in 1..1) {
//        view.text = "Expire in $differenceInDays days"
//        view.setTextColor(Color.RED)
//    } else if (differenceInDays > 0){
//        view.text = "Expire in $differenceInDays days"
//    } else if (differenceInDays < 0) {
//        val dif = abs(differenceInDays)
//        view.text = "Expired $dif days ago!!!"
//        view.setTextColor(Color.DKGRAY)
//    } else {
//        view.text = "Expire today!!!"
//        view.setTextColor(Color.RED)
//    }
//
//}
fun setDate(view: TextView, date: String?) {
    if (date != null && date.isNotEmpty()) {
        val currentDate = Calendar.getInstance().time.toString()
        val dateFormat1 = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val parsedDate = dateFormat1.parse(currentDate)

        val dateFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val date1 = dateFormat2.parse(date)
        val date2 = dateFormat2.parse(dateFormat2.format(parsedDate))

        val differenceInMillis = date1.time - date2.time
        val differenceInDays = TimeUnit.MILLISECONDS.toDays(differenceInMillis)

        if (differenceInDays in 1..1) {
            view.text = "Expire in $differenceInDays day"
            view.setTextColor(Color.RED)
        } else if (differenceInDays > 0) {
            view.text = "Expire in $differenceInDays days"
        } else {
            val dif = abs(differenceInDays)
            view.text = if (dif == 0L) "Expire today!!!" else "Expired $dif days ago!!!"
            view.setTextColor(if (dif == 0L) Color.BLUE else Color.DKGRAY)
        }
    } else {
        // Handle case when date is empty or null
        view.text = "No date specified"
        view.setTextColor(Color.BLACK)
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("setImageView")
fun setImageView(view: ImageView, imagePath: String) {
    if (imagePath != "") {
        showImage(view, imagePath, HORIZONTAL_PREVIEW_SCALE, VERTICLE_PREVIEW_SCALE)
    }
}

@BindingAdapter("setTimestamp")
fun setTimestamp(view: TextView, timestamp: Long){
    view.text = DateFormat.getInstance().format(timestamp)
}
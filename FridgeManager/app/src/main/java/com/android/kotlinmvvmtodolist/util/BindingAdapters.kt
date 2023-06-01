package com.android.kotlinmvvmtodolist.util

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import java.text.DateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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


//@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SetTextI18n")
@BindingAdapter("setDate")
fun setDate(view: TextView, date: String){
//    val startDate = LocalDate.parse(date)
//    val endDate = LocalDate.now()
//
//    val daysDifference = ChronoUnit.DAYS.between(startDate, endDate)
//    val monthsDifference = ChronoUnit.MONTHS.between(startDate, endDate)
//    val yearsDifference = ChronoUnit.YEARS.between(startDate, endDate)
//
//    view.text = daysDifference.toString()
    view.text = date
}

@BindingAdapter("setTimestamp")
fun setTimestamp(view: TextView, timestamp: Long){
    view.text = DateFormat.getInstance().format(timestamp)
}
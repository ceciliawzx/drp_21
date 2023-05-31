package com.android.kotlinmvvmtodolist.util

import android.annotation.SuppressLint
import android.graphics.Color
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.DateFormat

@SuppressLint("SetTextI18n")
@BindingAdapter("setPriority")
fun setPriority(view: TextView, priority: Int){
    when(priority){
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

@BindingAdapter("setTimestamp")
fun setTimestamp(view: TextView, timestamp: Long){
    view.text = DateFormat.getInstance().format(timestamp)
}
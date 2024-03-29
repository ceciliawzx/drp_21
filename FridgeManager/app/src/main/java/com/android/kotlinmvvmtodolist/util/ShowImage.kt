package com.android.kotlinmvvmtodolist.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.widget.ImageView

object ShowImage {

    public val HORIZONTAL_PREVIEW_SCALE = 0.3
    public val VERTICLE_PREVIEW_SCALE = 0.3

    fun showImage(imageView: ImageView, imagePath: String, horScale: Double, verScale: Double) {
        val imageBitmap = BitmapFactory.decodeFile(imagePath)
        if (imageBitmap != null) {

            val newWidth = (imageBitmap.width * horScale).toInt()
            val newHeight = (imageBitmap.height * verScale).toInt()
            val resizedBitmap =
                Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, true)
            imageView.visibility = View.VISIBLE
            imageView.setImageBitmap(resizedBitmap)
        }
    }

    fun showProfileImage(imageView: ImageView, imageBytes: String, horScale: Double, verScale: Double) {
        val imageByteArray = Base64.decode(imageBytes, Base64.DEFAULT)
        val imageBitMap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
            imageView.setImageBitmap(imageBitMap)
    }
}
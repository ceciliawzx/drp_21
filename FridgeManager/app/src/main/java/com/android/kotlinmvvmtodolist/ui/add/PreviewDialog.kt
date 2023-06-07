package com.android.kotlinmvvmtodolist.ui.add

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.android.kotlinmvvmtodolist.R

class PreviewDialog(private val imageBitmap: Bitmap) :
    DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_preview)

        val imageView = dialog.findViewById<ImageView>(R.id.dialog_preview)
        imageView.setImageBitmap(imageBitmap)

        // Set click listener to dismiss the dialog when the image is clicked
        imageView.setOnClickListener {
            dismiss()
        }

        return dialog
    }
}
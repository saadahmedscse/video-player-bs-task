package com.saadahmedev.videoplayer.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.databinding.LayoutDownloadPopupBinding

class DownloadDialog private constructor(private val context: Context) {

    private val dialog = Dialog(context)

    companion object {
        fun getInstance(context: Context): DownloadDialog {
            return DownloadDialog(context)
        }
    }

    fun show(onCancelClicked: () -> Unit) {
        if (dialog.isShowing) return

        val binding: LayoutDownloadPopupBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_download_popup,
            null,
            false
        )

        dialog.setContentView(binding.root)
        dialog.setCancelable(false)

        binding.btnCancel.setOnClickListener {
            dismiss()
            onCancelClicked.invoke()
        }

        dialog.apply {
            window?.apply {
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )

                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            show()
        }
    }

    fun dismiss() {
        if (!dialog.isShowing) return
        dialog.dismiss()
    }
}
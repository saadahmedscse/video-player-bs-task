package com.saadahmedev.videoplayer.util.extension

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("app:tint")
fun setTint(view: AppCompatImageView, @ColorInt color: Int) {
    view.imageTintList = ColorStateList.valueOf(color)
}
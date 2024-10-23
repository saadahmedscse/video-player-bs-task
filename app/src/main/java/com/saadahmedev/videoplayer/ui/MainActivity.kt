package com.saadahmedev.videoplayer.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.saadahmedev.videoplayer.base.BaseActivity
import com.saadahmedev.videoplayer.databinding.ActivityMainBinding
import com.saadahmedev.videoplayer.databinding.CustomToolbarBinding

class MainActivity : BaseActivity<MainActivityViewModel, ActivityMainBinding>(ActivityMainBinding::inflate) {
    override val viewmodel: MainActivityViewModel by viewModels()
    override val customToolbarBinding: CustomToolbarBinding
        get() = binding.customToolbar

    override fun onActivityCreate(savedInstanceState: Bundle?) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun observeData() {
        //
    }
}
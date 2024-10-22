package com.saadahmedev.videoplayer.ui.home

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.base.BaseFragment
import com.saadahmedev.videoplayer.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override val toolbarTitle: String get() = getString(R.string.title_home)
    override val viewmodel: HomeViewModel by viewModels()

    override fun onFragmentCreate(savedInstanceState: Bundle?) {
        //
    }

    override fun observeData() {
        //
    }
}
package com.saadahmedev.videoplayer.ui.home

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.base.BaseFragment
import com.saadahmedev.videoplayer.databinding.FragmentHomeBinding
import com.saadahmedev.videoplayer.domain.model.ListType
import com.saadahmedev.videoplayer.domain.model.StreamItem

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override val toolbarTitle: String get() = getString(R.string.title_home)
    override val viewmodel: HomeViewModel by viewModels()

    private val hlsAdapter by lazy {
        StreamItemAdapter(onItemClick = { item, _, _ -> onStreamItemClicked(item) })
    }

    private val dashAdapter by lazy {
        StreamItemAdapter(onItemClick = { item, _, _ -> onStreamItemClicked(item) })
    }

    override fun onFragmentCreate(savedInstanceState: Bundle?) {
        binding.apply {
            recyclerViewHls.adapter = hlsAdapter
            recyclerViewDash.adapter = dashAdapter
        }

        hlsAdapter.addItems(viewmodel.getHLSVideos())
        dashAdapter.addItems(viewmodel.getDASHVideos())
    }

    override fun observeData() {
        //
    }

    private fun onStreamItemClicked(item: StreamItem) {
        sharedViewModel.apply {
            clearQueue()
            addToQueue(item)
        }
        navigate(
            destination = R.id.action_homeFragment_to_playerFragment
        )
    }
}
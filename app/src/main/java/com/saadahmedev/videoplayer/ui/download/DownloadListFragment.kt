package com.saadahmedev.videoplayer.ui.download

import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.base.BaseFragment
import com.saadahmedev.videoplayer.databinding.FragmentDownloadListBinding
import com.saadahmedev.videoplayer.domain.model.ListType
import com.saadahmedev.videoplayer.domain.model.PlayerMode
import com.saadahmedev.videoplayer.domain.model.StreamItem
import com.saadahmedev.videoplayer.domain.model.VideoType
import com.saadahmedev.videoplayer.ui.home.StreamItemAdapter

class DownloadListFragment : BaseFragment<DownloadListViewModel, FragmentDownloadListBinding>(FragmentDownloadListBinding::inflate) {

    override val toolbarTitle: String get() = getString(R.string.downloaded_streams)
    override val viewmodel: DownloadListViewModel by viewModels()

    private val adapter by lazy {
        StreamItemAdapter(
            onItemClick = { item, _, _ -> onStreamItemClicked(item) }
        )
    }

    override fun onFragmentCreate(savedInstanceState: Bundle?) {
        binding.recyclerView.adapter = adapter
        adapter.addItems(sharedViewModel.getOfflineStreams(requireContext()))
    }

    override fun observeData() {}

    private fun onStreamItemClicked(item: StreamItem) {
        sharedViewModel.apply {
            clearQueue()
            addToQueue(item)
            playerMode = PlayerMode.OFFLINE
        }

        navigate(
            destination = R.id.action_downloadListFragment_to_playerFragment
        )
    }
}
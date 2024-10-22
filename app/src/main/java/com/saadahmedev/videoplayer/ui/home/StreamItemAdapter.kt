package com.saadahmedev.videoplayer.ui.home

import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.base.BaseRecyclerAdapter
import com.saadahmedev.videoplayer.databinding.LayoutListVideoItemBinding
import com.saadahmedev.videoplayer.domain.model.StreamItem

class StreamItemAdapter(private val onItemClick: (StreamItem) -> Unit) : BaseRecyclerAdapter<StreamItem, LayoutListVideoItemBinding>() {

    override val layoutRes: Int get() = R.layout.layout_list_video_item

    override fun onBind(binding: LayoutListVideoItemBinding, item: StreamItem, position: Int) {
        binding.item = item

        binding.root.setOnClickListener { onItemClick.invoke(item) }
    }
}
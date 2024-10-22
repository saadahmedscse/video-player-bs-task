package com.saadahmedev.videoplayer.ui.home

import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.base.BaseRecyclerAdapter
import com.saadahmedev.videoplayer.databinding.LayoutListVideoItemBinding
import com.saadahmedev.videoplayer.domain.model.ListType
import com.saadahmedev.videoplayer.domain.model.StreamItem
import com.saadahmedev.videoplayer.util.extension.gone
import com.saadahmedev.videoplayer.util.extension.visible

class StreamItemAdapter(
    private val onItemClick: ((StreamItem) -> Unit)? = null,
    private val onActionClick: ((StreamItem, ListType) -> Unit)? = null,
    private val listType: ListType = ListType.ALL
) : BaseRecyclerAdapter<StreamItem, LayoutListVideoItemBinding>() {

    override val layoutRes: Int get() = R.layout.layout_list_video_item

    override fun onBind(binding: LayoutListVideoItemBinding, item: StreamItem, position: Int) {
        binding.item = item

        binding.root.setOnClickListener { onItemClick?.invoke(item) }
        binding.btnAction.setOnClickListener { onActionClick?.invoke(item, listType) }

        when (listType) {
            ListType.ALL -> {
                binding.btnAction.gone()
            }
            ListType.AVAILABLE -> {
                binding.btnAction.visible()
                binding.btnAction.setImageResource(R.drawable.ic_add)
                binding.btnAction.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.colorHls), PorterDuff.Mode.SRC_IN)
            }
            ListType.QUEUE -> {
                binding.btnAction.visible()
                binding.btnAction.setImageResource(R.drawable.ic_remove)
                binding.btnAction.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.colorDash), PorterDuff.Mode.SRC_IN)
            }
        }
    }
}